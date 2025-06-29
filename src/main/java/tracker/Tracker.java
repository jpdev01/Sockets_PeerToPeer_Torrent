package tracker;

import shared.Message;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Tracker {
    private static final int PORT = 8888;
    private static final Map<String, List<String>> peerFileMap = new ConcurrentHashMap<>();

    /*
        * Método principal que inicia o tracker e escuta pacotes UDP.
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        TrackerPeerPurge.startInactivePeerChecker();
        startDumpProcess();

        DatagramSocket socket = new DatagramSocket(PORT);
        System.out.println("[Tracker] Iniciado na porta " + PORT);

        while (true) {
            byte[] buffer = new byte[8192];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            try {
                handlePacket(packet, socket);
            } catch (Exception e) {
                System.err.println("[Tracker] Erro ao processar pacote: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /*
     * Remove um peer do tracker.
     */
    public static void removePeer(String peerId) {
        if (peerFileMap.containsKey(peerId)) {
            peerFileMap.remove(peerId);
            System.out.println("[Tracker] Peer removido: " + peerId);
        }
    }

    /*
     * Inicia um processo agendado que faz dump do estado atual dos peers a cada 10 segundos.
     */
    private static void startDumpProcess() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("--------------------");
                System.out.println("[Tracker] Dumping estado atual dos peers");
                for (Map.Entry<String, List<String>> entry : peerFileMap.entrySet()) {
                    System.out.println("[Tracker] Peer " + entry.getKey() + " -> [" + entry.getValue() + "]");
                }
                System.out.println("--------------------");
            } catch (Exception e) {
                System.err.println("[Tracker] Erro ao fazer dump: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    /*
        * Processa o pacote recebido, decodificando a mensagem e tratando-a de acordo com seu tipo.
        * Se for uma solicitação de peers, envia a lista de peers disponíveis.
        * Se for uma atualização de arquivo, atualiza a lista de pedaços do peer.
     */
    private static void handlePacket(DatagramPacket packet, DatagramSocket socket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Message msg = (Message) ois.readObject();

        String peerId = packet.getAddress().getHostAddress() + ":" + packet.getPort();

        switch (msg.type) {
            case REQUEST_PEERS -> handleRequestPeers(peerId, msg, socket, packet);
            case FILE_UPDATE -> handleFileUpdate(peerId, msg);
            default -> System.out.println("[Tracker] Tipo de mensagem desconhecido: " + msg.type);
        }
    }

    /*
     * Trata a solicitação de peers, enviando a lista de peers disponíveis para o peer solicitante.
     * Ignora o próprio peer na lista de resposta.
     * A lista é enviada como uma mensagem serializada via UDP.
     */
    private static void handleRequestPeers(String peerId, Message message, DatagramSocket socket, DatagramPacket packet) throws IOException {
        System.out.println("[Tracker] Enviando lista de peers para " + peerId);

        Message response = new Message(Message.Type.PEER_LIST, null, null);
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : peerFileMap.entrySet()) {
            if (entry.getKey().equals(message.peerTcpAddress)) {
                continue; // Ignora o próprio peer
            }
            list.add(entry.getKey() + "|" + String.join(",", entry.getValue()));
        }
        response.pieces = list;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(response);
        byte[] respBytes = baos.toByteArray();

        DatagramPacket responsePacket = new DatagramPacket(
                respBytes, respBytes.length, packet.getAddress(), packet.getPort()
        );
        socket.send(responsePacket);
    }

    /*
     * Trata a atualização de arquivo, armazenando os pedaços disponíveis do peer.
     * Se o peer já estiver registrado, atualiza sua lista de pedaços.
     * Caso contrário, adiciona o peer com seus pedaços.
     */
    private static void handleFileUpdate(String peerId, Message msg) {
        TrackerPeerPurge.store(msg.peerTcpAddress);

        peerFileMap.put(msg.peerTcpAddress, new ArrayList<>(msg.pieces));
        System.out.println("[Tracker] Atualizado: " + msg.peerTcpAddress + " com pedaços " + msg.pieces);
    }
}
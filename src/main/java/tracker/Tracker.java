package tracker;

import shared.Message;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Tracker {
    private static final int PORT = 8888;
    private static final Map<String, List<String>> peerFileMap = new ConcurrentHashMap<>();

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

    public static void removePeer(String peerId) {
        if (peerFileMap.containsKey(peerId)) {
            peerFileMap.remove(peerId);
            System.out.println("[Tracker] Peer removido: " + peerId);
        }
    }

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

    private static void handleFileUpdate(String peerId, Message msg) {
        TrackerPeerPurge.store(peerId);

        peerFileMap.put(msg.peerTcpAddress, new ArrayList<>(msg.pieces));
        System.out.println("[Tracker] Atualizado: " + msg.peerTcpAddress + " com pedaços " + msg.pieces);
    }
}
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

    private static void handlePacket(DatagramPacket packet, DatagramSocket socket) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Message msg = (Message) ois.readObject();

        String peerId = packet.getAddress().getHostAddress() + ":" + packet.getPort();

        switch (msg.type) {
            case REQUEST_PEERS -> handleRequestPeers(peerId, socket, packet);
            case FILE_UPDATE -> handleFileUpdate(peerId, msg);
            default -> System.out.println("[Tracker] Tipo de mensagem desconhecido: " + msg.type);
        }
    }

    private static void handleRequestPeers(String peerId, DatagramSocket socket, DatagramPacket packet) throws IOException {
        System.out.println("[Tracker] Enviando lista de peers para " + peerId);

        Message response = new Message(Message.Type.PEER_LIST, null, null);
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : peerFileMap.entrySet()) {
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
        if (msg.pieces == null || msg.pieces.isEmpty()) {
            System.out.println("[Tracker] Atualização ignorada: lista de pedaços vazia para " + msg.peerAddress);
            return;
        }

        peerFileMap.put(msg.peerAddress, new ArrayList<>(msg.pieces));
        System.out.println("[Tracker] Atualizado: " + msg.peerAddress + " com pedaços " + msg.pieces);
    }
}
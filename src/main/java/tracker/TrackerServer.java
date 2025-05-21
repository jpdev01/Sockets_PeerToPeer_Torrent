package tracker;

import java.net.*;
import java.util.*;

public class TrackerServer {

    private static final int PORT = 6969;
    private static final Map<String, Set<Integer>> peers = new HashMap<>();

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        byte[] buffer = new byte[4096];

        System.out.println("Tracker rodando na porta " + PORT);

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String received = new String(packet.getData(), 0, packet.getLength());
            String[] parts = received.split(";");

            String peerId = parts[0];
            Set<Integer> pieces = new HashSet<>();
            for (String s : parts[1].split(",")) pieces.add(Integer.parseInt(s));
            peers.put(peerId, pieces);

            StringBuilder response = new StringBuilder();
            for (Map.Entry<String, Set<Integer>> entry : peers.entrySet()) {
                if (!entry.getKey().equals(peerId)) {
                    response.append(entry.getKey()).append(":")
                            .append(entry.getValue().toString().replaceAll("[\\[\\] ]", "")).append(";");
                }
            }

            byte[] resp = response.toString().getBytes();
            InetAddress clientAddr = packet.getAddress();
            int clientPort = packet.getPort();
            DatagramPacket responsePacket = new DatagramPacket(resp, resp.length, clientAddr, clientPort);
            socket.send(responsePacket);
        }
    }
}

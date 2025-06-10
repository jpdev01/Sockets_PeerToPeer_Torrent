package peer;

import shared.Message;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Peer {

    private String id;
    private ArrayList<String> myPieces;
    private final int trackerPort = 8888;
    private final String trackerHost = "localhost"; // IP DO TRACKER
    private final PeerTCPHandler tcpHandler;
    private final UDPHandler udpHandler;

    public Peer(String id, List<String> pieces) {
        this.id = id;
        this.myPieces = new ArrayList<>(pieces);
        this.tcpHandler = new PeerTCPHandler(id, myPieces);
        this.udpHandler = new UDPHandler(trackerHost, trackerPort);
    }

    public void start() throws Exception {
        tcpHandler.startTCPServer(); // Inicia o servidor TCP
        requestPeers();
        startFileUpdateThread();
    }

    private void requestPeers() throws Exception {
        Message request = new Message(Message.Type.REQUEST_PEERS, id, null);
        Message response = udpHandler.send(request, true);
        System.out.println("[Peer] Lista de pares recebida:");

        List<String> peerList = response.pieces;
        for (String entry : peerList) {
            System.out.println(" → " + entry);
        }

        // Lógica para identificar o pedaço mais raro
        Map<String, Integer> pieceFreq = new HashMap<>();
        for (String entry : peerList) {
            String[] parts = entry.split("\\|");
            if (parts.length < 2) continue;
            String[] pieces = parts[1].split(",");
            for (String p : pieces) {
                pieceFreq.put(p, pieceFreq.getOrDefault(p, 0) + 1);
            }
        }

        String rarest = pieceFreq.entrySet().stream()
                .filter(e -> !myPieces.contains(e.getKey()))
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (rarest != null) {
            System.out.println("[Peer] Pedaço mais raro: " + rarest);

            // Solicita o pedaço mais raro ao primeiro peer
            tcpHandler.requestPieceFromPeer(peerList.get(0).split("\\|")[0], rarest);

            // Escolhe aleatoriamente outro peer
            String randomPeer = peerList.get((int) (Math.random() * peerList.size())).split("\\|")[0];
            System.out.println("[Peer] Escolhendo outro peer aleatoriamente: " + randomPeer);
        } else {
            System.out.println("[Peer] Todos os pedaços já presentes ou lista de peers insuficiente.");
        }
    }

    private void startFileUpdateThread() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    Message update = new Message(Message.Type.FILE_UPDATE, id, myPieces);
                    udpHandler.send(update, false);
                    System.out.println("[Peer] Lista de pedaços enviada ao tracker.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, TimeUnit.SECONDS.toMillis(3));
    }
}

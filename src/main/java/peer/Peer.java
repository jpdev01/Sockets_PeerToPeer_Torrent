package peer;

import shared.Message;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer {

    private String id;
    private ArrayList<String> myPieces;
    private final int trackerPort = 8888;
    private final String trackerHost = "localhost"; // IP DO TRACKER
    private final TCPHandler tcpHandler;
    private final UDPHandler udpHandler;
    private final String folderName;

    public Peer(String id, String folderName) {
        this.id = id;
        this.myPieces = new ArrayList<>(Objects.requireNonNull(FileManager.loadPieceNames(folderName)));
        this.folderName = folderName;
        this.tcpHandler = new TCPHandler(id, this.folderName, myPieces);
        this.udpHandler = new UDPHandler(trackerHost, trackerPort);
    }

    public void start() throws Exception {
        tcpHandler.startTCPServer();
        startRequestPeersScheduler();
        startFileUpdateThread();
    }

    private void startRequestPeersScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                requestPeers();
            } catch (Exception e) {
                System.err.println("[Peer] Erro ao solicitar peers: " + e.getMessage());
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private void requestPeers() throws Exception {
        Message request = new Message(Message.Type.REQUEST_PEERS, id, null);
        Message response = udpHandler.send(request, true);
        System.out.println("[Peer] Lista de pares recebida:");

        List<String> peerList = response.pieces;
        for (String entry : peerList) {
            System.out.println(" → " + entry);
        }

        // Lógica para identificar o pedaço mais raro e o peer correspondente
        Map<String, Integer> pieceFrequency = new HashMap<>();
        Map<String, String> pieceToPeer = new HashMap<>();
        for (String entry : peerList) {
            String[] parts = entry.split("\\|");
            if (parts.length < 2) continue;
            String peerAddress = parts[0];
            String[] pieces = parts[1].split(",");
            for (String p : pieces) {
                pieceFrequency.put(p, pieceFrequency.getOrDefault(p, 0) + 1);
                pieceToPeer.putIfAbsent(p, peerAddress);
            }
        }

        String rarest = pieceFrequency.entrySet().stream()
                .filter(e -> !myPieces.contains(e.getKey()))
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String rarestPeer = rarest != null ? pieceToPeer.get(rarest) : null;

        if (rarest != null) {
            System.out.println("[Peer] Pedaço mais raro: " + rarest);

            // Solicita o pedaço mais raro ao primeiro peer
            tcpHandler.requestPieceFromPeer(rarestPeer, rarest);

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

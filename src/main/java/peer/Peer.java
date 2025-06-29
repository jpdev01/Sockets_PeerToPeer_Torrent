package peer;

import shared.Message;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Peer {

    private String tcpAddress;
    private final FileManager fileManager;
    private final TCPHandler tcpHandler;
    private final UDPHandler udpHandler;

    public Peer(String id, String folderName) {
        this.tcpAddress = id;
        this.fileManager = new FileManager(folderName);
        this.tcpHandler = new TCPHandler(id, fileManager);
        this.udpHandler = new UDPHandler();
    }

    public void start() {
        tcpHandler.acceptConnections();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::sendTrackerUpdate, 0, 3, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(this::executePeersPolling, 0, 3, TimeUnit.SECONDS);
    }

    private void executePeersPolling() {
        try {
            Message request = new Message(Message.Type.REQUEST_PEERS, tcpAddress, null);
            Message response = udpHandler.send(request, true);
            System.out.println("[Peer] Lista de pares recebida:");

            List<String> peerList = response.pieces;
            for (String entry : peerList) {
                System.out.println(">> " + entry);
            }

            RarestPieceVO rarestPieceVO = new RarestPieceVO(peerList, fileManager);
            String rarestPiece = rarestPieceVO.piece;
            String rarestPieceOwner = rarestPieceVO.peer;

            if (rarestPiece != null) {
                System.out.println("[Peer] Pedaço mais raro: " + rarestPiece);

                // Solicita o pedaço mais raro ao peer
                tcpHandler.requestPieceFromPeer(rarestPieceOwner, rarestPiece);

                // Escolhe aleatoriamente outro peer
                String randomPeer = peerList.get((int) (Math.random() * peerList.size())).split("\\|")[0];
                System.out.println("[Peer] Escolhendo outro peer aleatoriamente: " + randomPeer);
            } else {
                System.out.println("[Peer] Todos os pedaços já presentes ou lista de peers insuficiente.");
            }   
        } catch (Exception e) {
            System.err.println("[Peer] Erro ao solicitar peers: " + e.getMessage());
        }
    }

    private void sendTrackerUpdate() {
        try {
            Message update = new Message(Message.Type.FILE_UPDATE, tcpAddress, fileManager.loadPieceNames());
            udpHandler.send(update, false);
            System.out.println("[Peer] Lista de pedaços enviada ao tracker.");
        } catch (Exception e) {
            System.err.println("[Peer] Erro ao enviar atualização ao tracker: " + e.getMessage());
        }
    }
}

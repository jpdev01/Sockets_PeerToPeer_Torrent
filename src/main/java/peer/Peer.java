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

    /*
     * Solicita a lista de peers ao tracker e processa a resposta.
     * A lista de peers é usada para determinar qual pedaço mais raro solicitar.
     * Se o pedaço mais raro for encontrado, solicita-o ao peer correspondente.
     * Também escolhe aleatoriamente outro peer da lista para potencialmente solicitar outro pedaço.
     */
    private void executePeersPolling() {
        try {
            Message request = new Message(Message.Type.REQUEST_PEERS, tcpAddress, null);
            Message response = udpHandler.send(request, true);
            System.out.println("[Peer] Lista de pares recebida:");

            List<String> peerList = response.pieces;
            for (String entry : peerList) {
                System.out.println(">> " + entry);
            }

            PeerCollection peerCollection = new PeerCollection(peerList, fileManager);
            String rarestPiece = peerCollection.rarestPiece;
            String rarestPieceOwner = peerCollection.rarestPieceOwner;

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

            peerCollection.pieceToPeer.forEach((piece, peer) -> {
                if (!fileManager.loadPieceNames().contains(piece)) {
                    System.out.println("[Peer] Solicitando pedaço aleatorio " + piece + " de " + peer);
                    tcpHandler.requestPieceFromPeer(peer, piece);
                }
            });
        } catch (Exception e) {
            System.err.println("[Peer] Erro ao solicitar peers: " + e.getMessage());
        }
    }

    /*
     * Envia uma atualização ao tracker com a lista de pedaços disponíveis.
     * Essa atualização é enviada a cada 3 segundos para manter o tracker informado sobre os arquivos que o peer possui
     */
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

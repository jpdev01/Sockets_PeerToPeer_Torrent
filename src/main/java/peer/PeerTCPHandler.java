package peer;

import shared.Message;

import java.io.*;
import java.net.*;
import java.util.List;

public class PeerTCPHandler {

    private String peerId;
    private List<String> myPieces;

    public PeerTCPHandler(String peerId, List<String> myPieces) {
        this.peerId = peerId;
        this.myPieces = myPieces;
    }

    public void startTCPServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(getPortFromId(peerId))) {
                System.out.println("[PeerTCPHandler] Servidor TCP iniciado na porta " + getPortFromId(peerId));

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                         ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                        Message request = (Message) in.readObject();

                        System.out.println("[PeerTCPHandler] Solicitação recebida: " + request.requestedPiece + " de " + request.peerAddress);
                        if (request.type == Message.Type.REQUEST_PIECE) {
                            String requestedPiece = request.requestedPiece;
                            if (myPieces.contains(requestedPiece)) {
                                System.out.println("[PeerTCPHandler] Enviando pedaço " + requestedPiece + " para " + request.peerAddress);
                                out.writeObject(new Message(Message.Type.SEND_PIECE, peerId, List.of(requestedPiece)));
                                System.out.println("[PeerTCPHandler] Pedaço " + requestedPiece + " enviado com sucesso.");
                            } else {
                                System.out.println("[PeerTCPHandler] Pedaço " + requestedPiece + " não encontrado.");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[PeerTCPHandler] Erro ao processar solicitação: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void requestPieceFromPeer(String peerInfo, String piece) {
        String[] peerParts = peerInfo.split(":");
        String peerHost = peerParts[0];
        int peerPort = Integer.parseInt(peerParts[1]);

        try (Socket socket = new Socket(peerHost, peerPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            System.out.println("[PeerTCPHandler] Solicitando pedaço " + piece + " de " + peerInfo);
            out.writeObject(new Message(Message.Type.REQUEST_PIECE, peerId, null, piece));

            Message response = (Message) in.readObject();
            if (response.type == Message.Type.SEND_PIECE) {
                System.out.println("[PeerTCPHandler] Pedaço " + piece + " recebido de " + peerInfo);
                myPieces.add(piece);
            }
        } catch (Exception e) {
            System.err.println("[PeerTCPHandler] Erro ao solicitar pedaço de " + peerInfo + ": " + e.getMessage());
        }
    }

    private int getPortFromId(String peerId) {
        return Integer.parseInt(peerId.split(":")[1]);
    }
}
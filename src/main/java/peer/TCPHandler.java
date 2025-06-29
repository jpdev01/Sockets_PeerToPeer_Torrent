package peer;

import shared.FilePiece;
import shared.Message;

import java.io.*;
import java.net.*;
import java.util.List;

public class TCPHandler {

    private final String peerTcpAddress;
    private final FileManager fileManager;

    public TCPHandler(String peerId, FileManager fileManager) {
        this.peerTcpAddress = peerId;
        this.fileManager = fileManager;
    }

    public void startTCPServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(getPortFromId(peerTcpAddress))) {
                System.out.println("[TCPHandler] Servidor TCP iniciado na porta " + getPortFromId(peerTcpAddress));

                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                         ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                        Message request = (Message) in.readObject();

                        System.out.println("[TCPHandler] Solicitação recebida: " + request.requestedPiece + " de " + request.peerTcpAddress);
                        if (request.type == Message.Type.REQUEST_PIECE) {
                            String requestedPiece = request.requestedPiece;
                            if (fileManager.loadPieceNames().contains(requestedPiece)) {

                                System.out.println("[TCPHandler] Enviando pedaço " + requestedPiece + " para " + request.peerTcpAddress);

                                FilePiece requestedFilePiece = fileManager.readFileFromDisk(requestedPiece);
                                Message message = new Message(Message.Type.SEND_PIECE, peerTcpAddress, null, requestedFilePiece);
                                out.writeObject(message);
                                System.out.println("[TCPHandler] Pedaço " + requestedPiece + " enviado com sucesso.");
                            } else {
                                System.out.println("[TCPHandler] Pedaço " + requestedPiece + " não encontrado.");
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[TCPHandler] Erro ao processar solicitação: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("[TCPHandler] Erro ao iniciar o servidor TCP: " + e.getMessage());
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

            System.out.println("[TCPHandler] Solicitando pedaço " + piece + " de " + peerInfo);
            out.writeObject(new Message(Message.Type.REQUEST_PIECE, peerTcpAddress, null, piece));

            Message response = (Message) in.readObject();
            if (response.type == Message.Type.SEND_PIECE) {
                System.out.println("[TCPHandler] Pedaço " + piece + " recebido de " + peerInfo);
                fileManager.saveFile(response.sharedFilePiece);
                // myPieces.add(response.sharedFilePiece.getName());
            }
        } catch (Exception e) {
            System.err.println("[TCPHandler] Erro ao solicitar pedaço de " + peerInfo + ": " + e.getMessage());
        }
    }

    private int getPortFromId(String peerId) {
        return Integer.parseInt(peerId.split(":")[1]);
    }
}
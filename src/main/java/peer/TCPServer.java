package peer;

import java.io.*;
import java.net.*;
import java.util.*;

public class TCPServer extends Thread {
    private int port;
    private Set<Integer> filePieces;

    public TCPServer(int port, Set<Integer> filePieces) {
        this.port = port;
        this.filePieces = filePieces;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleClient(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            int requestedPiece = Integer.parseInt(in.readLine());
            if (filePieces.contains(requestedPiece)) {
                out.write("DATA:" + requestedPiece + "\n");
            } else {
                out.write("NOT_FOUND\n");
            }
            out.flush();
        } catch (IOException e) { e.printStackTrace(); }
    }
}

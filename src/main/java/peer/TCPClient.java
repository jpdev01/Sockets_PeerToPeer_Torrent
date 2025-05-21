package peer;

import java.io.*;
import java.net.*;
import java.util.Set;

public class TCPClient {

    public static void requestPiece(String ip, int port, int piece, Set<Integer> myPieces) {
        try (Socket socket = new Socket(ip, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.write(piece + "\n");
            out.flush();

            String response = in.readLine();
            if (response.startsWith("DATA")) {
                myPieces.add(piece);
                System.out.println("Recebido pedaço " + piece + " de " + ip + ":" + port);
            } else {
                System.out.println("Pedaço " + piece + " não disponível em " + ip);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

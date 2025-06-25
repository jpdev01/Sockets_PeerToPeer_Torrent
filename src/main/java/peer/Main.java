package peer;

import shared.FilePiece;

import java.io.FileOutputStream;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws Exception {
        // args[0] = nome do peer (ex: "127.0.0.1:5001")
        // args[1...] = pasta de pedaços

        String peerId = Inet4Address.getLocalHost().getHostAddress().concat(":").concat(args[0]);

        String folderName = args[1];
        Peer peer = new Peer(peerId, folderName);
        peer.start();
    }
}

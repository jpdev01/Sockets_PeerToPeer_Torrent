package peer;

import java.net.Inet4Address;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {
        // args[0] = nome do peer (ex: "127.0.0.1:5001")
        // args[1...] = pedaços que ele já possui

        String peerId = Inet4Address.getLocalHost().getHostAddress().concat(args[0]);
        var pieces = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));

        Peer peer = new Peer(peerId, pieces);
        peer.start();
    }
}

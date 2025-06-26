package peer;

import java.net.Inet4Address;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // args[0] = nome do peer (ex: "127.0.0.1:5001")
        // args[1...] = pasta de pedaços

        String port = args[0];
        if (port == null || port.isEmpty()) {
            port = "5001";
        }
        String peerId = Inet4Address.getLocalHost().getHostAddress().concat(":").concat(args[0]);
        String folderName = args[1];

        resolveTrackerIp();

        Peer peer = new Peer(peerId, folderName);
        peer.start();
    }

    private static void resolveTrackerIp() {
        System.out.println("Insira o IP do Tracker:");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine().trim();

            if (IpValidator.isValid(input)) {
                TrackerConstants.HOST = input;
                break;
            } else {
                System.out.println("Endereço IP inválido. Tente novamente. Exemplo: 192.168.0.1");
            }
        }
    }
}

package peer;

import java.io.File;
import java.net.Inet4Address;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // args[0] = nome do peer (ex: "127.0.0.1:5001")
        // args[1...] = pasta de pedaços

        String peerId = getPeerId(args);
        String folderName = getFolderName(args);

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

    private static String getPeerId(String[] args)  throws Exception {
        String port = args.length > 0 ? args[0] : null;
        boolean mustUseDefaultPort = port == null || port.isEmpty() || !port.matches("\\d+");
        if (mustUseDefaultPort) port = "5001";

        String peerId = Inet4Address.getLocalHost().getHostAddress().concat(":").concat(port);
        return peerId;
    }

    private static String getFolderName(String[] args) {
        if (args.length > 1) {
            return args[1];
        } else {
            boolean folderExists = new File("./torrent-files").isDirectory();
            if (!folderExists) {
                System.out.println("A pasta './torrent-files' não existe. Criando pasta...");
                new File("./torrent-files").mkdirs();
            }
            return "./torrent-files";
        }
    }
}

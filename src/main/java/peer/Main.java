package peer;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        // args[0] = nome do peer (ex: "5001")
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

        String localAddress = getIp().toString().replace("\\/", "");
        String peerId = localAddress.concat(":").concat(port);
        System.out.println("[PEER MAIN], IP= " + peerId);
        return peerId;
    }

    private static String getFolderName(String[] args) {
        if (args.length > 1) {
            String folderName = args[1];
            return createFolderIfNecessary(folderName);
        } else {
            final String defaultFolder = "./torrent-files";
            return createFolderIfNecessary(defaultFolder);
        }
    }

    private static String createFolderIfNecessary(String folderName) {
        File folder = new File(folderName);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) System.err.println("Falha ao criar a pasta: " + folderName);
        }
        return folderName;
    }

    private static String getIp() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("google.com", 80));
            return socket.getLocalAddress().getHostAddress();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

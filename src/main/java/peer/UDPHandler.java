package peer;

import shared.Message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPHandler {

    private final String host;
    private final int port;

    public UDPHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send(Message message) throws Exception {
        send(message, false);
    }

    public Message send(Message message, boolean expectResponse) throws Exception {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        byte[] data = baos.toByteArray();

        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        socket.send(packet);

        if (expectResponse) {
            byte[] buffer = new byte[8192];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            ByteArrayInputStream bais = new ByteArrayInputStream(responsePacket.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (Message) ois.readObject();
        }

        return null;
    }
}

package shared;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    public enum Type {
        REQUEST_PEERS, PEER_LIST, REQUEST_PIECE, FILE_RESPONSE, FILE_UPDATE, SEND_PIECE
    }

    public Type type;
    public String peerAddress;
    public List<String> pieces;
    public String requestedPiece;

    public Message(Type type, String peerAddress, List<String> pieces, String requestedPiece) {
        this.type = type;
        this.peerAddress = peerAddress;
        this.pieces = pieces;
        this.requestedPiece = requestedPiece;
    }

    public Message(Type type, String peerAddress, List<String> pieces) {
        this.type = type;
        this.peerAddress = peerAddress;
        this.pieces = pieces;
    }
}

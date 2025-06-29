package peer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeerCollection {

    final Map<String, String> pieceToPeer;

    final String rarestPieceOwner;

    final String rarestPiece;

    /*
    * PeerCollection é uma coleção de peers que compartilham peças de um arquivo.
    * Cada string na lista peerList deve estar no formato "endereço|peça1,peça2,...,peçaN".
    * O construtor analisa essa lista e cria um mapa que associa cada peça ao peer que a possui.
    * Além disso, identifica a peça mais rara e o peer que a possui.
     */
    public PeerCollection(List<String> peerList, FileManager fileManager) {
        Map<String, Integer> pieceFrequency = new HashMap<>();
        this.pieceToPeer = new HashMap<>();
        for (String entry : peerList) {
            String[] parts = entry.split("\\|");
            if (parts.length < 2) continue;
            String peerAddress = parts[0];
            String[] pieces = parts[1].split(",");
            for (String p : pieces) {
                pieceFrequency.put(p, pieceFrequency.getOrDefault(p, 0) + 1);
                pieceToPeer.putIfAbsent(p, peerAddress);
            }
        }

        String rarest = pieceFrequency.entrySet().stream()
                .filter(e -> !fileManager.loadPieceNames().contains(e.getKey()))
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        this.rarestPiece = rarest;
        this.rarestPieceOwner = pieceToPeer.get(rarest);
    }
}

package peer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RarestPieceVO {

    String peer;

    String piece;

    public RarestPieceVO(List<String> peerList, FileManager fileManager) {
        Map<String, Integer> pieceFrequency = new HashMap<>();
        Map<String, String> pieceToPeer = new HashMap<>();
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

        this.piece = rarest;
        this.peer = pieceToPeer.get(rarest);
    }
}

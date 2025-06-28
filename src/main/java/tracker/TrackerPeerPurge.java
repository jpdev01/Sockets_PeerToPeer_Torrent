package tracker;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrackerPeerPurge {

    private static final long PEER_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(5);
    private static final Map<String, Long> peerLastUpdateMap = new ConcurrentHashMap<>();

    public static void store(String peerId) {
        peerLastUpdateMap.put(peerId, System.currentTimeMillis());
    }

    public static void startInactivePeerChecker() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            for (String peerId : new ArrayList<>(peerLastUpdateMap.keySet())) {
                long lastUpdate = peerLastUpdateMap.get(peerId);
                if (currentTime - lastUpdate > PEER_TIMEOUT_MS) {
                    Tracker.removePeer(peerId);
                    peerLastUpdateMap.remove(peerId);
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}

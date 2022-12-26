package chat.dim.cache.game;

import android.util.SparseArray;

import chat.dim.g1248.dbi.HistoryDBI;
import chat.dim.g1248.model.History;
import chat.dim.protocol.ID;

public class HistoryCache implements HistoryDBI {

    // gid => history
    private final SparseArray<History> historyCache = new SparseArray<>();

    @Override
    public History getHistory(int gid) {
        return historyCache.get(gid);
    }

    @Override
    public boolean saveHistory(History history) {
        historyCache.put(history.getGid(), history);
        return true;
    }

    public boolean updatePlayingHistory(int tid, int bid, int gid, ID player) {
        History history = historyCache.get(0);
        if (history == null) {
            return false;
        } else if (history.getTid() != tid || history.getBid() != bid) {
            return false;
        }
        ID pid = history.getPlayer();
        if (pid != null && !pid.equals(player)) {
            return false;
        }
        historyCache.remove(0);
        history.setGid(gid);
        historyCache.put(gid, history);
        return true;
    }
}

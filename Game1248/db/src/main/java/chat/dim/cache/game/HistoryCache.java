package chat.dim.cache.game;

import android.util.SparseArray;

import chat.dim.g1248.dbi.HistoryDBI;
import chat.dim.g1248.model.History;
import chat.dim.protocol.ID;
import chat.dim.utils.Log;

public class HistoryCache implements HistoryDBI {

    // gid => history
    private final SparseArray<History> historyCache = new SparseArray<>();

    @Override
    public History getHistory(int gid) {
        return historyCache.get(gid);
    }

    @Override
    public boolean saveHistory(History history) {
        History old = historyCache.get(history.getGid());
        // check time when old record exists
        if (history.after(old)) {
            // ok, save it
            historyCache.put(history.getGid(), history);
            return true;
        } else {
            Log.warning("history expired: old time=" + old.getTime() + ", new time=" + history.getTime());
            return false;
        }
    }

    public boolean updatePlayingHistory(int rid, int bid, int gid, ID player) {
        // get playing history
        History history = historyCache.get(gid);
        if (history == null) {
            // get new history if exists
            history = historyCache.get(0);
            if (history == null) {
                Log.error("no new history: gid=" + gid +
                        ", rid=" + rid + ", bid=" + bid + ", player=" + player);
                return false;
            }
            // move new history to its position: gid
            historyCache.remove(0);
        }
        Log.info("update history: gid=" + gid +
                ", rid=" + rid + ", bid=" + bid + ", player=" + player + ", history=" + history);

        history.setRid(rid);
        history.setBid(bid);
        history.setGid(gid);
        history.setPlayer(player);
        historyCache.put(gid, history);
        Log.info("history updated: history=" + history);
        return true;
    }
}

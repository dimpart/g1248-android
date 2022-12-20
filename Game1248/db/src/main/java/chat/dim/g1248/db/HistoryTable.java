package chat.dim.g1248.db;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.format.Base64;
import chat.dim.g1248.dbi.GameDBI;
import chat.dim.g1248.model.History;
import chat.dim.protocol.ID;

public class HistoryTable implements GameDBI {

    private final SparseArray<History> historyCache = new SparseArray<>();

    public void saveHistory(History history) {
        historyCache.put(history.getGid(), history);
    }

    @Override
    public History getHistory(int gid) {
        History history = historyCache.get(gid);
        if (history == null) {
            // TODO:
            history = testHistory();
            if (history.getGid() != gid) {
                return null;
            }
            historyCache.put(history.getGid(), history);
        }
        return history;
    }

    // FIXME:
    private static History testHistory() {
        String steps = Base64.encode(new byte[1]);
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        for (int i = 1; i < 16; ++i) {
            numbers.add(0);
        }

        Map<String, Object> historyInfo = new HashMap<>();
        historyInfo.put("tid", 1001);
        historyInfo.put("bid", 0);
        historyInfo.put("gid", 12301);
        historyInfo.put("player", ID.ANYONE.toString());
        historyInfo.put("score", 1);
        historyInfo.put("time", new Date().getTime() / 1000);
        historyInfo.put("steps", steps);
        historyInfo.put("state", numbers);
        historyInfo.put("size", "4*4");
        return new History(historyInfo);
    }
}

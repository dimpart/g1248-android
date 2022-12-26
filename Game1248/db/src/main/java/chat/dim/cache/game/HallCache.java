package chat.dim.cache.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.format.JSON;
import chat.dim.g1248.dbi.HallDBI;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.Score;
import chat.dim.g1248.model.Table;

public class HallCache implements HallDBI {

    // sorted tables
    private final List<Table> cachedTables = new ArrayList<>();

    @Override
    public List<Table> getTables(int start, int end) {
        List<Table> tables = new ArrayList<>();
        int total = cachedTables.size();
        if (total == 0) {
            Table placeholder = new Table();
            tables.add(placeholder);
        } else {
            if (end > total) {
                // TODO: query

                end = total;
            }
            for (int index = start; index < end; ++index) {
                tables.add(cachedTables.get(index));
            }
        }
        return tables;
    }

    @Override
    public boolean updateTable(int tid, List<Board> boards, Score best) {
        // create table
        String array = boards == null ? "[]" : JSON.encode(boards);
        String dict = best == null ? "{}" : JSON.encode(best);

        Map<String, Object> info = new HashMap<>();
        info.put("tid", tid);
        info.put("boards", array);
        info.put("best", dict);
        Table table = new Table(info);

        // update cache
        int total = cachedTables.size();
        int index;
        int oid;
        for (index = 0; index < total; ++index) {
            oid = cachedTables.get(index).getTid();
            if (oid < tid) {
                continue;
            } else if (oid == tid) {
                // old record exists, remove it
                cachedTables.remove(index);
            }
            break;
        }
        cachedTables.add(index, table);
        return true;
    }
}

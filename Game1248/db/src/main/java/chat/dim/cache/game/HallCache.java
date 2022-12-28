package chat.dim.cache.game;

import java.util.ArrayList;
import java.util.List;

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
            // place an empty table
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
        Table table = new Table();
        table.setTid(tid);
        if (boards != null) {
            table.setBoards(boards);
        }
        if (best != null) {
            table.setBest(best);
        }

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
            // OK, place the new table here
            break;
        }
        cachedTables.add(index, table);
        return true;
    }
}

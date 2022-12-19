package chat.dim.g1248.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.g1248.dbi.HallDBI;
import chat.dim.g1248.model.Table;
import chat.dim.protocol.ID;

public class HallTable implements HallDBI {

    @Override
    public List<Table> getTables(int start, int end) {
        List<Table> tables = new ArrayList<>();

        // TODO: load tables from local storage
        int tableCount = 20;
        int boardCount = 4;

        Map<String, Object> tableInfo;
        Map<String, Object> boardInfo;
        List<Map<String, Object>> boardList;
        for (int i = 0; i < tableCount; ++i) {
            tableInfo = new HashMap<>();
            boardList = new ArrayList<>();
            for (int j = 0; j < boardCount; ++j) {
                boardInfo = new HashMap<>();
                boardInfo.put("bid", j);
                boardInfo.put("player", ID.ANYONE.toString());
                boardList.add(boardInfo);
            }
            tableInfo.put("boards", boardList);
            tables.add(new Table(tableInfo));
        }

        return tables;
    }
}

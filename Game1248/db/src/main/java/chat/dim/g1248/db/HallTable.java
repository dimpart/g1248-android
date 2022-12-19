package chat.dim.g1248.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.g1248.dbi.HallDBI;
import chat.dim.g1248.model.Table;
import chat.dim.protocol.ID;

/**
 *  Game Table
 *  ~~~~~~~~~~
 *
 *  JSON: {
 *      tid    : {TABLE_ID},
 *      // current playing boards
 *      boards : [
 *          {
 *              bid    : {BOARD_ID},     // 0, 1, 2, 3
 *              player : "{PLAYER_ID}",  // current player
 *
 *              // details, will not show in hall
 *              gid    : {GAME_ID},      // game id
 *              score  : 10000,          // current sore
 *              state  : [               // current state
 *                  0, 1, 2, 4,
 *                  0, 1, 2, 4,
 *                  0, 1, 2, 4,
 *                  0, 1, 2, 4
 *              ],
 *              size   : "4*4"
 *          },
 *          //...
 *      ],
 *      // score of the winner in this table, may be null
 *      best   : {
 *          bid    : {BOARD_ID},
 *          gid    : {GAME_ID},      // game id
 *          player : "{PLAYER_ID}",  // game player
 *          score  : 10000,          // game sore
 *          time   : {TIMESTAMP}
 *      }
 *  }
 */

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

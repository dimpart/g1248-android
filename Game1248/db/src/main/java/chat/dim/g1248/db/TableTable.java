package chat.dim.g1248.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.g1248.dbi.TableDBI;
import chat.dim.g1248.model.Board;
import chat.dim.protocol.ID;

/**
 *  Game Board
 *  ~~~~~~~~~~
 *
 *  JSON: {
 *      bid    : {BOARD_ID},     // 0, 1, 2, 3
 *      player : "{PLAYER_ID}",  // current player
 *
 *      // details, will not show in hall
 *      gid    : {GAME_ID},      // game id
 *      score  : 10000,          // current sore
 *      state  : [               // current state
 *          0, 1, 2, 4,
 *          0, 1, 2, 4,
 *          0, 1, 2, 4,
 *          0, 1, 2, 4
 *      ],
 *      size   : "4*4"
 *  }
 */

public class TableTable implements TableDBI {

    @Override
    public List<Board> getBoards(int tid) {
        List<Board> boards = new ArrayList<>();

        // TODO: load boards from local storage
        final int boardCount = 4;
        List<Integer> state = new ArrayList<>();
        for (int i = 0; i < 16; ++i) {
            state.add((int) Math.pow(2, i));
        }

        Map<String, Object> boardInfo;
        for (int i = 0; i < boardCount; ++i) {
            boardInfo = new HashMap<>();
            boardInfo.put("bid", i);
            boardInfo.put("player", ID.ANYONE.toString());
            boardInfo.put("gid", 12345);
            boardInfo.put("score", 10000);
            boardInfo.put("state", state);
            boardInfo.put("size", "4*4");
            boards.add(new Board(boardInfo));
        }

        return boards;
    }
}

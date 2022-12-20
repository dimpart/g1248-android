package chat.dim.game1248.table;

import androidx.lifecycle.ViewModel;

import java.util.List;

import chat.dim.g1248.db.Database;
import chat.dim.g1248.model.Board;

public class TableViewModel extends ViewModel {

    private static Database database = Database.getInstance();

    public Board getBoard(int tid, int bid) {

        List<Board> boards = database.tableTable.getBoards(tid);
        if (boards == null) {
            return null;
        }

        // 1. get by 'bid'
        for (Board item : boards) {
            if (item.getBid() == bid) {
                return item;
            }
        }

        // 2. get by index
        if (bid < 0 || bid >= boards.size()) {
            return null;
        }
        Board candidate = boards.get(bid);
        // if 'bid' not in the board info, then take it.
        if (candidate.containsKey("bid")) {
            return null;
        } else {
            return candidate;
        }
    }
}

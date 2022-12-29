package chat.dim.game1248.table;

import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Random;

import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.State;
import chat.dim.g1248.model.Step;
import chat.dim.utils.Log;

public class TableViewModel extends ViewModel {

    Board getBoard(int tid, int bid) {

        SharedDatabase database = SharedDatabase.getInstance();

        List<Board> boards = database.getBoards(tid);
        if (boards != null) {
            // 1. get by 'bid'
            for (Board item : boards) {
                if (item.getBid() == bid) {
                    return item;
                }
            }

            // 2. get by index
            if (0 <= bid && bid < boards.size()) {
                Board candidate = boards.get(bid);
                // if 'bid' not in the board info, then take it.
                if (!candidate.containsKey("bid")) {
                    return candidate;
                }
            }
        }

        // new board
        return new Board(tid, bid, Board.DEFAULT_SIZE);
    }

    History getCurrentGameHistory(int tid, int bid, int gid) {

        SharedDatabase database = SharedDatabase.getInstance();

        // get history by gid
        History history = database.getHistory(gid);
        if (history == null) {
            if (gid > 0) {
                Log.error("history not found: gid=" + gid);
                return null;
            }
            // new game with first random number
            Step first = new Step(randomByte() & 0x3F);
            State matrix = new State(Board.DEFAULT_SIZE.width);
            matrix.showNumber(first);
            history = new History(tid, bid, Board.DEFAULT_SIZE);
            history.addStep(first.getByte());
            history.setMatrix(matrix);
        }
        if (history.getTid() != tid || history.getBid() != bid) {
            // move to current board
            history.setTid(tid);
            history.setBid(bid);
            database.saveHistory(history);
        }

        return history;
    }

    static byte randomByte() {
        Random random = new Random();
        return (byte) random.nextInt();
    }
}

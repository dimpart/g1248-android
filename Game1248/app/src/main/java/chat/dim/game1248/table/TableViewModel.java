package chat.dim.game1248.table;

import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Random;

import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.State;
import chat.dim.g1248.model.Step;

public class TableViewModel extends ViewModel {

    public Board getBoard(int tid, int bid) {

        SharedDatabase database = SharedDatabase.getInstance();

        List<Board> boards = database.getBoards(tid);
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

    public void saveHistory(History history) {

        SharedDatabase database = SharedDatabase.getInstance();

        database.saveHistory(history);
    }
    public History getHistory(int tid, int bid, int gid) {

        SharedDatabase database = SharedDatabase.getInstance();

        History history = database.getHistory(gid);
        //gid = history.getGid();
        if (gid == 0) {
            if (history == null) {
                // new game with first random number
                Step first = new Step(randomByte() & 0x3F);
                State matrix = new State(4);
                matrix.showNumber(first);
                history = new History();
                history.addStep(first.getByte());
                history.setState(matrix);
                saveHistory(history);
            }
            // move to current board
            history.setTid(tid);
            history.setBid(bid);
        }

        return history;
    }

    public static byte randomByte() {
        Random random = new Random();
        return (byte) random.nextInt();
    }
}

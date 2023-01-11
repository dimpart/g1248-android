package chat.dim.game1248.room;

import androidx.lifecycle.ViewModel;

import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.Stage;
import chat.dim.g1248.model.Step;
import chat.dim.utils.Log;

public class RoomViewModel extends ViewModel {

    Board getBoard(int rid, int bid) {

        SharedDatabase database = SharedDatabase.getInstance();

        return database.getBoard(rid, bid);
    }

    History getCurrentGameHistory(int rid, int bid, int gid) {

        SharedDatabase database = SharedDatabase.getInstance();

        // get history by gid
        History history = database.getHistory(gid);
        if (history == null) {
            if (gid > 0) {
                Log.error("history not found: gid=" + gid);
                return null;
            }
            // new game with first random number
            Step first = Step.first();
            Stage matrix = new Stage(Board.DEFAULT_SIZE);
            matrix.showNumber(first);
            history = new History(rid, bid, Board.DEFAULT_SIZE);
            history.addStep(first.getByte());
            history.setMatrix(matrix);
            database.saveHistory(history);
        } else if (history.getRid() != rid || history.getBid() != bid) {
            // move to current board
            history.setRid(rid);
            history.setBid(bid);
            database.saveHistory(history);
        }

        return history;
    }
}

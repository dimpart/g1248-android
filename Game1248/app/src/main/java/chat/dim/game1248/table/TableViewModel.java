package chat.dim.game1248.table;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import chat.dim.g1248.db.Database;
import chat.dim.g1248.model.Board;

public class TableViewModel extends ViewModel {

    private static Database database = Database.getInstance();

    public List<Board> getBoards(int tid) {

        List<Board> boards = database.tableTable.getBoards(tid);
        if (boards == null) {
            return new ArrayList<>();
        } else {
            return boards;
        }
    }
}

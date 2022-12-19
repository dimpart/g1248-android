package chat.dim.game1248.hall;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import chat.dim.g1248.db.Database;
import chat.dim.g1248.model.Table;

public class HallViewModel extends ViewModel {

    private static Database database = Database.getInstance();

    public List<Table> getTables(int start, int end) {

        List<Table> tables = database.hallTable.getTables(start, end);
        if (tables == null) {
            return new ArrayList<>();
        } else {
            return tables;
        }
    }
}

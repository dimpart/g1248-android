package chat.dim.game1248.hall;

import androidx.lifecycle.ViewModel;

import java.util.List;

import chat.dim.g1248.db.Database;
import chat.dim.g1248.model.Table;

public class HallViewModel extends ViewModel {

    private static Database database = Database.getInstance();

    public Table[] getTables(int start, int end) {

        List<Table> tables = database.hallTable.getTables(start, end);
        if (tables == null) {
            return new Table[0];
        }

        Table[] list = new Table[tables.size()];
        for (int index = 0; index < tables.size(); ++index) {
            list[index] = tables.get(index);
        }
        return list;
    }
}

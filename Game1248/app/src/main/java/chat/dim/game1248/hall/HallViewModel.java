package chat.dim.game1248.hall;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Table;

public class HallViewModel extends ViewModel {

    public List<Table> getTables(int start, int end) {

        SharedDatabase database = SharedDatabase.getInstance();

        List<Table> tables = database.getTables(start, end);
        if (tables == null) {
            return new ArrayList<>();
        } else {
            return tables;
        }
    }
}

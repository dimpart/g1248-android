package chat.dim.game1248.hall;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Room;

public class HallViewModel extends ViewModel {

    public List<Room> getRooms(int start, int end) {

        SharedDatabase database = SharedDatabase.getInstance();

        List<Room> rooms = database.getRooms(start, end);
        if (rooms == null) {
            return new ArrayList<>();
        } else {
            return rooms;
        }
    }
}

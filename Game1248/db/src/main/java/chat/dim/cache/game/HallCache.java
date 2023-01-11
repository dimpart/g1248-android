package chat.dim.cache.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chat.dim.g1248.dbi.HallDBI;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.Room;
import chat.dim.g1248.model.Score;

public class HallCache implements HallDBI {

    // sorted rooms
    private final List<Room> cachedRooms = new ArrayList<>();

    private final Room placeholder = new Room();

    @Override
    public Room getRoom(int rid) {
        if (rid == 0) {
            return placeholder;
        }
        Iterator<Room> iterator = cachedRooms.iterator();
        Room item;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (item.getRid() == rid) {
                return item;
            }
        }
        return null;
    }

    @Override
    public List<Room> getRooms(int start, int end) {
        List<Room> rooms = new ArrayList<>();
        int total = cachedRooms.size();
        if (total == 0) {
            // place an empty room
            rooms.add(placeholder);
        } else {
            if (end > total) {
                // TODO: query

                end = total;
            }
            for (int index = start; index < end; ++index) {
                rooms.add(cachedRooms.get(index));
            }
        }
        return rooms;
    }

    @Override
    public boolean updateRoom(int rid, List<Board> boards, Score best) {
        // create room
        Room room = new Room();
        room.setRid(rid);
        if (boards != null) {
            room.setBoards(boards);
        }
        if (best != null) {
            room.setBest(best);
        }

        // update cache
        int total = cachedRooms.size();
        int index;
        int oid;
        for (index = 0; index < total; ++index) {
            oid = cachedRooms.get(index).getRid();
            if (oid < rid) {
                continue;
            } else if (oid == rid) {
                // old record exists, remove it
                cachedRooms.remove(index);
            }
            // OK, place the new room here
            break;
        }
        cachedRooms.add(index, room);
        return true;
    }
}

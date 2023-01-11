package chat.dim.g1248.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Room;
import chat.dim.notification.NotificationCenter;
import chat.dim.protocol.Content;
import chat.dim.protocol.CustomizedContent;
import chat.dim.protocol.ID;
import chat.dim.protocol.ReliableMessage;
import chat.dim.utils.Log;

public class HallHandler extends GameHallContentHandler {

    private final SharedDatabase database;

    public HallHandler(SharedDatabase db) {
        super();
        database = db;
    }

    @Override
    protected List<Content> handleSeekRequest(ID sender, CustomizedContent content, ReliableMessage rMsg) {
        // S -> C: "seeking"
        throw new AssertionError("should not happen: " + content);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Content> handleSeekResponse(ID sender, CustomizedContent content, ReliableMessage rMsg) {
        Log.info("[GAME] received seek response: " + sender + ", " + content);
        // S -> C: "rooms"
        Object array = content.get("rooms");
        List<Room> rooms;
        if (array instanceof List) {
            rooms = Room.convertRooms((List<Object>) array);
            for (Room item : rooms) {
                database.updateRoom(item.getRid(), item.getBoards(), item.getBest());
            }
        } else {
            throw new AssertionError("cat not fetch rooms: " + content);
        }

        Map<String, Object> info = new HashMap<>();
        info.put("rooms", rooms);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.postNotification(NotificationNames.RoomsUpdated, this, info);

        // no need to respond this content
        return null;
    }
}

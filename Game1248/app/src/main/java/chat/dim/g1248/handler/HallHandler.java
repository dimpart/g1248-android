package chat.dim.g1248.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Table;
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
        // S -> C: "tables"
        Object array = content.get("tables");
        List<Table> tables;
        if (array instanceof List) {
            tables = Table.convertTables((List<Object>) array);
            for (Table item : tables) {
                database.updateTable(item.getTid(), item.getBoards(), item.getBest());
            }
        } else {
            throw new AssertionError("cat not fetch tables: " + content);
        }

        Map<String, Object> info = new HashMap<>();
        info.put("tables", tables);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.postNotification(NotificationNames.TablesUpdated, this, info);

        // no need to respond this content
        return null;
    }
}

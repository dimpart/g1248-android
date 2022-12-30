package chat.dim.g1248.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.History;
import chat.dim.notification.NotificationCenter;
import chat.dim.protocol.Content;
import chat.dim.protocol.CustomizedContent;
import chat.dim.protocol.ID;
import chat.dim.protocol.ReliableMessage;
import chat.dim.utils.Log;

public class HistoryHandler extends GameHistoryContentHandler {

    private final SharedDatabase database;

    public HistoryHandler(SharedDatabase db) {
        super();
        database = db;
    }

    @Override
    protected List<Content> handleFetchRequest(ID sender, CustomizedContent content, ReliableMessage rMsg) {
        // S -> C: "fetching"
        throw new AssertionError("should not happen: " + content);
    }

    @Override
    protected List<Content> handleFetchResponse(ID sender, CustomizedContent content, ReliableMessage rMsg) {
        Log.info("[GAME] received fetch response: " + sender + ", " + content);
        // S -> C: "fetched"
        History history = History.parseHistory(content.get("history"));
        if (history == null) {
            Log.error("fetch response error: " + content);
            return null;
        }
        if (!database.saveHistory(history)) {
            Log.error("failed to save history: " + history);
            return null;
        }

        Map<String, Object> info = new HashMap<>();
        info.put("history", history);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.postNotification(NotificationNames.GameHistoryUpdated, this, info);

        // no need to respond this content
        return null;
    }
}

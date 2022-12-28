package chat.dim.g1248.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.cache.game.HistoryCache;
import chat.dim.g1248.GlobalVariable;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Board;
import chat.dim.game1248.Client;
import chat.dim.game1248.NotificationNames;
import chat.dim.notification.NotificationCenter;
import chat.dim.protocol.Content;
import chat.dim.protocol.CustomizedContent;
import chat.dim.protocol.ID;
import chat.dim.protocol.ReliableMessage;
import chat.dim.utils.Log;

public class TableHandler extends GameTableContentHandler {

    private final SharedDatabase database;

    public TableHandler(SharedDatabase db) {
        super();
        database = db;
    }

    @Override
    protected List<Content> handleWatchRequest(ID sender, CustomizedContent content, ReliableMessage rMsg) {
        // C -> S: "watching"
        throw new AssertionError("should not happen: " + content);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Content> handleWatchResponse(ID sender, CustomizedContent content, ReliableMessage rMsg) {
        Log.info("[GAME] received watch response: " + sender + ", " + content);
        // S -> C: "boards"
        int tid;
        Object integer;
        integer = content.get("tid");
        if (integer == null) {
            throw new AssertionError("table id not found: " + content);
        } else {
            tid = ((Number) integer).intValue();
        }

        GlobalVariable shared = GlobalVariable.getInstance();
        Client client = (Client) shared.terminal;

        Object array = content.get("boards");
        if (array instanceof List) {
            List<Board> boards = Board.convert((List<Object>) array);
            for (Board item : boards) {
                if (tid == client.tid && item.getBid() == client.bid) {
                    // FIXME:
                    Log.debug("this board is playing");
                    continue;
                }

                database.updateBoard(tid, item);

                Map<String, Object> info = new HashMap<>();
                info.put("tid", tid);
                info.put("bid", item.getBid());
                info.put("board", item);
                NotificationCenter nc = NotificationCenter.getInstance();
                nc.postNotification(NotificationNames.GameBoardUpdated, this, info);
            }
        }

        return null;
    }

    @Override
    protected List<Content> handlePlayRequest(ID sender, CustomizedContent content, ReliableMessage rMsg) {
        // C -> S: "playing"
        throw new AssertionError("should not happen: " + content);
    }

    @Override
    protected List<Content> handlePlayResponse(ID sender, CustomizedContent content, ReliableMessage rMsg) {
        Log.info("[GAME] received play response: " + sender + ", " + content);
        // S -> C: "played"
        int tid = (int) content.get("tid");
        int bid = (int) content.get("bid");
        int gid = (int) content.get("gid");
        ID player = ID.parse(content.get("player"));
        if (tid > 0 && gid > 0 && player != null) {
            HistoryCache cache = (HistoryCache) database.historyDatabase;
            cache.updatePlayingHistory(tid, bid, gid, player);
        }
        return null;
    }
}

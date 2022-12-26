package chat.dim.g1248.handler;

import java.util.List;

import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Table;
import chat.dim.protocol.Content;
import chat.dim.protocol.CustomizedContent;
import chat.dim.protocol.ID;
import chat.dim.protocol.ReliableMessage;

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
        // S -> C: "tables"
        Object array = content.get("tables");
        if (array instanceof List) {
            List<Table> tables = Table.convert((List<Object>) array);
            for (Table item : tables) {
                database.updateTable(item.getTid(), item.getBoards(), item.getBest());
            }
        } else {
            throw new AssertionError("cat not fetch tables: " + content);
        }
        // no need to respond this content
        return null;
    }
}

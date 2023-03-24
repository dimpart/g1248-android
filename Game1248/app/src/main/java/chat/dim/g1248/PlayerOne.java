package chat.dim.g1248;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chat.dim.ClientMessenger;
import chat.dim.Terminal;
import chat.dim.fsm.Delegate;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.Room;
import chat.dim.g1248.protocol.GameCustomizedContent;
import chat.dim.g1248.protocol.GameHallContent;
import chat.dim.g1248.protocol.GameHistoryContent;
import chat.dim.g1248.protocol.GameRoomContent;
import chat.dim.mkm.User;
import chat.dim.network.ClientSession;
import chat.dim.network.SessionState;
import chat.dim.notification.NotificationCenter;
import chat.dim.protocol.Address;
import chat.dim.protocol.ArrayContent;
import chat.dim.protocol.Content;
import chat.dim.protocol.Envelope;
import chat.dim.protocol.ForwardContent;
import chat.dim.protocol.GroupCommand;
import chat.dim.protocol.ID;
import chat.dim.protocol.InstantMessage;
import chat.dim.protocol.ReliableMessage;
import chat.dim.protocol.SecureMessage;
import chat.dim.protocol.TextContent;
import chat.dim.utils.Log;

public enum PlayerOne implements Delegate<StateMachine, StateTransition, PlayerState> {

    INSTANCE;

    public static PlayerOne getInstance() {
        return INSTANCE;
    }

    private static final ID CHAT_GROUP = ID.create("chatroom", Address.EVERYWHERE, null);
    private static final String APP_ID = GameCustomizedContent.APP_ID;  // "chat.dim.g1248"

    public User user = null;

    public ID chatBot = null;
    public ID gameBot = null;

    public Room room = null;
    public Board board = null;

    private StateMachine fsm;

    PlayerOne() {
        start();
    }

    public boolean equals(ID other) {
        User current = user;
        if (current == null) {
            return other == null;
        } else {
            return current.getIdentifier().equals(other);
        }
    }

    public int getRid() {
        Room current = room;
        return current == null ? -1 : current.getRid();
    }
    public int getBid() {
        Board current = board;
        return current == null ? -1 : current.getBid();
    }
    public int getGid() {
        Board current = board;
        return current == null ? -1 : current.getGid();
    }

    private ID getLoginUser() {
        User current = user;
        if (current == null) {
            Log.error("current user not set");
            return null;
        }
        GlobalVariable shared = GlobalVariable.getInstance();
        Terminal client = shared.terminal;
        SessionState sessionState = client.getState();
        ClientMessenger messenger = client.getMessenger();
        if (sessionState == null || messenger == null) {
            // not connect
            return null;
        }
        ClientSession session = messenger.getSession();
        ID uid = session.getIdentifier();
        if (uid == null || !sessionState.equals(SessionState.Order.RUNNING)) {
            // handshake not accepted
            return null;
        }
        return current.getIdentifier();
    }
    private static ClientMessenger getMessenger() {
        GlobalVariable shared = GlobalVariable.getInstance();
        Terminal client = shared.terminal;
        return client.getMessenger();
    }

    public static final int BASE_ROOM_ID = 2048 * 1000 * 1000;

    private boolean sendChatContent(Content content, int rid) {
        ID bot = chatBot;
        ID current = getLoginUser();
        if (bot == null || current == null) {
            Log.error("cannot send chat content: " + current + " => " + bot);
            return false;
        }
        ClientMessenger messenger = getMessenger();

        //int rid = room == null ? -1 : room.getRid();
        rid += BASE_ROOM_ID;

        content.setGroup(CHAT_GROUP);
        content.put("app", APP_ID);
        content.put("room", rid);

        Envelope env = Envelope.create(current, CHAT_GROUP, null);
        InstantMessage iMsg = InstantMessage.create(env, content);
        SecureMessage sMsg = messenger.encryptMessage(iMsg);
        if (sMsg == null) {
            Log.error("failed to encrypt message");
            return false;
        }
        ReliableMessage rMsg = messenger.signMessage(sMsg);
        if (rMsg == null) {
            Log.error("failed to sign message");
            return false;
        }
        ForwardContent forward = ForwardContent.create(rMsg);
        forward.put("app", APP_ID);
        forward.put("room", rid);

        Map<String, Object> info = new HashMap<>();
        info.put("content", content);
        info.put("msg", iMsg);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.postNotification(NotificationNames.MessageSent, this, info);


        return messenger.sendContent(current, bot, forward, 0).second != null;
    }
    private boolean sendChatRequest(GroupCommand command, int rid) {
        ID bot = chatBot;
        ID current = getLoginUser();
        if (bot == null || current == null) {
            Log.error("cannot send chat request: " + current + " => " + bot);
            return false;
        }
        ClientMessenger messenger = getMessenger();

        //int rid = room == null ? -1 : room.getRid();
        rid += BASE_ROOM_ID;

        command.put("app", APP_ID);
        command.put("room", rid);

        List<Content> contents = new ArrayList<>();
        contents.add(command);
        ArrayContent array = ArrayContent.create(contents);
        array.put("app", APP_ID);
        array.put("room", rid);

        return messenger.sendContent(current, bot, array, 0).second != null;
    }
    private boolean sendGameContent(GameCustomizedContent content) {
        ID bot = gameBot;
        ID current = getLoginUser();
        if (bot == null || current == null) {
            Log.error("cannot send game content: " + current + " => " + bot);
            return false;
        }
        ClientMessenger messenger = getMessenger();
        return messenger.sendContent(current, bot, content, 0).second != null;
    }

    public boolean joinRoom(int rid) {
        GroupCommand command = GroupCommand.join(CHAT_GROUP);
        return sendChatRequest(command, rid);
    }
    public boolean quitRoom(int rid) {
        GroupCommand command = GroupCommand.quit(CHAT_GROUP);
        return sendChatRequest(command, rid);
    }
    public boolean queryRoom() {
        if (room == null) {
            return false;
        }
        int rid = room.getRid();
        GroupCommand command = GroupCommand.query(CHAT_GROUP);
        return sendChatRequest(command, rid);
    }

    public void sendText(String text, int rid) {
        TextContent content = TextContent.create(text);
        boolean ok = sendChatContent(content, rid);
        if (ok) {
            Log.info("send message: " + text);
        } else {
            Log.warning("message not send: " + text);
        }
    }

    public void sendSeeking(int start, int end) {
        GameHallContent request = GameHallContent.seek(start, end);
        boolean ok = sendGameContent(request);
        if (ok) {
            Log.info("seeking sent: start=" + start + ", end=" + end);
        } else {
            Log.warning("seeking not send: start=" + start + ", end=" + end);
        }
    }
    public void sendWatching(int rid, int bid) {
        GameRoomContent request = GameRoomContent.watch(rid, bid);
        boolean ok = sendGameContent(request);
        if (ok) {
            Log.info("watching sent: rid=" + rid + ", bid=" + bid);
        } else {
            Log.warning("watching not send: rid=" + rid + ", bid=" + bid);
        }
    }
    public void sendPlaying(History history) {
        GameRoomContent request = GameRoomContent.play(history);
        boolean ok = sendGameContent(request);
        if (ok) {
            Log.info("playing sent");
        } else {
            Log.warning("playing not send");
        }
    }
    public void sendFetching(int gid) {
        GameHistoryContent request = GameHistoryContent.fetch(gid);
        boolean ok = sendGameContent(request);
        if (ok) {
            Log.info("fetching sent: " + gid);
        } else {
            Log.warning("fetching not send: " + gid);
        }
    }

    public void start() {
        StateMachine machine = fsm;
        if (machine == null) {
            machine = new StateMachine(this);
            machine.setDelegate(this);
            machine.start();
            fsm = machine;
        }
    }
    public void stop() {
        StateMachine machine = fsm;
        if (machine != null) {
            fsm = null;
            machine.stop();
        }
    }

    public PlayerState getCurrentState() {
        StateMachine machine = fsm;
        return machine == null ? null : machine.getCurrentState();
    }

    //
    //  FSM Delegate
    //

    @Override
    public void enterState(PlayerState next, StateMachine ctx, long now) {
        // called before state changed
    }

    @Override
    public void exitState(PlayerState previous, StateMachine ctx, long now) {
        // called after state changed
        PlayerState current = ctx.getCurrentState();
        Log.info("player state changed: " + previous + " => " + current);
        if (current == null) {
            return;
        }
        if (current.equals(PlayerState.Order.SEEKING)) {
            // send request to the bot
            sendSeeking(0, 20);
        } else if (current.equals(PlayerState.Order.WATCHING)) {
            // TODO: previous == PLAYING?
            // send request to the bot
            Room watchRoom = room;
            Board watchBoard = board;
            if (watchRoom != null && watchBoard != null) {
                sendWatching(watchRoom.getRid(), watchBoard.getBid());
            }
        }
    }

    @Override
    public void pauseState(PlayerState current, StateMachine ctx, long now) {

    }

    @Override
    public void resumeState(PlayerState current, StateMachine ctx, long now) {

    }
}

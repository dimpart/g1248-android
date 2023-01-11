package chat.dim.g1248;

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
import chat.dim.protocol.Content;
import chat.dim.protocol.ID;
import chat.dim.utils.Log;

public enum PlayerOne implements Delegate<StateMachine, StateTransition, PlayerState> {

    INSTANCE;

    public static PlayerOne getInstance() {
        return INSTANCE;
    }

    public ID bot = null;
    public User user = null;

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

    private static boolean sendContent(ID sender, ID receiver, Content content) {
        GlobalVariable shared = GlobalVariable.getInstance();
        Terminal client = shared.terminal;
        SessionState sessionState = client.getState();
        ClientMessenger messenger = client.getMessenger();
        if (sessionState == null || messenger == null) {
            // not connect
            return false;
        }
        ClientSession session = messenger.getSession();
        ID uid = session.getIdentifier();
        if (uid == null || !sessionState.equals(SessionState.RUNNING)) {
            // handshake not accepted
            return false;
        }
        return messenger.sendContent(sender, receiver, content, 0).second != null;
    }
    public boolean sendGameContent(GameCustomizedContent content) {
        ID gameBot = bot;
        User currentUser = user;
        if (gameBot == null || currentUser == null) {
            return false;
        }
        return sendContent(user.getIdentifier(), gameBot, content);
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
    public void enterState(PlayerState next, StateMachine ctx) {
        // called before state changed
    }

    @Override
    public void exitState(PlayerState previous, StateMachine ctx) {
        // called after state changed
        PlayerState current = ctx.getCurrentState();
        Log.info("player state changed: " + previous + " => " + current);
        if (current == null) {
            return;
        }
        if (current.equals(PlayerState.SEEKING)) {
            // send request to the bot
            sendSeeking(0, 20);
        } else if (current.equals(PlayerState.WATCHING)) {
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
    public void pauseState(PlayerState current, StateMachine ctx) {

    }

    @Override
    public void resumeState(PlayerState current, StateMachine ctx) {

    }
}

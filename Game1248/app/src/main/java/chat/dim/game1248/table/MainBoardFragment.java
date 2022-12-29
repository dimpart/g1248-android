package chat.dim.game1248.table;

import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.g1248.PlayerOne;
import chat.dim.g1248.PlayerState;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.State;
import chat.dim.g1248.model.Step;
import chat.dim.notification.Notification;
import chat.dim.protocol.ID;
import chat.dim.threading.BackgroundThreads;
import chat.dim.threading.MainThread;
import chat.dim.utils.Log;

public class MainBoardFragment extends BoardFragment {

    public MainBoardFragment(int tid, int bid) {
        super(tid, bid);
    }

    @Override
    public void onReceiveNotification(Notification notification) {
        String name = notification.name;
        Map info = notification.userInfo;
        assert name != null && info != null : "notification error: " + notification;
        if (!name.equals(NotificationNames.GameBoardUpdated)) {
            // should not happen
            return;
        }
        int tid = (int) info.get("tid");
        int bid = (int) info.get("bid");
        if (tid != tableId || bid != boardId) {
            // not mine
            return;
        }
        Board board = (Board) info.get("board");
        assert board != null && board.getBid() == bid : "bid error: " + bid + ", " + board;
        ID player = board.getPlayer();
        if (player == null) {
            // received data error?
            Log.error("error board: " + board);
            return;
        }
        PlayerOne theOne = PlayerOne.getInstance();
        if (theOne.equals(player)) {
            Log.info("skip my board");
            return;
        }

        Log.info("[GAME] refreshing tid: " + tid + ", bid: " + bid);
        reloadBoard(board);
    }

    @Override
    protected void reloadBoard(Board board) {
        PlayerOne theOne = PlayerOne.getInstance();
        PlayerState playerState = theOne.getCurrentState();
        if (playerState == null || !playerState.equals(PlayerState.PLAYING)) {
            // 'watching'
            super.reloadBoard(board);
        }

        // 1. check player on the game board
        ID player = board.getPlayer();
        if (player != null && !theOne.equals(player)) {
            Log.error("player not match: " + player);
            return;
        }

        // 2. get game history with gid on the board
        int gid = board.getGid();
        History history = mViewModel.getCurrentGameHistory(tableId, boardId, gid);
        if (history == null) {
            Log.error("history not found, fetching gid: " + gid);
            theOne.sendFetching(gid);
            return;
        }
        Log.info("history: " + history);

        // 3. refresh info from history
        State matrix = history.getMatrix();
        state.clear();
        state.addAll(matrix.toArray());
        score = history.getScore();
        steps = history.getSteps();

        MainThread.call(this::onReload);
    }

    void onSwipe(Step.Direction direction) {
        Log.debug("swipe: " + direction);
        BackgroundThreads.rush(() -> doSwipe(direction));
    }

    private void doSwipe(Step.Direction direction) {
        PlayerOne theOne = PlayerOne.getInstance();
        PlayerState playerState = theOne.getCurrentState();
        if (playerState == null || !playerState.equals(PlayerState.PLAYING)) {
            // 'watching'
            Log.warning("the player one is watching, cannot play game");
            return;
        }

        // 1. check player on the game board
        Board board = theOne.board;
        assert board != null : "failed to get board: tid=" + tableId + ", bid=" + boardId;
        ID player = board.getPlayer();
        if (player != null && !theOne.equals(player)) {
            Log.error("this seat is occupied");
            return;
        }

        // 2. get game history with gid on the board
        int gid = board.getGid();
        History history = mViewModel.getCurrentGameHistory(tableId, boardId, gid);
        if (history == null) {
            Log.error("history not found, fetching gid: " + gid);
            theOne.sendFetching(gid);
            return;
        }
        Log.info("history: " + history);

        // do swipe
        byte prefix = (byte) ((direction.value & 0x03) << 6);
        byte suffix = (byte) (TableViewModel.randomByte() & 0x3F);
        Step next = new Step(prefix | suffix);

        State matrix = history.getMatrix();
        if (!matrix.swipe(next)) {
            // nothing moved
            Log.info("nothing moved");
            return;
        }
        matrix.showNumber(next);

        // update history
        history.addStep(next.getByte());
        history.setMatrix(matrix);

        SharedDatabase db = SharedDatabase.getInstance();
        db.saveHistory(history);

        // post to the bot
        theOne.sendPlaying(history);

        // 3. refresh info from history
        state.clear();
        state.addAll(matrix.toArray());
        score = history.getScore();
        steps = history.getSteps();

        MainThread.call(this::onReload);
    }
}

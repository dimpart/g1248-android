package chat.dim.game1248.table;

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
        PlayerOne theOne = PlayerOne.getInstance();
        PlayerState state = theOne.getCurrentState();
        if (state == null || !state.equals(PlayerState.PLAYING)) {
            // 'watching'
            super.onReceiveNotification(notification);
        } else {
            Log.info("the player one is playing, don't refresh board");
        }
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
        Log.debug("history: " + history);

        // 3. refresh info from history
        State matrix = history.getState();
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
        Log.debug("history: " + history);

        // do swipe
        byte prefix = (byte) ((direction.value & 0x03) << 6);
        byte suffix = (byte) (TableViewModel.randomByte() & 0x3F);
        Step next = new Step(prefix | suffix);

        State matrix = history.getState();
        if (!matrix.swipe(next)) {
            // nothing moved
            Log.info("nothing moved");
            return;
        }
        matrix.showNumber(next);

        // update history
        history.addStep(next.getByte());
        history.setState(matrix);

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

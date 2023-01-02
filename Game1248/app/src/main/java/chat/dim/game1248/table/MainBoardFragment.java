package chat.dim.game1248.table;

import java.util.List;
import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.g1248.PlayerOne;
import chat.dim.g1248.PlayerState;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.Square;
import chat.dim.g1248.model.State;
import chat.dim.g1248.model.Step;
import chat.dim.g1248.model.Table;
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
        PlayerOne theOne = PlayerOne.getInstance();
        Board board = theOne.board;
        if (board == null || board.getGid() <= 0) {
            Log.error("error board: " + board);
            return;
        }

        Log.info("[GAME] refreshing tid: " + tid + ", bid: " + bid);
        reloadBoard(board);
    }

    @Override
    void loadBoard() {
        PlayerOne theOne = PlayerOne.getInstance();
        Table table = theOne.table;
        Board board = theOne.board;
        assert table != null && board != null : "player one error: " + table + ", " + board;
        if (!(board instanceof History)) {
            History history = mViewModel.getCurrentGameHistory(tableId, boardId, board.getGid());
            if (history != null) {
                board = history;
                theOne.board = history;
            }
        }
        Log.info("show my board: " + board);
        reloadBoard(board);
    }

    void onSwipe(Step.Direction direction) {
        Log.debug("swipe: " + direction);
        BackgroundThreads.rush(() -> doSwipe(direction));
    }

    private void doSwipe(Step.Direction direction) {
        PlayerOne theOne = PlayerOne.getInstance();
        PlayerState playerState = theOne.getCurrentState();
        if (playerState != null) {
            if (playerState.equals(PlayerState.DEFAULT)) {
                // TODO: show message to user
                Log.warning("failed to create local user?");
            } else if (playerState.equals(PlayerState.SEEKING)) {
                // 'seeking'
                Log.error("should not happen: " + playerState);
                return;
            } else if (playerState.equals(PlayerState.WATCHING)) {
                // 'watching'
                Log.warning("the player one is watching, cannot play game");
                return;
            }
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
        List<Square.Movement> movements = matrix.swipe(next);
        Log.info("movements: " + movements);
        if (movements.size() == 0) {
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

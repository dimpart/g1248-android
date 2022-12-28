package chat.dim.game1248.table;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import chat.dim.g1248.GlobalVariable;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.State;
import chat.dim.g1248.model.Step;
import chat.dim.game1248.NotificationNames;
import chat.dim.mkm.User;
import chat.dim.notification.Notification;
import chat.dim.notification.NotificationCenter;
import chat.dim.protocol.ID;
import chat.dim.threading.BackgroundThreads;
import chat.dim.threading.MainThread;
import chat.dim.utils.Log;

public class MainBoardFragment extends BoardFragment {

    public MainBoardFragment(int tid, int bid) {
        super(tid, bid);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.addObserver(this, NotificationNames.PlayRespond);
    }

    @Override
    public void onDestroy() {
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.removeObserver(this, NotificationNames.PlayRespond);
        super.onDestroy();
    }

    @Override
    public void onReceiveNotification(Notification notification) {
        String name = notification.name;
        Map info = notification.userInfo;
        assert name != null && info != null : "notification error: " + notification;
        if (!name.equals(NotificationNames.GameBoardUpdated) &&
                !name.equals(NotificationNames.PlayRespond)) {
            // should not happen
            return;
        }
        int tid = (int) info.get("tid");
        int bid = (int) info.get("bid");
        if (tid != tableId || bid != tableId) {
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

        GlobalVariable shared = GlobalVariable.getInstance();
        User user = shared.facebook.getCurrentUser();
        ID current = user == null ? null : user.getIdentifier();
        if (player.equals(current)) {
            // no need to refresh my game
            return;
        }

        Log.info("[GAME] refreshing tid: " + tid + ", bid: " + bid);
        reloadBoard(board);
    }

    @Override
    protected void reloadBoard(Board board) {
        ID player = board.getPlayer();

        GlobalVariable shared = GlobalVariable.getInstance();
        User user = shared.facebook.getCurrentUser();
        if (user == null || (player != null && !user.getIdentifier().equals(player))) {
            // when local user not set, or the board's current player not matched
            // TODO: send 'watching' to the bot

            super.reloadBoard(board);
            return;
        }
        // when the board's current player is not set, or exactly be current user
        // TODO: send 'playing' to the bot

        // get history with gid in the board
        int gid = board.getGid();
        History history = mViewModel.getHistory(tableId, boardId, gid);
        Log.debug("history: " + history);
        // get info from history
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

        // 0. check current user
        GlobalVariable shared = GlobalVariable.getInstance();
        User user = shared.facebook.getCurrentUser();
        ID current = user == null ? null : user.getIdentifier();
        if (current == null) {
            // FIXME: current user not set yet
            Log.error("current user not set yet");
            return;
        }

        // 1. get game board
        Board board = mViewModel.getBoard(tableId, boardId);
        assert board != null : "failed to get board: tid=" + tableId + ", bid=" + boardId;
        ID player = board.getPlayer();
        if (player != null && !player.equals(current)) {
            // TODO: this seat is occupied
            Log.info("this seat is occupied");
            return;
        }

        // 2. get game history id on the board
        int gid = board.getGid();
        History history = mViewModel.getHistory(tableId, boardId, gid);
        Log.debug("history: " + history);

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

        // post for client
        Map<String, Object> info = new HashMap<>();
        info.put("tid", tableId);
        info.put("bid", boardId);
        info.put("gid", gid);
        info.put("history", history);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.postNotification(NotificationNames.PlayNextMove, this, info);

        reloadBoard(board);
    }
}

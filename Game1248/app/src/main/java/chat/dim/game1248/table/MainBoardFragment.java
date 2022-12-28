package chat.dim.game1248.table;

import chat.dim.g1248.GlobalVariable;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.History;
import chat.dim.g1248.model.State;
import chat.dim.g1248.model.Step;
import chat.dim.mkm.User;
import chat.dim.protocol.ID;
import chat.dim.threading.BackgroundThreads;
import chat.dim.threading.MainThread;
import chat.dim.utils.Log;

public class MainBoardFragment extends BoardFragment {

    public MainBoardFragment(int tid, int bid) {
        super(tid, bid);
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

        // 1. check current user
        GlobalVariable shared = GlobalVariable.getInstance();
        User user = shared.facebook.getCurrentUser();
        if (user == null) {
            // TODO: current user not set yet
            return;
        }
        ID current = user.getIdentifier();

        // 2. get game history id from the board
        int gid = 0;
        Board board = mViewModel.getBoard(tableId, boardId);
        if (board != null) {
            ID player = board.getPlayer();
            if (player != null && !player.equals(current)) {
                // TODO: this seat is occupied
                return;
            }
            gid = board.getGid();
        }
        History history = mViewModel.getHistory(tableId, boardId, gid);
        Log.debug("history: " + history);

        byte prefix = (byte) ((direction.value & 0x03) << 6);
        byte suffix = (byte) (TableViewModel.randomByte() & 0x3F);
        Step next = new Step(prefix | suffix);

        State matrix = history.getState();
        if (!matrix.swipe(next)) {
            // nothing moved
            return;
        }
        matrix.showNumber(next);

        // update history
        history.addStep(next.getByte());
        history.setState(matrix);

        reloadData();
    }
}

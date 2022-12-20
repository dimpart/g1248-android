package chat.dim.game1248.table;

import java.util.List;

import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.Square;
import chat.dim.g1248.model.Step;
import chat.dim.threading.MainThread;

public class MainBoardFragment extends BoardFragment {

    public MainBoardFragment(int tid, int bid) {
        super(tid, bid);
    }

    @Override
    protected void reloadData() {
        Board board = mViewModel.getBoard(tableId, boardId);
        List<Square> squares = board.getState();
        assert squares != null && squares.size() == 16 : "state error: " + squares;
        state.clear();
        state.addAll(Square.revert(squares));

        MainThread.call(this::onReload);
    }

    public void onSwipe(Step.Direction direction) {
        System.out.println("swipe: " + direction);
    }
}

package chat.dim.game1248.table;

import java.util.Random;

import chat.dim.g1248.model.History;
import chat.dim.g1248.model.State;
import chat.dim.g1248.model.Step;
import chat.dim.threading.BackgroundThreads;

public class MainBoardFragment extends BoardFragment {

    public MainBoardFragment(int tid, int bid) {
        super(tid, bid);
    }

    public void onSwipe(Step.Direction direction) {
        System.out.println("swipe: " + direction);
        BackgroundThreads.rush(() -> doSwipe(tableId, boardId, direction));
    }

    private void doSwipe(int tid, int bid, Step.Direction direction) {
        // TODO: check current player

        History history = mViewModel.getHistory(12301);
        if (history == null) {
            return;
        }
        System.out.println("history: " + history);

        byte prefix = (byte) ((direction.value & 0x03) << 6);
        byte suffix = (byte) (randomByte() & 0x3F);
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

    private static byte randomByte() {
        Random random = new Random();
        return (byte) random.nextInt();
    }
}

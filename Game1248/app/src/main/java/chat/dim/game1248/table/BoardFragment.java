package chat.dim.game1248.table;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.g1248.model.Board;
import chat.dim.g1248.model.Stage;
import chat.dim.game1248.R;
import chat.dim.notification.Notification;
import chat.dim.notification.NotificationCenter;
import chat.dim.notification.Observer;
import chat.dim.protocol.ID;
import chat.dim.threading.BackgroundThreads;
import chat.dim.threading.MainThread;
import chat.dim.utils.Log;

public class BoardFragment extends Fragment implements Observer {

    TableViewModel mViewModel = null;
    private BoardAdapter adapter = null;

    private TextView scoreView = null;
    private GridView boardView = null;

    int tableId;
    int boardId;
    int score;
    byte[] steps;
    final List<Integer> matrix = new ArrayList<>();

    public BoardFragment(int tid, int bid) {
        super();
        tableId = tid;
        boardId = bid;
        score = 0;
        steps = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.board_fragment, container, false);

        scoreView = view.findViewById(R.id.score_text_view);
        boardView = view.findViewById(R.id.board_grid_view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TableViewModel.class);
        // TODO: Use the ViewModel

        // create adapter
        adapter = new BoardAdapter(getContext(), R.layout.griditem_squares, matrix);
        boardView.setAdapter(adapter);
        // load data in background
        BackgroundThreads.rush(this::loadBoard);
    }

    void loadBoard() {
        Board board = mViewModel.getBoard(tableId, boardId);
        assert board != null : "failed to get board: tid=" + tableId + ", bid=" + boardId;
        reloadBoard(board);
    }

    void reloadBoard(Board board) {
        // get info from the board
        Stage stage = board.getMatrix();
        matrix.clear();
        matrix.addAll(stage.toArray());
        score = board.getScore();
        //steps = history.getSteps();

        MainThread.call(this::onReload);
    }

    void onReload() {
        if (steps == null) {
            scoreView.setText("Score: " + score);
        } else {
            scoreView.setText("Score: " + score + "    (steps: " + steps.length + ")");
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.addObserver(this, NotificationNames.GameBoardUpdated);
    }

    @Override
    public void onDestroy() {
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.removeObserver(this, NotificationNames.GameBoardUpdated);
        super.onDestroy();
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

        Log.info("[GAME] refreshing tid: " + tid + ", bid: " + bid);
        reloadBoard(board);
    }
}

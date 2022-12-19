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

import java.util.ArrayList;
import java.util.List;

import chat.dim.g1248.model.Board;
import chat.dim.game1248.R;
import chat.dim.threading.BackgroundThreads;
import chat.dim.threading.MainThread;

public class BoardsFragment extends Fragment {

    private TableViewModel mViewModel = null;
    private BoardAdapter adapter = null;

    private GridView boardView = null;

    private final List<Integer> state = new ArrayList<>();

    public static BoardsFragment newInstance() {
        return new BoardsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.boards_fragment, container, false);

        boardView = view.findViewById(R.id.board_grid_view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(TableViewModel.class);
        // TODO: Use the ViewModel

        // create adapter
        adapter = new BoardAdapter(getContext(), R.layout.griditem_squares, state);
        boardView.setAdapter(adapter);
        // load data in background
        BackgroundThreads.rush(this::reloadData);
    }

    private void reloadData() {
        // TODO: set table id after entered this activity
        List<Board> boards = mViewModel.getBoards(0);
        if (boards.size() == 0) {
            return;
        }
        Board board = boards.get(0);
        List<Integer> newState = board.getState();
        assert newState != null && newState.size() == 16 : "state error: " + newState;
        state.clear();
        state.addAll(newState);

        MainThread.call(this::onReload);
    }

    private void onReload() {
        adapter.notifyDataSetChanged();
    }
}

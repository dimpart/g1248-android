package chat.dim.game1248.hall;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import chat.dim.g1248.model.Table;
import chat.dim.game1248.R;
import chat.dim.threading.MainThread;

public class TablesFragment extends Fragment {

    private HallViewModel hallViewModel = null;
    private TablesAdapter tablesAdapter = null;

    private GridView tablesView = null;

    private Table[] tables = null;

    public static TablesFragment newInstance() {
        return new TablesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);

        tablesView = view.findViewById(R.id.tables_view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        hallViewModel = ViewModelProviders.of(this).get(HallViewModel.class);

        // TODO: Use the ViewModel
        //mViewModel.checkMembers();

        // participants
        tables = getTables();
        tablesAdapter = new TablesAdapter(getContext(), R.layout.griditem_tables, tables);
        tablesView.setAdapter(tablesAdapter);
    }

    private Table[] getTables() {
        return hallViewModel.getTables(0, 20);
    }

    void reloadData() {
        tables = getTables();
        MainThread.call(new Runnable() {
            @Override
            public void run() {
                onReload();
            }
        });
    }
    void onReload() {
        tablesAdapter.notifyDataSetChanged();
    }
}

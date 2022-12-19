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

import java.util.ArrayList;
import java.util.List;

import chat.dim.g1248.model.Table;
import chat.dim.game1248.R;
import chat.dim.threading.BackgroundThreads;
import chat.dim.threading.MainThread;

public class TablesFragment extends Fragment {

    private HallViewModel mViewModel = null;
    private TablesAdapter adapter = null;

    private GridView tablesView = null;

    private final List<Table> tables = new ArrayList<>();

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
        mViewModel = ViewModelProviders.of(this).get(HallViewModel.class);

        // TODO: Use the ViewModel
        //mViewModel.checkMembers();

        // create adapter
        adapter = new TablesAdapter(getContext(), R.layout.griditem_tables, tables);
        tablesView.setAdapter(adapter);
        // load data in background
        BackgroundThreads.wait(this::reloadData);
    }

    private void reloadData() {
        List<Table> newTables = mViewModel.getTables(0, 20);
        tables.clear();
        tables.addAll(newTables);

        MainThread.call(this::onReload);
    }
    private void onReload() {
        adapter.notifyDataSetChanged();
    }
}

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
import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.g1248.model.Room;
import chat.dim.game1248.R;
import chat.dim.notification.Notification;
import chat.dim.notification.NotificationCenter;
import chat.dim.notification.Observer;
import chat.dim.threading.BackgroundThreads;
import chat.dim.threading.MainThread;

public class RoomsFragment extends Fragment implements Observer {

    private HallViewModel mViewModel = null;
    private RoomsAdapter adapter = null;

    private GridView roomsView = null;

    private final List<Room> rooms = new ArrayList<>();

    public static RoomsFragment newInstance() {
        return new RoomsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);

        roomsView = view.findViewById(R.id.rooms_view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(HallViewModel.class);

        // TODO: Use the ViewModel
        //mViewModel.checkMembers();

        // create adapter
        adapter = new RoomsAdapter(getContext(), R.layout.griditem_rooms, rooms);
        roomsView.setAdapter(adapter);
        // load data in background
        BackgroundThreads.wait(this::reloadRooms);
    }

    public void reloadRooms() {
        List<Room> newRooms = mViewModel.getRooms(0, 20);
        rooms.clear();
        rooms.addAll(newRooms);

        MainThread.call(this::onReload);
    }
    private void onReload() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.addObserver(this, NotificationNames.RoomsUpdated);
    }

    @Override
    public void onDestroy() {
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.removeObserver(this, NotificationNames.RoomsUpdated);
        super.onDestroy();
    }

    @Override
    public void onReceiveNotification(Notification notification) {
        String name = notification.name;
        Map info = notification.userInfo;
        assert name != null && info != null : "notification error: " + notification;
        if (!name.equals(NotificationNames.RoomsUpdated)) {
            // should not happen
            return;
        }
        reloadRooms();
    }
}

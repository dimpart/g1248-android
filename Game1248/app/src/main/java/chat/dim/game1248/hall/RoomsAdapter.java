package chat.dim.game1248.hall;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import java.util.List;

import chat.dim.g1248.model.Room;
import chat.dim.game1248.R;
import chat.dim.game1248.room.RoomActivity;

public class RoomsAdapter extends ArrayAdapter<Room> {

    private final int resId;

//    public RoomsAdapter(Context context, int resource) {
//        super(context, resource);
//    }
//
//    public RoomsAdapter(Context context, int resource, int textViewResourceId) {
//        super(context, resource, textViewResourceId);
//    }

    RoomsAdapter(Context context, int resource, List<Room> objects) {
        super(context, resource, objects);
        resId = resource;
    }

//    public RoomsAdapter(Context context, int resource, int textViewResourceId, Room[] objects) {
//        super(context, resource, textViewResourceId, objects);
//    }
//
//    public RoomsAdapter(Context context, int resource, List<Room> objects) {
//        super(context, resource, objects);
//    }
//
//    public RoomsAdapter(Context context, int resource, int textViewResourceId, List<Room> objects) {
//        super(context, resource, textViewResourceId, objects);
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resId, null);
            viewHolder = new ViewHolder();
            viewHolder.cardView = view.findViewById(R.id.card_view);
            viewHolder.roomImageView = view.findViewById(R.id.room_image_view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }


        Room room = getItem(position);
        viewHolder.showRoom(room);

        return view;
    }

    class ViewHolder {

        CardView cardView = null;

        ImageView roomImageView = null;

        private Room room = null;

        ViewHolder() {
        }

        @Override
        public void finalize() throws Throwable {
            super.finalize();
        }

        private void showRoom(Room room) {
            this.room = room;

            roomImageView.setOnClickListener(view -> clickRoom());
        }

        private void clickRoom() {
            int rid = room.getRid();

            Context content = getContext();
            Intent intent = new Intent();
            intent.setClass(content, RoomActivity.class);
            intent.putExtra("rid", rid);
            content.startActivity(intent);
        }
    }
}

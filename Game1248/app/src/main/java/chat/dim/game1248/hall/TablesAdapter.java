package chat.dim.game1248.hall;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.cardview.widget.CardView;

import chat.dim.g1248.model.Table;
import chat.dim.game1248.R;

public class TablesAdapter extends ArrayAdapter<Table> {

    private final int resId;

//    public TablesAdapter(Context context, int resource) {
//        super(context, resource);
//    }
//
//    public TablesAdapter(Context context, int resource, int textViewResourceId) {
//        super(context, resource, textViewResourceId);
//    }

    public TablesAdapter(Context context, int resource, Table[] objects) {
        super(context, resource, objects);
        resId = resource;
    }

//    public TablesAdapter(Context context, int resource, int textViewResourceId, Table[] objects) {
//        super(context, resource, textViewResourceId, objects);
//    }
//
//    public TablesAdapter(Context context, int resource, List<Table> objects) {
//        super(context, resource, objects);
//    }
//
//    public TablesAdapter(Context context, int resource, int textViewResourceId, List<Table> objects) {
//        super(context, resource, textViewResourceId, objects);
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resId, null);
            viewHolder = new ViewHolder();
            viewHolder.cardView = view.findViewById(R.id.cardView);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }


        Table table = getItem(position);
        viewHolder.showTable(table);

        return view;
    }

    class ViewHolder {

        CardView cardView = null;

        ViewHolder() {
        }

        @Override
        public void finalize() throws Throwable {
            super.finalize();
        }

        private void showTable(Table table) {

        }
    }
}

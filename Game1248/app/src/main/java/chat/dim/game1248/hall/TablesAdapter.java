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

import chat.dim.g1248.model.Table;
import chat.dim.game1248.R;
import chat.dim.game1248.table.TableActivity;

public class TablesAdapter extends ArrayAdapter<Table> {

    private final int resId;

//    public TablesAdapter(Context context, int resource) {
//        super(context, resource);
//    }
//
//    public TablesAdapter(Context context, int resource, int textViewResourceId) {
//        super(context, resource, textViewResourceId);
//    }

    TablesAdapter(Context context, int resource, List<Table> objects) {
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
            viewHolder.cardView = view.findViewById(R.id.card_view);
            viewHolder.tableImageView = view.findViewById(R.id.table_image_view);
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

        ImageView tableImageView = null;

        private Table table = null;

        ViewHolder() {
        }

        @Override
        public void finalize() throws Throwable {
            super.finalize();
        }

        private void showTable(Table table) {
            this.table = table;

            tableImageView.setOnClickListener(view -> clickTable());
        }

        private void clickTable() {
            int tid = table.getTid();

            Context content = getContext();
            Intent intent = new Intent();
            intent.setClass(content, TableActivity.class);
            intent.putExtra("tid", tid);
            content.startActivity(intent);
        }
    }
}

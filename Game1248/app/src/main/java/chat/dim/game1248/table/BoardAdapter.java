package chat.dim.game1248.table;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import java.util.List;

import chat.dim.game1248.R;

public class BoardAdapter extends ArrayAdapter<Integer> {

    private final int resId;

    public BoardAdapter(Context context, int resource, List<Integer> objects) {
        super(context, resource, objects);
        resId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resId, null);
            viewHolder = new ViewHolder();
            viewHolder.cardView = view.findViewById(R.id.card_view);
            viewHolder.textView = view.findViewById(R.id.text_view);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }

        int num = getItem(position);
        viewHolder.showNumber(num);

        return view;
    }

    class ViewHolder {

        CardView cardView = null;

        TextView textView = null;

        ViewHolder() {
        }

        @Override
        public void finalize() throws Throwable {
            super.finalize();
        }

        void showNumber(int num) {
            textView.setText(String.format("%d", num));
        }
    }
}

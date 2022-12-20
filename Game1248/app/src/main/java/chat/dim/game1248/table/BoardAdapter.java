package chat.dim.game1248.table;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.cardview.widget.CardView;

import java.util.List;

import chat.dim.g1248.model.Square;
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
            viewHolder.cards[0] = view.findViewById(R.id.square0);
            viewHolder.cards[1] = view.findViewById(R.id.square1);
            viewHolder.cards[2] = view.findViewById(R.id.square2);
            viewHolder.cards[3] = view.findViewById(R.id.square3);
            viewHolder.cards[4] = view.findViewById(R.id.square4);
            viewHolder.cards[5] = view.findViewById(R.id.square5);
            viewHolder.cards[6] = view.findViewById(R.id.square6);
            viewHolder.cards[7] = view.findViewById(R.id.square7);
            viewHolder.cards[8] = view.findViewById(R.id.square8);
            viewHolder.cards[9] = view.findViewById(R.id.square9);
            viewHolder.cards[10] = view.findViewById(R.id.square10);
            viewHolder.cards[11] = view.findViewById(R.id.square11);
            viewHolder.cards[12] = view.findViewById(R.id.square12);
            viewHolder.cards[13] = view.findViewById(R.id.square13);
            viewHolder.cards[14] = view.findViewById(R.id.square14);
            viewHolder.cards[15] = view.findViewById(R.id.square15);
            viewHolder.cards[16] = view.findViewById(R.id.square16);
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

        CardView[] cards = new CardView[17];

        ViewHolder() {
        }

        @Override
        public void finalize() throws Throwable {
            super.finalize();
        }

        void showNumber(int num) {
            int order = Square.getOrder(num);
            CardView cardView;
            for (int index = 0; index < cards.length; ++index) {
                cardView = cards[index];
                if (cardView == null) {
                    continue;
                }
                if (index == order) {
                    cardView.setVisibility(View.VISIBLE);
                } else {
                    cardView.setVisibility(View.GONE);
                }
            }
        }
    }
}

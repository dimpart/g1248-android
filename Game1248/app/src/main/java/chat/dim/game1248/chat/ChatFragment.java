package chat.dim.game1248.chat;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.g1248.PlayerOne;
import chat.dim.game1248.R;
import chat.dim.notification.Notification;
import chat.dim.notification.NotificationCenter;
import chat.dim.notification.Observer;
import chat.dim.protocol.Content;
import chat.dim.protocol.InstantMessage;
import chat.dim.protocol.ReliableMessage;
import chat.dim.threading.MainThread;
import chat.dim.utils.Log;

public class ChatFragment extends Fragment implements Observer {

    private ChatViewModel mViewModel = null;

    private TextView historyTextView = null;
    private EditText chatText = null;

    private int roomId;

    public ChatFragment(int rid) {
        super();
        roomId = rid;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_fragment, container, false);

        historyTextView = view.findViewById(R.id.chat_history);
        chatText = view.findViewById(R.id.chat_text);

        // FIXME: duplicate views
        chatText.setVisibility(View.VISIBLE);

        chatText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                doSend();
                return true;
            }
            return false;
        });
        chatText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                doSend();
                return true;
            }
            return false;
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        // TODO: Use the ViewModel
    }

    private void doSend() {
        String text = chatText.getText().toString();
        if (text.length() > 0) {
            PlayerOne theOne = PlayerOne.getInstance();
            theOne.sendText(text, roomId);
        }
        // clear text after sent
        chatText.setText("");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.addObserver(this, NotificationNames.MessageSent);
        nc.addObserver(this, NotificationNames.MessageReceived);
    }

    @Override
    public void onDestroy() {
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.removeObserver(this, NotificationNames.MessageSent);
        nc.removeObserver(this, NotificationNames.MessageReceived);
        super.onDestroy();
    }

    @Override
    public void onReceiveNotification(Notification notification) {
        String name = notification.name;
        Map info = notification.userInfo;
        assert name != null && info != null : "notification error: " + notification;

        ChatViewModel model = mViewModel;
        if (model == null) {
            Log.error("view model not ready yet");
            return;
        }

        Content content = (Content) info.get("content");
        if (content == null) {
            Log.error("failed to get content");
            return;
        }

        // check room id
        int rid = content.getInt("room");
        rid -= PlayerOne.BASE_ROOM_ID;
        if (rid != roomId) {
            Log.error("room id not match: " + rid + ", " + roomId);
            return;
        }

        // get message text
        String text;
        if (name.equals(NotificationNames.MessageReceived)) {
            ReliableMessage rMsg = (ReliableMessage) info.get("msg");
            text = mViewModel.getTextAfterReceived(content, rMsg);
        } else if (name.equals(NotificationNames.MessageSent)) {
            InstantMessage iMsg = (InstantMessage) info.get("msg");
            text = mViewModel.getTextAfterSent(content, iMsg);
        } else {
            // should not happen
            Log.error("notification error: " + notification);
            return;
        }

        // show message
        TextView textView = historyTextView;
        if (textView == null) {
            Log.error("history text view not ready yet: " + text);
        } else {
            MainThread.call(() -> textView.setText(text));
        }
    }
}

package chat.dim.ui;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import chat.dim.g1248.NotificationNames;
import chat.dim.game1248.R;
import chat.dim.network.SessionState;
import chat.dim.notification.Notification;
import chat.dim.notification.NotificationCenter;
import chat.dim.notification.Observer;
import chat.dim.threading.MainThread;

public abstract class TitledActivity extends AppCompatActivity implements Observer {

    private String originTitle = null;
    public String serverState = null;

    protected TitledActivity() {
        super();
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.addObserver(this, NotificationNames.ServerStateChanged);
    }

    @Override
    protected void onDestroy() {
        NotificationCenter nc = NotificationCenter.getInstance();
        nc.removeObserver(this, NotificationNames.ServerStateChanged);
        super.onDestroy();
    }

    @Override
    public void onReceiveNotification(Notification notification) {
        String name = notification.name;
        Map info = notification.userInfo;
        if (NotificationNames.ServerStateChanged.equals(name)) {
            serverState = (String) info.get("state");
            MainThread.call(this::refreshTitle);
        }
    }

    private void refreshTitle() {
        CharSequence status;
        if (serverState == null) {
            status = "...";
        } else if (serverState.equals(SessionState.DEFAULT)) {
            status = getText(R.string.server_default);
        } else if (serverState.equals(SessionState.CONNECTING)) {
            status = getText(R.string.server_connecting);
        } else if (serverState.equals(SessionState.CONNECTED)) {
            status = getText(R.string.server_connected);
        } else if (serverState.equals(SessionState.HANDSHAKING)) {
            status = getText(R.string.server_handshaking);
        } else if (serverState.equals(SessionState.ERROR)) {
            status = getText(R.string.server_error);
        } else if (serverState.equals(SessionState.RUNNING)) {
            status = null;
        } else {
            status = "?";
        }

        // change title
        if (originTitle == null) {
            originTitle = (String) getTitle();
        }
        if (status == null) {
            setTitle(originTitle);
        } else {
            setTitle(originTitle + " (" + status + ")");
        }
    }

    @Override
    public void setTitle(int titleId) {
        CharSequence title = getText(titleId);
        if (title instanceof String) {
            originTitle = (String) title;
        }
        //super.setTitle(titleId);
        refreshTitle();
    }

    protected void setOriginTitle(String title) {
        originTitle = title;
        setTitle(title);
    }
}

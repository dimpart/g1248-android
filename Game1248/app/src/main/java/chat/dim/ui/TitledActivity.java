/* license: https://mit-license.org
 * ==============================================================================
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Albert Moky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ==============================================================================
 */
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
    public SessionState sessionState = null;

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
            sessionState = (SessionState) info.get("state");
            MainThread.call(this::refreshTitle);
        }
    }

    private void refreshTitle() {
        CharSequence status;
        if (sessionState == null) {
            status = "...";
        } else if (sessionState.equals(SessionState.Order.DEFAULT)) {
            status = getText(R.string.server_default);
        } else if (sessionState.equals(SessionState.Order.CONNECTING)) {
            status = getText(R.string.server_connecting);
        } else if (sessionState.equals(SessionState.Order.CONNECTED)) {
            status = getText(R.string.server_connected);
        } else if (sessionState.equals(SessionState.Order.HANDSHAKING)) {
            status = getText(R.string.server_handshaking);
        } else if (sessionState.equals(SessionState.Order.ERROR)) {
            status = getText(R.string.server_error);
        } else if (sessionState.equals(SessionState.Order.RUNNING)) {
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

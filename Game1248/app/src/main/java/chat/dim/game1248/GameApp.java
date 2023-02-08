package chat.dim.game1248;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import chat.dim.CommonFacebook;
import chat.dim.Config;
import chat.dim.Register;
import chat.dim.Terminal;
import chat.dim.format.Base64;
import chat.dim.format.DataCoder;
import chat.dim.g1248.Client;
import chat.dim.g1248.GlobalVariable;
import chat.dim.g1248.PlayerOne;
import chat.dim.http.HTTPClient;
import chat.dim.mkm.User;
import chat.dim.protocol.ID;
import chat.dim.type.Triplet;
import chat.dim.ui.Application;
import chat.dim.utils.Log;
import chat.dim.utils.Nickname;

public final class GameApp extends Application {

    private static GameApp ourInstance = null;

    public static GameApp getInstance() {
        return ourInstance;
    }

    public GameApp() {
        super();
        ourInstance = this;
    }

    @Override
    protected void onEnterForeground(Activity activity) {
        GlobalVariable shared = GlobalVariable.getInstance();
        Terminal client = shared.terminal;
        if (client != null) {
            client.enterForeground();
        }
    }

    @Override
    protected void onEnterBackground(Activity activity) {
        GlobalVariable shared = GlobalVariable.getInstance();
        Terminal client = shared.terminal;
        if (client != null) {
            client.enterBackground();
        }
    }

    User prepareLocalUser() {
        GlobalVariable shared = GlobalVariable.getInstance();
        CommonFacebook facebook = shared.facebook;
        Config config = shared.config;
        Client client = (Client) shared.terminal;
        // check local user
        User user = facebook.getCurrentUser();
        if (user == null) {
            String nickname = Nickname.getInstance().english();
            Register register = new Register(shared.adb);
            ID uid = register.createUser(nickname, null);
            user = facebook.getUser(uid);
            facebook.setCurrentUser(user);
            List<ID> localUsers = new ArrayList<>();
            localUsers.add(uid);
            shared.adb.saveLocalUsers(localUsers);
        }
        PlayerOne theOne = PlayerOne.getInstance();
        theOne.user = user;
        // get the nearest neighbor station
        String host;
        int port;
        Triplet<String, Integer, ID> neighbor = client.getNeighborStation();
        if (neighbor != null) {
            host = neighbor.first;
            port = neighbor.second;
        } else {
            host = config.getStationHost();
            port = config.getStationPort();
        }
        // connect to the station
        Log.info("[GAME] user " + user + " login (" + host + ":" + port + ") ...");
        client.connect(host, port);
        // OK
        return user;
    }

    static {

        Log.LEVEL = Log.DEVELOP;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        path += File.separator + "chat.dim.game1248";

        HTTPClient http = HTTPClient.getInstance();
        http.setRoot(path);

        // prepare plugins
        GlobalVariable shared = GlobalVariable.getInstance();
        assert shared != null;

        // android.Base64
        Base64.coder = new DataCoder() {
            @Override
            public String encode(byte[] data) {
                return android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);
            }

            @Override
            public byte[] decode(String string) {
                return android.util.Base64.decode(string, android.util.Base64.DEFAULT);
            }
        };
    }
}

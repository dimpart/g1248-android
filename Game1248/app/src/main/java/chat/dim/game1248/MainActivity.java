package chat.dim.game1248;

import android.content.res.AssetManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.os.Environment;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import chat.dim.CommonFacebook;
import chat.dim.Config;
import chat.dim.Register;
import chat.dim.format.Base64;
import chat.dim.format.DataCoder;
import chat.dim.g1248.Client;
import chat.dim.g1248.GlobalVariable;
import chat.dim.g1248.PlayerOne;
import chat.dim.game1248.hall.RoomsFragment;
import chat.dim.http.HTTPClient;
import chat.dim.io.Permissions;
import chat.dim.mkm.User;
import chat.dim.protocol.ID;
import chat.dim.threading.BackgroundThreads;
import chat.dim.threading.MainThread;
import chat.dim.type.Triplet;
import chat.dim.utils.Log;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RoomsFragment roomsFragment = null;

    public MainActivity() {
        super();
        MainThread.prepare();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            roomsFragment = RoomsFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, roomsFragment)
                    .commitNow();
        }


        try {
            // load config file
            AssetManager am = getResources().getAssets();
            InputStreamReader isr = new InputStreamReader(am.open("config.ini"));
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            Client.prepare(sb.toString(), this);
            // launch client
            launch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void launch() {
        if (!Permissions.canWriteExternalStorage(this)) {
            Permissions.requestExternalStoragePermissions(this);
            Log.warning("Requesting external storage permissions");
            return;
        }
        BackgroundThreads.rush(() -> {
            GlobalVariable shared = GlobalVariable.getInstance();
            CommonFacebook facebook = shared.facebook;
            Config config = shared.config;
            Client client = (Client) shared.terminal;
            // check local user
            User user = facebook.getCurrentUser();
            if (user == null) {
                Register register = new Register(shared.adb);
                ID uid = register.createUser("Player ONE", null);
                user = facebook.getUser(uid);
                facebook.setCurrentUser(user);
                List<ID> localUsers = new ArrayList<>();
                localUsers.add(uid);
                shared.adb.saveLocalUsers(localUsers);
            }
            PlayerOne theOne = PlayerOne.getInstance();
            theOne.user = user;
            // show user ID
            String uid = user.getIdentifier().toString();
            MainThread.call(() -> {
                TextView textView = findViewById(R.id.nav_user_id);
                textView.setText(uid);
            });
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
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

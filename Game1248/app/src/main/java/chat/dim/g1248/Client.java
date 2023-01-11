package chat.dim.g1248;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.Set;

import chat.dim.ClientMessenger;
import chat.dim.CommonFacebook;
import chat.dim.CompatibleMessenger;
import chat.dim.Config;
import chat.dim.Processor;
import chat.dim.Terminal;
import chat.dim.cache.game.HallCache;
import chat.dim.cache.game.HistoryCache;
import chat.dim.cache.game.TableCache;
import chat.dim.database.CipherKeyDatabase;
import chat.dim.database.DocumentDatabase;
import chat.dim.database.GroupDatabase;
import chat.dim.database.MetaDatabase;
import chat.dim.database.PrivateKeyDatabase;
import chat.dim.database.UserDatabase;
import chat.dim.dbi.MessageDBI;
import chat.dim.dbi.SessionDBI;
import chat.dim.g1248.handler.HallHandler;
import chat.dim.g1248.handler.HistoryHandler;
import chat.dim.g1248.handler.TableHandler;
import chat.dim.network.ClientSession;
import chat.dim.network.SessionState;
import chat.dim.network.StateMachine;
import chat.dim.protocol.ID;
import chat.dim.sqlite.DatabaseConnector;
import chat.dim.sqlite.account.AccountDatabase;
import chat.dim.sqlite.message.MessageDatabase;
import chat.dim.type.Triplet;

public class Client extends Terminal {

    public Client(CommonFacebook barrack, SessionDBI sdb) {
        super(barrack, sdb);
    }

    @Override
    public void exitState(SessionState previous, StateMachine ctx) {
        super.exitState(previous, ctx);
        // called after state changed
        SessionState current = ctx.getCurrentState();
        if (current == null) {
            return;
        }
        if (current.equals(SessionState.RUNNING)) {
            // request tables for hall view
            PlayerOne theOne = PlayerOne.getInstance();
            theOne.sendSeeking(0, 20);
        }
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getVersionName() {
        return null;
    }

    @Override
    public String getSystemVersion() {
        return null;
    }

    @Override
    public String getSystemModel() {
        return null;
    }

    @Override
    public String getSystemDevice() {
        return null;
    }

    @Override
    public String getDeviceBrand() {
        return null;
    }

    @Override
    public String getDeviceBoard() {
        return null;
    }

    @Override
    public String getDeviceManufacturer() {
        return null;
    }

    @Override
    protected Processor createProcessor(CommonFacebook facebook, ClientMessenger messenger) {
        return new AppMessageProcessor(facebook, messenger);
    }

    @Override
    protected ClientMessenger createMessenger(ClientSession session, CommonFacebook facebook) {
        MessageDBI mdb = (MessageDBI) facebook.getDatabase();
        return new CompatibleMessenger(session, facebook, mdb);
    }

    private static SharedDatabase createDatabase(Config config, Context context) {
        String rootDir = config.getDatabaseRoot();
        String pubDir = config.getDatabasePublic();
        String priDir = config.getDatabasePrivate();

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (path.endsWith(File.separator)) {
            path = path.substring(0, path.length() - 1);
        }
        // replace "/sdcard"
        rootDir = path + rootDir.substring(7);
        pubDir = path + pubDir.substring(7);
        priDir = path + priDir.substring(7);


        String adbFile = config.getString("sqlite", "account");
        String mdbFile = config.getString("sqlite", "message");
        //String sdbFile = config.getString("sqlite", "session");
        //String gdbFile = config.getString("sqlite", "game");

        DatabaseConnector adb = new AccountDatabase(context, adbFile);
        DatabaseConnector mdb = new MessageDatabase(context, mdbFile);
        //DatabaseConnector sdb = new SessionDatabase(context, sdbFile);
        //DatabaseConnector gdb = new GameDatabase(context, gdbFile);

        SharedDatabase db = SharedDatabase.getInstance();
        db.privateKeyDatabase = new PrivateKeyDatabase(rootDir, pubDir, priDir, adb);
        db.metaDatabase = new MetaDatabase(rootDir, pubDir, priDir, adb);
        db.documentDatabase = new DocumentDatabase(rootDir, pubDir, priDir, adb);
        db.userDatabase = new UserDatabase(rootDir, pubDir, priDir, adb);
        db.groupDatabase = new GroupDatabase(rootDir, pubDir, priDir, adb);
        db.cipherKeyDatabase = new CipherKeyDatabase(rootDir, pubDir, priDir, mdb);

        db.hallDatabase = new HallCache();
        db.tableDatabase = new TableCache();
        db.historyDatabase = new HistoryCache();
        return db;
    }

    public Triplet<String, Integer, ID> getNeighborStation() {
        Set<Triplet<String, Integer, ID>> neighbors = database.allNeighbors();
        if (neighbors != null) {
            for (Triplet<String, Integer, ID> station : neighbors) {
                if (station.first != null && station.second > 0) {
                    return station;
                }
            }
        }
        return null;
    }

    public static void prepare(String iniFileContent, Context context) {
        GlobalVariable shared = GlobalVariable.getInstance();
        if (shared.terminal != null) {
            // already loaded
            return;
        }
        // Step 1: load config
        Config config = Config.load(iniFileContent);
        shared.config = config;
        ID bot = config.getANS("g1248");
        assert bot != null : "bot id not set";

        // Step 2: create database
        SharedDatabase db = createDatabase(config, context);
        shared.adb = db;
        shared.mdb = db;
        shared.sdb = db;

        // Step 3: create facebook
        CommonFacebook facebook = new CommonFacebook(db);
        shared.facebook = facebook;

        // Step 4: create terminal
        Client client = new Client(facebook, db);
        Thread thread = new Thread(client);
        thread.setDaemon(false);
        thread.start();
        shared.terminal = client;

        // Step 5: create customized content handlers
        shared.gameHallContentHandler = new HallHandler(db);
        shared.gameTableContentHandler = new TableHandler(db);
        shared.gameHistoryContentHandler = new HistoryHandler(db);

        // Step 6: prepare player one
        PlayerOne theOne = PlayerOne.getInstance();
        theOne.bot = bot;
        //theOne.user = facebook.getCurrentUser();
    }
}

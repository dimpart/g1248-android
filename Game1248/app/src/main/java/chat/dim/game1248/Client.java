package chat.dim.game1248;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import chat.dim.ClientMessenger;
import chat.dim.CommonFacebook;
import chat.dim.CompatibleMessenger;
import chat.dim.Config;
import chat.dim.Terminal;
import chat.dim.cache.game.HallCache;
import chat.dim.cache.game.HistoryCache;
import chat.dim.cache.game.TableCache;
import chat.dim.core.Processor;
import chat.dim.database.CipherKeyDatabase;
import chat.dim.database.DocumentDatabase;
import chat.dim.database.GroupDatabase;
import chat.dim.database.MetaDatabase;
import chat.dim.database.PrivateKeyDatabase;
import chat.dim.database.UserDatabase;
import chat.dim.dbi.MessageDBI;
import chat.dim.dbi.SessionDBI;
import chat.dim.filesys.ExternalStorage;
import chat.dim.g1248.AppMessageProcessor;
import chat.dim.g1248.GlobalVariable;
import chat.dim.g1248.SharedDatabase;
import chat.dim.g1248.handler.HallHandler;
import chat.dim.g1248.handler.TableHandler;
import chat.dim.network.ClientSession;
import chat.dim.sqlite.DatabaseConnector;

public class Client extends Terminal {

    public Client(CommonFacebook barrack, SessionDBI sdb) {
        super(barrack, sdb);
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

    private static SharedDatabase createDatabase(Config config) {
        String rootDir = config.getDatabaseRoot();
        String pubDir = config.getDatabasePublic();
        String priDir = config.getDatabasePrivate();

        ExternalStorage.setRoot(rootDir);

        String adbPath = config.getString("sqlite", "account");
        String mdbPath = config.getString("sqlite", "message");
        //String sdbPath = config.getString("sqlite", "session");
        //String gdbPath = config.getString("sqlite", "game");

        DatabaseConnector adb = new DatabaseConnector(adbPath);
        DatabaseConnector mdb = new DatabaseConnector(mdbPath);
        //DatabaseConnector sdb = new DatabaseConnector(sdbPath);
        //DatabaseConnector gdb = new DatabaseConnector(gdbPath);

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
    private static String getAssets(AssetManager am, String filename) throws IOException {
        InputStream is = am.open(filename);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void prepare(AssetManager am) throws IOException {
        GlobalVariable shared = GlobalVariable.getInstance();
        if (shared.config != null) {
            // already loaded
            return;
        }
        // Step 1: load config
        String ini = getAssets(am, "config.ini");
        Config config = Config.load(ini);

        // Step 2: create database
        SharedDatabase db = createDatabase(config);
        shared.adb = db;
        shared.mdb = db;
        shared.sdb = db;

        // Step 3: create facebook
        shared.facebook = new CommonFacebook(db);

        // Step 4: create customized content handlers
        shared.gameHallContentHandler = new HallHandler(db);
        shared.gameTableContentHandler = new TableHandler(db);
    }
}

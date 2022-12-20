package chat.dim.g1248.db;

import java.util.List;
import java.util.Set;

import chat.dim.crypto.DecryptKey;
import chat.dim.crypto.PrivateKey;
import chat.dim.crypto.SymmetricKey;
import chat.dim.dbi.AccountDBI;
import chat.dim.dbi.MessageDBI;
import chat.dim.dbi.SessionDBI;
import chat.dim.g1248.GlobalVariable;
import chat.dim.http.HTTPClient;
import chat.dim.protocol.Document;
import chat.dim.protocol.ID;
import chat.dim.protocol.LoginCommand;
import chat.dim.protocol.Meta;
import chat.dim.protocol.ReliableMessage;
import chat.dim.type.Pair;
import chat.dim.type.Triplet;

public enum Database implements AccountDBI, MessageDBI, SessionDBI {

    INSTANCE;

    public static Database getInstance() {
        return INSTANCE;
    }

    public HallTable hallTable;
    public TableTable tableTable;
    public HistoryTable historyTable;

    Database() {

        hallTable = new HallTable();
        tableTable = new TableTable();
        historyTable = new HistoryTable();

        GlobalVariable shared = GlobalVariable.getInstance();
        shared.adb = this;
        shared.mdb = this;
        shared.sdb = this;

        // initialize all factories & plugins
        HTTPClient.getInstance();
    }

    //
    //  AccountD BI
    //

    @Override
    public boolean saveDocument(Document doc) {
        return false;
    }

    @Override
    public Document getDocument(ID entity, String type) {
        return null;
    }

    @Override
    public ID getFounder(ID group) {
        return null;
    }

    @Override
    public ID getOwner(ID group) {
        return null;
    }

    @Override
    public List<ID> getMembers(ID group) {
        return null;
    }

    @Override
    public boolean saveMembers(List<ID> members, ID group) {
        return false;
    }

    @Override
    public List<ID> getAssistants(ID group) {
        return null;
    }

    @Override
    public boolean saveAssistants(List<ID> bots, ID group) {
        return false;
    }

    @Override
    public boolean saveMeta(Meta meta, ID entity) {
        return false;
    }

    @Override
    public Meta getMeta(ID entity) {
        return null;
    }

    @Override
    public boolean savePrivateKey(PrivateKey key, String type, ID user) {
        return false;
    }

    @Override
    public List<DecryptKey> getPrivateKeysForDecryption(ID user) {
        return null;
    }

    @Override
    public PrivateKey getPrivateKeyForSignature(ID user) {
        return null;
    }

    @Override
    public PrivateKey getPrivateKeyForVisaSignature(ID user) {
        return null;
    }

    @Override
    public List<ID> getLocalUsers() {
        return null;
    }

    @Override
    public boolean saveLocalUsers(List<ID> users) {
        return false;
    }

    @Override
    public List<ID> getContacts(ID user) {
        return null;
    }

    @Override
    public boolean saveContacts(List<ID> contacts, ID user) {
        return false;
    }

    //
    //  Message DBI
    //

    @Override
    public SymmetricKey getCipherKey(ID sender, ID receiver, boolean generate) {
        return null;
    }

    @Override
    public void cacheCipherKey(ID sender, ID receiver, SymmetricKey key) {

    }

    @Override
    public Pair<List<ReliableMessage>, Integer> getReliableMessages(ID receiver, int start, int limit) {
        return null;
    }

    @Override
    public boolean cacheReliableMessage(ID receiver, ReliableMessage msg) {
        return false;
    }

    @Override
    public boolean removeReliableMessage(ID receiver, ReliableMessage msg) {
        return false;
    }

    //
    //  Session DBI
    //

    @Override
    public Pair<LoginCommand, ReliableMessage> getLoginCommandMessage(ID identifier) {
        return null;
    }

    @Override
    public boolean saveLoginCommandMessage(ID identifier, LoginCommand cmd, ReliableMessage msg) {
        return false;
    }

    @Override
    public Set<Triplet<String, Integer, ID>> allNeighbors() {
        return null;
    }

    @Override
    public ID getNeighbor(String ip, int port) {
        return null;
    }

    @Override
    public boolean addNeighbor(String ip, int port, ID station) {
        return false;
    }

    @Override
    public boolean removeNeighbor(String ip, int port) {
        return false;
    }
}

/* license: https://mit-license.org
 *
 *  DIMP : Decentralized Instant Messaging Protocol
 *
 *                                Written in 2022 by Moky <albert.moky@gmail.com>
 *
 * ==============================================================================
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Albert Moky
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
package chat.dim.sqlite.account;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import chat.dim.sqlite.DatabaseConnector;

public class AccountDatabase extends DatabaseConnector {

    private static final int DB_VERSION = 1;

    static final String T_PRIVATE_KEY = "t_private_key";
    static final String T_META        = "t_meta";
    static final String T_DOCUMENT    = "t_document";

    static final String T_USER = "t_local_user";
    static final String T_CONTACT = "t_contact";

    public AccountDatabase(Context context, String name) {
        super(context, name, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // private key
        db.execSQL("CREATE TABLE " + T_PRIVATE_KEY + "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user VARCHAR(64)," +
                "pri_key TEXT," +
                "type CHAR(1)," +
                "sign BIT," +
                "decrypt BIT)"
        );
        db.execSQL("CREATE INDEX key_id_index ON " + T_PRIVATE_KEY + "(user)");

        // meta
        db.execSQL("CREATE TABLE " + T_META + "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "did VARCHAR(64)," +
                "type INTEGER," +
                "pub_key TEXT," +
                "seed VARCHAR(20)," +
                "fingerprint VARCHAR(88))");
        db.execSQL("CREATE INDEX meta_id_index ON " + T_META + "(did)");

        // document
        db.execSQL("CREATE TABLE " + T_DOCUMENT + "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "did VARCHAR(64)," +
                "type VARCHAR(8)," +
                "data TEXT," +
                "signature VARCHAR(88))");
        db.execSQL("CREATE INDEX doc_id_index ON " + T_DOCUMENT + "(did)");

        // local user
        db.execSQL("CREATE TABLE " + T_USER + "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user VARCHAR(64)," +
                "chosen BIT)");

        // user contacts
        db.execSQL("CREATE TABLE " + T_CONTACT + "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user VARCHAR(64)," +
                "contact VARCHAR(64)," +
                "alias VARCHAR(32))");
        db.execSQL("CREATE INDEX user_id_index ON " + T_CONTACT + "(user)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

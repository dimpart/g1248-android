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

import android.content.ContentValues;

import java.util.List;

import chat.dim.dbi.UserDBI;
import chat.dim.protocol.ID;
import chat.dim.sqlite.DataRowExtractor;
import chat.dim.sqlite.DataTableHandler;
import chat.dim.sqlite.DatabaseConnector;

public class UserTable extends DataTableHandler<ID> implements UserDBI {

    private DataRowExtractor<ID> extractor;

    public UserTable(DatabaseConnector connector) {
        super(connector);
        extractor = null;
    }

    @Override
    protected DataRowExtractor<ID> getDataRowExtractor() {
        return extractor;
    }

    private boolean prepare() {
        if (extractor == null) {
            // prepare data row extractor
            extractor = (cursor, index) -> {
                String user = cursor.getString(0);
                return ID.parse(user);
            };
        }
        return true;
    }


    @Override
    public List<ID> getLocalUsers() {
        if (!prepare()) {
            // db error
            return null;
        }
        String[] columns = {"user"};
        return select(AccountDatabase.T_USER, columns, null, null);
    }

    @Override
    public boolean saveLocalUsers(List<ID> users) {
        List<ID> localUsers = getLocalUsers();
        if (localUsers == null) {
            // db error
            return false;
        }
        int count = 0;
        // remove expelled contact(s)
        for (ID item : localUsers) {
            if (users.contains(item)) {
                continue;
            }
            if (removeLocalUser(item)) {
                ++count;
            }
        }
        // insert new contact(s)
        for (ID item : users) {
            if (localUsers.contains(item)) {
                continue;
            }
            if (addLocalUser(item)) {
                ++count;
            }
        }
        return count > 0;
    }

    public boolean addLocalUser(ID user) {
        ContentValues values = new ContentValues();
        values.put("user", user.toString());
        return insert(AccountDatabase.T_USER, null, values) > 0;
    }

    public boolean removeLocalUser(ID user) {
        String[] whereArgs = {user.toString()};
        return delete(AccountDatabase.T_USER, "user=?", whereArgs) > 0;
    }

    @Override
    public List<ID> getContacts(ID user) {
        throw new AssertionError("call ContactTable");
    }

    @Override
    public boolean saveContacts(List<ID> contacts, ID user) {
        throw new AssertionError("call ContactTable");
    }
}

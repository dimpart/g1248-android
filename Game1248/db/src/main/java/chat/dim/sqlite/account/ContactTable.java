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

public class ContactTable extends DataTableHandler<ID> implements UserDBI {

    private DataRowExtractor<ID> extractor;

    public ContactTable(DatabaseConnector connector) {
        super(connector);
        // lazy load
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
                String contact = cursor.getString(0);
                return ID.parse(contact);
            };
        }
        return true;
    }

    @Override
    public List<ID> getLocalUsers() {
        throw new AssertionError("call UserTable");
    }

    @Override
    public boolean saveLocalUsers(List<ID> users) {
        throw new AssertionError("call UserTable");
    }

    @Override
    public List<ID> getContacts(ID user) {
        if (!prepare()) {
            // db error
            return null;
        }
        String[] columns = {"contact"};
        String[] selectionArgs = {user.toString()};
        return select(AccountDatabase.T_CONTACT, columns, "user=?", selectionArgs);
    }

    @Override
    public boolean saveContacts(List<ID> newContacts, ID user) {
        List<ID> oldContacts = getContacts(user);
        if (oldContacts == null) {
            // db error
            return false;
        }
        int count = 0;
        // remove expelled contact(s)
        for (ID item : oldContacts) {
            if (newContacts.contains(item)) {
                continue;
            }
            if (removeContact(item, user)) {
                ++count;
            }
        }
        // insert new contact(s)
        for (ID item : newContacts) {
            if (oldContacts.contains(item)) {
                continue;
            }
            if (addContact(item, user)) {
                ++count;
            }
        }
        return count > 0;
    }

    public boolean addContact(ID contact, ID user) {
        ContentValues values = new ContentValues();
        values.put("user", user.toString());
        values.put("contact", contact.toString());
        return insert(AccountDatabase.T_CONTACT, null, values) > 0;
    }

    public boolean removeContact(ID contact, ID user) {
        String[] whereArgs = {user.toString(), contact.toString()};
        return delete(AccountDatabase.T_CONTACT, "user=? AND contact=?", whereArgs) > 0;
    }
}

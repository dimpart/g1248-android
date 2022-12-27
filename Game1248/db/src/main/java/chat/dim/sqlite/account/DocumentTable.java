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

import chat.dim.dbi.DocumentDBI;
import chat.dim.protocol.Document;
import chat.dim.protocol.ID;
import chat.dim.sqlite.DataRowExtractor;
import chat.dim.sqlite.DataTableHandler;
import chat.dim.sqlite.DatabaseConnector;

public final class DocumentTable extends DataTableHandler<Document> implements DocumentDBI {

    private DataRowExtractor<Document> extractor;

    public DocumentTable(DatabaseConnector connector) {
        super(connector);
        // lazy load
        extractor = null;
    }

    @Override
    protected DataRowExtractor<Document> getDataRowExtractor() {
        return extractor;
    }

    private boolean prepare() {
        if (extractor == null) {
            // prepare data row extractor
            extractor = (cursor, index) -> {
                String did = cursor.getString(0);
                String type = cursor.getString(1);
                String data = cursor.getString(2);
                String signature = cursor.getString(3);
                ID identifier = ID.parse(did);
                assert identifier != null : "did error: " + did;
                if (type == null || type.length() == 0) {
                    type = "*";
                }
                Document doc = Document.create(type, identifier, data, signature);
                if (type.equals("*")) {
                    if (identifier.isGroup()) {
                        type = Document.BULLETIN;
                    } else {
                        type = Document.VISA;
                    }
                }
                doc.put("type", type);
                return doc;
            };
        }
        return true;
    }

    @Override
    public Document getDocument(ID entity, String type) {
        if (!prepare()) {
            // db error
            return null;
        }
        String[] columns = {"did", "type", "data", "signature"};
        String[] selectionArgs = {entity.toString()};
        List<Document> results = select(AccountDatabase.T_DOCUMENT, columns, "did=?", selectionArgs,
                null, null, "id DESC", null);
        // return first result only
        return results == null || results.size() == 0 ? null : results.get(0);
    }

    // TODO: support multi documents
    @Override
    public boolean saveDocument(Document doc) {
        if (!prepare()) {
            // db error
            return false;
        }
        ID identifier = doc.getIdentifier();
        String type = doc.getType();
        String data = (String) doc.get("data");
        String signature = (String) doc.get("signature");

        Document old = getDocument(identifier, type);
        if (old == null) {
            // old record not found, insert it as new record
            ContentValues values = new ContentValues();
            values.put("did", identifier.toString());
            values.put("type", type);
            values.put("data", data);
            values.put("signature", signature);
            return insert(AccountDatabase.T_DOCUMENT, null, values) > 0;
        }
        String data2 = (String) old.get("data");
        String sig2 = (String) old.get("signature");
        assert data2 != null && sig2 != null : "old document error: " + old;
        if (data2.equals(data) && sig2.equals(signature)) {
            // same document
            return true;
        }
        // old record exists, update it
        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("data", data);
        values.put("signature", signature);

        String[] selectionArgs = {identifier.toString()};
        return update(AccountDatabase.T_DOCUMENT, values, "did=?", selectionArgs) > 0;
    }
}

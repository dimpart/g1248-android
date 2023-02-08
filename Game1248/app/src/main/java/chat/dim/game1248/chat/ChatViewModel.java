package chat.dim.game1248.chat;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import chat.dim.CommonFacebook;
import chat.dim.g1248.GlobalVariable;
import chat.dim.protocol.Address;
import chat.dim.protocol.Content;
import chat.dim.protocol.Document;
import chat.dim.protocol.Envelope;
import chat.dim.protocol.ID;
import chat.dim.protocol.InstantMessage;
import chat.dim.protocol.ReliableMessage;
import chat.dim.protocol.TextContent;
import chat.dim.protocol.Visa;
import chat.dim.type.Pair;
import chat.dim.utils.Log;

@SuppressWarnings("WeakerAccess")
public class ChatViewModel extends ViewModel {
    // TODO: Implement the ViewModel

    private static final int MAX_HISTORY_COUNT = 8;

    private final List<Pair<Envelope, Content>> messages = new ArrayList<>();

    String getTextAfterSent(Content content, InstantMessage iMsg) {
        if (content == null || iMsg == null) {
            Log.error("message error: " + content + ", " + iMsg);
        } else {
            messages.add(new Pair<>(iMsg.getEnvelope(), content));
            if (messages.size() > 3) {
                messages.remove(0);
            }
        }
        return getText();
    }
    String getTextAfterReceived(Content content, ReliableMessage rMsg) {
        if (content == null || rMsg == null) {
            Log.error("message error: " + content + ", " + rMsg);
        } else {
            messages.add(new Pair<>(rMsg.getEnvelope(), content));
            if (messages.size() > MAX_HISTORY_COUNT) {
                messages.remove(0);
            }
        }
        return getText();
    }

    private String getText() {
        StringBuilder sb = new StringBuilder();
        for (Pair<Envelope, Content> msg : messages) {
            if (msg.second instanceof TextContent) {
                String nickname = getNickname(msg.first.getSender());
                String text = ((TextContent) msg.second).getText();
                sb.append(nickname).append(": ").append(text).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private String getNickname(ID identifier) {
        // get name from visa document
        GlobalVariable shared = GlobalVariable.getInstance();
        CommonFacebook facebook = shared.facebook;
        Document doc = facebook.getDocument(identifier, "*");
        if (doc instanceof Visa) {
            Visa visa = (Visa) doc;
            String name = visa.getName();
            if (name != null && name.length() > 0) {
                return name;
            }
        }
        // build name from ID.address
        Address address = identifier.getAddress();
        return "User(" + address.toString().substring(address.length() - 4) + ")";
    }
}

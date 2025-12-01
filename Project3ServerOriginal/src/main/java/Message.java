import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    //client stuff
    public String userId;

    public String fromID;
    public String toID;

    int purpose = -1;

    String message;

    //server stuff
    boolean newIDAccepted;

    ArrayList<String> userNames = new ArrayList<String>();

    ArrayList<String> allChat = new ArrayList<String>();

    Message(int purpose, String userId) {
        this.purpose = purpose;
        this.userId = userId;
    }

    Message(int purpose, String userId, ArrayList<String> allChat) {
        this.purpose = purpose;
        this.userId = userId;
        this.allChat = allChat;
    }

    Message(int purpose, ArrayList<String> userNames){
        this.purpose = purpose;
        this.userNames = userNames;
    }

    Message(int purpose, ArrayList<String> userNames, ArrayList<String> allChat){
        this.purpose = purpose;
        this.userNames = userNames;
        this.allChat = allChat;
    }

    Message(int purpose, String fromId, String toID, String message) {
        this.purpose = purpose;
        this.message = message;
        this.fromID = fromId;
        this.toID = toID;
    }

    //1 = new user ID
    //2 = send a message
    //3 = user does not exist
    //4 = add to user list
    //5 = remove from user list


}

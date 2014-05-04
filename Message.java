import java.io.Serializable;

public class Message implements Serializable {
	
	private static final long serialVersionUID = 1L;
	String messageId;
	int timeStamp;
	//Node sender;
	//ArrayList<Node> receivers;
	int senderNode;
	int receiverNode;
	boolean delivered;
	int counter;
	public enum messageType {Reply, New , Final};
	String type;
	String messageText;
}


import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;



/**
 * @author Rashmi Lengade
 */
public class Node {
	static int  NodeId;
	static String hostName;
	static int portNumber;
	static SctpServerChannel serverSocket;
	static LamportClock clock = new LamportClock();
	static HashMap<Integer,String> configuration = new HashMap<Integer, String>();
	static ConcurrentHashMap<Integer,SctpChannel> connectionDetails = new ConcurrentHashMap<Integer, SctpChannel>();
	static Queue<Message> msgQueue = new LinkedBlockingDeque<Message>();
	static Comparator<Message> comparator = new Node.queueComparator();
	static PriorityQueue<Message> priorityQueue = new PriorityQueue<Message>(20000,comparator); 
	static int[] numberOfMessages = new int[20];
	static int sendCount;
	static int recieveCount;
	static int recievedMessages;
	public int getNodeId() {
		return NodeId;
	}
	public void setNodeId(int nodeId) {
		NodeId = nodeId;
	}
	static class queueComparator implements Comparator<Message>  {

		@Override
		public int compare(Message o1, Message o2) {
			int i= o1.timeStamp - o2.timeStamp;
			if(i==0)
			{
				return o1.messageId.compareTo(o2.messageId);
			}
			return i;
		}

	}
}
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;


public class Client implements Runnable{

	public int counter=0;
	HashMap<Integer, SctpChannel> connectionDetails;

	public Client(HashMap<Integer, SctpChannel> con) {
		this.connectionDetails = con;
	}
	@Override
	public void run() {

		while(true)
		{
			while(!(Node.msgQueue.isEmpty()))
			{
				Message m=null;
				synchronized (Node.msgQueue) {
					m = Node.msgQueue.remove();
				}
				if(m==null)
				{
					continue;
				}
					if(m.type.equals(Message.messageType.New.toString()))
					{
						counter = counter + Node.numberOfMessages[Node.NodeId];
						Node.clock.increment();
						m.timeStamp = Node.clock.Time;
						m.counter ++;
						m.delivered =false;
						synchronized (Node.priorityQueue) {
							Node.priorityQueue.add(m);
						}
						String msg = m.messageText+" "+m.timeStamp + " from ID " +m.messageId +"\n";
						try (Writer sentWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/004/r/rx/rxl122130/AOSInput/Node"+Node.NodeId+".txt",true), "utf-8"))) {
							sentWriter.write(msg);
						} catch (IOException e) {
							e.printStackTrace();
						}
						sendAll(Node.connectionDetails,m);
					}
					else if(m.type.equals(Message.messageType.Reply.toString()))
					{
						counter++;
						send(Node.connectionDetails, m);
					}
					else if(m.type.equals(Message.messageType.Final.toString()))
					{
						counter = counter + Node.numberOfMessages[Node.NodeId];
						sendAll(Node.connectionDetails,m);
					}
				}
			}
			/*if(counter==Node.sendCount)
			{
				
			}*/
		}	
	

	public void send (ConcurrentHashMap<Integer, SctpChannel> connectionDetails, Message message)
	{
		for(Entry<Integer , SctpChannel> entry : connectionDetails.entrySet())
		{
			if(entry.getKey().equals(message.senderNode))
			{
				try {
					message.senderNode = Node.NodeId;
					message.timeStamp = Node.clock.increment();
					sendMessage(entry.getValue(),message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public  void sendAll(ConcurrentHashMap<Integer, SctpChannel> connectionDetails, Message message) {

		for(Entry<Integer , SctpChannel> entry : connectionDetails.entrySet())
		{
			try {
				sendMessage(entry.getValue(),message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	private static  void sendMessage(SctpChannel clientSock, Message message) throws IOException
	{
		// prepare byte buffer to send massage
		ByteBuffer sendBuffer = ByteBuffer.allocate(60000);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] yourBytes;
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(message);
			yourBytes = bos.toByteArray();
		} finally {
			out.close();
			bos.close();
		}

		sendBuffer.clear();
		//Reset a pointer to point to the start of buffer 
		sendBuffer.put(yourBytes);
		sendBuffer.flip();
		try {
			//Send a message in the channel 
			MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
			clientSock.send(sendBuffer, messageInfo);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

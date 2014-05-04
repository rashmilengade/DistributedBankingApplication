import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map.Entry;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;


public class Server implements Runnable {
	public int count =0;
	SctpServerChannel serverSocket;
	public Server(SctpServerChannel serverSock) {
		this.serverSocket = serverSock;
	}
	@Override
	public void run() {
		ByteBuffer byteBuffer;
		byteBuffer = ByteBuffer.allocate(60000);
		String hostname = null;
		// Accept Connections from all other nodes
		SctpChannel[] clientSockets = new SctpChannel[Node.configuration.size()];
		for(int i=1; i<Node.configuration.size() - Node.NodeId; i++)
		{
			try {
				clientSockets[i] = serverSocket.accept();
				clientSockets[i].configureBlocking(false);
				Iterator<SocketAddress> it = clientSockets[i].getRemoteAddresses().iterator();
				boolean flag = false;
				while(it.hasNext())
				{
					InetSocketAddress sc = (InetSocketAddress) it.next();
					String hostName = sc.getHostName().toString();
					for(Entry<Integer,String> entry : Node.configuration.entrySet())
					{
						String value = entry.getValue();
						String[] values = value.split(" ");
						if(hostName.equals(values[0]))
						{
							Node.connectionDetails.put(entry.getKey(),clientSockets[i]);
							flag = true;
							break;
						}
					}
					if(flag)
					{
						break;
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		//Accept messages from all the nodes and process it
		boolean flag = true;
		while(flag)
		{	
			for(Entry<Integer,SctpChannel> entry : Node.connectionDetails.entrySet())
			{
				try {
					byteBuffer.clear();
					MessageInfo msgInfo = entry.getValue().receive(byteBuffer,null,null);
					byteBuffer.flip();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(byteBuffer.remaining() >0)
				{

					byte[] yourBytes = byteBuffer.array();
					ByteArrayInputStream bis = new ByteArrayInputStream(yourBytes);
					ObjectInput in = null;
					try {
						in = new ObjectInputStream(bis);
						Message messageInfo =(Message) in.readObject(); 
						/*					if(messageInfo !=null)
						{
						 */						sendMessage(messageInfo);
						 //					}

					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} finally {
						try {
							bis.close();
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			/*if(count==15)
			{
				//printQueue();
				flag = false;
			}*/
		}
	}

	public void sendMessage(Message message)
	{
		if(message.type.equals(Message.messageType.New.toString()))
		{
			synchronized (Node.priorityQueue) {
				Node.priorityQueue.add(message);
			}
			Node.clock.increment();
			/*System.out.println(message.messageText + "of the type "+ message.type + "from node : "+ message.senderNode +" at TimeStamp " + message.timeStamp);*/
			message.timeStamp = Node.clock.compareAndSetTime(message.timeStamp);
			message.type = Message.messageType.Reply.toString();
			synchronized (Node.msgQueue) {
				Node.msgQueue.add(message);
			}			
		}
		else if(message.type.equals(Message.messageType.Reply.toString()))
		{
			/*System.out.println(message.messageText + "of the type "+ message.type + "with id : "+ message.messageId +" at TimeStamp" + message.timeStamp);*/
			int time = Node.clock.compareAndSetTime(message.timeStamp);
			
			synchronized (Node.priorityQueue) {
				Iterator<Message> it = Node.priorityQueue.iterator();
				while(it.hasNext())
				{
					Message m = it.next();
					if(message.messageId.equals(m.messageId))
					{
						
						/*synchronized (Node.priorityQueue) {*/
							/*Node.priorityQueue.remove(m);}*/
						Node.priorityQueue.remove(m);
						m.counter ++;
						if(message.timeStamp > m.timeStamp)
						{
							/*Node.priorityQueue.remove(m);*/
							
							m.timeStamp = message.timeStamp;
							/*Node.priorityQueue.add(m);*/
						}
						
						
						if(m.counter == Node.configuration.size())
						{
							m.type = Message.messageType.Final.toString();
							synchronized (Node.msgQueue) {
								Node.msgQueue.add(m);}
								m.delivered = true;
								/*if((Node.priorityQueue.peek() != null)&& (Node.priorityQueue.peek().delivered))
								{
									Node.priorityQueue.notify();
								}*/
								
						}
						Node.priorityQueue.add(m);
						if((Node.priorityQueue.peek() != null)&& (Node.priorityQueue.peek().delivered))
						{
							Node.priorityQueue.notify();
						}
						/*synchronized (Node.priorityQueue) {*/ 
							/*Node.priorityQueue.add(m);*//*}*/
						break;
					}
				}}
		}
		else if(message.type.equals(Message.messageType.Final.toString()))
		{ 
			
			synchronized (Node.priorityQueue) {
				/*if(!Node.priorityQueue.contains(message))
				{
					Node.priorityQueue.add(message);
				}*/
				Iterator<Message> it =Node.priorityQueue.iterator();
				while(it.hasNext())
				{
					Message m = it.next();
					if(m.messageId.equals(message.messageId))
					{
						Node.priorityQueue.remove(m);
						message.delivered = true;
						Node.priorityQueue.add(message);
						if(Node.priorityQueue.peek().delivered)
						{
							Node.priorityQueue.notify();
						}
						break;
					}
					
				}
			}
			
			/*System.out.println("Final Message from " + message.messageId + "is with timestamp " + message.timeStamp );*/
		}
		count++;
	}
	public void printQueue()
	{
		System.out.println("Prority Queue Enteries------------------");
		synchronized (Node.priorityQueue) {
		while(!(Node.priorityQueue.isEmpty())){
			Message m = Node.priorityQueue.poll();
			System.out.println("Message in Queue is : " +m.messageId + " with timestamp " +m.timeStamp + " count is" + m.counter);
		}}
	}
}

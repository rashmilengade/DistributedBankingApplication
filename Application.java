import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Random;

public class Application implements Runnable {
public int cnt=0,n=0,sum=0,amount=1000;
public String[] messageText = {"Withdraw 1000","Deposit 500", "Interest 5%"}; 
public Random r = new Random();

	@Override
	public void run() {
		for(int j=0; j<Node.configuration.size(); j++)
		{
			if(j== Node.NodeId)
				n= Node.numberOfMessages[j];
			sum=sum+Node.numberOfMessages[j];
		}
		synchronized (Node.msgQueue) {
			
			for(int i=0; i<n; i++)
			{
				Message message = new Message();
				message.senderNode = Node.NodeId;
				String msg = "HELLO ";
				message.type = Message.messageType.New.toString();
				message.messageText = messageText[r.nextInt(2)];
				message.delivered = false;
				message.messageId = i + "--" + Node.NodeId;
				Node.msgQueue.add(message);
			}
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true)
		{
			//Logic to deliver a message
			synchronized (Node.priorityQueue) {

				if(!(Node.priorityQueue.isEmpty()) &&!(Node.priorityQueue.peek().delivered))
				{
					try {
						Node.priorityQueue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(!(Node.priorityQueue.isEmpty()) &&!(Node.priorityQueue.peek().delivered))
				{
					continue;
				}
				while(!(Node.priorityQueue.isEmpty()) && (Node.priorityQueue.peek().delivered))
				{
					Node.recievedMessages++;
					cnt++;
					Message m=  Node.priorityQueue.poll();
					processMessage(m);
					System.out.println("-----------Delivered Message "+m.messageId +" with TimeStamp : " + m.timeStamp);
					String msg = m.messageText+" "+m.timeStamp + " from ID " +m.messageId+"\n";
					try (Writer deliveredWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/004/r/rx/rxl122130/AOSOutput/Node"+Node.NodeId+".txt",true), "utf-8"))) {
						deliveredWriter.write(msg);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if(Node.recievedMessages == sum)
			{
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				HashMap<String, Integer> sentMessages = new HashMap<String, Integer>();
				try {
					sentMessages = SentMessagesMap();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(CompareFile())
				{
					System.out.println("Comparison of all delivered files result is true ");
					System.out.println("Comparision of input output result : "+CompareInputOutput(sentMessages)); 
					System.out.println("Final amount is : " + amount);
				}
				break;
			}
		}
	}
	
	private void processMessage(Message m) {
		String value = m.messageText;
		String[] values = value.split(" ");
		if(values[0].equals("Withdraw"))
		{
			amount = amount + Integer.parseInt(values[1]);
		}
		else if(values[0].equals("Deposit"))
		{
			amount = amount - Integer.parseInt(values[1]);
		}
		else if(values[0].equals("Interest"))
		{
			amount = (int)(amount + (amount*0.05));
		}
	}

	public HashMap<String, Integer> SentMessagesMap() throws IOException {
		
		HashMap<String, Integer> sentMessages = new HashMap<String, Integer>();
		
		File fileset = new File("/home/004/r/rx/rxl122130/AOSInput");
		File[] files = fileset.listFiles();
		for(int i=0; i<files.length; i++)
		{
			FileReader fr;
			BufferedReader br=null;
			try {
				fr = new FileReader(files[i]);
				br = new BufferedReader(fr);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 
			String value;
			while((value =br.readLine()) != null)
			{
				
				String[] values = value.split(" ");
				String key = values[values.length-1];
				sentMessages.put(key, 1);
			}
		}
		return sentMessages;
	}
	
public boolean CompareInputOutput(HashMap<String, Integer> sentMessages) {

	FileReader fr;
	BufferedReader br=null;
	try {
		
		fr = new FileReader("/home/004/r/rx/rxl122130/AOSOutput/Node"+Node.NodeId+".txt");
		br = new BufferedReader(fr);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} 
	try {
		String value;
		while((value=br.readLine()) != null)
		{
			String[] values = value.split(" ");
			String key = values[values.length -1];
			boolean keyResult = sentMessages.containsKey(key);
			if(keyResult == false)
			{
				return false;
			}
		}
		
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	try {
		br.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return true;
}
	
	public static boolean CompareFile() {
		int j=0;
		System.out.println("in compare file");
		File fileset = new File("/home/004/r/rx/rxl122130/AOSOutput");
		File[] files = fileset.listFiles();
		for(int i=0; i<files.length; i++)
		{
			if(files[i].isFile())
			{
				System.out.println("Comparing file "+i);
				for(j=i+1; j<files.length; j++)
				{
					if(files[j].isFile())
					{
						try {
							System.out.println("Comparing files "+i+" with "+j);
							FileReader fr = new FileReader(files[i]); 
							FileReader fr1 = new FileReader(files[j]);

							BufferedReader br = new BufferedReader(fr);
							BufferedReader br2 = new BufferedReader(fr1);

							String s1;
							String s2 = null;

							while(((s1 = br.readLine()) != null) && ((s2 = br2.readLine()) != null)) { 
								//System.out.println("compare result false");
								/*System.out.println("Line s1: "+s1);
		    					System.out.println("Line s2: "+s2);*/
								if(s1.compareTo(s2) !=0)	
								{
									System.out.println("Comparision result is false");
									br.close();
									br2.close();
									return false;
								}
							}
							if((s1 = br.readLine()) != null){
								br.close();
								br2.close();
								return false;
							}
							if((s2 = br2.readLine()) != null){
								br.close();
								br2.close();
								return false;
							}
							br.close();
							br2.close();


						}catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return true;
	}
}

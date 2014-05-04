import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;


public class NodeStart {

	public static void main(String[] args) throws IOException {
		Node node = new Node();
		HashMap<Integer, String> configuration = new HashMap<Integer,String>();
		HashMap<Integer, SctpChannel> connectionDetails = new HashMap<Integer,SctpChannel>();

		String fileName = null;
		if (0 < args.length) {
		    fileName = args[0];
		  }
		else
		{
			System.out.println("Invalid File Name.. !!! Please run again");
		}
		configuration = MakeConfiguration(node,fileName);
		node.configuration = configuration;
		int sum=0;
		for(int i=0;i<Node.configuration.size(); i++){
			if(i != Node.NodeId)
				sum = sum + Node.numberOfMessages[i];
		}
		Node.sendCount = (Node.numberOfMessages[Node.NodeId] *2)+sum;
		Node.recieveCount = (sum*2) + (Node.numberOfMessages[Node.NodeId] * (Node.configuration.size() -1));
		/*System.out.println("Send Count : " + Node.sendCount);
		System.out.println("Receive Count : " + Node.recieveCount);*/
		SctpServerChannel serverSocket;
		serverSocket = SctpServerChannel.open();
		InetSocketAddress serverAddress = new InetSocketAddress(node.portNumber);
		serverSocket.bind(serverAddress);
		Thread serverThread = new Thread(new Server(serverSocket));
		serverThread.start();
		System.out.println("Bound port : " + node.portNumber);
		/*System.out.println("Waiting for connection ...");*/

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ConnectAll(configuration);

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Thread clientThread = new Thread(new Client(connectionDetails));
		clientThread.start();
		
		Thread application = new Thread(new Application());
		application.start();
		
		/*
		if(Node.recievedMessages == (sum+Node.numberOfMessages[Node.NodeId]))
		{
			System.out.println("Comparison result : "+CompareFile());
		}*/
		
	}

	/*public static boolean CompareFile() {

		System.out.println("in compare file");
		File fileset = new File("");
		File[] files = fileset.listFiles();
		for(int i=0; i<files.length; i++)
		{
			if(files[i].isFile())
			{

				for(int j=i+1; j<files.length; j++)
				{
					if(files[j].isFile())
					{
						try {

							FileReader fr = new FileReader(files[i]); 
							FileReader fr1 = new FileReader(files[j]);

							BufferedReader br = new BufferedReader(fr);
							BufferedReader br2 = new BufferedReader(fr1);

							String s1;
							String s2 = null;

							while(((s1 = br.readLine()) != null) && ((s2 = br2.readLine()) != null)) { 
								System.out.println("Line s1: "+s1);
		    					System.out.println("Line s2: "+s2);
								if(s1.compareTo(s2) !=0)	
								{
									br.close();
									br2.close();
									return false;
								}
							}
							if(br.ready()){
								br.close();
								br2.close();
								return false;
							}
							if(br2.ready()){
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

*/	public static void ConnectAll(HashMap<Integer, String> configuration) throws IOException {

		for(int i=0; i<Node.NodeId; i++)
		{
			String value = configuration.get(i);
			String[] values = value.split(" ");
			String hostName = values[0];
			int portNumber = Integer.parseInt(values[1]);

			SctpChannel clientSocket;
			InetSocketAddress serverAddr = new InetSocketAddress(hostName,portNumber); 
			clientSocket = SctpChannel.open();
			clientSocket.connect(serverAddr, 0, 0);
			clientSocket.configureBlocking(false);
			Node.connectionDetails.put(i, clientSocket);
		}
	}


	public static HashMap<Integer, String> MakeConfiguration(Node node,String fileName) throws NumberFormatException, IOException {

		HashMap<Integer, String> configuration = new HashMap<Integer,String>();
		String myHostName;
		int i=0;
		myHostName = java.net.InetAddress.getLocalHost().getHostName();
		FileReader file = new FileReader(fileName);
		BufferedReader br = new BufferedReader(file);
		String line;
		while((line = br.readLine()) != null)
		{
			String[] token = line.split(" ");
			int key = Integer.parseInt(token[0]);
			String hostName = token[1];
			String portNumber = token[2];
			Node.numberOfMessages[i] = Integer.parseInt(token[3]);
			i++;
			if(myHostName.equals(hostName))
			{
				node.hostName = hostName;
				node.portNumber = Integer.parseInt(portNumber);
				node.setNodeId(key);

			}
			configuration.put(key, hostName + " " + portNumber);
		}
		System.out.println("My Node ID " + node.getNodeId());
		br.close();
		file.close();
		return configuration;
	}
}

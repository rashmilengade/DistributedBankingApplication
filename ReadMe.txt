Rashmi lengade 

rxl122130@utdallas.edu

The src folder contains all the source files given below:

1. Node.java : This file contains all the details that a node requires to keep it with itself.(Like NodeID, SenderNode details, 
ConnectionDetails i.e. the pool of connections, Configuration details).
2. Message.java : This file contains the details that a message needs (Like the message ID, Timestamp, etc).
3. LamportClock : This file contains the Lamport's clock implementation.
4. NodeStart.java : This has the main method from where the program starts. First all the configurations are made and the then server 
thread is started. This the connection sockets are made to the nodes below it. and the client thread is started. Once connections are 
made the application thread is started. 
5. Server.java : This file contains details of server thread. This accepts all the connections and updates the connection pool. 
This is then ready to accept messages from other nodes.
6. Client.java : This file contains details of client thread. This sends the messages one by one taking it from the queue that is 
shared between client and server.
7.Application.java : This file sends broadcast message and then delivers it as and when the messages is ready to be delivered in the 
priority queue. 

To Compile the program do 

javac *.java

To execute the program 

java NodeStart.java Configuration.txt

(I have attached the configuration file that take the domain name and port number and number of messages to be broadcasted by each node)

Testing :
To ensure that  the program works corrects I have done the file comparison between the files at each node. and also the whether the same sent messages
are received by the receiver. Also the same number of messages sent are received.
This code is tested for test cases.








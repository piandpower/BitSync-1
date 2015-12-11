import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	public static String devId;
	public static String[] availableChunks, chunkOwnedArray;
	String intTo, connTo;
	public static String filename;		// ex. 1800.mp3
	Object[] objectMessage;
	public static int sPort;    //This as a server will be listening on this port number
	int cPort;    //Port through which messages are exchanged
	public static int[] sPortArray = {18121,18122,18123,18124,18125};
	int[] cPortArray = {18221,18222,18223,18224,18225};
	String[] folderNameArray = {"Client2","Client3","Client4","Client5","Client6"};
	public static String folderName;
	final static int noDev=5;
	static long fileSize;
	public static String sleep="0";
	public static String noChunks="0";

	//************Peer-to-peer*******//
	ServerSocket serverSocket;
	Socket sSocket;
	public static String[] rowSocket, row;

	Functions f;

	public Client() {}

	public void setIdentity(String devId){
		sPort = sPortArray[Integer.parseInt(devId)-2];
		cPort = cPortArray[Integer.parseInt(devId)-2];
		folderName = folderNameArray[Integer.parseInt(devId)-2];
	}

	void run()
	{
		String chunkId;
		try{
			//create a socket to connect to the server
			rowSocket=f.configReader("1");
			requestSocket = new Socket("localhost", Integer.parseInt(rowSocket[3]));
			System.out.println("C:  Connected to Server on port "+rowSocket[3]+".");
			//initialize inputStream and outputStream

			out = new ObjectOutputStream(requestSocket.getOutputStream());

			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			f.receiveFile(devId, requestSocket, in, out, -1);

			
		}
		catch (ConnectException e) {
			System.err.println("C:  Initiate a server first.");
			System.exit(1);
		} 
		catch(UnknownHostException unknownHost){
			System.err.println("C:  You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				if(in!=null) in.close();
				if(out!=null) out.close();
				if(requestSocket!=null) {
					requestSocket.close();
				}
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}

	}
	
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
			System.out.println("C:  Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}




	
	// Main method
	public static void main(String args[])
	{	
		Client client = new Client();
		client.f = new Functions();
		
		if(args.length==1||args.length==2){
			client.devId = args[0];
			if(args.length==2)sleep = args[1];
			client.setIdentity(client.devId);
			client.row=client.f.configReader(devId);
			Client.sPort=Integer.valueOf(row[3]);
			chunkOwnedArray=client.f.chunkOwned(client.folderName);
			client.run();
		}
		else{
			System.out.println("C:  Please put appropriate arguments.");
			System.exit(1);
		}

		System.out.println("C:  Client "+client.devId+" entered peer-to-peer sharing mode.");



		// As a client, downloads from
		new Thread(new PeerClient()).start();


		// As a server, uploads to
		new Thread(new PeerServer()).start();



	}
}
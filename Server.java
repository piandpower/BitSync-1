import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {
	
	ServerSocket sSocket;   //serversocket used to listen on port number 8000
	Socket connection = null; //socket for the connection with the client
	ObjectOutputStream out;  //stream write to the socket
	ObjectInputStream in;    //stream read from the socket
	public static String filePath;
	public static String filename;
	public static String[] chunkOwnedArray;
	public static String devId="1";
	Functions f;
	public static String[] row;
	public static String noChunks = "0"; // Total number of chunks

	public Server() {}



	public static void main(String args[]) throws IOException {
		Functions f = new Functions();
		try{
		//Read config
		row=f.configReader(devId);
		ServerSocket sSocket = new ServerSocket(Integer.parseInt(row[3]), 10);

		// Arguments Handling
		if (args.length != 1)		{
			System.out.println("S:  Must specify a file-path argument.");
		}
		else{
			String currentDir=System.getProperty("user.dir");
			filePath=currentDir+"/srcFile/"+args[0];
			filename=args[0];
		}
		
		
		
		
		// Get list of chunks owned by server
		chunkOwnedArray=f.chunkOwned("-1");
		
		// Call FileSplitter
				FileSplitter fs = new FileSplitter();
				fs.split(filePath);
				
		System.out.println("S:  Waiting for connection(s).");
		while(true){
			Socket connection = sSocket.accept();
			System.out.println("S:  Connection received from " + connection.getInetAddress().getHostName());

			new Thread(new MultiThreadServer(connection)).start();
		}
		}catch(java.net.BindException be){
			System.out.println("S:  There's already a server running on the same port.");
			System.exit(1);
		}
	}

}

class MultiThreadServer implements Runnable {
	String message;    //message received from the client
	String MESSAGE;    //uppercase message send to the client
	ObjectOutputStream out;  //stream write to the socket
	ObjectInputStream in;    //stream read from the socket
	Socket csocket;
	String devId, filePath, filename, intTo, connTo;
	String chunkId;
	String[] chunkOwnedClientArray, chunkOwnedArray;
	List chunkOwnedClientList;
	final static int noDev=5;
	Functions f;

	MultiThreadServer(Socket csocket) {
		this.csocket = csocket;
		this.devId = Server.devId;
		this.filePath = Server.filePath;
		this.filename = Server.filename;
		this.chunkOwnedArray = Server.chunkOwnedArray;
	}
	@Override
	public void run() {
		try{
			Functions f = new Functions();
			out = new ObjectOutputStream(csocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(csocket.getInputStream());


			//Look for chunks
			String currentDir=System.getProperty("user.dir");
			File folder = new File(currentDir+"/srcFile");
			File[] listOfFiles = folder.listFiles();
			int fileCount=0;
			OutputStream os;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()&&listOfFiles[i].getName().toLowerCase().contains("chunk")) {  // 0 1 10 11 12 13 14 2 20 21 22 23 3 4 5 ....
					fileCount++;
					String chunkName=listOfFiles[i].getName().toString();
					chunkId=chunkName.substring(chunkName.lastIndexOf('.') + 6);
					File file = new File(currentDir+"/srcFile/"+chunkName);
					long fileSize=file.length();
					
					Object[] rPayload=f.xPayloadServer(chunkId, fileSize, devId, chunkOwnedArray, filename, in, out, Server.noChunks);
					
					chunkOwnedClientArray=(String[]) rPayload[1];					// Array of Chunks owned by Client
					chunkOwnedClientList = Arrays.asList(chunkOwnedClientArray);
					connTo=(String) rPayload[0];
					
					if((connTo.equals("2")&& Integer.parseInt(chunkId)%noDev==0) ||
							(connTo.equals("3")&& (Integer.parseInt(chunkId)-1)%noDev==0) ||
							(connTo.equals("4")&& (Integer.parseInt(chunkId)-2)%noDev==0) ||
							(connTo.equals("5")&& (Integer.parseInt(chunkId)-3)%noDev==0) ||
							(connTo.equals("6")&& (Integer.parseInt(chunkId)-4)%noDev==0)
							){
						f.sendFile(chunkName, csocket,-1);
					}
					

				}
			}
			Server.noChunks=String.valueOf(fileCount);

			f.xPayloadServer("-1", (long)0, devId, chunkOwnedArray, filename, in, out, Server.noChunks);
			System.out.println("\nS:  All chunks sent to "+connTo+".");

		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				csocket.close();
				System.out.println("S:  Client "+connTo+" disconnected."+" Thread closed.");
			}
			catch(IOException ioException){
				System.out.println("S:  Client "+connTo+" disconnected.");
			}
		}

	}

	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("S:  Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}
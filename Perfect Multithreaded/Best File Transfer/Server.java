import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {
	static int sPort = 12161;    //The server will be listening on this port number
	ServerSocket sSocket;   //serversocket used to listen on port number 8000
	Socket connection = null; //socket for the connection with the client
	String message;    //message received from the client
	String MESSAGE;    //uppercase message send to the client
	ObjectOutputStream out;  //stream write to the socket
	ObjectInputStream in;    //stream read from the socket
	public static String filePath;
	public static String filename;
	public static String[] chunkOwnedArray;
	public static String devId="1";

	public void Server() {}



	public static void main(String args[]) throws IOException {
		ServerSocket sSocket = new ServerSocket(sPort, 10);

		// Arguments Handling
		if (args.length != 1)		{
			System.out.println("Must specify a file-path argument.");
		}
		else{
			String currentDir=System.getProperty("user.dir");
			filePath=currentDir+"/src/srcFile/"+args[0];
			filename=args[0];
		}
		
		// Get list of chunks owned
		chunkOwned();
		
		// Call FileSplitter
				FileSplitter fs = new FileSplitter();
				fs.split(filePath);

		System.out.println("Waiting for connection");
		while(true){
			Socket connection = sSocket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());

			new Thread(new MultiThreadServer(connection)).start();
		}
	}

	// List chunks in possession
	public static void chunkOwned(){
		//Look for chunks
		String currentDir=System.getProperty("user.dir");
		File folder = new File(currentDir);
		File[] listOfFiles = folder.listFiles();
		int fileCount=0;


		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()&&listOfFiles[i].getName().toLowerCase().contains("chunk")) {
				fileCount=fileCount+1;

			}
		}
		String[] chunkOwnedArray1= new String[fileCount];
		fileCount=0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()&&listOfFiles[i].getName().toLowerCase().contains("chunk")) {
				String chunkName=listOfFiles[i].getName().toString();
				String chunkIdOwned=chunkName.substring(chunkName.lastIndexOf('.') + 6);
				chunkOwnedArray1[fileCount]=chunkIdOwned;
				fileCount++;
			}
		}
		chunkOwnedArray=chunkOwnedArray1;
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
			out = new ObjectOutputStream(csocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(csocket.getInputStream());


			//Look for chunks
			String currentDir=System.getProperty("user.dir");
			File folder = new File(currentDir+"/src/srcFile");
			File[] listOfFiles = folder.listFiles();
			int fileCount=0;
			OutputStream os;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()&&listOfFiles[i].getName().toLowerCase().contains("chunk")) {  // 0 1 10 11 12 13 14 2 20 21 22 23 3 4 5 ....
					fileCount++;
					String chunkName=listOfFiles[i].getName().toString();
					chunkId=chunkName.substring(chunkName.lastIndexOf('.') + 6);
					
					xPayload(chunkId);
					if((connTo.equals("2")&& Integer.parseInt(chunkId)%noDev==0) ||
							(connTo.equals("3")&& (Integer.parseInt(chunkId)-1)%noDev==0) ||
							(connTo.equals("4")&& (Integer.parseInt(chunkId)-2)%noDev==0) ||
							(connTo.equals("5")&& (Integer.parseInt(chunkId)-3)%noDev==0) ||
							(connTo.equals("6")&& (Integer.parseInt(chunkId)-4)%noDev==0)
							){
						
						System.out.println(chunkName);
						sendFile(chunkName);
					}
					

				}
			}

			xPayload("-1");
			System.out.println("All chunks sent.");

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
				System.out.println("Thread closed.");
			}
			catch(IOException ioException){
				System.out.println("Client "+devId+" disconnected.");
			}
		}

	}

	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}

	// Exchange chunkId, devId, and ownedChunkArray
	void xPayload(String chunkId){
		Socket conn = null;			
		try{

			// Get message
			Object[] rPayload = (Object[]) in.readObject();
			chunkOwnedClientArray=(String[]) rPayload[1];
			connTo=(String) rPayload[0];
			chunkOwnedClientList = Arrays.asList(chunkOwnedClientArray);

			// Send Message
			Object[] payload = {chunkId, devId, chunkOwnedArray, filename};

			out.writeObject(payload);
			out.flush();
			System.out.println("chunkId being sent: "+chunkId+", connected to: "+connTo);
			
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}


	//Send File
	public void sendFile(String chunkName) throws IOException{
		OutputStream os =null;
		String currentDir=System.getProperty("user.dir");
		chunkName=currentDir+"/src/srcFile/"+chunkName;
		File myFile = new File(chunkName);

		byte[] arrby = new byte[(int)myFile.length()];
		
		try {
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream  bis = new BufferedInputStream(fis);
			bis.read(arrby, 0, arrby.length);

			os = csocket.getOutputStream();
			System.out.println("Sending File.");
			os.write(arrby, 0, arrby.length);
			os.flush();
			System.out.println("File Sent.");
//			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
//			os.close();
		}

	}
}
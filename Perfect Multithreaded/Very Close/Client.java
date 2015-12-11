import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	String devId;
	String[] availableChunks, chunkOwnedArray;
	String intTo, filename, connTo;
	Object[] objectMessage;
	int sPort;    //This as a server will be listening on this port number
	int cPort;    //Port through which messages are exchanged
	int[] sPortArray = {18121,18122,18123,18124,18125};
	int[] cPortArray = {18221,18222,18223,18224,18225};
	String[] folderNameArray = {"Client2","Client3","Client4","Client5","Client6"};
	String folderName;
	final static int noDev=5;

	public void Client() {}

	public void setIdentity(String devId){
		sPort = sPortArray[Integer.parseInt(devId)-2];
		cPort = cPortArray[Integer.parseInt(devId)-2];
		folderName = folderNameArray[Integer.parseInt(devId)-2];
	}

	void run()
	{
		try{
				//create a socket to connect to the server
				requestSocket = new Socket("localhost", 12161);
				System.out.println("Connected to localhost in port 8000");
				//initialize inputStream and outputStream

				out = new ObjectOutputStream(requestSocket.getOutputStream());

				out.flush();
				in = new ObjectInputStream(requestSocket.getInputStream());

				while(true){
				String chunkId=xPayload();

				if((Integer.parseInt(chunkId)-Integer.parseInt(devId)+2)%noDev==0){
				
				// Write file
				String currentDir=System.getProperty("user.dir");
				//fileDest = currentDir+"/src/"+folderName+"/"+filename+".chunk"+chunkId;		// Change Client Folder identifier
				String fileDest = currentDir+"/"+folderName+"/"+filename+".chunk"+chunkId;		// Change Client Folder identifier

				byte[] myByteArray = new byte[102400];
				InputStream is = requestSocket.getInputStream();

				int bytesRead = is.read(myByteArray, 0, myByteArray.length);
				int   current = bytesRead;
				
				do {
					bytesRead = is.read(myByteArray, current, myByteArray.length - current);
					if (bytesRead >= 0) current += bytesRead;
//				} while (bytesRead > -1);
				} while (current < 102400);

				FileOutputStream fos = new FileOutputStream(fileDest);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				bos.write(myByteArray, 0, current);
				bos.flush();
				System.out.println("Done");
				}

			}
		}
		catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				if(in!=null) in.close();
				if(out!=null) out.close();
				if(requestSocket!=null) requestSocket.close();

				System.out.println("Client Thread Closed.");
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
			System.out.println("Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//main method
	public static void main(String args[])
	{
		Client client = new Client();
		client.devId = args[0];
		client.setIdentity(client.devId);
		client.chunkOwned();
		client.run();
	}

	// Exchange chunkId & chunkList & devId
	String xPayload(){
		try { 
			System.out.println("Exchanging payloads.");
			
			// Write Object
			Object[] payload={devId, chunkOwnedArray};
			out.writeObject(payload);

			// Read Object
			objectMessage = (Object[]) (in).readObject();
			connTo=(String) objectMessage[1];			//connected device's Id
			availableChunks=(String[]) objectMessage[2];
			filename = (String) objectMessage[3];
			System.out.println(objectMessage[0]+", "+connTo+", "+filename+", "+availableChunks.length);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (ConnectException e) {
			System.err.println("Control Connection refused. Initiate a server first.");
		} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		return (String) objectMessage[0];			//return chunkId
	}

	// List chunks in possession
	public void chunkOwned(){
		//Look for chunks
		String currentDir=System.getProperty("user.dir");
		//		File folder = new File(currentDir+"/src/"+folderName);			// Change Client Id
		File folder = new File(currentDir+"/"+folderName);			// Change Client Id
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

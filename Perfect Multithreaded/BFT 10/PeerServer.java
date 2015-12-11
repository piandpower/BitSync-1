import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class PeerServer implements Runnable {
	Socket sSocket;
	ServerSocket serverSocket;
	Functions f;
	ObjectOutputStream out;
	ObjectInputStream in;

	@Override
	public void run() {
		f= new Functions();

		System.out.println("PS: PeerServer thread running.");
		try {
			serverSocket = new ServerSocket(Client.sPort, 10);
			System.out.println("PS: Waiting for "+Client.row[1]+".");
			sSocket = serverSocket.accept();
			System.out.println("PS: Acting as a server to "+Client.row[1]+".");

			try {
				// Get Input and Output Streams
				in = new ObjectInputStream(sSocket.getInputStream());
				out = new ObjectOutputStream(sSocket.getOutputStream());
				out.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}

			while(true){		// Main Body Here
//				sendMessage();
				//Look for chunks
				String currentDir=System.getProperty("user.dir")+"/"+Client.folderName;
				File folder = new File(currentDir);
//
				File[] listOfFiles = folder.listFiles();
				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()&&listOfFiles[i].getName().toLowerCase().contains("chunk")) {  // 0 1 10 11 12 13 14 2 20 21 22 23 3 4 5 ....
						String chunkName=listOfFiles[i].getName().toString();
						String chunkId=chunkName.substring(chunkName.lastIndexOf('.') + 6);
						File file = new File(currentDir+"/"+chunkName);
						long fileSize=file.length();

						Object[] rPayload=xPayloadServer(chunkId, fileSize, Client.devId, Client.chunkOwnedArray, Client.filename, in, out);

						String[] chunkOwnedClientArray=(String[]) rPayload[1];					// Array of Chunks owned by Client
						List<String> chunkOwnedClientList = Arrays.asList(chunkOwnedClientArray);
						String connTo=(String) rPayload[0];

						if(!chunkOwnedClientList.contains(chunkId))
						{	
							chunkName = currentDir+"/"+chunkName;
							File myFile = new File(chunkName);

							byte[] fileByteArray = new byte[(int)myFile.length()];

							try {
								FileInputStream fis = new FileInputStream(myFile);
								BufferedInputStream  bis = new BufferedInputStream(fis);
								bis.read(fileByteArray, 0, fileByteArray.length);

								OutputStream out = sSocket.getOutputStream();
								out.write(fileByteArray, 0, fileByteArray.length);
								out.flush();
								
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}else{
							
//							byte[] fileByteArray = new byte[0];
//							OutputStream out = sSocket.getOutputStream();
//							out.write(fileByteArray, 0, 0);
//							out.flush();
							// Write previous file
							chunkName = currentDir+"/"+chunkName;
							File myFile = new File(chunkName);
							
							
							byte[] fileByteArray = new byte[(int)myFile.length()];

							try {
								FileInputStream fis = new FileInputStream(myFile);
								BufferedInputStream  bis = new BufferedInputStream(fis);
								bis.read(fileByteArray, 0, fileByteArray.length);

								OutputStream out = sSocket.getOutputStream();
								out.write(fileByteArray, 0, fileByteArray.length);
								out.flush();
								
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				Object[] rPayload=xPayloadServer("-1", (long) 0, Client.devId, Client.chunkOwnedArray, Client.filename, in, out);
				String connTo=(String) rPayload[0];
				System.out.println("PS: Client "+connTo+"'s chunk list has been updated.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			//			try {
			//				sSocket.close();
			//				serverSocket.close();
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
		}

	}
	void sendMessage() throws IOException{
		try {
			out = new ObjectOutputStream(sSocket.getOutputStream());
			String msg="Message from "+Client.devId;
			out.writeObject(msg);
			out.flush();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Exchange chunkId, devId, and ownedChunkArray
			Object[] xPayloadServer(String chunkId, long fileSize, String devId, String[] chunkOwnedArray, String filename, ObjectInputStream in, ObjectOutputStream out){
				Object[] rPayload = null;
				
				try{
					// Get message
					rPayload = (Object[]) in.readObject();
					// Send Message
					Object[] payload = {chunkId, devId, chunkOwnedArray, filename, fileSize};
					
					out.writeObject(payload);
					out.flush();
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
					return rPayload;
			}
}
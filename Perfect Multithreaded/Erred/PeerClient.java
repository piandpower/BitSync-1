import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerClient implements Runnable {
	Socket cSocket;
	Functions f;
	ObjectOutputStream out;
	ObjectInputStream in;
	@Override
	public void run() {
		f = new Functions();
		cSocket=null;

		System.out.println("PC: PeerClient thread running.");
		do{ //while(cSocket==null);
		try {

			
				
					Thread.sleep(3000);	// Retry Timeout
					cSocket = new Socket("localhost", Client.sPortArray[Integer.valueOf(Client.row[2])-2]);
				
			

			System.out.println("PC: Connection accepted by "+Client.row[2]+".");

			if(cSocket!=null){
				// Get Input and Output Streams
				out = new ObjectOutputStream(cSocket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(cSocket.getInputStream());
			}

			int noChunkOwned = 0;
			if(cSocket!=null)
				do{		// Main Body Here

					Object[] rPayload;


					int k=0;
					String chunkId;
					String connTo;
					String[] availableChunks;
					String filename;
					long fileSize;

					do{				//while(chunkId!=-1);
						noChunkOwned = f.noChunkOwned(Client.folderName);
						rPayload = xPayloadClient(Client.devId, Client.chunkOwnedArray, out, in);
						chunkId = (String) rPayload[0];
						connTo=(String) rPayload[1];			//connected device's Id
						availableChunks=(String[]) rPayload[2];
						filename = (String) rPayload[3];
						fileSize = (long) rPayload[4];
						
						if(k==0){
							System.out.println("PC: Request for chunks sent to Client "+connTo);
							k=1;
						}
						
						System.out.print("PS: Chunk List Sent.");
						

						// Write file
						String currentDir=System.getProperty("user.dir");
						//fileDest = currentDir+"/src/"+folderName+"/"+filename+".chunk"+chunkId;		// Change Client Folder identifier
						String fileDest = currentDir+"/"+Client.folderName+"/"+Client.filename+".chunk"+chunkId;		// Change Client Folder identifier


						byte[] myByteArray = new byte[102400];
						InputStream is = null;
						if(cSocket!=null&&!chunkId.equals("-1")){

							is = cSocket.getInputStream();
						}

						if(is!=null&&!chunkId.equals("-1")){
							int bytesRead = is.read(myByteArray, 0, myByteArray.length);
							int   current = bytesRead;
							//System.out.println(fileSize);
							if((fileSize-65536>0)&&Integer.parseInt(chunkId)!=-1){ // if last chunk is smaller than 65536 Bytes
								do {
									bytesRead = is.read(myByteArray, current, myByteArray.length - current);
									if (bytesRead >= 0) current += bytesRead;
									//									//} while (bytesRead > -1);
								} while (current != (int)fileSize); //************
							}
							//
							FileOutputStream fos = new FileOutputStream(fileDest);
							BufferedOutputStream bos = new BufferedOutputStream(fos);
							//
							bos.write(myByteArray, 0, (int)fileSize);
							bos.flush();
							System.out.println("PC: Chunk "+chunkId+" received.");
						}
						if(Integer.valueOf(chunkId)==-1){
							System.out.println("PC: All chunks received from the serving client.");
						}

					}while(Integer.valueOf(chunkId)!=-1);

					// On chunkList update
					if(chunkId.equals("-1"))System.out.println("PC: This client's ("+Client.devId+") chunk list is up-to-date.");
					System.out.println("PC: Next query will occur in 4.5 sec");
					Thread.sleep(4500);
				}while(noChunkOwned<Integer.parseInt(Client.noChunks));
				
				// On receiving all chunks.
			if(noChunkOwned==Integer.parseInt(Client.noChunks)){
				System.out.println("PC: _____________________________________________________________");
				System.out.println("PC: All chunks have been received, and is being joined into a whole file.");
				FileSplitter fs = new FileSplitter();
				fs.join(Client.filename, Client.folderName);

			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (java.net.ConnectException e) {
			System.out.println("PC: Connection refused. Retrying in 3s, please wait.");
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally{
			//							try {
			//								if(cSocket!=null)cSocket.close();
			//								if(in!=null)in.close();
			//								if(out!=null)out.close();
			//							} catch (IOException e) {
			//								e.printStackTrace();
			//							}
		}
		}while(cSocket==null);


	}

	void getMessage(){
		try {
			in = new ObjectInputStream(cSocket.getInputStream());
			String msg=(String) in.readObject();
			System.out.println(msg);
			Thread.sleep(5000);
		} catch (InterruptedException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Exchange chunkId & chunkList & devId
	Object[] xPayloadClient(String devId, String[] chunkOwnedArray, ObjectOutputStream out, ObjectInputStream in){
		Object[] objectMessage = null;
		try { 

			// Write Object
			Object[] payload={devId, chunkOwnedArray};
			out.writeObject(payload);

			// Read Object
			objectMessage = (Object[]) (in).readObject();


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
		return objectMessage;			//return chunkId
	}

}


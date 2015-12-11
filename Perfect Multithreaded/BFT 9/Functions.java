import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;


public class Functions {
	public static final int verbose=0;

	//Send File
	public void sendFile(String chunkName, Socket csocket, int flag) throws IOException{
		OutputStream os =null;
		String currentDir=System.getProperty("user.dir");
		if(flag==-1){
			chunkName=currentDir+"/src/srcFile/"+chunkName;
		}
		else{
			chunkName=currentDir+"/"+Client.folderName+"/"+chunkName;
		}
		File myFile = new File(chunkName);

		byte[] arrby = new byte[(int)myFile.length()];

		try {
			FileInputStream fis = new FileInputStream(myFile);
			BufferedInputStream  bis = new BufferedInputStream(fis);
			bis.read(arrby, 0, arrby.length);

			os = csocket.getOutputStream();
			os.write(arrby, 0, arrby.length);
			os.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//Receive File
	public void receiveFile(String devId, Socket cSocket, ObjectInputStream in, ObjectOutputStream out, int flag) throws IOException{
		int k=0;
		String chunkId;
		String connTo;
		String[] availableChunks;
		long fileSize;
		Object[] rPayload;
		do{
			if(flag==-1){
				// Sleep
				try {
					Thread.sleep(Integer.valueOf(Client.sleep));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			rPayload = xPayloadClient(Client.devId, Client.chunkOwnedArray, out, in);
			chunkId = (String) rPayload[0];
			connTo=(String) rPayload[1];			//connected device's Id
			availableChunks=(String[]) rPayload[2];
			Client.filename = (String) rPayload[3];
			fileSize = (long) rPayload[4];
			Client.noChunks = (String) rPayload[5];

			if((Integer.parseInt(chunkId)-Integer.parseInt(devId)+2)%Client.noDev==0&&Integer.parseInt(chunkId)!=-1){

				if(k==0){
					System.out.println("C: Receiving files from Server; Please wait.");
					k=1;
				}

				// Write file
				String currentDir=System.getProperty("user.dir");
				//fileDest = currentDir+"/src/"+folderName+"/"+filename+".chunk"+chunkId;		// Change Client Folder identifier
				String fileDest = currentDir+"/"+Client.folderName+"/"+Client.filename+".chunk"+chunkId;		// Change Client Folder identifier


				byte[] myByteArray = new byte[102400];
				InputStream is = cSocket.getInputStream();

				int bytesRead = is.read(myByteArray, 0, myByteArray.length);
				int   current = bytesRead;
				//					System.out.println(fileSize);

				if((fileSize-65536>0)&&Integer.parseInt(chunkId)!=-1){ // if last chunk is smaller than 65536 Bytes
					do {
						bytesRead = is.read(myByteArray, current, myByteArray.length - current);
						if (bytesRead >= 0) current += bytesRead;
						//} while (bytesRead > -1);
					} while (current != (int)fileSize);
				}

				FileOutputStream fos = new FileOutputStream(fileDest);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				bos.write(myByteArray, 0, (int)fileSize);
				bos.flush();
			}

			if(Integer.valueOf(chunkId)==-1){
				System.out.println("C: Appropriate chunks have be received from Server.");
			}

		}while(Integer.valueOf(chunkId)!=-1);
	}

	// Exchange chunkId, devId, and ownedChunkArray
	Object[] xPayloadServer(String chunkId, long fileSize, String devId, String[] chunkOwnedArray, String filename, ObjectInputStream in, ObjectOutputStream out, String noChunks){
		Object[] rPayload = null;

		try{
			// Get message
			rPayload = (Object[]) in.readObject();
			// Send Message
			Object[] payload = {chunkId, devId, chunkOwnedArray, filename, fileSize, noChunks};

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
			catch(java.io.StreamCorruptedException se){
				System.out.println("Stream Corrupted.");
			}catch(IOException ioException){
				ioException.printStackTrace();
			}
			return objectMessage;			//return chunkId
		
	}

	// List chunks in possession
	public String[] chunkOwned(String folderName){
		//Look for chunks
		String currentDir;
		if(folderName.equals("-1")){
			currentDir=System.getProperty("user.dir")+"/src/srcFile";
		}
		else{
			currentDir=System.getProperty("user.dir")+"/"+folderName;

		}
		File folder = new File(currentDir);


		File[] listOfFiles = folder.listFiles();
		int fileCount=0;


		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()&&listOfFiles[i].getName().toLowerCase().contains("chunk")) {
				fileCount=fileCount+1;

			}
		}
		String[] chunkOwnedArray= new String[fileCount];
		fileCount=0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()&&listOfFiles[i].getName().toLowerCase().contains("chunk")) {
				String chunkName=listOfFiles[i].getName().toString();
				String chunkIdOwned=chunkName.substring(chunkName.lastIndexOf('.') + 6);
				chunkOwnedArray[fileCount]=chunkIdOwned;
				fileCount++;
			}
		}
		return chunkOwnedArray;

	}

	//Config reader
	public String[] configReader(String devId){
		BufferedReader br = null;
		String line = null;
		String[] row = {"-1","-1","-1"};

		try {
			String currentDir=System.getProperty("user.dir");
			br = new BufferedReader(new FileReader(currentDir+"/config.txt"));
			while ((line = br.readLine()) != null) {
				String[] data = line.split(",");
				if(data[0].equals(devId))
					row=data;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return row;
	}


	// List chunks in possession
	public int noChunkOwned(String folderName){
		//Look for chunks
		String currentDir;
		if(folderName.equals("-1")){
			currentDir=System.getProperty("user.dir")+"/src/srcFile";
		}
		else{
			currentDir=System.getProperty("user.dir")+"/"+folderName;

		}
		File folder = new File(currentDir);


		File[] listOfFiles = folder.listFiles();
		int fileCount=0;


		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()&&listOfFiles[i].getName().toLowerCase().contains("chunk")) {
				fileCount++;
			}
		}
		return fileCount;

	}

}

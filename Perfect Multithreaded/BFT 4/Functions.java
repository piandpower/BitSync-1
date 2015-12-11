import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;


public class Functions {
	//Send File
		public void sendFile(String chunkName, Socket csocket) throws IOException{
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
				os.write(arrby, 0, arrby.length);
				os.flush();
				System.out.print(".");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		// Exchange chunkId, devId, and ownedChunkArray
		Object[] xPayloadServer(String chunkId, long fileSize, String devId, String[] chunkOwnedArray, String filename, ObjectInputStream in, ObjectOutputStream out){
			Socket conn = null;
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
			System.out.println(currentDir);
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

}

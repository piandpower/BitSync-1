import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PeerServer implements Runnable {
	Socket sSocket;
	ServerSocket serverSocket;

	@Override
	public void run() {
		System.out.println(Client.row[1]);
		System.out.println("PeerServer thread running.");
		try {
			serverSocket = new ServerSocket(Client.sPort, 10);
			System.out.println("Waiting for "+Client.row[1]+".");
			sSocket = serverSocket.accept();
			System.out.println("Acting as a server to "+Client.row[1]+".");

			while(true){
				sendMessage();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				sSocket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	void sendMessage() throws IOException{
		try {
			ObjectOutputStream out = new ObjectOutputStream(sSocket.getOutputStream());
			String msg="Message from "+Client.devId;
			out.writeObject(msg);
			out.flush();
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
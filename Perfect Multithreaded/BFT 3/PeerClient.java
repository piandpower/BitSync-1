import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class PeerClient implements Runnable {
	Socket cSocket;
	@Override
	public void run() {

		System.out.println(Client.row[2]);
		System.out.println("PeerClient thread running.");
		do{
			try {
				Thread.sleep(3000);	// Retry Timeout
				cSocket = new Socket("localhost", Client.sPortArray[Integer.valueOf(Client.row[2])-2]);


				System.out.println("Connection accepted by "+Client.row[2]+".");

				while(true){
					getMessage();
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (java.net.ConnectException e) {
				System.out.println("Connection refused. Retrying in 3s, please wait.");
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				try {
					if(cSocket!=null)cSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}while(cSocket==null);

	}

	void getMessage(){
		try {
			ObjectInputStream in = new ObjectInputStream(cSocket.getInputStream());
			String msg=(String) in.readObject();
			System.out.println(msg);
			Thread.sleep(5000);
		} catch (InterruptedException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}


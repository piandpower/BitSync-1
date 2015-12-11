import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {
	int sPort = 12181;    //The server will be listening on this port number
	ServerSocket sSocket;   //serversocket used to listen on port number 8000
	Socket connection = null; //socket for the connection with the client
	String message;    //message received from the client
	String MESSAGE;    //uppercase message send to the client
	ObjectOutputStream out;  //stream write to the socket
	ObjectInputStream in;    //stream read from the socket

	public void Server() {}



	public static void main(String args[]) throws IOException {
		ServerSocket sSocket = new ServerSocket(12181, 10);

		System.out.println("Waiting for connection");
		while(true){			
			//accept a connection from the client
			Socket connection = sSocket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());

			new Thread(new MultiThreadServer(connection)).start();
		}
	}

}

class MultiThreadServer implements Runnable {
	String message;    //message received from the client
	String MESSAGE;    //uppercase message send to the client
	ObjectOutputStream out;  //stream write to the socket
	ObjectInputStream in;    //stream read from the socket
	   Socket csocket;
	   MultiThreadServer(Socket csocket) {
	      this.csocket = csocket;
	   }
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			while(true){

				//initialize Input and Output streams
				out = new ObjectOutputStream(csocket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(csocket.getInputStream());
				try{

					//receive the message sent from the client
					message = (String)in.readObject();
					//show the message to the user
					System.out.println("Receive message: " + message);
					//Capitalize all letters in the message
					MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					sendMessage(MESSAGE);

				}

				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
			}
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				//				in.close();
				//				out.close();
				csocket.close();
				System.out.println("Thread closed.");
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
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message: " + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
}
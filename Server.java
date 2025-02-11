import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

// Patrick Kallenbach - CNT4007 Project 1

public class Server {
	int sPort = 8000;    //The server will be listening on this port number
	ServerSocket sSocket;   //serversocket used to lisen on port number 8000
	Socket connection = null; //socket for the connection with the client
	String message;    //message received from the client
	String MESSAGE;    //uppercase message send to the client
	ObjectOutputStream out;  //stream write to the socket
	ObjectInputStream in;    //stream read from the socket

    	public void Server() {}

	void run()
	{
		try{
			//create a serversocket
			sSocket = new ServerSocket(sPort, 10);
			//Wait for connection
			System.out.println("Waiting for connection");
			//accept a connection from the client
			connection = sSocket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			// try{
				while(true)
				{
					//receive the message sent from the client

					// confirm instruction: get or upload
						// get: find data in map storage with filename key and retrieve
						// upload: create new entry in map and begin receiving files

					// get: send stored packets one by one across
						// send "done" message to confirm file is finished sending
						// if any requests for missing packets are returned, send each missing packet again

					// upload: receive packets one by one and add to server database
						// request missing files when receiving "done" message
					
				}
			// }
			// catch(ClassNotFoundException classnot){
			// 		System.err.println("Data received in unknown format");
			// 	}
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				sSocket.close();
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
	public static void main(String args[]) {
		Server s = new Server();
		s.run();  

	}
}

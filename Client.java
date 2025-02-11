import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

// Patrick Kallenbach - CNT4007 Project 1

public class Client {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	String request;                //User request (get, upload)

	public void Client() {}

	void run()
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

			while(true)
			{
				// Get user input
				request = bufferedReader.readLine();

				// Split input into at most two parts, command and filename
				String[] args = request.split(" ", 2);
				String command = args[0];
				String filename = args[1];

				switch (command) {
					case "get":
						System.out.println("GETTING FILE " + filename);
						break;
					case "upload":
						System.out.println("UPLOADING FILE " + filename);
						break;
					default:
						System.out.println("Unknown request: " + command);
				}
				
				// Implement get and upload functions
					// send message with get or upload command, wait for response before continuing
						// get: send request message to server with file name, wait for response
						// upload: send request message to server, wait for response

				// upload command: Add file to the server
					// loop through file and break into packets
					// store packets in dictionary, map, something like that
					// transfer packets one by one
						// send packets containing length and progress of message as header
				// get command: retrieve file from server
					// receive all packets sorting by position
					// if any are missing, request them at the end and insert
			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		// catch ( ClassNotFoundException e ) {
        //     		System.err.println("Class not found");
        // 	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
				requestSocket.close();
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
		client.run();
	}

}

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
	String request;                //User request (get, upload)
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
			try{
				while(true)
				{
					// Read message from client
					request = (String)in.readObject();

					// Split input into at most two parts, command and filename
					String[] args = request.split(" ", 2);
					String command = args[0];
					String filename = args[1];

					SortedMap<Long, byte[]> storedFile = new TreeMap<>();

					String confirm = "";

					// cases for different commands
					switch (command) {
						case "GET":
							System.out.println("Sending file to client!!! " + filename);
							
							sendMessage("DONE");
							
							break;
						case "UPLOAD":
							sendMessage("OK");
							
							// run set to false when file is fully received
							boolean run = true;

							while (run) {
								// prepare receiving message
								Object message;
								message = in.readObject();

								// get total file length from within loop
								long totalLength = -1;

								while (message instanceof byte[]) {
									// load packet buffer
									ByteBuffer packetBuffer = ByteBuffer.wrap((byte[])message);

									// get current packet number and total number of packets
									long packetNumber = packetBuffer.getLong();
									if (totalLength != -1) totalLength = packetBuffer.getLong();
									
									// place packet in storage map
									storedFile.put(packetNumber, (byte[])message);
									
									// listen for new message
									message = in.readObject();
								}
								
								// prepare list of missing packets to report to client
								List<Long> missedPackets = new ArrayList<>();

								// add all missing packets reported from storage into array
								for (long i = 0; i < totalLength; i++) {
									if (!storedFile.containsKey(i)) {
										missedPackets.add(i);
									}
								}

								// If there are missing packets in the map
								if (!missedPackets.isEmpty()) {
									// Prepare byte buffer of packet ids
									ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * missedPackets.size());
									for (Long value : missedPackets) {
										buffer.putLong(value);
									}
									buffer.flip();

									// send missing packets to client
									out.writeObject(buffer.array());
									out.flush();
								}
								else { // if no missing packets in file
									run = false;
									sendMessage("OK"); // report full reception of file to client
								}
							}
							System.out.println("Uploading file to server: " + filename);

							// Create file to write new content to
							FileOutputStream outfile = new FileOutputStream("newUploadTestFile.pptx");

							// For each entry now held in storedFile, write to outfile
							for (SortedMap.Entry<Long, byte[]> receivePacket : storedFile.entrySet()) {

								// Create new bytebuffer with information from each packet
								ByteBuffer receiveData = ByteBuffer.wrap(receivePacket.getValue());
								long packetNumber = receiveData.getLong();
								long totalPackets = receiveData.getLong();
								int packetLength = (int)receiveData.getLong(); // important value from buffer, determines packet length

								// Prepare array to write to file
								byte[] writeOut = new byte[packetLength];
								receiveData.get(writeOut, 0, packetLength);

								// write character by character to outfile
								for (byte character : writeOut) {
									outfile.write(character);
								}
							}

							System.out.println("Finished writing " + filename + " to server.");
							
							// close output file
							outfile.close();

							break;
					}					
				}
			}
			catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
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

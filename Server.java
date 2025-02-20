import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

// Patrick Kallenbach - CNT4007 Project 1

public class server {
	int sPort = 8000;    //The server will be listening on this port number
	ServerSocket sSocket;   //serversocket used to lisen on port number 8000
	Socket connection = null; //socket for the connection with the client
	String request;                //User request (get, upload)
	ObjectOutputStream out;  //stream write to the socket
	ObjectInputStream in;    //stream read from the socket

	int packetSize = 1000;

    public void server() {}

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
					Object message;

					// cases for different commands
					switch (command) {
						case "GET":
							// Prepare input file
							FileInputStream infile = new FileInputStream(filename);

							// Prepare tracking values
							long track = 0;
							long fileLength = infile.getChannel().size();
							long remainingFileLength = fileLength;

							while (remainingFileLength > 0) {
								// Define Header information
									// (Packet #, Total Packet #, Packet Length)
								ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES*3); // Create a 24-byte buffer
								
								// Place tracking values in header
								buffer.putLong(track);
								buffer.putLong(fileLength / packetSize);
								buffer.putLong(Math.min(packetSize, remainingFileLength));
								buffer.flip(); // Flip to read header in correct order

								byte[] header = buffer.array();

								// Read data into temporary storage
								byte[] data = new byte[packetSize];
								infile.read(data);

								// Write header and data into packet
								byte[] packet = new byte[packetSize + 24];

								// Copy header and data to packet
								System.arraycopy(header, 0, packet, 0, 24);
								System.arraycopy(data, 0, packet, 24, packetSize);

								// Insert packet into storedFile map
								storedFile.put(track, packet);

								// update tracking counters
								track += 1;
								remainingFileLength -= packetSize;
							}

							// Send all values to server
							for (SortedMap.Entry<Long, byte[]> sendPacket : storedFile.entrySet()) {
								out.writeObject(sendPacket.getValue());
								out.flush();
							}
							// Send completion message to alert server of complete transfer
							sendMessage("DONE");

							message = in.readObject();
									
							System.out.println("File sent to client.");
							
							out.flush();

							break;
						case "UPLOAD":
							sendMessage("OK");
							
							// prepare receiving message
							message = in.readObject();

							while (message instanceof byte[]) {
								// load packet buffer
								ByteBuffer packetBuffer = ByteBuffer.wrap((byte[])message);

								// get current packet number and total number of packets
								long packetNumber = packetBuffer.getLong();
								
								// place packet in storage map
								storedFile.put(packetNumber, (byte[])message);
								
								// listen for new message
								message = in.readObject();
							}
							sendMessage("OK"); // report full reception of file to client
							
							System.out.println("Uploading file to server: " + filename);

							// Create file to write new content to
							FileOutputStream outfile = new FileOutputStream("new" + filename.substring(0, 1).toUpperCase() + filename.substring(1));

							int lastPercentage = 0; // used for displaying percentage uploaded

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

								// Write to file
								outfile.getChannel().write(ByteBuffer.wrap(writeOut));

								// Track progress in increments of 5%
								float progress = packetNumber * 20 / totalPackets;
								if (Math.floor(progress) != lastPercentage) {
									System.out.println("Upload " + (int)(progress * 5) + "% complete.");
									lastPercentage = (int)Math.floor(progress);
								}
							}

							System.out.println("Finished uploading " + filename + " to server.");
							
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
		server s = new server();
		s.run();  

	}
}

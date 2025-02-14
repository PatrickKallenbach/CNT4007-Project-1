import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Paths;
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

				// Check input length
				if (args.length == 1) {
					System.out.println("Unknown request. Please use \"get <filename>\" or \"upload <filename>\"");
				}
				else {
					// Break input line into arguments
					String command = args[0];
					String filename = args[1];

					String confirm;

					// Define cases for different commands
					switch (command) {
						case "get":
							System.out.println("GETTING FILE " + filename);

							sendMessage("GET " + filename);

							// get command: retrieve file from server
								// receive all packets sorting by position
								// if any are missing, request them at the end and insert

							
							confirm = (String)in.readObject();
							System.out.println("File received.");

							break;
						case "upload":
							System.out.println("UPLOADING FILE " + filename);

							int packetSize = 1000;

							sendMessage("UPLOAD " + filename);
							confirm = (String)in.readObject();

							SortedMap<Long, byte[]> storedFile = new TreeMap<>();

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

							outfile.close();

							sendMessage("DONE");
							
							// upload command: Add file to the server
								// loop through file and break into packets
								// store packets in dictionary, map, something like that
								// transfer packets one by one
									// send packets containing length and progress of message as header
						
									
							confirm = (String)in.readObject();
							System.out.println("File uploaded.");
							
							out.flush();

							break;
						default:
							System.out.println("Unknown request. Please use \"get <filename>\" or \"upload <filename>\"");
					}
					
					// Implement get and upload functions
						// send message with get or upload command, wait for response before continuing
							// get: send request message to server with file name, wait for response
							// upload: send request message to server, wait for response

					}
				}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e ) {
            		System.err.println("Class not found");
        	} 
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

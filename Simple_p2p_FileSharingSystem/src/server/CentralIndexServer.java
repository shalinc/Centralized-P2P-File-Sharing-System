package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.io.File;
import java.io.FileNotFoundException;



public class CentralIndexServer
{

	//Connection PortNumber to Server Socket
	public static int portNumber = 5004;

	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub

		//Start the Server
		//Listen for connection at portNumber

		ServerSocket indexServer = new ServerSocket(portNumber);

		//wait for connection from clients/peer
		System.out.println("Server is Up and Running ...");

		while(true)
		{
			Socket socket = indexServer.accept();	//accept client connection

			//Thread Creation in Process
			Thread t = new Thread(new ConnectPeer(socket));
			t.start();
		}
	}
}

class ConnectPeer implements Runnable 
{

	public Socket clientSocket;	//Client Socket

	//HashMap to store Key (Filename) and Value (where file is located)
	public static HashMap<String, ArrayList<String>> fileMapping = new 
			HashMap<String, ArrayList<String>>();

	//Ctor to initialize the Socket
	public ConnectPeer(Socket socket) throws IOException
	{
		this.clientSocket = socket;	
	}

	@Override
	public void run()
	{

		System.out.println("Connected to Client");
		boolean loop = true;

		//loop unitl client present
		while(loop)
		{
			try
			{
				String nameOfFile;
				String peerID;
				String searchFileName;
				List<String> fileIP = null;

				//for communication over sockets between Client and server
				DataInputStream dIn = new DataInputStream(clientSocket.getInputStream());
				DataOutputStream dOut = new DataOutputStream(clientSocket.getOutputStream());

				//read the choice according to MENU
				String choice = dIn.readUTF();

				//Functionality MENU
				switch(choice)
				{
				case "1": //Registration of Files

					/*nameOfFile = dIn.readUTF();	//read the fileName*/
					peerID = dIn.readUTF();

					//System.out.println("Registering the File ...");

					//call registry to register the file
					//registry(peerID,nameOfFile);

					//System.out.println("File Registered!!!");


					/* ***!!!PERFORMANCE EVALUATION!!! ***/

					
					File dir= new File("C:/p2psharedFolder/23000/");
					File[] sharedfiles = dir.listFiles();

					System.out.println("# of files registered: " + sharedfiles.length);

					System.out.println("Registering the File(s) ...");

					long startTime = System.currentTimeMillis();
					long endTime;
					
					for(int i=0;i<sharedfiles.length;i++)
						registry(peerID,sharedfiles[i].getName());

					System.out.println("File(s) Registered!!!");

					endTime = System.currentTimeMillis();

					long totalTime = endTime - startTime;
					System.out.println("Total time to register "+ sharedfiles.length +" files: " +(totalTime)+"msec");

					/*System.out.println("List of all the files on different Peers");
					for(Map.Entry<String, ArrayList<String>> entry : fileMapping.entrySet())
					{
						System.out.println("File Name is: "+entry.getKey()+" PeerID is: "+entry.getValue());
					}*/
					break;

				case "2":	//Search

					searchFileName = dIn.readUTF();	//get the filename to be searched
					System.out.println("Please wait while we get the details for the file ...");
					try
					{
						fileIP = search(searchFileName);	//results where file is present
						String searchDetails="";
						ListIterator<String> iterator = fileIP.listIterator();
						while(iterator.hasNext())
						{
							searchDetails+=iterator.next()+" ";
						}
						String sendDetails = "The file is present with these Peers: "+searchDetails;
						dOut.writeUTF(sendDetails);
						dOut.flush();
						System.out.println("Search Complete found at: "+searchDetails);

					}catch(Exception e)
					{
						dOut.writeUTF("File is not Registered");
						System.out.println("File is not Registered");
						break;
					}

					/* ***!!!PERFORMANCE EVALUATION!!! ***/
					/*File dir1= new File("/home/shalin/p2psharedFolder/23000/");
					File[] sharedfiles1 = dir1.listFiles();

					System.out.println("# of files registered: " + sharedfiles1.length);

					//System.out.println("Registering the File(s) ...");

					long startTime1 =System.currentTimeMillis();
					long endTime1;

					for(int i=0;i<sharedfiles1.length;i++)
					{
						fileIP = search(sharedfiles1[i].getName());
						System.out.println("File present with "+fileIP);
					}

					String searchDetails="";
					ListIterator<String> iterator = fileIP.listIterator();
					while(iterator.hasNext())
					{
						searchDetails+=iterator.next()+" ";
					}
					String sendDetails = "The file is present with these Peers: "+searchDetails;
					dOut.writeUTF(sendDetails);
					dOut.flush();
					System.out.println("Search Complete found at: "+searchDetails);

					endTime1 = System.currentTimeMillis();

					long totalTime1 = endTime1 - startTime1;
					System.out.println("Total time to search files: "+(totalTime1)+"msec");*/

					break;

				case "4":	//Exit

					clientSocket.close();
					System.out.println("Client Dissconnected");
					loop = false;	//exit from the while(true) loop
					//System.exit(0);
					break;

				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	/*
	 *	This method registers the files with the indexing server
	 */
	public boolean registry(String peerID, String files) throws IOException
	{
		//create temporary lists to store the new PeerID's for the files
		ArrayList<String> peers = new ArrayList<String>();
		ArrayList<String> check = new ArrayList<String>();

		//add peers to the list
		peers.add(peerID);

		String fileName = files;

		check = fileMapping.get(fileName);	//get the value(PeerID) for the Key(Filename)

		//check if file is already registered by the same peer, if not register it
		if(check == null || check.isEmpty())
		{
			fileMapping.put(fileName, peers);
			//System.out.println("Registered "+files+ " Successfully");
		}
		else
		{
			Iterator<String> iterator = check.listIterator();

			while(iterator.hasNext())
			{
				String chkPid = iterator.next();

				if(chkPid.equals(peerID))
				{
					//System.out.println("Already Registered !!!");
					return true;
				}
			}

			//add new PeerID to existing FileName
			check.add(peerID);
			fileMapping.put(fileName,check);
		}
		return true;
	}

	/*
	 * This method is used to search for an already registered file,
	 * return the list of peers where the file is present
	 */
	public List<String> search(String fileName) throws IOException
	{
		List <String> filePeers = new ArrayList<String>();

		filePeers = fileMapping.get(fileName);
		return filePeers;
	}
}
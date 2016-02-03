package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;



public class Client 
{
	@SuppressWarnings("deprecation")
	public static void main(String[] args) 
	{
		//Path for file location	-- You can Change this to any Folder path from where you need to download files
		//String fileDirectory = "/home/shalin/p2psharedFolder/";		//Linux
		String fileDirectory = "C:/p2psharedFolder/";				//Windows

		while(true)
		{
			try 
			{	
				Socket cAsServer = new Socket("localhost",5004);
				int userInpPort = Integer.parseInt(args[0]);

				//thread for peer upload
				Thread t1 = new Thread(new ClientUpload(fileDirectory,userInpPort));
				t1.start();

				String choice;

				do
				{
					System.out.println("****MENU****");
					System.out.println("1. Register Files");
					System.out.println("2. Search for a File");
					System.out.println("3. Obtain a File");
					System.out.println("4. Exit");

					DataInputStream dIS = new DataInputStream(System.in);
					choice = dIS.readLine();

					DataInputStream dInpServer = new DataInputStream(cAsServer.getInputStream());
					DataOutputStream dOutServer = new DataOutputStream(cAsServer.getOutputStream());
					
					String userRegisterInfo = null;
					String searchFileName = null;
					
					switch(choice)
					{

					case "1":	//Register files

						dOutServer.writeUTF(choice);
						dOutServer.flush();

						/*System.out.println("Enter the filename (with extension) to register");
						userRegisterInfo = dIS.readLine();

						dOutServer.writeUTF(userRegisterInfo);	*/
						dOutServer.writeUTF(Integer.toString(userInpPort));
						break;

					case "2":	//Search for file

						dOutServer.writeUTF(choice);
						dOutServer.flush();

						System.out.println("Enter the filename to be searched: ");
						searchFileName = dIS.readLine();
						dOutServer.writeUTF(searchFileName);

						String searchResults = dInpServer.readUTF();
						
						System.out.println(searchResults);
						break;

					case "3":	//Obtain the file

						System.out.println("Enter the fileName");
						String obtainFileName = dIS.readLine();
						System.out.println("Enter the PeerID");
						String obtainPeerID = dIS.readLine();

						long startTime = System.currentTimeMillis();
						obtain(obtainFileName,obtainPeerID,userInpPort);
						long endTime = System.currentTimeMillis();
						System.out.println("Time required: "+(endTime-startTime)+"msec");
						break;

					case "4":	//Exit

						System.out.println("Client Connection Closing .. Bye !!!");
						dOutServer.writeUTF(choice);
						cAsServer.close();
						System.exit(0);
						break;

					default:
						break;

					}
				}while(!(choice.equals("4")));
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	

	/*
	 * Obtain(filename,peerID,inPort) : This method is used to download the file,
	 * requested by the peer from peerID (another peer)
	 */
	private static void obtain(String fileName,String peerID,int inPort) throws IOException
	{

		//path to store the downloaded file  !!! ***CHANGE THE LINE BELOW*** !!!
		//String filePath = "/home/shalin/p2psharedFolder/"+inPort+"/";		//Linux
		String filePath = "C:/p2psharedFolder/"+inPort+"/";				//windows

		//create a directory folder for peer if the folder doesnot exists where the file needs to be downloaded
		File createDirectory = new File(filePath);
		if(!createDirectory.exists())
		{
			System.out.println("Creating a new folder named: "+inPort);
			createDirectory.mkdir();
			System.out.println("The file will be found at: "+filePath);
		}
		
		//Make a connection with server to get file from
		int portNumberForP2P = Integer.parseInt(peerID);
		Socket peerClient = new Socket("localhost",portNumberForP2P);
		System.out.println("Downloading File Please wait ...");

		//Input & Output for socket Communication
		DataInputStream in = new DataInputStream(peerClient.getInputStream());
		DataOutputStream out = new DataOutputStream(peerClient.getOutputStream());

		//System.out.println("writing filename to serverclient");
		out.writeUTF(fileName);
		out.flush();
		out.writeUTF(peerID);
		//System.out.println("Wrote filename to serverclient");

		
		String strFilePath = filePath + fileName;

		long buffSize = in.readLong();
		int newBuffSize = (int) buffSize;
		
		byte[] b = new byte[newBuffSize];

		int numberofbytesread = in.read(b);

		//Write the file requested by the peer
		try 
		{
			FileOutputStream writeFileStream = new FileOutputStream(strFilePath);

			writeFileStream.write(b);

			writeFileStream.close();

			System.out.println("Downloaded Successfully");
			System.out.println("Display file " + fileName);

			peerClient.close();

		} catch (FileNotFoundException ex) 
		{
			System.out.println("FileNotFoundException : " + ex);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}

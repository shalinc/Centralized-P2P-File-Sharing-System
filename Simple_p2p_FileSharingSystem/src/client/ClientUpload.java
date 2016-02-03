package client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientUpload implements Runnable
{
	String fileDownloadPath;
	int portNumber;
	Socket peerSocket;
	DataInputStream dIn;
	DataOutputStream dOut;

	public ClientUpload(String filePath,int userInpPort)
	{
		this.portNumber = userInpPort;
		this.fileDownloadPath = filePath;
	}

	public void run()
	{
		try
		{
		
			ServerSocket downloadSocket = new ServerSocket(portNumber);
			//System.out.println("starting client socket now");
			
			while (true) 
			{
				//accept the connection from the socket
				peerSocket = downloadSocket.accept();
				System.out.println("Client connected for File sharing ...");

				dOut = new DataOutputStream(peerSocket.getOutputStream());
				dIn = new DataInputStream(peerSocket.getInputStream());

				//get the fileName from ClientAskingForFile
				String fileName = dIn.readUTF();
				System.out.println("Requested file is: "+fileName);
				String peerForFile = dIn.readUTF();
				
				
				// !!!***COMMENT THE BELOW LINE***!!! -- as this downloads from a specific folder(named "PORT_NO")
				File checkFile = new File(fileDownloadPath + peerForFile +"/" + fileName);
				
				// !!!***UNCOMMENT THE BELOW LINE***!!!	-- this will download from the path you specify in Client.java line (18) 
				//File checkFile = new File(fileDownloadPath + fileName);
				
				FileInputStream fin = new FileInputStream(checkFile);
				BufferedInputStream buffReader = new BufferedInputStream(fin);
				
				//check if the file exists, for it to be downloaded
				if (!checkFile.exists()) 
				{
					System.out.println("File doesnot Exists");
					buffReader.close();
					return;
				}

				//get the file size, as the buffer needs to be allocated an initial size
				int size = (int) checkFile.length();	//convert from long to int
				
				byte[] buffContent = new byte[size];
				
				//send file size
				dOut.writeLong(size);
				
				//allocate a buffer to store contents of file
				
				int startRead = 0;	//how much is read in total
				int numOfRead = 0;	//how much is read in each read() call

				//read into buffContent, from StartRead until end of file
				while (startRead < buffContent.length && (numOfRead = buffReader.read(buffContent, startRead, buffContent.length - startRead)) >= 0) 
				{
					startRead = startRead + numOfRead;
				}

				//Validate all the bytes have been read
				if (startRead < buffContent.length) 
				{
					System.out.println("File Read Incompletely" + checkFile.getName());
				}

				dOut.write(buffContent);
				buffReader.close();
				//peerSocket.shutdownOutput();
			}

		}catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}

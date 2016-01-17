package Peers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class peer {
	static List<Peer_Info> peerInfo = new ArrayList<Peer_Info>();

	public static void main(String[] args)
	{
		String serverIP = args[0];
		int serverPort = Integer.parseInt(args[1]);
		Socket peerSocket = null;
		Socket peerToPeerSocket = null;
		DataInputStream inStream = null;
		DataOutputStream outStream = null;
		DataInputStream input_s = null;
		DataOutputStream output_s = null;
		String hostname = null;
		int uploadPort;
		Scanner scanner = new Scanner(System.in);
		String rfcPpathName = null;
		int numOfRFC;
		int rfcNum;
		String rfcTitle;
		boolean clientAlive = true;
		String addRequest = null;
		Peer_Info new_peer_info = new Peer_Info();
		 ServerSocket  serverSocket = null;
		
		 int count_ =0;
		
		
		try {
			peerSocket = new Socket(serverIP, serverPort);
			inStream = new DataInputStream(peerSocket.getInputStream());
			outStream =new DataOutputStream(peerSocket.getOutputStream());
			hostname = java.net.InetAddress.getLocalHost().getHostAddress();

			
			System.out.println("Enter the upload port number:");
			uploadPort = Integer.parseInt(scanner.nextLine());
			System.out.println("Enter the RFC path for this client:");
			rfcPpathName = scanner.nextLine();
		   new_peer_info.setRFChost(hostname);
		   new_peer_info.setRFCport(uploadPort);
		   new_peer_info.setRFCpath(rfcPpathName);
		   System.out.println("peerInfo size is:"+ peerInfo.size());
		   peerInfo.add(new_peer_info);
		   //new change
		   try {
			 serverSocket = new ServerSocket(uploadPort);// server creates a new serversocket object to listen to a specific port
			 Thread  listenPeer = new Thread(new P2PConHandler(serverSocket, rfcPpathName));	
			 listenPeer.start();
			 } 
			 catch(IOException e)
			 {
				 System.out.println("IOException: " + e); 
			 }
			
		  
			outStream.writeUTF("Hello Server!!");
			System.out.println(inStream.readUTF());//will print the hello message from the server

		    
			File rfcListFile = new File(rfcPpathName);
			numOfRFC = rfcListFile.listFiles().length;
			System.out.println("No of Rfc for this client: " +numOfRFC);
			outStream.writeInt(numOfRFC);
			
			
			File[] rfcFiles = rfcListFile.listFiles();
			
			for(File f : rfcFiles)
			{
				addRequest = frameRFCAddReq(f,hostname,uploadPort);
				System.out.println(addRequest);
				outStream.writeUTF(addRequest);
	
			}
			
			String test = inStream.readUTF();
			System.out.println("client: " +test);
			
		
			while (clientAlive){
		/* Client options*/
			int option = 0;
			System.out.println("Options:");
			System.out.println("1. List all RFCs");
			System.out.println("2. Lookup for an RFC");
			System.out.println("3. Download an RFC");
			System.out.println("4. Leave the system");
			System.out.println("Enter the option:");
			
			option = Integer.parseInt(scanner.nextLine());
			outStream.writeInt(option);
			switch(option)
			{
			case 1: 
				 String listAllReq = frameListAllReq(hostname, uploadPort);
				 System.out.println("\nList All request: \n"+listAllReq);
				 outStream.writeUTF(listAllReq);
				 String listAllRes = inStream.readUTF();
				 System.out.println("\nList All response: \n"+listAllRes);
				break;
				
			case 2:
				 System.out.println("Enter the rfc number you want to lookup :");	
				 int rfc_num = Integer.parseInt(scanner.nextLine());
				 System.out.println("Enter the rfc title you want to lookup :");	
				 String rfc_Tit = scanner.nextLine();
				 String lookreq = frameLookupReq(rfc_num, hostname,uploadPort, rfc_Tit);
				 System.out.println("\nLookup request: \n"+lookreq);
				 outStream.writeUTF(lookreq);
				 String lookres = inStream.readUTF();
			     System.out.println("\nLookup response: \n"+lookres);
				break;
			case 3:
			
				 int opt = inStream.readInt();
//				 System.out.println("client in case: " + opt);	
				 System.out.println("Enter the rfc number you want to download :");	
				 int rfcNo = Integer.parseInt(scanner.nextLine());
				 System.out.println("Enter the rfc title you want to download :");	
				 String rfcTit = scanner.nextLine();
				
				 //refactor in progress
				 String lookupreq = frameLookupReq(rfcNo, hostname,uploadPort, rfcTit);
				 System.out.println("\nLookup request: \n"+lookupreq);
				 outStream.writeUTF(lookupreq);
				 int length = inStream.readInt();
				 System.out.println("No of peers with rfc "+rfcNo +"is :" +length);
			     String lookupres = inStream.readUTF();
			     System.out.println("\nLookup response: \n"+lookupres);
			     String[] parselookupres = lookupres.split("\n");
			     String line1 = parselookupres[1];
			     
			     if(!line1.equals(" "))
			     {
			    	 String[] parseline1 = line1.split(" ");
			    	 int size = parseline1.length;
			    	 String title = parseline1[2];
			    	 for(int k =3; k<=size-3; k++)
			    	 {
			    	  title += " "+parseline1[k];
			    	  
			    	 }
//			    	 System.out.println(title);
			    	 String host = parseline1[size-2];
			    	 int upPort =  Integer.parseInt(parseline1[size -1]);
			    	 System.out.println("Download RFC with hostname: " + host + "Port: " +upPort);

		        /* Starting this peer to create socket on above port number and hostname*/
		        
		        peerToPeerSocket = new Socket(host, upPort);
		        input_s = new DataInputStream(peerToPeerSocket.getInputStream());
		        output_s =new DataOutputStream(peerToPeerSocket.getOutputStream());
				InetAddress iAddress = InetAddress.getLocalHost();//need to go inside the run()
			    String myHostName = iAddress.getHostName();
		        
		        /*connection created .. Now two peers can talk over the peerToPeerSocket */
		      		        
		        String request = "GET RFC "+rfcNo+" P2P-CI/1.0"+"\n"+"Host: "+ myHostName +"\n"+ "OS: "+ System.getProperty("os.name") + "\n" ;
		        System.out.println("\nDownload request:\n"+request);
		        output_s.writeUTF(request);
		        int bytecount = input_s.readInt();
		        byte [] mybytearray  = new byte [1024];
			    int len = 0;
			    int bytcount = 1024;
			    File newRfcFile = new File(rfcPpathName+"/"+rfcNo+"_"+title+".txt");
			    FileOutputStream outFile = new FileOutputStream(newRfcFile);
			    InputStream is1 = peerToPeerSocket.getInputStream();
			    BufferedInputStream in2 = new BufferedInputStream(is1, 1024);
			    System.out.println("\nDownload Response"+"\n");
			    String response = input_s.readUTF();
			    System.out.println(response);
			    while ((len = in2.read(mybytearray, 0, 1024)) != -1) {
			      bytcount = bytcount + 1024;
			      outFile.write(mybytearray, 0, len);
			    }
			    BufferedReader br = new BufferedReader(new FileReader(newRfcFile));
			    String line = null;
			    while ((line = br.readLine()) != null) {
			      System.out.println(line);
			    }
			    System.out.println("RFC download successful");
			    String newaddreq = frameRFCAddReq(newRfcFile, hostname, uploadPort);
			    outStream.writeUTF(newaddreq);
			    is1.close();
			    in2.close();
			    outFile.close();
			    input_s.close();
			    output_s.close();
			    peerToPeerSocket.close();
			    
				}
				else
				{
					System.out.println("RFC notfound with any peer");
				}
					  
              
		        break;
				
			case 4:
				
				outStream.writeInt(uploadPort);
				outStream.writeUTF(hostname);
				inStream.readInt();
				System.out.println("Successfully left the system");
				outStream.close();
				inStream.close();
				peerSocket.close();
				clientAlive = false;
				System.exit(0);
				break;
		    default: 
		    	System.out.println("Not an valid option");
			}
			
		    
			
	} //end of while loop
	
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 	
		
	}
	public static String frameRFCAddReq(File f, String hostname, int portNo )
	{
		String[] arr1 ;
		int rfcno;
		String str = f.getName();
//		System.out.println(str);
		str= str.replace(".txt", "");
		String[] strArray = str.split("_");
//		System.out.println(strArray[0]);
//		if(strArray[0].contains("\\"))
//		{
//		 strArray[0]= strArray[0].replace("\\", "");		
//		}
		 rfcno = Integer.parseInt(strArray[0]);
		String rfctitle = strArray[1];
		String addReq = "ADD RFC "+rfcno+" P2P-CI/1.0" + "\n" + "Host: "+hostname+"\n" + "Port: " +portNo +"\n" + "Title: "+rfctitle;
		
		return addReq;
	}
	
	public static String frameLookupReq(int rfcNo, String hostname, int portNo, String rfcTit)
	{	
		String lookupReq = "LOOKUP RFC "+rfcNo+" P2P-CI/1.0" + "\n" + "Host: "+hostname+"\n" + "Port: " +portNo +"\n" + "Title: "+rfcTit;
		
		return lookupReq;
	}
	
	public static String frameListAllReq(String hostname, int portNo)
	{	
		String listAllReq = "LIST ALL" + " P2P-CI/1.0" + "\n" + "Host: "+hostname+"\n" + "Port: " +portNo;
		
		return listAllReq;
	}
	
}

package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CIServer {
	
	
	public static void main(String args[])
	{ 
		 ServerSocket  serverSocket = null;
		 Socket socket = null;
		 int count = 0; //remove if not used in PeerThread class
		 int portNumber =Integer.parseInt(args[0]);
		 
		//server needs to maintain two linked list: //1. list of currently active peers //2. index of RFC's 
		 ArrayList<Active_Peers>list_active_peer = new ArrayList<Active_Peers>();
		 ArrayList<RFC_Index>list_rfc_index = new ArrayList<RFC_Index>();
		 List<Active_Peers>active_peer = (List)Collections.synchronizedList(list_active_peer);
		 List<RFC_Index>rfc_index = (List) Collections.synchronizedList(list_rfc_index);
		 try {
			serverSocket = new ServerSocket(portNumber);// server creates a new serversocket object to listen to a specific port
					 
		 } 
		 catch(IOException e)
		 {
			 System.out.println("IOException: " + e); 
		 }
		 
		 while(true)
		 {
		 try{
			 socket = serverSocket.accept();//returns a new Socket object socket which is bound to the same local port and has its remote address and remote port set to that of the client
			 count ++;
			 /*start a new thread to handle each peer*/
			 Thread peerCon = new Thread (new P2SConHandler(socket,count,active_peer,rfc_index));
			 peerCon.start();			
		  }
		 catch (SocketTimeoutException s) {
				
				System.out.println("Socket timed out.");
		   }
		 catch(IOException e){
		 
			 System.out.println("IOException: " + e); 
		  }
		 
		 }
		
		
	}

}

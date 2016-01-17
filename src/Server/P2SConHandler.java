package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class P2SConHandler implements Runnable{
	
	Socket socket;
	int number;
	List<Active_Peers>active_peers;
	List<RFC_Index> rfc_index;
	DataInputStream inputStream;
	DataOutputStream outputStream;
	String host;
	int port;
	int rfcNo;
	String rfcTitle;
	RFC_Index new_rfc_index;
	Active_Peers new_active_peer;
	int noOfRFC;
	int count1 =1;
	int count2 =1;
	String addReq;
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock writeLock = readWriteLock.writeLock();

	
	boolean clientConnected = true;
	public  P2SConHandler(Socket newSocket, int number,List<Active_Peers> active_peer, List<RFC_Index> rfc_indices)
	{
		this.socket = newSocket;
		this.number = number;
		this.active_peers = active_peer;
		this.rfc_index = rfc_indices;
		

		
		try {
			inputStream = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			outputStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	@Override
		public void run() {
			// TODO Auto-generated method stub
			
		
		try {
			
			System.out.println(inputStream.readUTF());//will print the hello message from the client
			outputStream.writeUTF("Hello client");
			noOfRFC = inputStream.readInt();
			
			for(int i =0; i< noOfRFC; i++)
			{
				String addRequest = inputStream.readUTF();			
				String[] parsedReq = parseAddReq(addRequest);
				rfcNo = Integer.parseInt(parsedReq[0]);
				host = parsedReq[1];
				port = Integer.parseInt(parsedReq[2]);
				rfcTitle = parsedReq[3];
				
				System.out.println("rfcNo = "+rfcNo+" host = "+host+" port = "+port+" rfcTitle = "+rfcTitle); // comment it after testing
	
				ADD(rfcNo, rfcTitle,host);
			}
		/*May need to add this code to a method*/	
			new_active_peer = new Active_Peers(host, port);
			active_peers.add(new_active_peer);
			
	/* Printing the list for debug purpose */	
			for(RFC_Index rfc:rfc_index)
			{
				System.out.println("RFC NO:"+ rfc.getRFCNum() +":RFC title:"+ rfc.getRFCitle()+":hostname:" + rfc.getRFCHost());
				count1++;
			}
			count1 =1;
			
			for(Active_Peers a:active_peers)
			{
				System.out.println("Active peer:"+ count2 +":hostname:"+ a.getHostName()+":port:" + a.getPortNum());
				count2++;
			}
		
			count2 =1;
			String msg = "connection established";
			System.out.println(msg);
			outputStream.writeUTF(msg);
			
		
			while(clientConnected)
			{
			 int option = inputStream.readInt();
			 
			 switch(option){
			 case 1:
				String listAllReq = inputStream.readUTF();
				System.out.println("\n"+listAllReq);
				String[] parseListAllReq = listAllReq.split("\n");
				String firstLine = parseListAllReq[0];
				String[] parseFirstline = firstLine.split(" ");
				String ver = parseFirstline[2];
				 /*status codes */
				String stat_code;
				 if(rfc_index.size() == 0){
					 stat_code = "404 Not Found";
				    }
				    
				    else if(!ver.equals("P2P-CI/1.0")){
				    	stat_code = "505 P2P-CI Version Not Supported";
				    }
				    else {
				    	stat_code = "200 OK";
				    }
				//

				 String listAllRes = LIST(stat_code);
				 outputStream.writeUTF(listAllRes);
				 break;
			 case 2:
				 List<peer_list> Peers_with_rfc;
				 String lookRequest = inputStream.readUTF();
					String[] lookReqLines = lookRequest.split("\n");
					
					String line_1 = lookReqLines[0];
					System.out.println("\n"+line_1);
					String[] parseline_1 = line_1.split(" ");
					int rfcSrchNo = Integer.parseInt(parseline_1[2]);
					String vers = parseline_1[3];
					
					String line_2 = lookReqLines[1];
					System.out.println(line_2);
					String[] parseline_2 = line_2.split(" ");
					String lookhost = parseline_2[1];
					
					String line_3 = lookReqLines[2];
					System.out.println(line_3);
					String[] parseline_3 = line_3.split(" ");
					int lookport = Integer.parseInt(parseline_3[1]);
					
					String line_4 = lookReqLines[3];
					System.out.println(line_4);
					String[] parseline_4 = line_4.split(": ");
					String lookrfctitle = parseline_4[1];
					
//					System.out.println("RFC looked up  by host " +lookhost + "with port "+lookport + "is: "+rfcSrchNo);
					// Search for hostname in rfc_index
					 Peers_with_rfc = LOOKUP(rfcSrchNo);
					 int ln = Peers_with_rfc.size();
//					 System.out.println("Server: No of peers with RFC "+rfcSrchNo +"is: "+ln);
//					 outputStream.writeInt(ln);
					 String s_code;
					 /*status codes */
					 if(ln == 0){
						 s_code = "404 Not Found";
					    }
					    
					    else if(!vers.equals("P2P-CI/1.0")){
					    	s_code = "505 P2P-CI Version Not Supported";
					    }
					    else {
					    	s_code = "200 OK";
					    }
					 
					 String rfc_title = getRFCTitle(rfcSrchNo);
					 String lookresponse = frameLookupResponse(Peers_with_rfc, s_code, rfcSrchNo,rfc_title);
					 outputStream.writeUTF(lookresponse);
				 break;
			 case 3:
				
				 List<peer_list> Clients_with_rfc;
//				 System.out.println("Server in case: "+option);
				 outputStream.writeInt(option);
				 
				    String lookupRequest = inputStream.readUTF();
					String[] lookupReqLines = lookupRequest.split("\n");
					
					String l1 = lookupReqLines[0];
					System.out.println("\n"+l1);
					String[] parsel1 = l1.split(" ");
					int rfcSearchNo = Integer.parseInt(parsel1[2]);
					String version = parsel1[3];
					
					String l2 = lookupReqLines[1];
					System.out.println(l2);
					String[] parsel2 = l2.split(" ");
					String lookuphost = parsel2[1];
					
					String l3 = lookupReqLines[2];
					System.out.println(l3);
					String[] parsel3 = l3.split(" ");
					int lookupport = Integer.parseInt(parsel3[1]);
					
					String l4 = lookupReqLines[3];
					System.out.println(l4);
					String[] parsel4 = l4.split(": ");
					String lookuprfctitle = parsel4[1];
					
					System.out.println("\nRFC looked for by host " +lookuphost + "with port "+lookupport + "is: "+rfcSearchNo);
					// Search for hostname in rfc_index
					 Clients_with_rfc = LOOKUP(rfcSearchNo);
					 int length = Clients_with_rfc.size();
					 System.out.println("\nServer: No of peers with RFC "+rfcSearchNo +"is: "+length);
					 outputStream.writeInt(length);
					 String status_code;
					 /*status codes */
					 if(length == 0){
						 status_code = "404 Not Found";
					    }
					    
					    else if(!version.equals("P2P-CI/1.0")){
					    	status_code = "505 P2P-CI Version Not Supported";
					    }
					    else {
					    	status_code = "200 OK";
					    }
					 
					 String rfctitle = getRFCTitle(rfcSearchNo);
					 String lookupresponse = frameLookupResponse(Clients_with_rfc, status_code, rfcSearchNo,rfctitle);
//					 System.out.println("Server:Lookup response: \n"+lookupresponse);
					 outputStream.writeUTF(lookupresponse);
					 
					 String newAddReq = inputStream.readUTF();
					 String[] parseNewAddReq = parseAddReq(newAddReq);
					 int newrfcNo = Integer.parseInt(parseNewAddReq[0]);
					 String newrfchost = parseNewAddReq[1];
					 int newrfcport = Integer.parseInt(parseNewAddReq[2]);
					 String	newrfcTitle = parseNewAddReq[3];
					 ADD(newrfcNo, newrfcTitle,newrfchost);
				
				 break;
			 case 4:
				 int leave_port = inputStream.readInt();
				 String leave_host = inputStream.readUTF();
				 
				 deleteActivePeers(leave_host);
//				 List<RFC_Index>syn_rfc_index = (List) Collections.synchronizedList(rfc_index);	 
				 
				
				 deleteRFC(leave_host);
				 
				 /*print for debug */
                 System.out.println("\n Now rfc index size is: "+rfc_index.size());
				 for(RFC_Index rfc:rfc_index)
					{
					
						System.out.println("RFC NO:"+ rfc.getRFCNum() +":RFC title:"+ rfc.getRFCitle()+":hostname:" + rfc.getRFCHost());
						count1++;
					}
					count1 =1;
				
					System.out.println("\n Now Active peer list size is: "+active_peers.size());
					for(Active_Peers a:active_peers)
					{
						System.out.println("Active peer:"+ count2 +":hostname:"+ a.getHostName()+":port:" + a.getPortNum());
						count2++;
					}
				
					count2 =1;
						
				 /*print complete */
					
					outputStream.writeInt(option);
				
					inputStream.close();
					outputStream.close();
					socket.close();
					System.out.println("\nsocket for peer "+leave_host+" closed");
					clientConnected = false;
				 break;
				 default:
					 
			 }
			} //WHILE COMMENT
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public void deleteRFC(String leave_host)
	    {
	        writeLock.lock();
	        try
	        {
	        	for (Iterator<RFC_Index> iter = rfc_index.listIterator(); iter.hasNext(); ) 
	        	{
	        		RFC_Index rfc = iter.next();
	        		if(leave_host.equals(rfc.getRFCHost())) {
	        	        iter.remove();
	        	    }
	        	}
	        	
	        	
	        	
	         }
	
	        finally
	        {
	            writeLock.unlock();
	        }
	
	    }
	
	public void deleteActivePeers(String leave_host)
    {
        writeLock.lock();
        try
        {
        	for (Iterator<Active_Peers> iter = active_peers.listIterator(); iter.hasNext(); ) 
        	{
        		Active_Peers a = iter.next();
        		if(leave_host.equals(a.getHostName())) {
        	        iter.remove();
        	    }
        	}
        	
         }

        finally
        {
            writeLock.unlock();
        }

    }

	public List<peer_list> LOOKUP(int rfcNumber)
	{
		ArrayList<peer_list>client_list_1 = new ArrayList<peer_list>();
		List<peer_list> client_list_2 = (List)Collections.synchronizedList(client_list_1);
		String rfcHostName;
		String rfcTitle;
		int rfcUploadPort = 0;
		peer_list rfc_peer_list = new peer_list();
		
		for(RFC_Index rfc:rfc_index)
		{
		     if(rfcNumber == rfc.getRFCNum())
		     {
		    	 
		    	 rfcHostName = rfc.getRFCHost();
		    	 for(Active_Peers peers: active_peers)
		    	 {
		    	
		    		 if(rfcHostName.equals(peers.getHostName()))
		    		 {
		    			 rfcUploadPort = peers.getPortNum();
		    		 }
		    		 
		    	 }
		    	 rfc_peer_list.setHost(rfcHostName);
		    	 rfc_peer_list.setPort(rfcUploadPort);
		    	 client_list_2.add(rfc_peer_list);
		     }
		     
		     
		    	
		}  
//		System.out.println("test: lookup: After for loop:client_list ->"+client_list_2.size());  
		return client_list_2;
	}
	
	public String getRFCTitle(int rfcNumber)
	{
		String rfcTitle = null;
		for(RFC_Index rfc:rfc_index)
		{
		     if(rfcNumber == rfc.getRFCNum())
		     {
		    	 rfcTitle = rfc.getRFCitle();
		     }
		 }
		return rfcTitle;
		
	
	}
	
	public void ADD(int rfcNo, String rfcTitle, String host )
	{
		new_rfc_index = new RFC_Index(rfcNo, rfcTitle,host);
		rfc_index.add(new_rfc_index);
	}
	
	public String frameLookupResponse(List<peer_list>Clients_with_rfc, String status_code, int rfcno, String rfctitle)
	{
		String response = "P2P-CI/1.0 " +status_code + "\n";
		if(status_code.equals("200 OK"))
		{
		 for(peer_list client_list :Clients_with_rfc)
		 {
		  String host = client_list.getHost();
//		  System.out.println("Server: Sending port :" +client_list.getPort());
		  int port = client_list.getPort();	
//		  System.out.println("Server: lookup port :" +port);
		  response += "RFC " +rfcno+ " "+rfctitle+ " "+host+" "+port+ "\n";		
		 }
		}
		else
		{
			response += " ";
		}
		 return response;
	}
	
	public String LIST(String status_code)
	{
		int rfcno =0; ;
		String rfctitle = "";
		String host = "";
		int port = 0;
		
		String response = "P2P-CI/1.0 " +status_code + "\n";
		if(status_code.equals("200 OK"))
		{
			for(RFC_Index rfc:rfc_index)
			{
				
//				System.out.println("RFC NO:"+ rfc.getRFCNum() +":RFC title:"+ rfc.getRFCitle()+":hostname:" + rfc.getRFCHost());
//				count1++;
				rfcno = rfc.getRFCNum();
				rfctitle = rfc.getRFCitle();
				host = rfc.getRFCHost();
				for(Active_Peers a:active_peers)
				{
//					System.out.println("Active peer:"+ count2 +":hostname:"+ a.getHostName()+":port:" + a.getPortNum());
//					count2++;
					if(host.equals(a.getHostName()))
						port = a.getPortNum();
				}
				response += "RFC " +rfcno+ " "+rfctitle+ " "+host+" "+port+ "\n";
			}
		
		 }
		
		else
		{
			response += " ";
		}
		 return response;
	}
	
	public String[] parseAddReq(String addRequest)
	{
		String parsedMsg[] = {"", "", "", ""};
		
		String[] addReqLines = addRequest.split("\n");
		String line1 = addReqLines[0];
		System.out.println(line1);
		String[] parseline1 = line1.split(" ");
//		rfcNo = Integer.parseInt(parseline1[2]);
		parsedMsg[0] = parseline1[2];
		
		String line2 =  addReqLines[1];
		System.out.println(line2);
		String[] parseline2 = line2.split(" ");
//		host = parseline2[1];
		parsedMsg[1] = parseline2[1];
		
		String line3 =  addReqLines[2];
		System.out.println(line3);
		String[] parseline3 = line3.split(" ");
//      port = 	parseline3[1];
		parsedMsg[2]  = parseline3[1];
		
		
		String line4 =  addReqLines[3];
		System.out.println(line4);
		String[] parseline4 = line4.split(": ");
//		rfctitle = parseline4[1];
		parsedMsg[3]  = parseline4[1];
		
		return parsedMsg;
	}
	
}

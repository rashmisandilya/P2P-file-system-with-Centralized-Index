package Peers;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class P2PConHandler implements Runnable{
	
	ServerSocket serversocket;
	 Socket psocket = null;
	 DataInputStream in_s;
	 DataOutputStream out_s;
	 String rfcPath;
	
	
	public  P2PConHandler(ServerSocket serversocket, String rfcPath)
	{
		this.serversocket = serversocket;
		this.rfcPath = rfcPath;
			
	}

	@Override
	public void run() {
		
		String res = "";
		DateFormat dateFormat = new SimpleDateFormat("E, d MMM y HH:mm:ss z");
		Date date = new Date();
		// TODO Auto-generated method stub
		while(true)
		 {
		 try{
			 psocket = serversocket.accept();
			 in_s = new DataInputStream(psocket.getInputStream());
			 out_s = new DataOutputStream(psocket.getOutputStream());
			 
			 String req = in_s.readUTF();
//			 p2p_outStream.writeUTF(req);
			 System.out.println("Request"+"\n");
			 System.out.println(req);
			 String[] reqparse = req.split(" ");
			 final String requestedRFC = reqparse[2];
			 String version = reqparse[3];
//			 System.out.println(version);
//			 p2p_outStream.writeUTF(requestedRFC);
			 String status_code;
			 
			 File dir = new File(rfcPath);
			 File[] rfcFiles = dir.listFiles(new FilenameFilter() {
			     public boolean accept(File dir, String name) {
			         return name.startsWith(requestedRFC);
			     }
			 });
			 
			 File rfcFile = rfcFiles[0];
//			 System.out.println("rfcfile name is:"+rfcFile.getName());
			 int length = (int) rfcFile.length();
//			 System.out.println("server : "+length);
			 out_s.writeInt(length);
			
	////////////////////////  All is well ///////////////////////////
			 /*status codes */
			 if(!rfcFile.exists()){
				 status_code = "404 Not Found";
			    }
			    
			    else if(version != "P2P-CI/1.0"){
			    	status_code = "505 P2P-CI Version Not Supported";
			    }
			    else {
			    	status_code = "200 OK";
			    }
			 // TODO: NEED TO IMPLEMENT 400-BAD REQUEST
			 /*1st set of solution*/	
			 
			
			 OutputStream outS = psocket.getOutputStream();
			 byte[] byteArray = new byte[1024];
			 FileInputStream fis = new FileInputStream(rfcFile);
			 BufferedOutputStream bout = new BufferedOutputStream(outS,1024);
			 int i = 0;
			 int bytecount  = 1024;
//			 bis.read(byteArray,0,byteArray.length);
//			 outS.write(byteArray,0,byteArray.length);
//			 System.out.println("server: Sending file");
//			 p2p_outStream.writeUTF("server: Sending file");
			 
			 res = "P2P-CI/1.0 " +status_code + "\n" +"Date: " +dateFormat.format(date)+ " "+ "\n"+ "OS: "+ System.getProperty("os.name")
					 + "\n" + "Last-Modified: " +dateFormat.format(rfcFile.lastModified())+ " "+"\n"+"Content-Length: "+ length +"\n"
					 + "Content-Type: text/text"+ "\n";
			 
			 out_s.writeUTF(res+"\n");
			 while ((i = fis.read(byteArray, 0, 1024)) != -1) {
				 bytecount = bytecount + 1024;
			      bout.write(byteArray, 0, i);
			      bout.flush();
			    }
			    psocket.shutdownOutput(); /* important */
//			    System.out.println("Bytes Sent :" + bytecount);
			    bout.close();
			    fis.close();
	
			   /*1st set of solution*/
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

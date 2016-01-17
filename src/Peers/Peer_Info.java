package Peers;

public class Peer_Info {
	
	private Integer port;
	private String host;
	private String rfcpath;
	
	public Integer getRFCport()
	{
		return port;
	}

	public void setRFCport(int peerport)
	{
		port = peerport;
	}
	

	public void setRFChost(String peerhost)
	{
		host = peerhost;
	}
	
	public String getRFCHost()
	{
		return host;
	}

	public void setRFCpath(String path)
	{
		rfcpath = path;
	}
	
	public String getRFCpath()
	{
		return rfcpath;
	}

}

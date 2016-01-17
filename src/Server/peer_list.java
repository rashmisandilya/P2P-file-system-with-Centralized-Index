package Server;

public class peer_list {

		public Integer uploadPort =0;;
		public String hostname = null;
		
		public peer_list(){
		
			
		}
		
		public String getHost()
		{
			return hostname;
		}

		public void setHost(String host)
		{
			hostname = host;
		}
		
		public Integer getPort()
		{
			return uploadPort;
		}

		public void setPort(Integer port)
		{
			uploadPort = port;
		}
	}




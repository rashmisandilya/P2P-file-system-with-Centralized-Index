package Server;

public class Active_Peers {
	
	private String hostName;
	private Integer portNum;
	
	    public Active_Peers(String host, Integer port)
	    {
		 hostName = host;
		 portNum = port;
        }
		
		public String getHostName()
		{
			return hostName;
		}
		
		public void setHostName(String hName)
		{
			hostName = hName;
		}
		
		public Integer getPortNum()
		{
			return portNum;
		}

		public void setPortNum(Integer pNo)
		{
			portNum = pNo;
		}

}

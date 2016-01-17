package Server;

public class RFC_Index {
	
	private Integer rfcNo;
	private String rfcTitle;
	private String rfcHost;
	
	
	public RFC_Index(Integer rfcNum, String title, String host)
	{
		rfcNo = rfcNum;
		rfcTitle = title;
		rfcHost = host;
	}
	
	public Integer getRFCNum()
	{
		return rfcNo;
	}

	public void setRFCNum(Integer num)
	{
		rfcNo = num;
	}
	
	public String getRFCitle()
	{
		return rfcTitle;
	}

	public void setRFCTitle(String title)
	{
		rfcTitle = title;
	}
	
	public String getRFCHost()
	{
		return rfcHost;
	}

	public void setRFCHost(String host)
	{
		rfcHost = host;
	}
}

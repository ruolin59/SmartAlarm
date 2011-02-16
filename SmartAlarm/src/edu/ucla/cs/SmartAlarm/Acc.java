package edu.ucla.cs.SmartAlarm;
import java.util.ArrayList;

public class Acc {
	private String m_raw;
	private String m_data;
	
	//cleaned up data, may have 2 to 3 axes
	private ArrayList<Integer> m_xDat;
	private ArrayList<Integer> m_yDat;
	private ArrayList<Integer> m_zDat;	//may not be used
	
	private String m_id;
	private final int LENH = 5;
	private int m_length;
	private String m_format;
	private boolean axis3;	//Does the data have 3 axes or 2
	
	//Final Constants
	private final int HEX = 16;	//for conversion from hex strings to ints
	private final String ID3X = "56";	//If the id is 0x56
	
	//Constructor
	public Acc(String raw){
		m_raw = raw;
		axis3 = false;	//initially set to false
		parse();
	}
	
	//getter methods
	public String getData(){
		return m_data;
	}
	
	public ArrayList<Integer> getX(){
		return m_xDat;
	}
	
	public ArrayList<Integer> getY(){
		return m_yDat;
	}
	
	public ArrayList<Integer> getZ(){
		if (axis3)
			return m_zDat;
		return null;	//this one may not exist
	}
	
	public String getID(){
		return m_id;
	}
	
	public int getlenH(){
		return LENH;
	}
		
	public int getlen(){
		return m_length;
	}
	
	public String getFormat(){
		return m_format;
	}
	
	private void parse(){
		m_id = m_raw.substring(0,2);
		if (m_id.compareTo(ID3X) == 0)
			axis3 = true;
		
		String len = m_raw.substring(2,6);
		m_length = Integer.parseInt(len, HEX) - LENH;
		m_format = m_raw.substring(6,8);
		m_data = m_raw.substring(LENH*2,(LENH+m_length)*2);
		
		int datLen;	//presize the arrayList for optimization
		if (axis3){
			datLen = m_length*2/3;
			m_zDat = new ArrayList<Integer>(datLen);	//need z axis
		}
		else
			datLen = m_length;

		m_xDat = new ArrayList<Integer>(datLen);
		m_yDat = new ArrayList<Integer>(datLen);
		
		//Sort all the parts of the ACC data into its approriate arrayList
		int i = 0;
		while (i < m_data.length()){
			String oneNum = m_data.substring(i,i+2);
			m_xDat.add(Integer.parseInt(oneNum, HEX));
			i+=2;
			oneNum = m_data.substring(i,i+2);
			m_yDat.add(Integer.parseInt(oneNum, HEX));
			i+=2;
			
			if (axis3){
				oneNum = m_data.substring(i,i+2);
				m_zDat.add(Integer.parseInt(oneNum, HEX));
				i+=2;
			}
		}
			
	}
}

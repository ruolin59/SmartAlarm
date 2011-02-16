package edu.ucla.cs.SmartAlarm;
import java.util.ArrayList;

public class Ecg {
	private String m_raw;
	private String m_datah;
	private ArrayList<Integer> m_data;
	private String m_id;
	private final int LENH = 5;	//header length, fixed
	private int m_length;	//data length, excludes header length
	private String m_format;
	private final int HEX = 16;

	// Constructor
	public Ecg(String raw){
		m_raw = raw;
		parse();
	}
		
	// Get methods
	public ArrayList<Integer> getData(){
		return m_data;
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
		
	// Parses raw data
	private void parse(){
		m_id = m_raw.substring(0,2);	//first byte is the id
		String len = m_raw.substring(2,6);	//second to third byte is the total length
		m_length = Integer.parseInt(len, HEX) - LENH;	//length given includes header length
		m_format = m_raw.substring(6,8);	//fourth byte is the format
		m_datah = m_raw.substring(LENH*2,(LENH+m_length)*2);	//fifth byte is not used, rest are all data
		
		m_data = new ArrayList<Integer>(m_length*2);
		
		//changing the data from hex string to arraylist of integers
		for (int i = 0; i < m_datah.length(); i+=2){
			String oneNum = m_datah.substring(i,i+2);	//take 2 chars at a time, giving a number
			m_data.add(Integer.parseInt(oneNum, HEX));
		}
	}
}

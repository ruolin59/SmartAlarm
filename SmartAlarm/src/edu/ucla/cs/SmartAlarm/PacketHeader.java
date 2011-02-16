package edu.ucla.cs.SmartAlarm;

public class PacketHeader {
	private String synBytes;
	private int battLvl;	//percentage
	private int seqNum;
	private final int HEX = 16;	//for conversion from hex strings to intspktH
//	private final int FULLBAT = 200;
	
	
	public PacketHeader(String raw){
		synBytes = raw.substring(0,4);
		String oneNum  = raw.substring(4,6);
		battLvl = (Integer.parseInt(oneNum,HEX));
		String seq = raw.substring(6,10);
		seqNum = Integer.parseInt(seq,HEX) >> 4;	//seqNum is the top 12 bits
	}
	
	public int getBatt(){
		return battLvl;
	}
	
	public String getSyn(){
		return synBytes;
	}
	
	public int getSeq(){
		return seqNum;
	}
}

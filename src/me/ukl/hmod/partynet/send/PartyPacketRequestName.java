package me.ukl.hmod.partynet.send;


public class PartyPacketRequestName extends PartyPacket {
	
	public String partialName;
	
	public PartyPacketRequestName(String pname) {
		super("GET_NAME");
		this.partialName = pname;
	}

}

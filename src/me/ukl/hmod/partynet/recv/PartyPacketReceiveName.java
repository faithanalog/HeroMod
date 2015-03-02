package me.ukl.hmod.partynet.recv;

public class PartyPacketReceiveName extends PartyPacketReceive {
	
	public String fullName;
	
	public PartyPacketReceiveName(String fullName) {
		this.fullName = fullName;
	}
	
	@Override
	public int getType() {
		return PartyPacketReceive.NAME;
	}

}

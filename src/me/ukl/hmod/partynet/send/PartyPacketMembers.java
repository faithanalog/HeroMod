package me.ukl.hmod.partynet.send;

import java.util.List;

public class PartyPacketMembers extends PartyPacket {
	
	public List<String> partyMembers;
	
	public PartyPacketMembers(List<String> mems) {
		super("MEMS");
		this.partyMembers = mems;
	}

}

package me.ukl.hmod.partynet.send;

import java.util.List;

public class PartyPacketJoin extends PartyPacket {
	
	public String name;
	public int x, y, z, hp, mana;
	public List<String> partyMembers;
	
	public PartyPacketJoin() {
		super("JOIN");
	}

}

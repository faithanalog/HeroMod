package me.ukl.hmod.partynet.send;

public class PartyPacketHealth extends PartyPacket {
	
	public int hp;
	
	public PartyPacketHealth(int hp) {
		super("HP");
		this.hp = hp;
	}

}

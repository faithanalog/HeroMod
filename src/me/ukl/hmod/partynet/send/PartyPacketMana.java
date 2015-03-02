package me.ukl.hmod.partynet.send;

public class PartyPacketMana extends PartyPacket {
	
	public int mana;
	
	public PartyPacketMana(int mana) {
		super("MANA");
		this.mana = mana;
	}

}

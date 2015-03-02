package me.ukl.hmod.partynet.recv;

public class PartyPacketReceiveMana extends PartyPacketReceive {
	
	public int mana;
	
	public PartyPacketReceiveMana(int mana) {
		this.mana = mana;
	}
	
	@Override
	public int getType() {
		return PartyPacketReceive.MANA;
	}

}

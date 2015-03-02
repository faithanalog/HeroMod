package me.ukl.hmod.partynet.recv;

public class PartyPacketReceiveHealth extends PartyPacketReceive {
	
	public int hp;
	
	public PartyPacketReceiveHealth(int hp) {
		this.hp = hp;
	}
	
	@Override
	public int getType() {
		return PartyPacketReceive.HEALTH;
	}

}

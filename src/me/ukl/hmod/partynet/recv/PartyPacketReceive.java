package me.ukl.hmod.partynet.recv;

public abstract class PartyPacketReceive {
	
	public static final int HEALTH = 0;
	public static final int MANA = 1;
	public static final int POSITION = 2;
	public static final int NAME = 3;
	
	public String name;
	
	public abstract int getType();

}

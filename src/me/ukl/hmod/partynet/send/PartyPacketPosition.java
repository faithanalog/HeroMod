package me.ukl.hmod.partynet.send;

public class PartyPacketPosition extends PartyPacket {
	
	int x, y, z;
	
	public PartyPacketPosition(int x, int y, int z) {
		super("POS");
		this.x = x;
		this.y = y;
		this.z = z;
	}

}

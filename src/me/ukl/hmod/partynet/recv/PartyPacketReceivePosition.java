package me.ukl.hmod.partynet.recv;

public class PartyPacketReceivePosition extends PartyPacketReceive {
	
	public int x;
	public int y;
	public int z;
	
	public PartyPacketReceivePosition(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int getType() {
		return PartyPacketReceive.POSITION;
	}

}

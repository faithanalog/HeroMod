package me.ukl.hmod;

import static com.mumfrey.liteloader.gl.GL.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import me.ukl.hmod.partynet.PartyNetHandler;
import me.ukl.hmod.partynet.recv.PartyPacketReceive;
import me.ukl.hmod.partynet.recv.PartyPacketReceiveHealth;
import me.ukl.hmod.partynet.recv.PartyPacketReceiveMana;
import me.ukl.hmod.partynet.recv.PartyPacketReceiveName;
import me.ukl.hmod.partynet.recv.PartyPacketReceivePosition;
import me.ukl.hmod.partynet.send.PartyPacketJoin;
import me.ukl.hmod.partynet.send.PartyPacketLeave;
import me.ukl.hmod.partynet.send.PartyPacketMana;
import me.ukl.hmod.partynet.send.PartyPacketMembers;
import me.ukl.hmod.partynet.send.PartyPacketPosition;
import me.ukl.hmod.partynet.send.PartyPacketRequestName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

public class PartyFrame {
	
	private static final int WIDTH = 70;
	private static final int HEIGHT = 32;
	
	public List<PartyMember> members = new ArrayList<PartyMember>(9);
	public Set<String> allMemNames = new HashSet<String>(10);
	
	public PartyNetHandler netHandler;
	public PartyDataSender dataSender;
	
	public PartyFrame(ManaBar mbar) {
		this.netHandler = new PartyNetHandler();
		dataSender = new PartyDataSender(this, mbar);
	}
	
	public void tick() {
		if (ScoreboardUtil.isHerocraft()) {
			Scoreboard sb = ScoreboardUtil.scoreboard();
			ScoreObjective obj = ScoreboardUtil.hcBoard();
			List<Score> scores = (List<Score>) sb.getSortedScores(obj);
			
			List<String> existingNames = new ArrayList<String>(16);
			
			List<EntityPlayer> servPlayers = Minecraft.getMinecraft().theWorld.playerEntities;
			
			boolean reading = false;
			
			for (int i = scores.size() - 1; i >= 0; i--) {
				Score s = scores.get(i);
				String nm = s.getPlayerName();
				//§a§lParty§f:
				if (nm.equals("§a§lParty§f:")) {
					reading = true;
				} else if (reading) {
					if (nm.trim().isEmpty())
						break;
					nm = StringUtils.stripControlCodes(nm).trim();
					String[] args = nm.split("[ ]+");
					if (Minecraft.getMinecraft().thePlayer.getName().startsWith(args[0])) {
						allMemNames.add(args[0]);
						continue;
					}
					allMemNames.add(args[0]);
					existingNames.add(args[0]);
					PartyMember mem = getMember(args[0]);
					
					if (mem == null) {
						mem = new PartyMember(args[0]);
						members.add(mem);
					}
					mem.lastSeen = 0;
					if (!mem.isUsingMod) {
						mem.positionKnown = false;
						for (EntityPlayer p : servPlayers) {
							if (p.getName().startsWith(args[0])) {
								mem.x = (int)p.posX;
								mem.y = (int)p.posY;
								mem.z = (int)p.posZ;
								mem.positionKnown = true;
							}
						}
					}
					try {
						mem.health = Integer.parseInt(args[1]);
					} catch (Exception e) {
						mem.health = 0;
					}
				}
			}
			
			//Write packet data
			dataSender.tick();
			
			//Handle packet-based data
			PartyPacketReceive pkt;
			while((pkt = netHandler.poll()) != null) {
				if (pkt.name == null) {
					continue;
				}
				PartyMember mem = getMember(pkt.name);
				if (mem == null)
					continue;
				if (!mem.isUsingMod) {
					mem.isUsingMod = true;
					netHandler.send(new PartyPacketRequestName(mem.name));
				}
				switch (pkt.getType()) {
				case PartyPacketReceive.HEALTH:
					mem.health = ((PartyPacketReceiveHealth)pkt).hp;
					break;
				case PartyPacketReceive.MANA:
					mem.mana = ((PartyPacketReceiveMana)pkt).mana;
					break;
				case PartyPacketReceive.POSITION:
					PartyPacketReceivePosition pos = (PartyPacketReceivePosition)pkt;
					mem.x = pos.x;
					mem.y = pos.y;
					mem.z = pos.z;
					mem.positionKnown = true;
					break;
				case PartyPacketReceive.NAME:
					mem.displayName = ((PartyPacketReceiveName)pkt).fullName;
					break;
				}
			}
			
			Iterator<PartyMember> itr = members.iterator();
			while (itr.hasNext()) {
				PartyMember mem = itr.next();
				if (++mem.lastSeen == 60) {
					itr.remove();
					allMemNames.remove(mem.name);
				}
			}
		} else {
			members.clear();
			allMemNames.clear();
		}
	}
	
	public PartyMember getMember(String name) {
		for (PartyMember m : members) {
			if (m.name.startsWith(name) || name.startsWith(m.name)) {
				return m;
			}
		}
		return null;
	}
	
	public void render() {
		
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer fr = mc.fontRendererObj;
		
		int memNum = 0;
		for (PartyMember mem : members) {
			int dispX = (memNum % 3) * (WIDTH + 2);
			int dispY = (memNum / 3) * (HEIGHT + 2);
			memNum++;
			
			glTranslatef(dispX, dispY, 0f);
			
			//Shading beneath name of player
			GuiScreen.drawRect(0, 0, WIDTH, 8, 0xE0000000);
			
			//Draw name of player
			String dispName = fr.trimStringToWidth(mem.displayName, WIDTH);
			fr.drawString(dispName, 0, 0, 0xFFFFFF);
			
			//Draw health bar below health
			int hpFill = mem.health * WIDTH / mem.maxHealth;
			int hpLoss = WIDTH - hpFill;
			
			GuiScreen.drawRect(0, 8, hpFill, 16, 0xE0C00000);
			GuiScreen.drawRect(hpFill, 8, hpFill + hpLoss, 16, 0xE0400000);
			
			//Draw health text
			int hp = Math.min(mem.health, mem.maxHealth);
			int maxhp = mem.maxHealth;

			
			int hpTextCol = 0xFFFF00;
			double pct = hp / (double)maxhp;
			if (pct >= 0.75) {
				hpTextCol = 0xFFFF55;
			} else if (pct >= 0.5) {
				hpTextCol = 0xFFCF40;
			} else if (pct >= 0.25) {
				hpTextCol = 0xFFA028;
			} else {
				hpTextCol = 0xFF7018;
			}
			if (mem.isPct) {
				int dispHP = hp * 100 / maxhp;
				fr.drawString(dispHP + "%", 0, 8, hpTextCol);
			} else {
				fr.drawString(hp + "/" + maxhp, 0, 8, hpTextCol);
			}
			
			//Draw mana bar below mana
			int manaFill = mem.mana * WIDTH / mem.maxMana;
			int manaLoss = WIDTH - manaFill;
			GuiScreen.drawRect(0, 16, manaFill, 24, 0xE00000C0);
			GuiScreen.drawRect(manaFill, 16, manaFill + manaLoss, 24, 0xE0000040);
			
			//Draw mana text
			int manaTextCol = 0xFFFF00;
			pct = mem.mana / (double)mem.maxMana;
			if (pct >= 0.75) {
				manaTextCol = 0xFFFF55;
			} else if (pct >= 0.5) {
				manaTextCol = 0xFFCF40;
			} else if (pct >= 0.25) {
				manaTextCol = 0xFFA028;
			} else {
				manaTextCol = 0xFF7018;
			}
			int dispMana = mem.mana * 100 / mem.maxMana;
			fr.drawString(dispMana + "%", 0, 16, manaTextCol);
			
			//Shading beneath coordinates
			GuiScreen.drawRect(0, 24, WIDTH, 32, 0xE0000000);
			if (mem.positionKnown) {
				fr.drawString(mem.x + ", " + mem.z, 0, 24, 0xFFFFFF);
			} else {
				fr.drawString("???, ???", 0, 24, 0xFFFFFF);
			}
			
			glTranslatef(-dispX, -dispY, 0f);
		}
		
	}

}

class PartyMember {
	
	private static Random rnd = new Random();
	
	public String name;
	public String displayName;
	public int health;
	public int maxHealth = 100; //Avoid divide-by-zero errs
	public int mana;
	public int maxMana = 100;
	public int x, y, z;
	
	public boolean positionKnown = false;
	public boolean isPct = true;
	public boolean isUsingMod = false;
	
	public int lastSeen = 0;
	
	public PartyMember(String name) {
		this.name = name;
		this.displayName = name;
	}
}

class PartyDataSender {
	
	private PartyFrame frm;
	private ManaBar manaBar;
	
	private int lastMana;
	private int lastX;
	private int lastY;
	private int lastZ;
	private Set<String> lastMems = new HashSet<String>(10);
	
	private boolean inParty;
	
	public PartyDataSender(PartyFrame frm, ManaBar manaBar) {
		this.frm = frm;
		this.manaBar = manaBar;
	}
	
	public void tick() {
		if (!inParty && !frm.members.isEmpty()) {
			inParty = true;
			PartyPacketJoin join = new PartyPacketJoin();
			
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayerSP player = mc.thePlayer;

			int mana = manaBar.mana * 100 / manaBar.maxMana;
			lastMana = join.mana = mana;
			lastX = join.x = (int)player.posX;
			lastY = join.y = (int)player.posY;
			lastZ = join.z = (int)player.posZ;
			join.name = player.getName();
			
			lastMems.clear();
			lastMems.addAll(frm.allMemNames);
			join.partyMembers = new ArrayList<String>(frm.allMemNames);
			
			frm.netHandler.send(join);
			return;
		} else if (inParty && frm.members.isEmpty()) {
			inParty = false;
			frm.netHandler.send(new PartyPacketLeave());
			return;
		}
		if (!inParty) {
			return;
		}
		
		int mana = manaBar.mana * 100 / manaBar.maxMana;
		if (lastMana != mana) {
			frm.netHandler.send(new PartyPacketMana(mana));
			lastMana = mana;
		}
		
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;
		
		int x = (int)player.posX;
		int y = (int)player.posY;
		int z = (int)player.posZ;
		if (x != lastX || y != lastY || z != lastZ) {
			lastX = x;
			lastY = y;
			lastZ = z;
			frm.netHandler.send(new PartyPacketPosition(x, y, z));
		}
		
		if (!lastMems.equals(frm.allMemNames)) {
			List<String> copy = new ArrayList<String>(frm.allMemNames);
			frm.netHandler.send(new PartyPacketMembers(copy));
			lastMems.clear();
			lastMems.addAll(frm.allMemNames);
		}
	}
	
}

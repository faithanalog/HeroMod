package me.ukl.hmod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class BoostTracker {
	
	public long boostExpire;
	public int boostPercent;
	
	private Pattern boostMsgPat;
	private Pattern boostMsgPat2;
	private Pattern hrsPat;
	private Pattern minsPat;
	private Pattern secsPat;
	
	public BoostTracker() {
		boostMsgPat = Pattern.compile("§r§7The §r§f(\\d+)§r§7% boost expires in §r§f(.*) §r");
		boostMsgPat2 = Pattern.compile("§r§d\\[Server§r§d\\] .* just enabled (\\d+)% bonus exp for (\\d+.)!§r");
		hrsPat = Pattern.compile("(\\d+) hours");
		minsPat = Pattern.compile("(\\d+) minutes");
		secsPat = Pattern.compile("(\\d+) seconds");
	}
	
	public void parseBoost(String message) {
		Matcher boostMsg = boostMsgPat.matcher(message);
		if (boostMsg.matches()) {
			Matcher hrs = hrsPat.matcher(message);
			Matcher mins = minsPat.matcher(message);
			Matcher secs = secsPat.matcher(message);
			
			long time = 0L;
			if (hrs.find()) {
				time += Long.parseLong(hrs.group(1)) * 60 * 60 * 1000L; 
			}
			if (mins.find()) {
				time += Long.parseLong(mins.group(1)) * 60 * 1000L; 
			}
			if (secs.find()) {
				time += Long.parseLong(secs.group(1)) * 1000L; 
			}
			boostExpire = Minecraft.getSystemTime() + time;
			boostPercent = Integer.parseInt(boostMsg.group(1));
		}
		
		//Message for the start of a boost
		boostMsg = boostMsgPat2.matcher(message);
		if (boostMsg.matches()) {
			boostPercent = Integer.parseInt(boostMsg.group(1));
			
			String timeStr = boostMsg.group(2);
			int time = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
			char timeC = timeStr.charAt(timeStr.length() - 1);
			long timeMillis = time * 1000L;
			switch(timeC) {
			case 'd':
				timeMillis *= 24;
				//Fall down to hours/minutes
			case 'h':
				timeMillis *= 60;
				//Fall down to minutes
			case 'm':
				timeMillis *= 60;
			}
			boostExpire = Minecraft.getSystemTime() + timeMillis;
		}
	}
	
	public void onJoin() {
		boostExpire = 0;
		boostPercent = 0;
	}
	
	public void render() {
		long time = boostExpire - Minecraft.getSystemTime();
		if (time < 0) {
			return;
		}
		
		String timeStr = "";
		time /= 1000L; //Seconds
		time /= 60L; //Minutes
		long m = (time % 60L);
		long h = (time / 60L);
		timeStr = String.format("%d%% Exp: %dh %dm", boostPercent, h, m);
		

		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		
		FontRenderer fr = mc.fontRendererObj;
		
		int x = 0;
		int y = res.getScaledHeight() - 12;
		int w = fr.getStringWidth(timeStr) + 4;
		
		GuiScreen.drawRect(x, y, x + w, y + 12, 0xFFAA00AA);
		fr.drawStringWithShadow(timeStr, x + 2, y + 2, 0xFFAA00);
		
	}

}

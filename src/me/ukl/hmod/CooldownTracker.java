package me.ukl.hmod;

import static com.mumfrey.liteloader.gl.GL.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

public class CooldownTracker implements Comparator<Cooldown> {
	
	private Map<String, Cooldown> cooldowns = new HashMap<String, Cooldown>();
	
	public void tick() {
		Scoreboard sb = ScoreboardUtil.scoreboard();
		ScoreObjective obj = ScoreboardUtil.hcBoard();
		List<Score> scores = (List<Score>) sb.getSortedScores(obj);
		long now = Minecraft.getSystemTime();
		
		boolean reading = false;
		
		//Iterate backwards because we want to start with Cooldowns (will be highest score)
		for (int i = scores.size() - 1; i >= 0; i--) {
			Score score = scores.get(i);
			String nm = score.getPlayerName();
			if (nm.equals("§9§lCooldowns§f:")) {
				reading = true;
			} else if (reading) {
				if (nm.trim().isEmpty())
					break;
				String[] args = StringUtils.stripControlCodes(nm).split(" +");
				if (args[0].equals("Global")) {
					continue;
				}
				if (args.length == 2) {
					String name = args[0];
					String time = args[1];
					long timeMillis;
					try {
						char end = time.charAt(time.length() - 1);
						if (Character.isDigit(end)) {
							//The text is floored, so we round up a bit to be safe
							timeMillis = Long.parseLong(time) * 1000L + 775L;
						} else {
							timeMillis = Long.parseLong(time.substring(0, time.length() - 1)) * 1000L;
							switch(end) {
							case 'd':
								timeMillis *= 24;
								//Fall down to hours/minutes
							case 'h':
								timeMillis *= 60;
								//Fall down to minutes
							case 'm':
								timeMillis *= 60;
							}
						}
					} catch (NumberFormatException e) {
						continue;
					}
					
					//If cooldown is longer than 3 minutes, ignore it
					if (timeMillis > 3 * 60 * 1000) {
						continue;
					}
					
					Cooldown cd = cooldowns.get(name);
					
					//If the CD has expired, and the scoreboard says 3 seconds or more
					//then assume it's a new cooldown
					if (cd != null && (cd.expireTime - now < 0 && timeMillis > 3000L)) {
						cd = null;
					}
					
					if (cd == null) {
						cd = new Cooldown(name);
						cooldowns.put(name, cd);
						cd.expireTime = now + timeMillis;
					}
					cd.expireTime = Math.min(cd.expireTime, now + timeMillis);
					cd.lastSeen = now;
				}
			}
		}
		
		Iterator<Cooldown> iter = cooldowns.values().iterator();
		while (iter.hasNext()) {
			Cooldown cd = iter.next();
			//If we haven't seen the cooldown on the CD list for 5 seconds, assume it's gone
			if (now - cd.lastSeen > 5000L)
				iter.remove();
		}
	}
	
	public void render() {		
		long now = Minecraft.getSystemTime();

		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		
		List<Cooldown> toDraw = new ArrayList(cooldowns.size());
		for (Cooldown cd : cooldowns.values()) {
			if (cd.expireTime - now < 0)
				continue;
			toDraw.add(cd);
		}
		Collections.sort(toDraw, this);
		
		int w = 80;
		int h = toDraw.size() * 9;
		int y = res.getScaledHeight() * 11 / 27;
		int x = res.getScaledWidth() - w;
		
		GuiScreen.drawRect(x - 1, y, x + w, y + h, 0x80000000);
		
		FontRenderer fr = mc.fontRendererObj;
		glPushMatrix();
		glTranslatef(x, y + 1.0f, 0f);
		for (Cooldown cd : toDraw) {
			double dif = (double)(cd.expireTime - now) / 1000.0;
			int color = 0xFFFFFF;
			if (dif <= 4.0) {
				color = 0xFFFF55;
			}
			if (dif <= 2.0) {
				color = 0x55FF55;
			}
			
			String timeStr = String.format("%.1f", dif);
			String skillStr = cd.skill;
			
			int timeW = fr.getStringWidth(timeStr);
			while (fr.getStringWidth(skillStr) + timeW > w - 6) {
				skillStr = skillStr.substring(0, skillStr.length() - 1);
			}
			
			fr.drawStringWithShadow(skillStr, 0, 0, color);
			fr.drawStringWithShadow(timeStr, w - timeW, 0, color);
			glTranslatef(0, 9, 0);
		}
		glPopMatrix();
		

	}

	//Cooldowns with longest expiry at front
	@Override
	public int compare(Cooldown l, Cooldown r) {
		long lv = l.expireTime;
		long rv = r.expireTime;
		return (lv < rv ? -1 : (lv == rv ? 0 : 1));
	}

}

class Cooldown {
	
	public String skill;
	public long expireTime;
	public long lastSeen;
	
	public Cooldown(String skill) {
		this.skill = skill;
	}
	
}
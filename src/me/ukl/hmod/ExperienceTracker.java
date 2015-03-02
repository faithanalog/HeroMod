package me.ukl.hmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import static com.mumfrey.liteloader.gl.GL.*;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class ExperienceTracker implements Comparator<ExpEvent> {
	
	public List<ExpEvent> expEvents = new ArrayList<ExpEvent>();
	
	public String expPerHourStr = "EXP/HR: 0.00";
	
	public void onExp(String hcClass, String amnt) {
		long now = Minecraft.getSystemTime();
		try {
			double amntNum = Double.parseDouble(amnt);
			expEvents.add(new ExpEvent(hcClass, amntNum, now));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
		final long hour = 1000 * 60 * 60;
		Iterator<ExpEvent> iter = expEvents.iterator();
		while (iter.hasNext()) {
			ExpEvent e = iter.next();
			if (now - e.time > hour / 2) {
				iter.remove();
			}
		}
		
		Collections.sort(expEvents, this);
	}
	
	public double expPerHour() {
		long now = Minecraft.getSystemTime();
		
		long maxTime = 0L;
		final long hour = 1000 * 60 * 60;
		
		double total = 0.0;
		
		for (ExpEvent e : expEvents) {
			if (now - e.time > hour / 2) {
				break;
			}
			total += e.xpAmount;
			maxTime = Math.max(maxTime, now - e.time);
		}
		
		return maxTime == 0 ? 0 : total * hour / (double)maxTime;
	}

	/**
	 * Oldest events last
	 */
	@Override
	public int compare(ExpEvent l, ExpEvent r) {
		long lv = r.time;
		long rv = l.time;
		return (lv < rv ? -1 : (lv == rv ? 0 : 1));
	}
	
	public void renderExp() {
		final long now = Minecraft.getSystemTime();
		List<ExpEvent> toDisplay = new ArrayList(5);
		int count = 0;
		for (ExpEvent evt : expEvents) {
			if (now - evt.time > 7000 || count == 5) {
				break;
			}
			toDisplay.add(evt);
			count++;
		}
		expPerHourStr = String.format("XP/HR: %d", (int)expPerHour());
		
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		
		
		int w = 70;
		int h = toDisplay.size() * 9 + 9;
		int y = res.getScaledHeight() - 28 - h;
		int x = res.getScaledWidth() - w;
		
		GuiScreen.drawRect(x - 1, y, x + w, y + h, 0x80000000);
		
		FontRenderer fr = mc.fontRendererObj;
		glPushMatrix();
		glTranslatef(x, y + h - 8, 0F);
		
		fr.drawStringWithShadow(expPerHourStr, 0, 0, 0xFFFFFF);
		glTranslatef(0, -9, 0);
		for (ExpEvent evt : toDisplay) {
			fr.drawStringWithShadow(evt.xpString, 0, 0, 0xFFFFFF);
			glTranslatef(0, -9, 0);
		}
		glPopMatrix();
	}

}

class ExpEvent {
	public final String hcClass;
	public final double xpAmount;
	public final long time;
	public final String xpString;
	
	public ExpEvent(String hcClass, double amnt, long time) {
		this.hcClass = hcClass;
		this.xpAmount = amnt;
		this.time = time;
		
		xpString = String.format("%s: %.2f", hcClass.substring(0, 5), amnt);
	}
}
package me.ukl.hmod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;

public class ScoreboardUtil {
	
	public static Scoreboard scoreboard() {
		Minecraft mc = Minecraft.getMinecraft();
		Scoreboard sb = mc.theWorld.getScoreboard();
		return sb;
	}
	
	public static ScoreObjective hcBoard() {
		return scoreboard().getObjective("play.hc.to");
	}
	
	public static boolean isHerocraft() {
		ScoreObjective obj = hcBoard();
		return obj != null;
	}
	
	/**
	 * Strips party data out of the scoreboard
	 */
	public static void stripParty() {
		if (!isHerocraft()) {
			return;
		}
		Scoreboard board = scoreboard();
		if (board.getObjectiveInDisplaySlot(1) == hcBoard()) {
			board.setObjectiveInDisplaySlot(1, null);
		}
		
	}

}

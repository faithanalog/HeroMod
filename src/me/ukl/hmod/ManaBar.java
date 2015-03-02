package me.ukl.hmod;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;
import static com.mumfrey.liteloader.gl.GL.*;

public class ManaBar {
	
	public int mana = 100;
	public int maxMana = 100;
	
	/**
	 * Do we know what the player's max mana actually is?
	 */
	public boolean maxManaKnown;
	
	private Pattern manaPat1;
	private Pattern manaPat2;
	
	private ResourceLocation bgTex;
	private ResourceLocation fillTex;
	private ResourceLocation glintTex;
	
	public ManaBar() {
		//TODO: Fix to work with all-red bars
		String manaRegex1 = "§r§9Mana: §r§f(\\d+)/(\\d+) - §r§9(\\d+)% §r§c\\[§r§[|§r94]+§r§c\\]§r";
		String manaRegex2 = "§r§9MANA §r§9(\\d+)% §r§c\\[§r§[|§r94]+§r§c\\]§r";
		manaPat1 = Pattern.compile(manaRegex1);
		manaPat2 = Pattern.compile(manaRegex2);
		
		bgTex = new ResourceLocation("hmod:textures/manabg.png");
		fillTex = new ResourceLocation("hmod:textures/manafill.png");
		glintTex = new ResourceLocation("hmod:textures/managlint.png");
	}
	
	/**
	 * Attempts to parse the message as a mana string
	 * @param message Chat message to parse
	 * @return Whether or not the message was a mana string
	 */
	public boolean parseMana(String message) {
		
		//If it's a level up message, return false but set mana to max
		if (message.matches("§r§7You gained a level! \\(Lvl §r§f\\d+§r§7 §r§f.*§r§7\\)§r")) {
			mana = maxMana;
			return false;
		}
		
		Matcher match1 = manaPat1.matcher(message);
		Matcher match2 = manaPat2.matcher(message);
		
		if (match1.matches()) {
			//The first match pattern is more detailed and provides
			//current mana & max mana
			mana = Integer.parseInt(match1.group(1));
			maxMana = Integer.parseInt(match1.group(2));
			maxManaKnown = true;
			return true;
		} else if (match2.matches()) {
			//The second match pattern only provides percentages, but we can
			//can guess at the actual mana if we have already found
			//the max
			mana = Integer.parseInt(match2.group(1)) * maxMana / 100;
			return true;
		}
		return false;
	}
	
	public void render() {
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer fr = mc.fontRendererObj;
		
		ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		
		
		int w = 106;
		int h = 22;
		int x = res.getScaledWidth() - w - 10;
		int y = res.getScaledHeight() - h;
		
		int fillW = w - 6;
		int fillH = h - 6;
		int barLength = fillW * mana / maxMana;
		
		glPushMatrix();
		glTranslatef(x, y, 0.0f);
		
		//Draw background
		glColor4f(1f, 1f, 1f, 1f);
		mc.getTextureManager().bindTexture(bgTex);
		GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, w, h, w, h);
		
		glTranslatef(3.0f, 3.0f, 0.0f);
		//Draw fill
		GuiScreen.drawRect(0, 0, barLength, fillH, 0xFF0000CC);
		
		//Draw overlay
		mc.getTextureManager().bindTexture(glintTex);
		glColor4f(0.0f, 1.0f, 0.75f, 1.0f);
		glDepthMask(false);
		glBlendFunc(GL_SRC_COLOR, GL_ONE);
		glMatrixMode(GL_TEXTURE);
		glEnableBlend();
		
		Tessellator tess = Tessellator.getInstance();
		WorldRenderer wr = tess.getWorldRenderer();
		for (int pass = 0; pass < 2; pass++) {
			int animMulti = 3000 + (pass == 1 ? 1873 : 0);
			float texOffs = (float)(Minecraft.getSystemTime() % animMulti) / (float)animMulti;
			float passShift = (pass == 1 ? -1.0f : 4.0f) * 16.0f;
			
			glPushMatrix();
			glTranslatef(texOffs, 0.0f, 0.0f);
			glScalef(0.00390625F, 0.00390625F, 0.00390625F);
			
			wr.startDrawingQuads();
			wr.addVertexWithUV(        0,     0, 0,             passShift,  0.0f);
			wr.addVertexWithUV(        0, fillH, 0,                  0.0f, 16.0f);
			wr.addVertexWithUV(barLength, fillH, 0,             barLength, 16.0f);
			wr.addVertexWithUV(barLength,     0, 0, barLength + passShift,  0.0f);
			tess.draw();
			glPopMatrix();
		
		}
		glDisableBlend();
		glMatrixMode(GL_MODELVIEW);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDepthMask(true);
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		//Draw text
		String text;
		if (maxManaKnown) {
			text = mana + "/" + maxMana;
		} else {
			text = mana + "%";
		}
		int textX = (fillW - fr.getStringWidth(text)) / 2;
		int textY = fillH - 12;
		fr.drawStringWithShadow(text, textX, textY, 0xFFFFFF);
		
		//Pop all transformations
		glPopMatrix();
	}

}

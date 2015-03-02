package me.ukl.hmod.map;

import static com.mumfrey.liteloader.gl.GL.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import com.google.gson.Gson;

public class Waypoint {
	
	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_DEATH = 1;
	
	private String name;
	private int x, y, z;
	private int type;
	private int color;
	private boolean active;
	
	public Waypoint() {
		
	}
	
	public Waypoint(String name, int x, int y, int z, int color, int type) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
		this.color = color;
		this.active = true;
	}
	
	public String getName() {
		return name;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public int getType() {
		return type;
	}
	
	public int getColor() {
		return this.color;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public double getDistFromPlayer(double camx, double camy, double camz) {
		Minecraft mc = Minecraft.getMinecraft();
		double tx = getX() - camx + 0.25;
		double ty = getY() - camy + 1.0;
		double tz = getZ() - camz + 0.25;
		return Math.sqrt(tx * tx + ty * ty + tz * tz);
	}
	
	public void render(double tx, double ty, double tz, float fov, Minecraft mc, float ptick) {
		glPushMatrix();
		glTranslated(tx, ty, tz);
		
		double dist = Math.sqrt(tx * tx + ty * ty + tz * tz);
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		float perspectiveDist = (float) Math.max(mc.gameSettings.renderDistanceChunks * 16 * MathHelper.SQRT_2, dist * MathHelper.SQRT_2);
		gluPerspective(fov, mc.displayWidth / (float)mc.displayHeight, 0.05f, perspectiveDist);
		glMatrixMode(GL_MODELVIEW);
		
		double scl = (dist * 0.1 + 1) * (1 / 37.5);
		float alpha = (float)Math.max(0.0D, 1.0D - dist * (1.6 / (mc.gameSettings.renderDistanceChunks * 12.8)));
		glScaled(scl, -scl, scl);
		glTranslatef(8, 0, 0);
		
		Entity view = mc.getRenderViewEntity();
		float pitch = view.prevRotationPitch + (view.rotationPitch - view.prevRotationPitch);
		float yaw = view.prevRotationYaw + (view.rotationYaw - view.prevRotationYaw);
		String text = String.format("%s: %1.2fm", getName(), dist);
		
		float r = (getColor() >> 16 & 0xFF) / 255F;
		float g = (getColor() >> 8 & 0xFF) / 255F;
		float b = (getColor() & 0xFF) / 255F;
		
		
		
		glRotatef(-yaw + 180, 0, 1, 0);
		glRotatef(pitch, 1, 0, 0);
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer wr = tes.getWorldRenderer();
		FontRenderer fr = mc.fontRendererObj;
		int len = fr.getStringWidth(text) / 2;
		
		glDisableTexture2D();
		glDisableDepthTest();
		wr.startDrawingQuads();
			wr.setColorRGBA_F(r, g, b, 0.4F);
			wr.addVertex( 8,  0, 0);
			wr.addVertex(-8,  0, 0);
			wr.addVertex(-8, 16, 0);
			wr.addVertex( 8, 16, 0);
		tes.draw();
		
		glEnableDepthTest();
		wr.startDrawingQuads();
			wr.setColorRGBA_F(r, g, b, alpha);
			wr.addVertex( 8,  0, 0);
			wr.addVertex(-8,  0, 0);
			wr.addVertex(-8, 16, 0);
			wr.addVertex( 8, 16, 0);
		tes.draw();
		
		
		if(len != 0) {
			int txtcolor = this.type == TYPE_NORMAL ? 0xFFFFFF : 0xFF0000;
			glTranslatef(0, 17, 0);
			glDisableDepthTest();
			wr.startDrawingQuads();
				wr.setColorRGBA_F(0, 0, 0, 0.6275F);
				wr.addVertex( len + 1, -1, 0);
				wr.addVertex(-len - 1, -1, 0);
				wr.addVertex(-len - 1, fr.FONT_HEIGHT, 0);
				wr.addVertex( len + 1, fr.FONT_HEIGHT, 0);
			tes.draw();
			glEnableTexture2D();
			fr.drawString(text, -len, 0, 0x66000000 | txtcolor);
			glEnableDepthTest();
			int a = (int)(255 * alpha) << 24;
			if(a != 0) {
				fr.drawString(text, -len, 0, a | txtcolor);
			}
		} else {
			glEnableTexture2D();
		}
		
		glPopMatrix();
		glMatrixMode(GL_PROJECTION);
		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
	}
	
	public static FloatBuffer getProjectionMatrix() {
		FloatBuffer buf = BufferUtils.createFloatBuffer(16);
		glGetFloat(GL_PROJECTION_MATRIX, buf);
		return buf;
	}
	
	/**
	 * Gets the current FOV
	 * @return FOV in degrees
	 */
	public static float getFOV() {
		FloatBuffer mat = getProjectionMatrix();
		return (float) Math.toDegrees(Math.atan(1 / mat.get(5)) * 2f);
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}

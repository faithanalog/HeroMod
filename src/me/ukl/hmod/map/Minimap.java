package me.ukl.hmod.map;

import static com.mumfrey.liteloader.gl.GL.*;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ukl.api.gl.ShaderProgram;
import me.ukl.api.gui.component.LabelBase;
import me.ukl.api.resource.CustomFont;
import me.ukl.hmod.LiteModHeroMod;
import me.ukl.hmod.softgfx.Bitmap;
import me.ukl.hmod.softgfx.BitmapGL;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL20;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mumfrey.liteloader.util.ObfuscationUtilities;

public class Minimap {
    
	public static int colorMapGenCountdown = 1;
    private static Map<Block, int[]> colorMap = null;
    
    public Bitmap data;
    public BitmapGL gldata;
    public double scale = 2.0;
    
    private int curX;
    private int curZ;
    private boolean wasDead;
    
	private List<Waypoint> waypoints = new ArrayList<Waypoint>();
	private List<Waypoint> deathpoints = new ArrayList<Waypoint>();
	private LiteModHeroMod mod;
	
	private ResourceLocation mapMask;
	private ResourceLocation mapOverlay;
	private ShaderProgram mapShader;
	private int mapScaleLoc;
    
    private static Field chunkIsModified;
    
    public Minimap(LiteModHeroMod mod) {
    	this.mod = mod;
        data = new Bitmap(33 * 16, 33 * 16);
        gldata = new BitmapGL(22 * 16, 22 * 16, true);
        
        if (chunkIsModified == null) {
        	String fName = ObfuscationUtilities.getObfuscatedFieldName("isModified", "q", "field_76643_l");
			try {
				chunkIsModified = Chunk.class.getDeclaredField(fName);
				chunkIsModified.setAccessible(true);
			} catch (Exception e) {
				System.err.println("o crap");
				e.printStackTrace();
			}
        	
        }
        
		mapMask = new ResourceLocation("hmod:textures/map_mask.png");
		mapOverlay = new ResourceLocation("hmod:textures/map_overlay.png");
		
		try {
			mapShader = new ShaderProgram("/assets/hmod/shaders/mapshader.vert", "/assets/hmod/shaders/mapshader.frag");
			mapScaleLoc = GL20.glGetUniformLocation(mapShader.prog, "map_scale");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void tick(Minecraft mc) throws IllegalArgumentException, IllegalAccessException {
        World world = mc.theWorld;
        if (world == null) {
            return;
        }
//        if (colorMapGenCountdown > 0) {
//        	colorMapGenCountdown--;
//        	if (colorMapGenCountdown == 0) {
//        		initColors();
//        	}
//        }
        handleKeyInput();
        handleDeathpointCreation();
        int px = MathHelper.floor_double(mc.thePlayer.posX);
        int pz = MathHelper.floor_double(mc.thePlayer.posZ);
        int cx = px >> 4;
        int cz = pz >> 4;
        
        //Shift pixel data around as needed
        int dx = cx - curX;
        int dz = cz - curZ;
        curX = cx;
        curZ = cz;
        if (dx < -1 || dx > 1 || dz < -1 || dz > 1) {
            fullMapRefresh();
        } else {
            if (dx < 0) {
                shiftChunksRight();
            }
            if (dx > 0) {
                shiftChunksLeft();
            }
            if (dz < 0) {
                shiftChunksDown();
            }
            if (dz > 0) {
                shiftChunksUp();
            }
        }
        
        //Rescan blocks
        for (int x = -16; x < 17; x++) {
            for (int z = -16; z < 17; z++) {
                Chunk c = world.getChunkFromChunkCoords(x + cx, z + cz);
                if (chunkIsModified.getBoolean(c)) {
                    int pixX = (x + 16) * 16;
                    int pixY = (z + 16) * 16;
                    if (pixX < 0 || pixY < 0 || pixX > data.width - 16 || pixY > data.height - 16) {
                        continue;
                    }
                    stitchChunk(pixX, pixY, c);
                    chunkIsModified.setBoolean(c, false);
                }
            }
        }
        
        
        double time = System.currentTimeMillis() / 1000d;
//        scale = 4.0;
        
        //Basically this code is set up so we can scale to any size, and move smoothly through it at a sub-block amount
        

        //Max amount we can move from the block in pixels
        int sclmax = (int)Math.ceil(scale) * 2;
        int wh = (int)((gldata.width + sclmax) / scale) >> 1 << 1;
        int xy = (data.width - wh - 16) / 2;
        int ox = (int)Math.floor((mc.thePlayer.posX - px + 1.0) * scale);
        int oz = (int)Math.floor((mc.thePlayer.posZ - pz + 1.0) * scale);
        long times = System.currentTimeMillis();
        
        int mw = gldata.width + sclmax;
        int mh = gldata.height + sclmax;
        gldata.drawBitmap(data, -ox, -oz, mw, mh, xy + (px & 0xF), xy + (pz & 0xF), wh, wh);
        gldata.update();
    }
    
    private void handleDeathpointCreation() {
    	Minecraft mc = Minecraft.getMinecraft();
		if(mc.currentScreen instanceof GuiGameOver) {
			if(!wasDead) {
				wasDead = true;
				int x = MathHelper.floor_double(mc.thePlayer.posX);
				int y = MathHelper.floor_double(mc.thePlayer.posY + 1);
				int z = MathHelper.floor_double(mc.thePlayer.posZ);
				Random rand = new Random();
				int r = rand.nextInt(256);
				int g = rand.nextInt(256);
				int b = rand.nextInt(256);
				Waypoint pt = new Waypoint("Death Point", x, y, z, r << 16 | g << 8 | b, Waypoint.TYPE_DEATH);
				deathpoints.add(pt);
				if(deathpoints.size() > 3) {
					deathpoints.remove(0);
				}
				try {
					this.saveCurrentWaypoints();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			wasDead = false;
			Iterator<Waypoint> pts = deathpoints.iterator();
			while(pts.hasNext()) {
				Waypoint nxt = pts.next();
				if(nxt.getDistFromPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ) <= 2.0) {
					pts.remove();
				}
			}
		}
    }
    
    private static FloatBuffer mapVerts;
    
    static {
    	float[] verts = new float[] {
    			352,   0, 0, 1, 0,
    			0,     0, 0, 0, 0,
    			0,   352, 0, 0, 1,
    			352, 352, 0, 1, 1,
    	};
    	mapVerts = BufferUtils.createFloatBuffer(verts.length);
    	mapVerts.put(verts).flip();
    }
    
    public void render(Minecraft mc, float ptick, ScaledResolution res) {
    	TextureManager mgr = mc.getTextureManager();
    	
        gldata.bind();
        Tessellator tes = Tessellator.getInstance();
        WorldRenderer wr = tes.getWorldRenderer();
        glPushMatrix();
        glScalef(0.25f, 0.25f, 0.25f);
        glColor4f(1, 1, 1, 1);
        glTranslatef(res.getScaledWidth() * 4 - 352, 0, 0);

//        wr.startDrawingQuads();
//        wr.addVertexWithUV(352, 0,   0, 1, 0);
//        wr.addVertexWithUV(0,   0,   0, 0, 0);
//        wr.addVertexWithUV(0,   352, 0, 0, 1);
//        wr.addVertexWithUV(352, 352, 0, 1, 1);
//        tes.draw();
        
        glEnableBlend();
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL20.glUseProgram(mapShader.prog);
        GL20.glUniform1f(mapScaleLoc, (float)scale);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        mapVerts.position(0);
        GL20.glVertexAttribPointer(0, 3, false, 5 * 4, mapVerts);
        mapVerts.position(3);
        GL20.glVertexAttribPointer(1, 2, false, 5 * 4, mapVerts);
        
        GL11.glDrawArrays(GL_QUADS, 0, 4);
        
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glUseProgram(0);
        
        glDisableTexture2D();
        glColor4f(1, 0, 0, 1);
        glTranslatef(352 >> 1, 352 >> 1, 0);
        glRotatef(180 + mc.thePlayer.rotationYaw, 0, 0, 1);
        wr.startDrawing(GL_TRIANGLES);
        wr.addVertex( 0, -16, 0);
        wr.addVertex(-8,   8, 0);
        wr.addVertex( 8,   8, 0);
        tes.draw();
        glPopMatrix();
        
        glPushMatrix();
        //Draw coords underneath
        FontRenderer fr = mc.fontRendererObj;
        CustomFont fnt = LabelBase.defaultFont;
        fnt.setScale(0.275f);
        int x = MathHelper.floor_double(mc.thePlayer.posX);
        int y = MathHelper.floor_double(mc.thePlayer.posY);
        int z = MathHelper.floor_double(mc.thePlayer.posZ);
        String coords = String.format("%d,%d,%d", x, y, z);
//        int w = fr.getStringWidth(coords);
        float w = fnt.getWidth(coords);
        float h = fnt.getSize();
        float dispX = res.getScaledWidth() - (352 >> 3) - (w * 0.5f);
        glTranslatef(dispX, (352 >> 2) + 3, 0);
        
        glColor4f(0f, 0f, 0f, 0.5f);
		wr.startDrawingQuads();
		wr.addVertex(w+1,  -1, 0);
		wr.addVertex( -1,  -1, 0);
		wr.addVertex( -1, h-0.5, 0);
		wr.addVertex(w+1, h-0.5, 0);
		tes.draw();
        
        glColor4f(1, 1, 1, 1);
        glEnableTexture2D();
        fnt.drawString(coords, 0, fnt.getSize() - fnt.getDescent());
//        fr.drawStringWithShadow(coords, 0, 0, 0xFFFFFF);
        glPopMatrix();
    }
    
    public void shiftChunksRight() {
        int srcRow = data.width - 17;
        int dstRow = data.width - 1;
        for (int y = 0; y < data.height; y++) {
            int src = srcRow, dst = dstRow;
            for (int x = 16; x < data.width; x++) {
                data.data[dst] = data.data[src];
                dst--;
                src--;
            }
            srcRow += data.width;
            dstRow += data.width;
        }
    }
    
    public void shiftChunksLeft() {
        int srcRow = 16;
        int dstRow = 0;
        for (int y = 0; y < data.height; y++) {
            int src = srcRow, dst = dstRow;
            for (int x = 16; x < data.width; x++) {
                data.data[dst] = data.data[src];
                dst++;
                src++;
            }
            srcRow += data.width;
            dstRow += data.width;
        }
    }
    
    public void shiftChunksDown() {
        int srcCol = (data.height - 17) * data.width;
        int dstCol = (data.height - 1) * data.width;
        for (int x = 0; x < data.width; x++) {
            int src = srcCol, dst = dstCol;
            for (int y = 16; y < data.height; y++) {
                data.data[dst] = data.data[src];
                dst -= data.width;
                src -= data.width;
            }
            srcCol++;
            dstCol++;
        }
    }
    
    public void shiftChunksUp() {
        int srcCol = 16 * data.width;
        int dstCol = 0;
        for (int x = 0; x < data.width; x++) {
            int src = srcCol, dst = dstCol;
            for (int y = 16; y < data.height; y++) {
                data.data[dst] = data.data[src];
                dst += data.width;
                src += data.width;
            }
            srcCol++;
            dstCol++;
        }
    }
    
    public void fullMapRefresh() throws IllegalArgumentException, IllegalAccessException {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) {
            return;
        }
        int cx = this.curX;
        int cz = this.curZ;       
        for (int x = -16; x < 17; x++) {
            for (int z = -16; z < 17; z++) {
                Chunk c = world.getChunkFromChunkCoords(cx + x, cz + z);
                int pixX = (x + 16) * 16;
                int pixY = (z + 16) * 16;
                if (pixX < 0 || pixY < 0 || pixX >= data.width - 16 || pixY >= data.height - 16) {
                    continue;
                }
                stitchChunk(pixX, pixY, c);
                chunkIsModified.setBoolean(c, false);;
            }
        }
    }
    
    public int getTopBlock(Chunk c, int x, int z) {
    	int y = Math.min(c.getHeight(x, z) + 3, c.getWorld().getActualHeight()) - 1;
        for (; y >= 0; y--) {
            Block b = c.getBlock(x, y, z);
            if (b == null || b == Blocks.air || b == Blocks.snow || b == Blocks.tallgrass) {
                continue;
            }
            return y;
        }
        return -1;
    }
    
    public int getColor(Block block, int data, int height, int color) {
        if (colorMap == null)
            return 0xFF000000;
        int[] colors = colorMap.get(block);
        if (colors == null)
            return 0xFF000000;
        int argb = colors[data & 0xF];
        
        int r = ((argb >> 16) & 0xFF) * ((color >> 16) & 0xFF) / 255;
        int g = ((argb >> 8) & 0xFF) * ((color >> 8) & 0xFF) / 255;
        int b = (argb & 0xFF) * (color & 0xFF) / 255;
        argb = (height << 24) | (r << 16) | (g << 8) | b;
        return argb;
    }
    
    public void stitchChunk(int pixx, int pixy, Chunk c) {
        int dstRow = pixx + pixy * data.width;
        World world = c.getWorld();
        for (int z = 0; z < 16; z++) {
            int dst = dstRow;
            for (int x = 0; x < 16; x++) {
                int y = this.getTopBlock(c, x, z);
                if (y < 0 || y >= world.getActualHeight()) {
                    dst++;
                    continue;
                }
                Block block = c.getBlock(x, y, z);
                if (block == null || block == Blocks.air) {
                    dst++;
                    continue;
                }
                BlockPos pos = new BlockPos(x + (c.xPosition << 4), y, z + (c.zPosition << 4));
                int bdata = c.getBlockMetadata(pos);
                int color = getColor(block, bdata, y, block.colorMultiplier(world, pos));
                data.data[dst++] = color;
            }
            dstRow += data.width;
        }
    }
    
	private void handleKeyInput() {
		Minecraft mc = Minecraft.getMinecraft();
		if(mod.mapCreateWaypoint.isPressed()) {
			int x = MathHelper.floor_double(mc.thePlayer.posX);
			int y = MathHelper.floor_double(mc.thePlayer.posY + 1);
			int z = MathHelper.floor_double(mc.thePlayer.posZ);
			mc.displayGuiScreen(new GuiCreateWaypoint(x, y, z, this));
		} else if(mod.mapLargeToggle.isPressed()) {
			
		} else if(mod.mapMenu.isPressed()) {
			mc.displayGuiScreen(new GuiDeleteWaypoint(this));
		} else if(mod.mapZoom.isPressed()) {
			if (scale == 1.0) {
				scale = 2.0;
			} else if (scale == 2.0) {
				scale = 4.0;
			} else if (scale == 4.0) {
				scale = 8.0;
			} else if (scale == 8.0) {
				scale = 1.0;
			}
		}
	}
    
	public List<Waypoint> getWaypoints() {
		return this.waypoints;
	}
	
	public List<Waypoint> getDeathpoints() {
		return this.deathpoints;
	}
	
	public void saveCurrentWaypoints() throws IOException {
		World currentWorld = Minecraft.getMinecraft().theWorld;
		if(currentWorld != null) {
			File dir = new File(LiteModHeroMod.modFolder, "waypoints");
			if(!dir.exists()) {
				dir.mkdirs();
			}
			String levelName = getCurrentWorldName();
			if(levelName != null) {
				File pointsFile = new File(dir, levelName + "_DIM" + getCurrentDimension() + ".json");
				saveWaypoints(pointsFile);
			}
		}
	}
	
	public void loadCurrentWaypoints() throws IOException {
		World currentWorld = Minecraft.getMinecraft().theWorld;
		if(currentWorld != null) {
			waypoints.clear();
			deathpoints.clear();
			try {
				loadCurrentReisWaypoints();
			} catch (Exception e) {
				e.printStackTrace();
			}
			File dir = new File(LiteModHeroMod.modFolder, "waypoints");
			if(dir.exists()) {
				String levelName = getCurrentWorldName();
				if(levelName != null) {
					File pointsFile = new File(dir, levelName + "_DIM" + getCurrentDimension() + ".json");
					loadWaypoints(pointsFile);
				}
			}
		}
	}
	
	public void loadCurrentReisWaypoints() throws IOException {
		Minecraft mc = Minecraft.getMinecraft();
		File dir = new File(mc.mcDataDir, "mods" + File.separatorChar + "rei_minimap");
		if(!dir.exists() || !dir.isDirectory()) {
			return;
		}
		String levelName = getCurrentWorldName();
		if(levelName == null) {
			return;
		}
		File waypointFile = new File(dir, levelName + ".DIM" + getCurrentDimension() + ".points");
		if(waypointFile.exists() && !waypointFile.isDirectory()) {
			loadReisWaypoints(waypointFile);
		}
	}
	
	public String getCurrentWorldName() {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.thePlayer;
		NetHandlerPlayClient handler = player.sendQueue;
		NetworkManager netManager = handler.getNetworkManager();
		SocketAddress addr = netManager == null ? null : netManager.getRemoteAddress();
		if(addr == null)
			return null;
		String addrStr = addr.toString().replaceAll("[\r\n]", "");
        Matcher matcher = Pattern.compile("(.*)/(.*):([0-9]+)").matcher(addrStr);
        
        String levelName = "";
        
		if (matcher.matches()) {
			levelName = matcher.group(1);
			if (levelName.isEmpty()) {
				levelName = matcher.group(2);
			}
			if (!matcher.group(3).equals("25565")) {
				levelName = levelName + "[" + matcher.group(3) + "]";
			}
		} else {
			String str = addr.toString().replaceAll("[a-z]", "a").replaceAll("[A-Z]", "A").replaceAll("[0-9]", "*");
		}

		for (char c : ChatAllowedCharacters.allowedCharactersArray) {
			levelName = levelName.replace(c, '_');
		}
		return levelName;
	}
	
	public int getCurrentDimension() {
		return Minecraft.getMinecraft().thePlayer.dimension;
	}
	
	public void saveWaypoints(File file) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Waypoints pts = new Waypoints();
		pts.waypoints = waypoints;
		pts.deathpoints = deathpoints;
		String json = gson.toJson(pts);
		FileOutputStream out = new FileOutputStream(file);
		out.write(json.getBytes(CharsetUtil.UTF_8));
		out.close();
	}
	
	public void loadWaypoints(File file) throws IOException  {
		FileInputStream in = new FileInputStream(file);
		Reader read = new InputStreamReader(in, CharsetUtil.UTF_8);
		Gson gson = new Gson();
		Waypoints points = gson.fromJson(read, Waypoints.class);
		this.waypoints.addAll(points.waypoints);
		this.deathpoints.addAll(points.deathpoints);
		read.close();
	}
	
	public void loadReisWaypoints(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		BufferedReader read = new BufferedReader(new InputStreamReader(in));
		Scanner scan = new Scanner(read);
		scan.useDelimiter("[:\\n]*");
		while(scan.hasNextLine()) {
			try {
				String name = scan.next();
				int x = scan.nextInt();
				int y = scan.nextInt();
				int z = scan.nextInt();
				boolean active = scan.nextBoolean();
				int color = scan.nextInt(16);
				int type = scan.hasNextInt() ? scan.nextInt() : 0;
				Waypoint point = new Waypoint(name, x, y, z, color, type);
				point.setActive(active);
				if(type == Waypoint.TYPE_NORMAL) {
					this.waypoints.add(point);
				} else if(type == Waypoint.TYPE_DEATH) {
					this.deathpoints.add(point);
				}
			} catch (Exception e) {
				System.err.println("Error loading Rei's waypoints");
				scan.close();
				return;
			}
		}
		scan.close();
	}
    
    public static void initColors() {
    	final Minecraft mc = Minecraft.getMinecraft();
        TextureManager mgr = mc.getTextureManager();
        mgr.bindTexture(TextureMap.locationBlocksTexture);
        final int w = GL11.glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
        final int h = GL11.glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);
        final IntBuffer buf = BufferUtils.createIntBuffer(w * h);
        GL11.glGetTexImage(GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buf);
        
        
    	
        Thread th = new Thread() {
            public void run() {
                Map<Block, int[]> colmap = new HashMap<Block, int[]>();
                List<ResourceLocation> blocks = new ArrayList<ResourceLocation>();
                blocks.addAll(Block.blockRegistry.getKeys());
                
                BlockModelShapes blkShapes = mc.getBlockRendererDispatcher().getBlockModelShapes();
                for (ResourceLocation b_loc : blocks) {
                    Block b = (Block) Block.blockRegistry.getObject(b_loc);
                    int[] colors = new int[16];
                    for (int d = 0; d < 16; d++) {
                        try {
                        	IBlockState state = b.getStateFromMeta(d);
                        	TextureAtlasSprite spt = blkShapes.getTexture(state);
//                            IIcon ico = b.func_149735_b(1, d);
//                            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/" + ico.getIconName() + ".png"));
//                            InputStream in = res.getInputStream();
//                            Bitmap img = Bitmap.load(in);
//                            in.close();
                            long rt = 0;
                            long gt = 0;
                            long bt = 0;
                            long div = 0;
                            
                            
                            int x = spt.getOriginX();
                        	int y = spt.getOriginY();
                        	int sw = spt.getIconWidth();
                        	int sh = spt.getIconHeight();
                        	for (int yy = 0; yy < sh; yy++) {
                        		for (int xx = 0; xx < sw; xx++) {
                        			int color = buf.get((xx + x) + (yy + y) * w);
									int a = color >>> 24;
									if (a != 0) {
									    rt += (color >> 16) & 0xFF;
									    gt += (color >> 8) & 0xFF;
									    bt += color & 0xFF;
									    div++;
									}
                        		}
                        	}
                            
                            rt /= div;
                            gt /= div;
                            bt /= div;
                            colors[d] = 0xFF000000 | ((int)rt & 0xFF) << 16 | ((int)gt & 0xFF) << 8 | ((int)bt & 0xFF);
                        } catch (Exception e) {
                            colors[d] = 0xFF000000;
                        }
                    }
                    colmap.put(b, colors);
                }
                colorMap = colmap;
            }
        };
        th.start();
    }

}
class Waypoints {
	public List<Waypoint> waypoints;
	public List<Waypoint> deathpoints;
}
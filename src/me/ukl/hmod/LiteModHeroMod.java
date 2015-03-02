package me.ukl.hmod;

import static com.mumfrey.liteloader.gl.GL.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ukl.api.gl.GLGCFactory;
import me.ukl.hmod.map.Minimap;
import me.ukl.hmod.map.Waypoint;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.InitCompleteListener;
import com.mumfrey.liteloader.PostRenderListener;
import com.mumfrey.liteloader.PreJoinGameListener;
import com.mumfrey.liteloader.RenderListener;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.core.LiteLoaderEventBroker.ReturnValue;

public class LiteModHeroMod implements PostRenderListener, PreJoinGameListener, Tickable, RenderListener, ChatFilter, InitCompleteListener {
	
	public static File modFolder;
	
	private PartyFrame partyFrame;
	private ExperienceTracker expTracker;
	private CooldownTracker cdTracker;
	private ManaBar manaBar;
	private BoostTracker boostTracker;
	private Minimap minimap;
	
	public KeyBinding guiBind;
	public KeyBinding mapMenu;
	public KeyBinding mapZoom;
	public KeyBinding mapLargeToggle;
	public KeyBinding mapCreateWaypoint;
	
	private boolean confirmHC = false;
	private boolean expired = true;
	
	private World curWorld;
	
	private String[] onJoinCommands = new String[] {
			"/boost",
			"/mana"
	};

	@Override
	public String getVersion() {
		return "13.37.0";
	}

	@Override
	public void init(File configPath) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					URL url = new URL("http://hmod.ukl.me:4071/testValid");
					BufferedReader rdr = new BufferedReader(new InputStreamReader(url.openStream()));
					String ln = rdr.readLine();
					if (ln.equals("yes")) {
						expired = false;
						System.out.println("MOD IS VALID!");
					}
					rdr.close();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
		}).start();
		
		modFolder = new File(configPath, "HeroMod");
		modFolder.mkdir();
		File settingsFile = new File(modFolder, "Settings.json");
//		if (settingsFile.exists()) {
//			try {
//				LiteModHM.settings = Settings.readSettings(settingsFile);
//			} catch (IOException e) {
//				e.printStackTrace();
//				LiteModHM.settings = new Settings();
//			}
//		} else {
//			LiteModHM.settings = new Settings();
//		}
		//While testing
//		try {
//			Settings.writeSettings(LiteModHM.settings, settingsFile);
//			LiteModHM.font = new HeroFont(LiteModHM.class.getResourceAsStream("/heromod/font/Ubuntu.zip"));
//			LiteModHM.fontMono = new HeroFont(LiteModHM.class.getResourceAsStream("/heromod/font/UbuntuMono.zip"));
//			LiteModHM.fontSmall = new HeroFont(LiteModHM.class.getResourceAsStream("/heromod/font/UbuntuSmall.zip"))
//				.setFilter(GL11.GL_NEAREST_MIPMAP_LINEAR, GL11.GL_NEAREST);
//			LiteModHM.fontSmallMono = new HeroFont(LiteModHM.class.getResourceAsStream("/heromod/font/UbuntuSmallMono.zip"))
//				.setFilter(GL11.GL_NEAREST_MIPMAP_LINEAR, GL11.GL_NEAREST);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void upgradeSettings(String version, File configPath,
			File oldConfigPath) {
		
	}

	@Override
	public String getName() {
		return "xXPotentially-Broken-But-Has-Some-Stuff-HeroModXx";
	}

	@Override
	public boolean onChat(IChatComponent chat, String message,
			ReturnValue<IChatComponent> newMessage) {
		if (!ScoreboardUtil.isHerocraft()) {
			return true;
		}
		
		//XP tracker
		String xpRegex = "§r§f(.*)§r§7: Gained §r§f([\\d\\.]+)§r§7 Exp§r";
		Pattern pat = Pattern.compile(xpRegex);
		Matcher matcher = pat.matcher(message);
		if (matcher.matches()) {
			expTracker.onExp(matcher.group(1), matcher.group(2));
			return false;
		}
		if (message.equals("§r§7No experience gained - block placed too recently.§r")) {
			System.out.println("NO EXP!");
			return false;
		}
		
//		System.out.println(message);
//		§r§c-----[ §r§fAcars§r§c ]-----§r
//		§r  §r§aClass : Paladin | Merchant§r
//		§r  §r§aLevel : 59 | 60§r
		
		//§r§7You gained a level! (Lvl §r§f10§r§7 §r§fNecromancer§r§7)§r
		//§r§2[MD§r§2]§r§d[§r§bDev§r§d]§r§2unknownloner§r§5<§r§e*§r§5>§r§2: t§r
		//§r§2[MD§r§2]§r§e§l[§r§3Ancient§r§e§l]§r§2unknownloner§r§5<§r§e*§r§5>§r§2: est§r
		
		boostTracker.parseBoost(message);
		
		//Mana bar
		if (manaBar.parseMana(message)) {
			return false;
		}
		
		//⦕❤⦖
		String uklRegex = "§d\\[§r§bDev§r§d\\]§r§([0-9])unknownloner";
		pat = Pattern.compile(uklRegex);
		matcher = pat.matcher(message);
		if (matcher.find()) {
			String match = matcher.group(1);
			String msg = matcher.replaceFirst("§d⦕§bPegasus§d⦖§r§" + match + "unknownloner");
			newMessage.set(new ChatComponentText(msg));
		}
		
		return true;
	}

	@Override
	public void onRender() {
		if (expired) {
			return;
		}
		if (Minecraft.getMinecraft().theWorld != null) {
			ScoreboardUtil.stripParty();
		}
	}

	@Override
	public void onRenderGui(GuiScreen currentScreen) {
		
	}

	@Override
	public void onRenderWorld() {
	}

	@Override
	public void onSetupCameraTransform() {
		
	}

	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame,
			boolean clock) {
		GLGCFactory.onTick();
		
//		if (inGame && minecraft.thePlayer.getName().equals("unknownloner")) {
//			minecraft.gameSettings.mouseSensitivity = 0.5f;
//		}
		if (expired) {
			return;
		}
		
		//Not specific to herocraft
		if (inGame) {
			if (clock) {
				if (curWorld != minecraft.theWorld) {
					curWorld = minecraft.theWorld;
					try {
						minimap.getWaypoints().clear();
						minimap.getDeathpoints().clear();
						minimap.loadCurrentWaypoints();
//						minimap.loadCurrentReisWaypoints();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					minimap.tick(minecraft);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			ScaledResolution res = new ScaledResolution(minecraft, minecraft.displayWidth, minecraft.displayHeight);
			minimap.render(minecraft, partialTicks, res);
		}
		
		
		//Specific to herocraft
		if (confirmHC && !inGame) {
			confirmHC = false;
			partyFrame.members.clear();
			try {
				partyFrame.netHandler.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!inGame || !ScoreboardUtil.isHerocraft()) {
			return;
		}
		
		if (clock && inGame) {
			if (!confirmHC) {
				confirmHC = true;
				boostTracker.onJoin();
				for (String s : onJoinCommands) {
					minecraft.thePlayer.sendChatMessage(s);
				}
				try {
					partyFrame.netHandler.connect("hmod.ukl.me", 4070);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			partyFrame.tick();
			cdTracker.tick();
		}
		if (inGame) {
			glDisableLighting();
			partyFrame.render();
			expTracker.renderExp();
			cdTracker.render();
			
			
			if (minecraft.currentScreen == null) {
				manaBar.render();
				boostTracker.render();
			}
			glEnableLighting();
		}
//		if (clock && minecraft.theWorld.getWorldTime() % 100 == 0) {
//			minecraft.thePlayer.sendChatMessage("/tell gabizou " + Math.random());
//		}
	}

	@Override
	public void onInitCompleted(Minecraft minecraft, LiteLoader loader) {
		expTracker = new ExperienceTracker();
		cdTracker = new CooldownTracker();
		manaBar = new ManaBar();
		partyFrame = new PartyFrame(manaBar);
		boostTracker = new BoostTracker();
		
//		Render renderer = new HeroPlayerRenderer();
//		ModUtilities.addRenderer(EntityPlayer.class, renderer);
		Minimap.initColors();
		minimap = new Minimap(this);
		guiBind = new KeyBinding("GuiBind", Keyboard.KEY_U, "heromod.settings");
		mapMenu = new KeyBinding("Map Menu", Keyboard.KEY_M, "heromod.settings");
		mapZoom = new KeyBinding("Map Zoom", Keyboard.KEY_Z, "heromod.settings");
		mapLargeToggle = new KeyBinding("Toggle Large Map", Keyboard.KEY_X, "heromod.settings");
		mapCreateWaypoint = new KeyBinding("Create Waypoint", Keyboard.KEY_C, "heromod.settings");
		registerKeys(guiBind, mapMenu, mapZoom, mapLargeToggle, mapCreateWaypoint);
		
		
		//Player list crap
		//NetHandlerPlayClient#func_175106_d()
		//List of NetworkPlayerInfo
		//NetworkPlayerInfo#func_178854_k().getFormattedText()
	}
	
	private void registerKeys(KeyBinding... binds) {
		if(binds != null) {
			for(KeyBinding bind : binds) {
				LiteLoader.getInput().registerKeyBinding(bind);
			}
		}
	}
	@Override
	public boolean onPreJoinGame(INetHandler netHandler,
			S01PacketJoinGame joinGamePacket) {
		confirmHC = false;
		return true;
	}

	@Override
	public void onPostRenderEntities(float partialTicks) {
		// TODO Auto-generated method stub
//		wings.render(partialTicks);
//		System.out.println("HELLO");
		
		Minecraft mc = Minecraft.getMinecraft();
		Entity cam = mc.getRenderViewEntity();
		List<Waypoint> points = minimap.getWaypoints();
		
		if(mc.isSingleplayer() && !mc.inGameHasFocus && mc.gameSettings.pauseOnLostFocus) {
			partialTicks = 1;
		}
		final double camx = cam.lastTickPosX + (cam.posX - cam.lastTickPosX) * partialTicks;
		final double camy = cam.lastTickPosY + (cam.posY - cam.lastTickPosY) * partialTicks;
		final double camz = cam.lastTickPosZ + (cam.posZ - cam.lastTickPosZ) * partialTicks;
		
		
		glSetActiveTextureUnit(GL_TEXTURE1);
		glDisableTexture2D();
		glSetActiveTextureUnit(GL_TEXTURE0);
		glDisableLighting();
//		glDisable(GL_ALPHA_TEST);
		glEnableBlend();
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glAlphaFunc(GL_GREATER, 0.1F);
		glDisableFog();
		points.addAll(minimap.getDeathpoints());
		Collections.sort(points, new Comparator<Waypoint>() {
			@Override
			public int compare(Waypoint arg0, Waypoint arg1) {
				double d0 = arg0.getDistFromPlayer(camx, camy, camz);
				double d1 = arg1.getDistFromPlayer(camx, camy, camz);
				return Double.compare(d1, d0);
			}
		});
		float fov = Waypoint.getFOV();
//		System.out.println(fov);
		for(Waypoint wp : points) {
			wp.render(wp.getX() - camx + 0.25, wp.getY() - camy + 1.0, wp.getZ() - camz + 0.25, fov, mc, partialTicks);
		}
		points.removeAll(minimap.getDeathpoints());
		glEnableFog();
		
		glSetActiveTextureUnit(GL_TEXTURE1);
		glEnableTexture2D();
		glSetActiveTextureUnit(GL_TEXTURE0);
//		glEnable(GL_ALPHA_TEST);
		glEnableLighting();
		glDisableBlend();
//		glAlphaFunc(GL_GREATER, 0.5F);
	}

	@Override
	public void onPostRender(float partialTicks) {
		// TODO Auto-generated method stub
		
	}

}

/**
 * This file is part of SpoutcraftMod, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 SpoutcraftDev <http://spoutcraft.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.ukl.api.gui;

import java.io.IOException;

import me.ukl.api.gui.component.RootContainer;
import me.ukl.api.gui.event.key.KeyPressEvent;
import me.ukl.api.gui.event.key.KeyReleaseEvent;
import me.ukl.api.gui.event.mouse.MouseDownEvent;
import me.ukl.api.gui.event.mouse.MouseMoveEvent;
import me.ukl.api.gui.event.mouse.MouseScrollEvent;
import me.ukl.api.gui.event.mouse.MouseUpEvent;
import me.ukl.api.gui.renderer.GuiRenderer;
import me.ukl.api.gui.renderer.GuiRendererDepth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class Gui extends GuiScreen {
	
	public static final int KEY_REPEAT_DELAY = 1;
	
    private int lastButton = -1;
    private long clickTime = 0;
    private RootContainer root;
    private GuiRenderer guiRenderer = new GuiRendererDepth();
    private int repeatDelay = KEY_REPEAT_DELAY;
    private int repeatKey = -1;
    private char repeatChar;

    public Gui() {
        this.root = new RootContainer(this);
    }

    @Override
    public void initGui() {
        guiRenderer.initGui(this);
        root.clearComponents();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTick) {
        guiRenderer.drawScreen(this, root, mouseX, mouseY, partialTick);
    }

    public GuiRenderer getRenderer() {
        return this.guiRenderer;
    }

    public RootContainer getRoot() {
        return root;
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        int key = Keyboard.getEventKey();
        char keyChar = Keyboard.getEventCharacter();
        if (Keyboard.getEventKeyState()) {
            if (key == 87) {
                this.mc.toggleFullscreen();
                return;
            }
            repeatKey = key;
            repeatChar = keyChar;
            repeatDelay = 8;
            this.keyTyped(keyChar, key);
            //            root.callEvent(new KeyPressEvent(root, key, keyChar));
            //            super.keyTyped(keyChar, key);
        } else {
            root.callEvent(new KeyReleaseEvent(root, key, keyChar));
            if(repeatKey == key) {
            	repeatKey = -1;
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int btn = Mouse.getEventButton();
        int scroll = Mouse.getEventDWheel();
        if (Mouse.getEventButtonState()) {
            if (this.lastButton != -1) {
                mouseClickMove(x, y, btn, System.currentTimeMillis() - this.clickTime);
            } else {
                mouseClicked(x, y, btn);
                this.clickTime = System.currentTimeMillis();
            }
            this.lastButton = btn;
        } else if (btn != -1) {
            mouseMovedOrUp(x, y, btn);
            this.lastButton = -1;
        } else {
            if (this.lastButton != -1) {
                mouseClickMove(x, y, this.lastButton, System.currentTimeMillis() - this.clickTime);
            } else {
                mouseMovedOrUp(x, y, -1);
            }
        }
        if (scroll != 0) {
            mouseScrolled(x, y, this.lastButton, scroll);
        }
    }

    @Override
    protected void keyTyped(char keyChar, int key) throws IOException {
        super.keyTyped(keyChar, key);
        root.callEvent(new KeyPressEvent(root, key, keyChar));
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) throws IOException {
        super.mouseClicked(x, y, btn);
        root.callEvent(new MouseDownEvent(root, btn, x, y));
    }

    protected void mouseMovedOrUp(int x, int y, int btn) {
        if (btn == -1) {
            root.callEvent(new MouseMoveEvent(root, btn, x, y));
        } else {
            root.callEvent(new MouseUpEvent(root, btn, x, y));
        }
    }

    protected void mouseScrolled(int x, int y, int btn, int amnt) {
        root.callEvent(new MouseScrollEvent(root, btn, x, y, amnt));
    }

    @Override
    protected void mouseClickMove(int x, int y, int btn, long timePressed) {
        super.mouseClickMove(x, y, btn, timePressed);
        root.callEvent(new MouseMoveEvent(root, btn, x, y));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return root.pausesGame();
    }

    @Override
    public void updateScreen() {
    	if(repeatKey != -1 && --repeatDelay == 0) {
    		repeatDelay = KEY_REPEAT_DELAY;
    		try {
				keyTyped(repeatChar, repeatKey);
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
        root.processEvents();
    }

    public void add(Component c) {
        root.addComponent(c);
    }

    public void remove(Component c) {
        root.removeComponent(c);
    }

    public void addListeners(Object o) {
        root.addEventListeners(o);
    }

    public void removeListeners(Object o) {
        root.removeEventListeners(o);
    }

    public int[] screenToScaled(int x, int y) {
        x = x * width / Minecraft.getMinecraft().displayWidth;
        y = y * height / Minecraft.getMinecraft().displayHeight;
        return new int[] {x, y};
    }

    public int[] scaledToScreen(int x, int y) {
        x = x * Minecraft.getMinecraft().displayWidth / width;
        y = y * Minecraft.getMinecraft().displayHeight / height;
        return new int[] {x, y};
    }
}

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

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import me.ukl.api.gui.event.AddListenerEvent;
import me.ukl.api.gui.event.Event;
import me.ukl.api.gui.event.EventHandler;
import me.ukl.api.gui.event.EventListener;
import me.ukl.api.gui.event.RemoveListenerEvent;
import me.ukl.api.gui.event.key.KeyPressEvent;
import me.ukl.api.gui.event.key.KeyReleaseEvent;
import me.ukl.api.gui.event.mouse.MouseDownEvent;
import me.ukl.api.gui.event.mouse.MouseMoveEvent;
import me.ukl.api.gui.event.mouse.MouseScrollEvent;
import me.ukl.api.gui.event.mouse.MouseUpEvent;
import me.ukl.api.util.Color;
import me.ukl.api.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

import org.lwjgl.input.Mouse;

import com.mumfrey.liteloader.gl.GL;

public abstract class Component {
    private int x;
    private int y;
    private int width;
    private int height;
    private Color foreground = Color.WHITE;
    private Color background = Color.TRANSPARENT;
    private boolean visible = true;
    private Gui gui = null;
    private List<EventListener> eventListeners = new ArrayList<EventListener>();
    private ArrayDeque<Event> eventQueue = new ArrayDeque<Event>();
    private Container parent;

    {
        //Automatically add this is as event listeners
        //This isn't in a constructor to ensure it always runs
        this.addEventListeners(this);
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setForeground(Color col) {
        this.foreground = col;
    }

    public void setBackground(Color col) {
        this.background = col;
    }

    public void setGui(Gui gui) {
        this.gui = gui;
    }

    protected void setParent(Container c) {
        this.parent = c;
    }

    public int getX() {
        //        return getParent() == null ? x : x - getParent().getX();
        return x;
    }

    public int getY() {
        //        return getParent() == null ? y : y - getParent().getY();
        return y;
    }

    public boolean isVisible() {
        return visible;
    }

    public Color getForeground() {
        return this.foreground;
    }

    public Color getBackground() {
        return this.background;
    }

    public Gui getGui() {
        return this.gui;
    }

    public Container getParent() {
        return this.parent;
    }

    public void addEventListeners(Object listener) {
        removeEventListeners(listener);
        callEvent(new AddListenerEvent(this, listener));
    }

    public void removeEventListeners(Object listener) {
        callEvent(new RemoveListenerEvent(this, listener));
    }

    public List<EventListener> getEventListeners() {
        return Collections.unmodifiableList(this.eventListeners);
    }

    protected void callEvent(Event e) {
        eventQueue.offer(e);
    }

    protected void processEvents() {
        Event e;
        while ((e = eventQueue.poll()) != null) {
            for (EventListener l : eventListeners) {
                if (!e.isCancelled() || l.getHandler().ignoreCancelled()) {
                    l.onEvent(e);
                }
            }
            if (!e.isCancelled()) {
                if (e instanceof AddListenerEvent) {
                    onAddListener((AddListenerEvent) e);
                } else if (e instanceof RemoveListenerEvent) {
                    onRemoveListener((RemoveListenerEvent) e);
                }
            }
        }
    }

    private void onAddListener(AddListenerEvent e) {
        Object listener = e.getListener();
        List<Method> handlers = EventListener.getHandlers(listener);
        for (Method m : handlers) {
            EventListener l = new EventListener(listener, m);
            eventListeners.add(l);
        }
        Collections.sort(eventListeners);
    }

    private void onRemoveListener(RemoveListenerEvent e) {
        Object listener = e.getListener();
        Iterator<EventListener> iter = eventListeners.iterator();
        while (iter.hasNext()) {
            if (iter.next().getListener() == listener) {
                iter.remove();
            }
        }
        Collections.sort(eventListeners);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public abstract void render();

    @EventHandler (priority = -1)
    public void onMouseDown(MouseDownEvent e) {
        mouseDown(e.getButton(), e.getX(), e.getY());
    }

    @EventHandler (priority = -1)
    public void onMouseUp(MouseUpEvent e) {
        mouseUp(e.getButton(), e.getX(), e.getY());
    }

    @EventHandler (priority = -1)
    public void onMouseMove(MouseMoveEvent e) {
        mouseMove(e.getButton(), e.getX(), e.getY());
    }

    @EventHandler (priority = -1)
    public void onMouseScroll(MouseScrollEvent e) {
        mouseScroll(e.getButton(), e.getX(), e.getY(), e.getScrollAmnt());
    }

    @EventHandler (priority = -1)
    public void onKeyPress(KeyPressEvent e) {
        keyPress(e.getKey(), e.getCharacter());
    }

    @EventHandler (priority = -1)
    public void onKeyRelease(KeyReleaseEvent e) {
        keyRelease(e.getKey(), e.getCharacter());
    }

    public void mouseDown(int btn, int x, int y) {

    }

    public void mouseUp(int btn, int x, int y) {

    }

    public void mouseMove(int btn, int x, int y) {

    }

    public void mouseScroll(int btn, int x, int y, int amnt) {

    }

    public void keyPress(int key, char c) {

    }

    public void keyRelease(int key, char c) {

    }

    public void setClip(int x, int y, int width, int height) {
        if (getGui() != null) {
            getGui().getRenderer().setClip(x, y, width, height);
        }
    }

    public void setSubClip(int x, int y, int width, int height, int curTransX, int curTransY) {
        if (getGui() != null) {
            getGui().getRenderer().setSubClip(x, y, width, height, curTransX, curTransY);
        }
    }

    public void clearClip() {
        if (getGui() != null) {
            getGui().getRenderer().clearClip();
        }
    }

    public void pushClip() {
        if (getGui() != null) {
            getGui().getRenderer().pushClip();
        }
    }

    public void popClip() {
        if (getGui() != null) {
            getGui().getRenderer().popClip();
        }
    }

    public int getMouseX() {
        if (gui == null) {
            return Mouse.getX();
        }
        return Mouse.getX() * gui.width / Minecraft.getMinecraft().displayWidth;
    }

    public int getMouseY() {
        if (gui == null) {
            return Mouse.getY();
        }
        return gui.height - (Mouse.getY() * gui.height / Minecraft.getMinecraft().displayHeight);
    }

    public int getMouseXRel() {
        if (parent == null) {
            return getMouseX();
        }
        return parent.getMouseXRel() - parent.getX();
    }

    public int getMouseYRel() {
        if (parent == null) {
            return getMouseY();
        }
        return parent.getMouseYRel() - parent.getY();
    }

    public boolean containsMouse() {
    	if(parent != null && !parent.containsMouse())
    		return false;
        int mx = getMouseXRel();
        int my = getMouseYRel();
        return containsPoint(mx, my);
    }

    public boolean containsPoint(int x, int y) {
        return x >= getX() && x <= getX() + getWidth() &&
                y >= getY() && y <= getY() + getHeight();
    }

    public boolean receiveAllEvents() {
        return false;
    }

    public boolean focusable() {
        return false;
    }

    public boolean isFocused() {
        if (getParent() == null) {
            return false;
        }
        return getParent().getFocusedComponent() == this;
    }

    public void focus() {
        if (getParent() != null) {
            getParent().setFocusedComponent(this);
            getParent().focus();
        }
    }

    public void fillRect(int x, int y, int width, int height, Color col) {
        RenderUtil.drawRect(x, y, width, height, col);
    }

    public void fillCircle(int x, int y, int radius, Color col) {
        //Scale of screen
        int scale = scaledToScreen(1, 0)[0];
        int numSegments = (int) (15 * Math.sqrt(radius * scale));

        GL.glDisableTexture2D();
        Tessellator tes = Tessellator.getInstance();
        WorldRenderer wr = tes.getWorldRenderer();
        wr.startDrawing(GL.GL_POLYGON);
        wr.setColorRGBA(col.getR(), col.getG(), col.getB(), col.getA());
        wr.setTranslation(x, y, 0);
        double angle = Math.PI * 2;
        double inc = -angle / numSegments;
        for (int i = 0; i < numSegments; i++) {
            wr.addVertex(Math.cos(angle) * radius, Math.sin(angle) * radius, 0);
            angle += inc;
        }
        tes.draw();
        wr.setTranslation(0, 0, 0);
        GL.glEnableTexture2D();
    }

    public int[] screenToScaled(int x, int y) {
        return getGui().screenToScaled(x, y);
    }

    public int[] scaledToScreen(int x, int y) {
        return getGui().scaledToScreen(x, y);
    }
}

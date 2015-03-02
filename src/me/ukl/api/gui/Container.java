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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.ukl.api.gui.event.EventHandler;
import me.ukl.api.gui.event.container.AddComponentEvent;
import me.ukl.api.gui.event.container.RemoveComponentEvent;
import me.ukl.api.gui.event.key.KeyPressEvent;
import me.ukl.api.gui.event.key.KeyReleaseEvent;
import me.ukl.api.gui.event.mouse.MouseDownEvent;
import me.ukl.api.gui.event.mouse.MouseMoveEvent;
import me.ukl.api.gui.event.mouse.MouseScrollEvent;
import me.ukl.api.gui.event.mouse.MouseUpEvent;

import org.lwjgl.opengl.GL11;

public class Container extends Component {
    private List<Component> components = new ArrayList<Component>();
    private Component focusedComponent;

    @Override
    public void render() {
        this.pushClip();
        GL11.glPushMatrix();
        GL11.glTranslatef(getX(), getY(), 0);
        this.setSubClip(0, 0, getWidth(), getHeight(), getX(), getY());
        this.fillRect(0, 0, getWidth(), getHeight(), getBackground());
        for (Component c : components) {
            if (c.isVisible()) {
                c.render();
            }
        }
        GL11.glPopMatrix();
        this.popClip();
    }

    public void addComponent(Component c) {
        callEvent(new AddComponentEvent(this, c));
    }

    public void removeComponent(Component c) {
        callEvent(new RemoveComponentEvent(this, c));
    }

    public void clearComponents() {
        for (Component c : getComponents()) {
            removeComponent(c);
        }
    }

    public List<Component> getComponents() {
        return Collections.unmodifiableList(components);
    }

    public Component getFocusedComponent() {
        return this.focusedComponent;
    }

    public void setFocusedComponent(Component c) {
        if (c == null) {
            clearFocus();
        } else if (components.contains(c)) {
            this.focusedComponent = c;
        }
    }

    public void clearFocus() {
        if (this.focusedComponent != null && this.focusedComponent instanceof Container) {
            ((Container) this.focusedComponent).clearFocus();
        }
        this.focusedComponent = null;
    }

    @EventHandler (priority = -1)
    public void onAddComponent(AddComponentEvent e) {
        e.getAddedComponent().setParent(this);
        e.getAddedComponent().setGui(getGui());
        components.add(e.getAddedComponent());
    }

    @EventHandler (priority = -1)
    public void onRemoveComponent(RemoveComponentEvent e) {
        e.getRemovedComponent().setParent(null);
        e.getRemovedComponent().setGui(null);
        components.remove(e.getRemovedComponent());
    }

    @Override
    public void mouseDown(int btn, int x, int y) {
        this.clearFocus();
        for (Component c : components) {
            boolean hasMouse = c.containsPoint(x, y);
            if (c.receiveAllEvents() || hasMouse) {
                c.callEvent(new MouseDownEvent(c, btn, x - c.getX(), y - c.getY()));
                if (hasMouse && c.focusable() && this.focusedComponent == null) {
                    this.focusedComponent = c;
                }
            }
        }
    }

    @Override
    public void mouseUp(int btn, int x, int y) {
        for (Component c : components) {
            if (c.receiveAllEvents() || c.containsPoint(x, y)) {
                c.callEvent(new MouseUpEvent(c, btn, x - c.getX(), y - c.getY()));
            }
        }
    }

    @Override
    public void mouseMove(int btn, int x, int y) {
        for (Component c : components) {
            if (c.receiveAllEvents() || c.containsPoint(x, y)) {
                c.callEvent(new MouseMoveEvent(c, btn, x - c.getX(), y - c.getY()));
            }
        }
    }

    @Override
    public void mouseScroll(int btn, int x, int y, int amnt) {
        for (Component c : components) {
            if (c.receiveAllEvents() || c.containsPoint(x, y)) {
                c.callEvent(new MouseScrollEvent(c, btn, x - c.getX(), y - c.getY(), amnt));
            }
        }
    }

    @Override
    public void keyPress(int key, char ch) {
        if (this.focusedComponent != null) {
            this.focusedComponent.callEvent(new KeyPressEvent(this, key, ch));
        }
    }

    @Override
    public void keyRelease(int key, char ch) {
        if (this.focusedComponent != null) {
            this.focusedComponent.callEvent(new KeyReleaseEvent(this, key, ch));
        }
    }

    @Override
    protected void processEvents() {
        //Because we process our events first,
        //it allows us to pass our events down to sub components in 1 tick
        super.processEvents();
        for (Component c : components) {
            c.processEvents();
        }
    }

    @Override
    public boolean focusable() {
        return true;
    }
}

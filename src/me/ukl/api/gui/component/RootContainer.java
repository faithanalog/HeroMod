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
package me.ukl.api.gui.component;

import me.ukl.api.gui.Component;
import me.ukl.api.gui.Container;
import me.ukl.api.gui.Gui;

import org.lwjgl.opengl.*;

public class RootContainer extends Container {
    public RootContainer(Gui gui) {
        super();
        this.setGui(gui);
    }

    @Override
    public int getWidth() {
        return getGui().width;
    }

    @Override
    public int getHeight() {
        return getGui().height;
    }

    public boolean pausesGame() {
        return false;
    }

    @Override
    public void render() {
        this.pushClip();
        GL11.glPushMatrix();
        GL11.glTranslatef(getX(), getY(), 0);
        this.setClip(0, 0, getWidth(), getHeight());
        this.fillRect(0, 0, getWidth(), getHeight(), getBackground());
        for (Component c : getComponents()) {
            if (c.isVisible()) {
                c.render();
            }
        }
        GL11.glPopMatrix();
        this.popClip();
    }
}

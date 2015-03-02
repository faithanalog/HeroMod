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
package me.ukl.api.gui.renderer;

import me.ukl.api.gui.Gui;
import me.ukl.api.gui.component.RootContainer;
import me.ukl.api.util.Color;
import me.ukl.api.util.RenderUtil;

import org.lwjgl.opengl.*;

public class GuiRendererDepth extends AbstractGuiRenderer {
    @Override
    public void initGui(Gui gui) {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawScreen(Gui gui, RootContainer root, int mouseX, int mouseY,
                           float partialTick) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glTranslatef(0, 0, 1);
        root.render();    //Clip setup by root!
        GL11.glPopAttrib();
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        super.setClip(x, y, width, height);
        GL11.glDepthMask(true);    //Write to depth
        clearClip();
        GL11.glDepthFunc(GL11.GL_ALWAYS);
        GL11.glColorMask(false, false, false, false);

        RenderUtil.drawRect(x, y, width, height, Color.WHITE);

        GL11.glDepthMask(false);//No more writing to depth
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glColorMask(true, true, true, true);
        //        addClip(x, y, width, height);
    }

    @Override
    public void clearClip() {
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
    }
}

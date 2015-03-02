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

import java.nio.ByteBuffer;

import me.ukl.api.gui.Gui;
import me.ukl.api.gui.component.RootContainer;
import me.ukl.api.util.Color;
import me.ukl.api.util.RenderUtil;
import me.ukl.api.util.TextureUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.*;

public abstract class GuiRendererFBO extends AbstractGuiRenderer {
    //This is actually slower than using the depth buffer, might as well not bother but it works
//    private int fbo = -1;
//    private int stencilBuff = -1;
//    private int guiTex = -1;
//
//    public GuiRendererFBO() {
//        this.guiTex = GL11.glGenTextures();
//        TextureUtil.bind(guiTex);
//        TextureUtil.setMinFilter(GL11.GL_NEAREST);
//        TextureUtil.setMagFilter(GL11.GL_NEAREST);
//        TextureUtil.bind(0);
//
//        this.stencilBuff = GL30.glGenRenderbuffers();
//        this.fbo = GL30.glGenFramebuffers();
//    }
//
//    @Override
//    public void initGui(Gui gui) {
//        Minecraft mc = Minecraft.getMinecraft();
//        int width = mc.displayWidth;
//        int height = mc.displayHeight;
//        TextureUtil.bind(guiTex);
//        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
//        TextureUtil.bind(0);
//
//        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, stencilBuff);
//        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);
//        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
//
//        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
//        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, guiTex, 0);
//        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, stencilBuff);
//        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, stencilBuff);
//
//        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//    }
//
//    @Override
//    public void drawScreen(Gui gui, RootContainer root, int mouseX, int mouseY,
//                           float partialTick) {
//        GL11.glPushAttrib(GL11.GL_STENCIL_BUFFER_BIT);
//        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
//        GL11.glClearColor(0, 0, 0, 0F);
//        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glEnable(GL11.GL_STENCIL_TEST);
//        root.render();
//
//        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
//
//        TextureUtil.bind(guiTex);
//
//        GL11.glDisable(GL11.GL_BLEND);
//        Tessellator tes = Tessellator.instance;
//        tes.startDrawingQuads();
//        tes.addVertexWithUV(0, 0, 0, 0, 1);
//        tes.addVertexWithUV(0, gui.height, 0, 0, 0);
//        tes.addVertexWithUV(gui.width, gui.height, 0, 1, 0);
//        tes.addVertexWithUV(gui.width, 0, 0, 1, 1);
//        tes.draw();
//        GL11.glEnable(GL11.GL_BLEND);
//        GL11.glPopAttrib();
//    }
//
//    @Override
//    public void setClip(int x, int y, int width, int height) {
//        super.setClip(x, y, width, height);
//        GL11.glColorMask(false, false, false, false);
//        GL11.glDepthMask(false);
//        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
//        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);
//        GL11.glStencilMask(0xFF);
//        clearClip();
//        //Actually fill stencil area
//        RenderUtil.drawRect(x, y, width, height, Color.WHITE);
//
//        GL11.glColorMask(true, true, true, true);
//        GL11.glDepthMask(true);
//        GL11.glStencilMask(0x00);
//        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
//    }
//
//    @Override
//    public void clearClip() {
//        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
//    }
}

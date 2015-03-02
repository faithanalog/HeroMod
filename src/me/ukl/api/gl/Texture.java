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
package me.ukl.api.gl;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import me.ukl.api.util.TextureUtil;
import net.minecraft.util.ResourceLocation;
import static org.lwjgl.opengl.GL11.*;

public class Texture extends GLObject {
    public Texture(int width, int height) {
        super(glGenTextures());
        bind();
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        initializeFilters();
    }

    public Texture(BufferedImage image) {
        super(TextureUtil.loadTexture(image));
        initializeFilters();
    }

    public Texture(ResourceLocation loc) {
        super(TextureUtil.loadTexture(loc));
        initializeFilters();
    }

    private void initializeFilters() {
        setMinFilter(MinFilter.LINEAR);
        setMagFilter(MagFilter.LINEAR);
    }

    @Override
    public void bind() {
        TextureUtil.bind(getID());
    }

    @Override
    public void unbind() {
        TextureUtil.bind(0);
    }

    @Override
    protected GLGCObject getGLGCObject() {
        return new TextureGLGCObject(getID());
    }

    public static void setMinFilter(MinFilter filter) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter.getGLEnum());
    }

    public static void setMagFilter(MagFilter filter) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter.getGLEnum());
    }

    public static void setWrapU(TextureWrap wrap) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap.getGLEnum());
    }

    public static void setWrapV(TextureWrap wrap) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap.getGLEnum());
    }
}

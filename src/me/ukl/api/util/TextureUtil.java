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
package me.ukl.api.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public class TextureUtil {
    public static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    public static int[] getRGB(BufferedImage img) {
        int[] rgb = new int[img.getWidth() * img.getHeight()];
        img.getRGB(0, 0, img.getWidth(), img.getHeight(), rgb, 0, img.getWidth());
        return rgb;
    }

    public static int loadTexture(BufferedImage img) {
        return loadTexture(getRGB(img), img.getWidth(), img.getHeight());
    }

    public static int loadTexture(int[] rgb, int width, int height) {
        IntBuffer rgbBuf = BufferUtils.createIntBuffer(rgb.length);
        rgbBuf.put(rgb);
        rgbBuf.flip();

        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
                0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, rgbBuf);
        return tex;
    }

    public static void loadTextureMipmap(int tex, BufferedImage img) {
        int newWidth = nextPowerOf2(img.getWidth());
        int newHeight = nextPowerOf2(img.getHeight());
        if (newWidth == img.getWidth() && newHeight == img.getHeight()) {
            loadTextureMipmap(tex, getRGB(img), newWidth, newHeight);
        } else {
            //Resize to a power of 2. The aspect ratio isn't a problem because all texture access uses U/V coords anyway
            //
            //Just a note here, this REALLY screws up transparency it turns out. You can't mipmap
            //a non power of 2 texture though, so if it makes stuff look bad switch to a power of 2 texture
            //
            //Any problems caused by this resize would happen with a power of 2 mipmapped texture as well
            //(like pixels bleeding between tiles)
            BufferedImage newImg = new BufferedImage(newWidth, newHeight,
                    img.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = newImg.createGraphics();
            //Best scaling
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(img, 0, 0, newWidth, newHeight, null);
            loadTextureMipmap(tex, getRGB(newImg), newWidth, newHeight);
        }
    }

    //Requires pre-creation of texture to set filters first
    public static void loadTextureMipmap(int tex, int[] rgb, int width, int height) {
        IntBuffer rgbBuf = BufferUtils.createIntBuffer(rgb.length);
        rgbBuf.put(rgb);
        rgbBuf.flip();

        bind(tex);

        //If OpenGL30, use glGenerateMipmap, else use the GL_GENERATE_MIPMAP tex param
        if (RenderUtil.GL_30) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
                    0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, rgbBuf);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height,
                    0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, rgbBuf);
        }
    }

    public static ByteBuffer createImageBufferFrom(ResourceLocation location, boolean alpha) {
        ByteBuffer buffer = null;
        if (!Files.isDirectory(Paths.get(location.getResourcePath()))) {
            try {
                final BufferedImage image = ImageIO.read(MINECRAFT.getResourceManager().getResource(location).getInputStream());
                int[] pixels = getRGB(image);
                buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * ((alpha) ? 4 : 3));

                for (int y = 0; y < image.getHeight(); y++) {
                    for (int x = 0; x < image.getWidth(); x++) {
                        int pixel = pixels[y * image.getWidth() + x];
                        buffer.put((byte) ((pixel >> 16) & 0xFF));
                        buffer.put((byte) ((pixel >> 8) & 0xFF));
                        buffer.put((byte) (pixel & 0xFF));
                        buffer.put((byte) ((pixel >> 24) & 0xFF));
                    }
                }

                buffer.flip();
            } catch (IOException ignore) {
            }
        }
        return buffer;
    }

    public static int loadTexture(ResourceLocation location) {
        if (!Files.isDirectory(Paths.get(location.getResourcePath()))) {
            try {
                final BufferedImage image = ImageIO.read(MINECRAFT.getResourceManager().getResource(location).getInputStream());
                return loadTexture(image);
            } catch (IOException ignore) {
            }
        }
        return 0;
    }

    public static void setMinFilter(int filter) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
    }

    public static void setMagFilter(int filter) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
    }

    public static void setWrapS(int wrap) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrap);
    }

    public static void setWrapT(int wrap) {
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrap);
    }

    public static void bind(int tex) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
    }

    public static void delete(int tex) {
        GL11.glDeleteTextures(tex);
    }

    private static int nextPowerOf2(int val) {
        val--;
        val |= val >> 1;
        val |= val >> 2;
        val |= val >> 4;
        val |= val >> 8;
        val |= val >> 16;
        val++;
        return val;
    }
}

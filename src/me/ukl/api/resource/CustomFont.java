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
package me.ukl.api.resource;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.FloatBuffer;
import java.util.Arrays;

import me.ukl.api.gl.ArrayBuffer;
import me.ukl.api.gl.BufferAccess;
import me.ukl.api.gl.Texture;
import me.ukl.api.util.Color;
import me.ukl.api.util.TextureUtil;
import static org.lwjgl.opengl.GL11.*;

public class CustomFont {
    //Matches up to the color codes found here http://minecraft.gamepedia.com/Formatting_codes
    public static final Color[] DEFAULT_COLOR_PALETTE = new Color[] {
            new Color(0x000000),
            new Color(0x0000AA),
            new Color(0x00AA00),
            new Color(0x00AAAA),
            new Color(0xAA0000),
            new Color(0xAA00AA),
            new Color(0xFFAA00),
            new Color(0xAAAAAA),
            new Color(0x555555),
            new Color(0x5555FF),
            new Color(0x55FF55),
            new Color(0x55FFFF),
            new Color(0xFF5555),
            new Color(0xFF55FF),
            new Color(0xFFFF55),
            new Color(0xFFFFFF)
    };
    private static final ArrayBuffer FONT_VBO = new ArrayBuffer();
    //x,y,u,b
    private static final int FONT_STRIDE = (2 + 2 + 4) * 4;
    private static final int FONT_VERT_OFF = 0;
    private static final int FONT_UV_OFF = 2 * 4;
    private static final int FONT_COLOR_OFF = (2 + 2) * 4;
    public final int fontSize;
    private int descent;
    private int fontHeight;
    private Texture fontTexture;
    private FontChar[] charMap = new FontChar[256];
    private float scale = 1F;
    private int red = 0xFF;
    private int green = 0xFF;
    private int blue = 0xFF;
    private int alpha = 0xFF;
    private Color[] palette = DEFAULT_COLOR_PALETTE;

    /**
     * Creates a new font based on the awt Font object. <br>Will generate font map based on font's current size so resize it to the desired font size before font creation
     *
     * @param fnt Font to base font renderer off of
     */
    public CustomFont(Font fnt) {
        fontSize = fnt.getSize();
        BufferedImage ctxImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ctxGfx = ctxImg.createGraphics();
        ctxGfx.setFont(fnt);
        FontRenderContext ctx = ctxGfx.getFontRenderContext();
        descent = ctxGfx.getFontMetrics().getDescent();

        float maxWidth = 0;
        float maxHeight = 0;
        for (int i = 0; i < 256; i++) {
            char c = (char) i;
            charMap[i] = new FontChar();
            Rectangle2D bounds = fnt.getStringBounds(c + "", ctx);
            charMap[i].width = (float) Math.ceil(bounds.getWidth());
            charMap[i].height = (float) Math.ceil(bounds.getHeight());
            charMap[i].posX = (float) Math.ceil(bounds.getX());
            charMap[i].posY = (float) Math.ceil(bounds.getY());
            maxWidth = Math.max(maxWidth, charMap[i].width);
            maxHeight = Math.max(maxHeight, charMap[i].height);
        }
        int imgWidth = (int) Math.ceil(maxWidth) * 16;
        int imgHeight = (int) Math.ceil(maxHeight) * 16;
        int cellWidth = imgWidth / 16;
        int cellHeight = imgHeight / 16;
        fontHeight = cellHeight;
        for (int i = 0; i < 256; i++) {
            charMap[i].texWidth = charMap[i].width / imgWidth;
            charMap[i].texHeight = charMap[i].height / imgHeight;
        }
        BufferedImage fontImg = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        //We just created a TYPE_INT_ARGB image, so we know it has a DataBufferInt backing it
        int[] imgRGB = ((DataBufferInt) fontImg.getRaster().getDataBuffer()).getData();
        Arrays.fill(imgRGB, 0xFFFFFF);

        Graphics2D g2d = fontImg.createGraphics();
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(fnt);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        for (int i = 0; i < 256; i++) {
            char c = (char) i;
            int x = (int) ((i % 16) * cellWidth - charMap[i].posX);
            int y = (int) ((i / 16) * cellHeight - charMap[i].posY);
            g2d.drawString(c + "", x, y);
        }
        g2d.dispose();
        this.fontTexture = new Texture(fontImg);
    }

    public void setColorPalette(Color[] colors) {
        if (colors.length < 16) {
            throw new IllegalArgumentException("Palette must contain 16 colors");
        }
        this.palette = colors;
    }

    /**
     * Sets the filter to use when scaling the font. Filters should be either GL_LINEAR or GL_NEAREST
     *
     * @param min Filter to use when scaling font down
     * @param mag Filter to use when scaling font up
     * @return this font object, so this can be chained with the constructor
     */
    public CustomFont setFilter(int min, int mag) {
        this.fontTexture.bind();
        TextureUtil.setMinFilter(min);
        TextureUtil.setMagFilter(mag);
        return this;
    }

    /**
     * Sets color to use when drawing strings. Will set alpha to 100%
     *
     * @param r Red component, 0-255
     * @param g Green component, 0-255
     * @param b Blue component, 0-255
     */
    public void setColor(int r, int g, int b) {
        setColor(r, g, b, 0xFF);
    }

    /**
     * Sets color to use when drawing strings.
     *
     * @param r Red component, 0-255
     * @param g Green component, 0-255
     * @param b Blue component, 0-255
     * @param a Alpha component, 0-255
     */
    public void setColor(int r, int g, int b, int a) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
    }

    /**
     * Sets color to use when drawing strings.
     *
     * @param color Color to use
     */
    public void setColor(Color color) {
        setColor(color.getR(), color.getG(), color.getB(), color.getA());
    }

    /**
     * Sets size of font. This only scales the existing font, so consider creating new CustomFonts for sizes which are much smaller or larger than the current one.
     *
     * @param size New size of font
     */
    public void setSize(int size) {
        this.scale = size / (float) fontSize;
    }

    /**
     * Sets scale of font.
     *
     * @param scale New scale to use, 1.0 is default
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Draws a string at the specific coordinates. This does work in 3d, just glTranslate for positioning on the z axis
     *
     * @param str String to draw
     * @param x X coordinate of string
     * @param y Y coordinate of string baseline
     */
    public void drawString(String str, float x, float y) {
        int len = str.length();
        if (len == 0) {
            return;
        }
        float height = getCharHeight();

        glPushAttrib(GL_ENABLE_BIT | GL_CURRENT_BIT);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glDisable(GL_ALPHA_TEST);
        float r = red / 255F, g = green / 255F, b = blue / 255F, a = alpha / 255F;

        this.fontTexture.bind();
        FONT_VBO.bind();
        ArrayBuffer.orphan(FONT_STRIDE * len * 4);
        FloatBuffer data = ArrayBuffer.mapUnsync(FONT_STRIDE * len * 4, BufferAccess.WRITE).asFloatBuffer();

        int charsDrawn = 0;
        float translateX = x;
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);

            if (c == TextFormat.CONTROL) {
                char nxt = str.charAt(++i);
                int colorLoc = "0123456789abcdef".indexOf(Character.toLowerCase(nxt));
                if (colorLoc != -1) {
                    Color paletteColor = palette[colorLoc];
                    r = paletteColor.getRF();
                    g = paletteColor.getGF();
                    b = paletteColor.getBF();
                    a = paletteColor.getAF();
                } else if (nxt == 'r') {
                    r = red / 255F;
                    g = green / 255F;
                    b = blue / 255F;
                    a = alpha / 255F;
                } else if (nxt == '#') {
                    try {
                        String hex = str.substring(i + 1, i + 7);
                        i += 6;
                        Color col = new Color(Integer.parseInt(hex, 16));
                        r = col.getRF();
                        g = col.getGF();
                        b = col.getBF();
                        a = col.getAF();
                    } catch (IndexOutOfBoundsException e) {
                        throw new IllegalArgumentException("True-Color definitions must have 6 characters (ex: #FFFFFF)");
                    }
                }
                continue;
            }

            FontChar fchar = charMap[c];
            float width = getWidth(c);
            if (width == 0) {
                continue;
            }
            charsDrawn++;
            float u0 = c % 16 / 16F;
            float u1 = u0 + fchar.texWidth;
            float v0 = c / 16 / 16F;
            float v1 = v0 + 0.0625F;
            float cx = fchar.posX * scale + translateX;
            float cy = fchar.posY * scale + y;

            data.put(new float[] {
                    cx, cy, u0, v0, r, g, b, a,
                    cx, cy + height, u0, v1, r, g, b, a,
                    cx + width, cy + height, u1, v1, r, g, b, a,
                    cx + width, cy, u1, v0, r, g, b, a
            });
            translateX += width;
        }
        ArrayBuffer.unmap();
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glVertexPointer(2, GL_FLOAT, FONT_STRIDE, FONT_VERT_OFF);
        glTexCoordPointer(2, GL_FLOAT, FONT_STRIDE, FONT_UV_OFF);
        glColorPointer(4, GL_FLOAT, FONT_STRIDE, FONT_COLOR_OFF);
        glDrawArrays(GL_QUADS, 0, charsDrawn * 4);

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);

        FONT_VBO.unbind();
        glPopAttrib();
    }

    /**
     * Gets font size with current scale
     *
     * @return font size
     */
    public float getSize() {
        return scale * fontSize;
    }

    /**
     * Gets length of most descenders with current scale
     *
     * @return descent
     */
    public float getDescent() {
        return scale * descent;
    }

    //Gets full height of character cells
    private float getCharHeight() {
        return scale * fontHeight;
    }

    /**
     * Gets width of the string with current scale
     *
     * @param str String to check width of
     * @return Width of str
     */
    public float getWidth(String str) {
        int len = str.length();
        float width = 0;
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == TextFormat.CONTROL) {
                char nxt = str.charAt(++i);
                if (nxt == '#') {
                    i += 6;
                }
                continue;
            }
            width += getWidth(c);
        }
        return width;
    }

    /**
     * Gets width of a character with current scale
     *
     * @param c Character to get width of
     * @return width of c
     */
    public float getWidth(char c) {
        if (c > 0xFF) {
            return 0;
        }
        return scale * this.charMap[c].width;
    }
}

class FontChar {
    public float width;
    public float height;
    public float posX;
    public float posY;
    public float texWidth;
    public float texHeight;

    protected FontChar() {
    }
}

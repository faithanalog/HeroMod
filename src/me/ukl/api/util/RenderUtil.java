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

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_ARRAY;
import static org.lwjgl.opengl.GL11.GL_CURRENT_BIT;
import static org.lwjgl.opengl.GL11.GL_ENABLE_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LIGHTING_BIT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_COORD_ARRAY;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glColorPointer;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPushAttrib;
import static org.lwjgl.opengl.GL11.glShadeModel;
import static org.lwjgl.opengl.GL11.glTexCoordPointer;
import static org.lwjgl.opengl.GL11.glVertexPointer;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glMapBuffer;
import static org.lwjgl.opengl.GL15.glUnmapBuffer;
import static org.lwjgl.opengl.GL30.GL_MAP_UNSYNCHRONIZED_BIT;
import static org.lwjgl.opengl.GL30.GL_MAP_WRITE_BIT;
import static org.lwjgl.opengl.GL30.glMapBufferRange;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GLContext;

public class RenderUtil {
    public static final Tessellator TESSELLATOR = Tessellator.getInstance();
    public static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    public static final boolean GL_30 = GLContext.getCapabilities().OpenGL30;
    public static final int DIR_LEFTRIGHT = 0;
    public static final int DIR_UPDOWN = 1;
    public static final int DIR_RIGHTLEFT = 2;
    public static final int DIR_DOWNUP = 3;
    //We'll use VBOS! We can use this for all rendering ops
    private static final int VERT_BUFF = glGenBuffers();
    //=====Gradient buffer constants=====
    //Stride is (x,y,r,g,b,a) * 4 bytes per float
    private static final int GRADIENT_STRIDE = (2 + 4) * 4;
    //4 vertices in the buffer
    private static final int GRADIENT_SIZE = GRADIENT_STRIDE * 4;
    private static final int GRADIENT_VERT_OFF = 0;
    private static final int GRADIENT_COLOR_OFF = 2 * 4;
    //=====drawTexture buffer constants=====
    //(x,y,u,v) * 4 bytes per float
    private static final int TEX_STRIDE = (2 + 2) * 4;
    private static final int TEX_SIZE = TEX_STRIDE * 4;
    private static final int TEX_VERT_OFF = 0;
    private static final int TEX_UV_OFF = 2 * 4;
    //=====drawRect buffer constants=====
    //(x,y) * 4 bytes per float
    private static final int RECT_STRIDE = 2 * 4;
    private static final int RECT_SIZE = RECT_STRIDE * 4;

//    public static void create2DRectangleModal(double x, double y, double width, double height, double zLevel) {
//        TESSELLATOR.startDrawingQuads();
//        TESSELLATOR.addVertexWithUV(x + 0, y + height, zLevel, 0, 1);
//        TESSELLATOR.addVertexWithUV(x + width, y + height, zLevel, 1, 1);
//        TESSELLATOR.addVertexWithUV(x + width, y + 0, zLevel, 1, 0);
//        TESSELLATOR.addVertexWithUV(x + 0, y + 0, zLevel, 0, 0);
//    }

    /**
     * Draws a textured rectangle on the screen, stretching the current texture to fill the rectangle.
     *
     * @param x X coordinate of top left corner
     * @param y Y coordinate of top left corner
     * @param width Width of rectangle to draw
     * @param height Height of rectangle to draw
     */
    public static void drawTexture(float x, float y, float width, float height) {
        drawTexture(x, y, width, height, 0, 0, 1, 1);
    }

    /**
     * Draws a textured rectangle on the screen
     *
     * @param x1 X coordinate of top left corner
     * @param y1 Y coordinate of top left corner
     * @param width Width of rectangle to draw
     * @param height Height of rectangle to draw
     * @param u1 U texture coordinate of top left corner
     * @param v1 V texture coordinate of top left corner
     * @param u2 U texture coordinate of bottom right corner
     * @param v2 V texture coordinate of bottom right corner
     */
    public static void drawTexture(float x1, float y1, float width, float height, float u1, float v1, float u2, float v2) {
        float x2 = x1 + width, y2 = y1 + height;
        glBindBuffer(GL_ARRAY_BUFFER, VERT_BUFF);
        glBufferData(GL_ARRAY_BUFFER, TEX_SIZE, GL_STREAM_DRAW);
        FloatBuffer data = mapBufferWriteUnsync(GL_ARRAY_BUFFER, TEX_SIZE, null).asFloatBuffer();
        data.put(new float[] {
                x1, y1, u1, v1,
                x1, y2, u1, v2,
                x2, y2, u2, v2,
                x2, y1, u2, v1
        });
        glUnmapBuffer(GL_ARRAY_BUFFER);

        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
        glVertexPointer(2, GL_FLOAT, TEX_STRIDE, TEX_VERT_OFF);
        glTexCoordPointer(2, GL_FLOAT, TEX_STRIDE, TEX_UV_OFF);

        glDrawArrays(GL_QUADS, 0, 4);

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Draws a gradient on the screen
     *
     * @param x1 X coordinate of top left corner
     * @param y1 Y coordinate of top left corner
     * @param width Width of gradient
     * @param height Height of gradient
     * @param c1 First color of gradient
     * @param c2 Second color of gradient
     * @param direction Direction of gradient, either DIR_LEFTRIGHT, DIR_UPDOWN, DIR_RIGHTLEFT, or DIR_DOWNUP
     */
    public static void drawGradient(float x1, float y1, float width, float height, Color c1, Color c2, int direction) {
        if (direction == DIR_RIGHTLEFT || direction == DIR_DOWNUP) {
            Color tmpCol = c1;
            c1 = c2;
            c2 = tmpCol;
        }
        float x2 = x1 + width, y2 = y1 + height;
        float r1 = c1.getRF(), g1 = c1.getGF(), b1 = c1.getBF(), a1 = c1.getAF();
        float r2 = c2.getRF(), g2 = c2.getGF(), b2 = c2.getBF(), a2 = c2.getAF();

        glBindBuffer(GL_ARRAY_BUFFER, VERT_BUFF);
        //Orphan previous buffer, allocate new one
        //See the buffer re-specification section on
        //http://www.opengl.org/wiki/Buffer_Object_Streaming
        //for more info on buffer oprhaning
        glBufferData(GL_ARRAY_BUFFER, GRADIENT_SIZE, GL_STREAM_DRAW);

        //We can directory upload data to the buffer
        //by mapping it into ram.
        FloatBuffer data = mapBufferWriteUnsync(GL_ARRAY_BUFFER, GRADIENT_SIZE, null).asFloatBuffer();
        switch (direction) {
            case DIR_RIGHTLEFT:
            case DIR_LEFTRIGHT:
                data.put(new float[] {
                        x1, y1, r1, g1, b1, a1,
                        x1, y2, r1, g1, b1, a1,
                        x2, y2, r2, g2, b2, a2,
                        x2, y1, r2, g2, b2, a2
                });
                break;
            case DIR_DOWNUP:
            case DIR_UPDOWN:
                data.put(new float[] {
                        x1, y1, r1, g1, b1, a1,
                        x1, y2, r2, g2, b2, a2,
                        x2, y2, r2, g2, b2, a2,
                        x2, y1, r1, g1, b1, a1
                });
                break;
        }
        //Data is already in the buffer, don't need to flip or anything
        glUnmapBuffer(GL_ARRAY_BUFFER);

        //Save flags/shade model
        glPushAttrib(GL_ENABLE_BIT | GL_LIGHTING_BIT);
        glEnable(GL_BLEND);
        glDisable(GL_ALPHA_TEST);
        glDisable(GL_TEXTURE_2D);
        //MC sets this to flat,
        //and if it's flat we get no gradient
        glShadeModel(GL_SMOOTH);

        //glBindBuffer(GL_ARRAY_BUFFER, VERT_BUFF);
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_COLOR_ARRAY);

        glVertexPointer(2, GL_FLOAT, GRADIENT_STRIDE, GRADIENT_VERT_OFF);
        glColorPointer(4, GL_FLOAT, GRADIENT_STRIDE, GRADIENT_COLOR_OFF);

        //Actually draw the stuff!
        glDrawArrays(GL_QUADS, 0, 4);

        glDisableClientState(GL_VERTEX_ARRAY);
        glDisableClientState(GL_COLOR_ARRAY);

        glPopAttrib();
        //Unbind the buffer or OpenGL yells at us later
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Draws a horizontal gradient on the screen
     *
     * @param x X coordinate of top left corner
     * @param y Y coordinate of top left corner
     * @param width Width of gradient
     * @param height Height of gradient
     * @param left Left color of gradient
     * @param right Right color of gradient
     */
    public static void drawGradientLeftRight(float x, float y, float width, float height, Color left, Color right) {
        drawGradient(x, y, width, height, left, right, DIR_LEFTRIGHT);
    }

    /**
     * Draws a vertical gradient on the screen
     *
     * @param x X coordinate of top left corner
     * @param y Y coordinate of top left corner
     * @param width Width of gradient
     * @param height Height of gradient
     * @param top Top color of gradient
     * @param bottom Bottom color of gradient
     */
    public static void drawGradientUpDown(float x, float y, float width, float height, Color top, Color bottom) {
        drawGradient(x, y, width, height, top, bottom, DIR_UPDOWN);
    }

    /**
     * Draws a colored rectangle on the screen
     *
     * @param x1 X coordinate of top left corner
     * @param y1 Y coordinate of top left corner
     * @param width Width of rectangle
     * @param height Height of rectangle
     * @param color Color of rectangle
     */
    public static void drawRect(float x1, float y1, float width, float height, Color color) {
        float x2 = x1 + width, y2 = y1 + height;
        float r = color.getRF(), g = color.getGF(), b = color.getBF(), a = color.getAF();

        glBindBuffer(GL_ARRAY_BUFFER, VERT_BUFF);
        glBufferData(GL_ARRAY_BUFFER, RECT_SIZE, GL_STREAM_DRAW);
        FloatBuffer data = mapBufferWriteUnsync(GL_ARRAY_BUFFER, RECT_SIZE, null).asFloatBuffer();
        data.put(new float[] {
                x1, y1,
                x1, y2,
                x2, y2,
                x2, y1
        });
        glUnmapBuffer(GL_ARRAY_BUFFER);

        //GL_CURRENT_BIT saves current color4f
        glPushAttrib(GL_ENABLE_BIT | GL_CURRENT_BIT);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);

        //Since it's a solid color, we can use glColor4f
        //instead of putting the data in with the vertices
        glColor4f(r, g, b, a);

        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(2, GL_FLOAT, RECT_STRIDE, 0);
        glDrawArrays(GL_QUADS, 0, 4);
        glDisableClientState(GL_VERTEX_ARRAY);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glEnable(GL_TEXTURE_2D);
        glPopAttrib();
    }

    /**
     * Will use GL30 for faster maps if available. Make sure when using this to orphan the buffer as well.
     */
    public static ByteBuffer mapBufferWriteUnsync(int target, int size, ByteBuffer previous) {
        if (GL_30) {
            return glMapBufferRange(target, 0, size, GL_MAP_WRITE_BIT | GL_MAP_UNSYNCHRONIZED_BIT, previous);
        }
        return glMapBuffer(target, GL_WRITE_ONLY, size, previous);
    }
}

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

import org.lwjgl.opengl.*;

public class Color {
    public static final Color WHITE = new Color(0xFFFFFF);
    public static final Color BLACK = new Color(0x000000);
    public static final Color RED = new Color(0xFF0000);
    public static final Color GREEN = new Color(0x00FF00);
    public static final Color BLUE = new Color(0x0000FF);
    public static final Color YELLOW = new Color(0xFFFF00);
    public static final Color CYAN = new Color(0x00FFFF);
    public static final Color PURPLE = new Color(0xFF00FF);
    public static final Color DARK_GRAY = new Color(0x444444);
    public static final Color GRAY = new Color(0x888888);
    public static final Color LIGHT_GRAY = new Color(0xCCCCCC);
    public static final Color TRANSPARENT = new Color(0x000000, 0x00);
    private final int r, g, b, a;

    public Color(int r, int g, int b, int a) {
        this.r = r & 0xFF;
        this.g = g & 0xFF;
        this.b = b & 0xFF;
        this.a = a & 0xFF;
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 0xFF);
    }

    public Color(float r, float g, float b, float a) {
        this((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public Color(float r, float g, float b) {
        this(r, g, b, 1F);
    }

    public Color(int rgb) {
        this(rgb, 0xFF);
    }

    public Color(int rgb, int alpha) {
        this(rgb >> 16, rgb >> 8, rgb, alpha);
    }

    public Color(int rgba, boolean alpha) {
        this((rgba >> 8) & 0xFFFFFF, alpha ? rgba & 0xFF : 0xFF);
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    public int getA() {
        return a;
    }

    public int getRGB() {
        return (r << 16) | (g << 8) | b;
    }

    public int getRGBA() {
        return (r << 24) | (g << 16) | (b << 8) | a;
    }

    public float getRF() {
        return r / 255F;
    }

    public float getGF() {
        return g / 255F;
    }

    public float getBF() {
        return b / 255F;
    }

    public float getAF() {
        return a / 255F;
    }

    public float[] getRGBAF() {
        return new float[] {
                r / 255F,
                g / 255F,
                b / 255F,
                a / 255F
        };
    }

    public Color multiply(Color other) {
        int newR = r * other.r / 255;
        int newG = g * other.g / 255;
        int newB = b * other.b / 255;
        int newA = a * other.a / 255;
        return new Color(newR, newG, newB, newA);
    }

    public void setGLColor() {
        GL11.glColor4f(r / 255F, g / 255F, b / 255F, a / 255F);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Color color = (Color) o;

        if (a != color.a) {
            return false;
        }
        if (b != color.b) {
            return false;
        }
        if (g != color.g) {
            return false;
        }
        if (r != color.r) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = r;
        result = 31 * result + g;
        result = 31 * result + b;
        result = 31 * result + a;
        return result;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d,%d,%d]", r, g, b, a);
    }
}

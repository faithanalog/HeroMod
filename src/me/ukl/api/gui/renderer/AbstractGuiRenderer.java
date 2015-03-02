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

import java.util.ArrayDeque;
import java.util.Arrays;

public abstract class AbstractGuiRenderer implements GuiRenderer {
    private ArrayDeque<int[]> clipStack = new ArrayDeque<int[]>();
    private int[] curClip = new int[4];

    @Override
    public void setClip(int x, int y, int width, int height) {
        curClip[0] = x;
        curClip[1] = y;
        curClip[2] = width;
        curClip[3] = height;
    }

    @Override
    public void setSubClip(int x, int y, int width, int height, int curTransX, int curTransY) {
        int[] clip = getClip();
        int x1 = Math.max(x, clip[0] - curTransX);
        int y1 = Math.max(y, clip[1] - curTransY);
        int x2 = Math.min(x + width, clip[0] - curTransX + clip[2]);
        int y2 = Math.min(y + height, clip[1] - curTransY + clip[3]);
        setClip(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    public void pushClip() {
        clipStack.push(Arrays.copyOf(curClip, curClip.length));
    }

    @Override
    public void popClip() {
        if (!clipStack.isEmpty()) {
            int[] newClip = clipStack.pop();
            setClip(newClip[0], newClip[1], newClip[2], newClip[3]);
        }
    }

    @Override
    public int[] getClip() {
        return clipStack.peek();
    }
}

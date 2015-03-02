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
import me.ukl.api.util.Color;

import org.lwjgl.opengl.*;

public class ScrollPane extends Container {
    private Component innerComponent;
    private int vertScroll = 0;
    private int vertScrollWidth = 5;
    private boolean movingScrollBar = false;
    private int movingScrollBarInitY = 0;
    private int movingScrollBarInitBarY = 0;

    public ScrollPane(Component toWrap) {
        super();
        this.innerComponent = toWrap;
        super.addComponent(toWrap);
        setForeground(Color.DARK_GRAY);
    }

    public int getInnerHeight() {
        return innerComponent.getHeight();
    }

    public int getInnerWidth() {
        return getWidth() - getVertScrollbarWidth();
    }

    public int getVertScroll() {
        return vertScroll;
    }

    public int getMaxVertScroll() {
        return getInnerHeight() - getHeight();
    }

    public int getVertScrollbarWidth() {
        return this.vertScrollWidth;
    }

    public int getVertScrollbarHeight() {
        return (int) ((getHeight() - 2) * getVertScrollbarScale());
    }

    public float getVertScrollbarScale() {
        //return (getHeight() - 2) / (float)getMaxVertScroll();
        return getHeight() / (float) getInnerHeight();
    }

    public void setVertScroll(int pixels) {
        this.vertScroll = Math.max(0, Math.min(pixels, getMaxVertScroll()));
        this.innerComponent.setY(-getVertScroll());
    }

    public void setVertScroll(float pct) {
        pct = Math.max(0, Math.min(1, pct));
        this.vertScroll = (int) (getMaxVertScroll() * pct);
        this.innerComponent.setY(-getVertScroll());
    }

    public void setVertScrollbarWidth(int width) {
        this.vertScrollWidth = width;
    }

    @Override
    public void render() {
        this.pushClip();
        GL11.glPushMatrix();
        GL11.glTranslatef(getX(), getY(), 0);

        this.fillRect(0, 0, getWidth(), getHeight(), getBackground());
        //GL11.glTranslatef(0, -getVertScroll(), 0);
        //this.setClip(0, getVertScroll(), getWidth(), getHeight());
        this.setSubClip(0, 0, getWidth(), getHeight(), getX(), getY());
        if (innerComponent.isVisible()) {
            innerComponent.render();
        }

        float barTranslate = getVertScroll() / (float) getMaxVertScroll() * (getHeight() - getVertScrollbarHeight() - 2) + 1;
        //this.popClip();
        this.fillRect(getWidth() - vertScrollWidth, 0, vertScrollWidth, getHeight(), getForeground().multiply(new Color(0xA0A0A0)));
        GL11.glTranslatef(0, barTranslate, 0);
        this.fillRect(getWidth() - vertScrollWidth + 1, 0, vertScrollWidth - 2, getVertScrollbarHeight(), getForeground());
        GL11.glPopMatrix();
        this.popClip();
    }

    @Override
    public void mouseDown(int btn, int x, int y) {
        if (x > getWidth() - getVertScrollbarWidth()) {
            this.movingScrollBar = true;
            int toScroll = (int) ((y / getVertScrollbarScale()));
            if (toScroll < getVertScroll()) {
                this.setVertScroll(toScroll);
            } else if (toScroll > getVertScroll() + getVertScrollbarHeight() / getVertScrollbarScale()) {
                this.setVertScroll(toScroll - (int) (getVertScrollbarHeight() / getVertScrollbarScale()));
            }
            movingScrollBarInitY = y;
            movingScrollBarInitBarY = getVertScroll();
        } else {
            super.mouseDown(btn, x, y);
        }
    }

    @Override
    public void mouseUp(int btn, int x, int y) {
        if (movingScrollBar) {
            movingScrollBar = false;
        } else {
            super.mouseUp(btn, x, y);
        }
    }

    @Override
    public void mouseMove(int btn, int x, int y) {
        if (btn == -1) {
            movingScrollBar = false;
        }
        if (movingScrollBar) {
            int off = (int) ((y - movingScrollBarInitY) / getVertScrollbarScale());
            int scroll = off + movingScrollBarInitBarY;
            this.setVertScroll(scroll);
        } else {
            super.mouseMove(btn, x, y);
        }
    }

    @Override
    public void mouseScroll(int btn, int x, int y, int amnt) {
        float mult = 0.2F;
        amnt = (int) (amnt * mult);
        this.setVertScroll(getVertScroll() - amnt);
    }
}

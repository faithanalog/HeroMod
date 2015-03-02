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

import me.ukl.api.gui.Container;
import me.ukl.api.gui.event.ActionEvent;
import me.ukl.api.util.Color;

public class Button extends LabelBase {
    private Color overColor;
    private Color overTxtColor;
    private Color clickColor;
    private boolean clicked = false;

    public Button() {
        this("");
    }

    public Button(String text) {
        super();
        this.setText(text);
        this.setForeground(Color.WHITE);
        this.setBackground(new Color(0x6F, 0x6F, 0x6F));
        this.setOverColor(new Color(0x6F + 0x6, 0x6F + 0x13, 0x6F + 0x4C));
        this.setClickColor(new Color(overColor.getR(), overColor.getG() + 0x10, overColor.getB() + 0x20));
        this.setOverTextColor(new Color(0xFF, 0xFF, 0x88));
        setWidth(-1);
        setHeight(-1);
    }

    public void setOverColor(Color c) {
        this.overColor = c;
    }

    public void setClickColor(Color c) {
        this.clickColor = c;
    }

    public void setOverTextColor(Color c) {
        this.overTxtColor = c;
    }

    public Color getOverColor() {
        return this.overColor;
    }

    public Color getClickColor() {
        return this.clickColor;
    }

    public Color getOverTextColor() {
        return this.overTxtColor;
    }

    @Override
    public int getWidth() {
        if (super.getWidth() >= 0) {
            return super.getWidth();
        }
        this.getFont().setSize(this.getFontSize());
        return (int) (this.getFont().getWidth(this.getText()) + 6);
    }

    @Override
    public int getHeight() {
        if (super.getHeight() >= 0) {
            return super.getHeight();
        }
        this.getFont().setSize(this.getFontSize());
        return (int) (this.getFont().getSize() + 4);
    }

    @Override
    public void setParent(Container c) {
        super.setParent(c);
    }

    @Override
    public void render() {
        drawBackground();
        drawText();
    }

    protected void drawBackground() {
        int width = this.getWidth();
        int height = this.getHeight();
        int x = this.getX();
        int y = this.getY();

        Color dispColor = this.getBackground();
        if (containsMouse()) {
            if (clicked) {
                dispColor = this.getClickColor();
            } else {
                dispColor = this.getOverColor();
            }
        }
        this.fillRect(x, y, width, height, Color.BLACK);
        this.fillRect(x + 1, y + 1, width - 2, height - 2, dispColor);
    }

    protected void drawText() {
        int width = this.getWidth();
        int height = this.getHeight();
        int x = this.getX();
        int y = this.getY();

        getFont().setSize(getFontSize());
        int strWidth = (int) getFont().getWidth(getText());
        float strDescent = getFont().getDescent();

        Color txtColor = this.getForeground();
        if (containsMouse()) {
            txtColor = this.getOverTextColor();
        }
        getFont().setColor(txtColor);
        getFont().drawString(getText(), x + getWidth() / 2 - strWidth / 2, y + getHeight() - strDescent - 2);
    }

    @Override
    public boolean receiveAllEvents() {
        return true;
    }

    @Override
    public void mouseDown(int btn, int x, int y) {
        if (btn == 0 && this.containsMouse()) {
            this.clicked = true;
        }
    }

    @Override
    public void mouseUp(int btn, int x, int y) {
        if (btn == 0 && clicked && this.containsMouse()) {
            callEvent(new ActionEvent(this));
        }
        this.clicked = false;
    }
}

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

import me.ukl.api.util.Color;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.input.*;
import org.lwjgl.opengl.*;

public class TextField extends LabelBase {
    private Color overTxtColor;

    public TextField() {
        super();
        this.setText("");
        this.setBackground(Color.DARK_GRAY);
        this.setForeground(Color.WHITE);
        this.setOverTextColor(new Color(0xFF, 0xFF, 0x88));
        setHeight(-1);
    }

    @Override
    public void render() {
        int width = this.getWidth();
        int height = this.getHeight();
        int x = this.getX();
        int y = this.getY();

        this.fillRect(x, y, width, height, Color.BLACK);
        this.fillRect(x + 1, y + 1, width - 2, height - 2, this.getBackground());

        this.pushClip();
        this.setSubClip(x + 2, y, getWidth() - 4, getHeight(), 0, 0);

        GL11.glPushMatrix();

        getFont().setSize(getFontSize());
        int strWidth = (int) getFont().getWidth(getText());
        if (strWidth > width - 6) {
            GL11.glTranslatef(-(strWidth - (width - 6)), 0, 0);
        }
        float strDescent = getFont().getDescent();

        Color txtColor = this.getForeground();
        if (containsMouse()) {
            txtColor = this.getOverTextColor();
        }
        getFont().setColor(txtColor);
        getFont().drawString(getText(), x + 2, y + getHeight() - strDescent - 2);

        if (this.isFocused()) {
            this.fillRect(x + 2 + strWidth + 1, y + 2, 1, height - 4, txtColor);
        }

        GL11.glPopMatrix();

        this.popClip();
    }

    @Override
    public boolean focusable() {
        return true;
    }

    public void setOverTextColor(Color c) {
        this.overTxtColor = c;
    }

    public Color getOverTextColor() {
        return this.overTxtColor;
    }

    @Override
    public int getHeight() {
        if (super.getHeight() >= 0) {
            return super.getHeight();
        }
        //this.getFont().setSize(this.getFontSize());
        //return (int) (this.getFont().getSize() + 4);
        return (int) (this.getFontSize() + 4);
    }

    @Override
    public void keyPress(int key, char c) {
        if (key == Keyboard.KEY_BACK) {
            int curLen = this.getText().length();
            if (curLen > 0) {
                this.setText(this.getText().substring(0, curLen - 1));
            }
        } else if (ChatAllowedCharacters.isAllowedCharacter(c)) {
            this.setText(this.getText() + c);
        }
    }

    @Override
    public void keyRelease(int key, char c) {

    }
}

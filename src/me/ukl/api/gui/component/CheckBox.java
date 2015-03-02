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

import me.ukl.api.gui.event.ActionEvent;
import me.ukl.api.gui.event.EventHandler;
import me.ukl.api.util.Color;
import me.ukl.api.util.RenderUtil;

import org.lwjgl.opengl.*;

public class CheckBox extends Button {
    private boolean checked = false;
    private Color uncheckedColor = Color.DARK_GRAY;
    private Color checkedColor = new Color(0x00FF00);

    public CheckBox() {
        this(false);
    }

    public CheckBox(boolean initValue) {
        super();
        this.checked = initValue;
    }

    @Override
    public void render() {
        this.drawBackground();
        Color checkColor = isChecked() ? getCheckedColor() : getUncheckedColor();
        float checkScale = getHeight() - 4;
        GL11.glPushMatrix();
        GL11.glTranslatef(getX() + getWidth() / 2, getY() + getHeight() / 2, 0);
        GL11.glScalef(checkScale, checkScale, checkScale);
        GL11.glTranslatef(0.05F, 0F, 0F);
        GL11.glRotatef(-45, 0, 0, 1);
        RenderUtil.drawRect(-0.5F, 0, 1F, 0.2F, checkColor);
        GL11.glTranslatef(-0.5F, 0F, 0F);
        GL11.glRotatef(-90, 0, 0, 1);
        RenderUtil.drawRect(-0.2F, 0F, 1 / 3F + 0.2F, 0.2F, checkColor);
        GL11.glPopMatrix();
    }

    @EventHandler (priority = -1)
    public void onCheckClicked(ActionEvent evt) {
        setChecked(!isChecked());
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setCheckedColor(Color col) {
        this.checkedColor = col;
    }

    public void setUncheckedColor(Color col) {
        this.uncheckedColor = col;
    }

    public boolean isChecked() {
        return this.checked;
    }

    public Color getCheckedColor() {
        return this.checkedColor;
    }

    public Color getUncheckedColor() {
        return this.uncheckedColor;
    }
}

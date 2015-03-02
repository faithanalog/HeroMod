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

public class RadioButton extends CheckBox {
    private RadioGroup group;

    public RadioButton() {
        this(null);
    }

    public RadioButton(RadioGroup group) {
        super(false);
        if (group != null) {
            group.addButton(this);
            this.group = group;
        }
    }

    @Override
    public void render() {
        this.drawBackground();
        Color checkColor = isChecked() ? getCheckedColor() : getUncheckedColor();
        int radius = (getHeight() - 4) / 2;
        this.fillCircle(getX() + getWidth() / 2, getY() + getHeight() / 2, radius, checkColor);
    }

    @Override
    @EventHandler (priority = -1)
    public void onCheckClicked(ActionEvent evt) {
        setChecked(true);
    }

    @Override
    public void setChecked(boolean checked) {
        if (getGroup() != null) {
            getGroup().setCheckedButton(this);
        }
    }

    @Override
    public boolean isChecked() {
        if (getGroup() == null) {
            return false;
        }
        return getGroup().getCheckedButton() == this;
    }

    public void setGroup(RadioGroup group) {
        if (getGroup() != null) {
            getGroup().removeButton(this);
        }
        this.group = group;
        this.group.addButton(this);
    }

    public RadioGroup getGroup() {
        return this.group;
    }
}

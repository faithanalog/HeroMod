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

import java.awt.Font;

import me.ukl.api.gui.Component;
import me.ukl.api.resource.CustomFont;

public abstract class LabelBase extends Component {
    public static final CustomFont defaultFont;

    static {
        try {
//            defaultFont = new CustomFont(Font.createFont(Font.TRUETYPE_FONT, SpoutcraftMod.class.getResourceAsStream("/assets/spoutcraft/fonts/ubuntu-regular.ttf")).deriveFont(33f));
        	defaultFont = new CustomFont(new Font(Font.SANS_SERIF, Font.PLAIN, 27));
        } catch (Exception e) {
            throw new RuntimeException("Could not load font", e);
        }
    }

    private CustomFont font;
    private int fontSize;
    private String text;

    public LabelBase() {
        this.setFont(defaultFont);
        this.setFontSize(11);
    }

    public void setFont(CustomFont font) {
        this.font = font;
    }

    public void setFontSize(int size) {
        this.fontSize = size;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CustomFont getFont() {
        return this.font;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public String getText() {
        return this.text;
    }
}

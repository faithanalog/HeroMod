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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.ukl.api.resource.CustomFont;
import me.ukl.api.util.Color;

import org.lwjgl.opengl.*;

public class TextArea extends TextField {
    private String[] lines;
    private List<String> linesWithWrap = new ArrayList<String>();

    @Override
    public int getHeight() {
        int height = super.getHeight();
        if (height == -1) {
            //getFont().setSize(getFontSize());
            return getFontSize() * getLinesWithWrap().size() + 4;
        } else {
            return height;
        }
    }

    @Override
    public void render() {
        int width = this.getWidth();
        int height = this.getHeight();
        int x = this.getX();
        int y = this.getY();

        this.fillRect(x, y, width, height, Color.BLACK);
        this.fillRect(x + 1, y + 1, width - 2, height - 2, this.getBackground());
        List<String> lines = getLinesWithWrap();

        CustomFont font = getFont();
        font.setSize(getFontSize());
        float descent = getFont().getDescent();

        Color txtColor = this.getForeground();
        if (containsMouse()) {
            txtColor = this.getOverTextColor();
        }
        getFont().setColor(txtColor);

        GL11.glPushMatrix();
        GL11.glTranslatef(x + 2, 2, 0);
        for (String line : lines) {
            GL11.glTranslatef(0, getFontSize(), 0);
            font.drawString(line, 0, 0);
        }

        if (this.isFocused()) {
            this.fillRect((int) font.getWidth(lines.get(lines.size() - 1)) + 1, 0, 1, getFontSize(), txtColor);
        }
        GL11.glPopMatrix();
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        genLines();
        genLinesWithWrap();
    }

    public String[] getLines() {
        return lines;
    }

    public List<String> getLinesWithWrap() {
        return Collections.unmodifiableList(linesWithWrap);
    }

    private void genLines() {
        this.lines = this.getText().split("\n");
    }

    private void genLinesWithWrap() {
        List<String> lines = linesWithWrap;
        lines.clear();
        String[] preLines = getLines();

        CustomFont font = getFont();
        font.setSize(getFontSize());
        int width = getWidth();
        for (String line : preLines) {
            if (font.getWidth(line) <= width - 6) {
                lines.add(line);
            } else {
                StringBuilder nxtLine = new StringBuilder();
                String[] words = line.split(" +");
                float curWidth = 0;
                for (String word : words) {
                    float wordWidth = font.getWidth(word);
                    if (curWidth + wordWidth > width - 6) {
                        lines.add(nxtLine.toString());
                        nxtLine = new StringBuilder();
                        curWidth = 0;
                    }
                    curWidth += wordWidth;
                }
                if (nxtLine.length() > 0) {
                    lines.add(nxtLine.toString());
                }
            }
        }
    }
}

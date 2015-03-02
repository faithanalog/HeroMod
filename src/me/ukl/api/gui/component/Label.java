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

public class Label extends LabelBase {
    public Label() {
        this("");
    }

    public Label(String text) {
        super();
        this.setText(text);
        this.setWidth(-1);
        this.setHeight(-1);
    }

    @Override
    public int getWidth() {
    	int width = super.getWidth();
    	if(width == -1) {
	        this.getFont().setSize(this.getFontSize());
	        return (int) this.getFont().getWidth(this.getText());
    	} else {
    		return width;
    	}
    }

    @Override
    public int getHeight() {
    	int height = super.getHeight();
    	if(height == -1) {
    		return this.getFontSize();
    	} else {
    		return height;
    	}
    }

    @Override
    public void render() {
        this.fillRect(getX(), getY(), getWidth(), getHeight(), getBackground());
        this.getFont().setColor(this.getForeground());
        this.getFont().setSize(this.getFontSize());
        
        this.pushClip();
        if(super.getWidth() != -1 && super.getHeight() != -1) {
        	this.setSubClip(getX(), getY() - getFontSize(), getWidth(), getHeight(), 0, 0);
        }
        this.getFont().drawString(getText(), getX(), getY());
        this.popClip();
    }
}

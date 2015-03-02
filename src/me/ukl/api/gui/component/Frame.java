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
import java.util.List;

import me.ukl.api.gui.Component;
import me.ukl.api.gui.Container;
import me.ukl.api.resource.CustomFont;
import me.ukl.api.util.Color;

import org.lwjgl.opengl.GL11;

public class Frame extends Container {
    private static final CustomFont titleFont;

    static {
        try {
//            titleFont = new CustomFont(Font.createFont(Font.TRUETYPE_FONT, SpoutcraftMod.class.getResourceAsStream("/assets/spoutcraft/fonts/ubuntu-regular.ttf")).deriveFont(27f));
        	titleFont = new CustomFont(new Font(Font.SANS_SERIF, Font.PLAIN, 27));
        } catch (Exception e) {
            throw new RuntimeException("Could not load font", e);
        }
    }

    private Label frameTitle;
    private Container innerContainer;
    private Color titleBackColor = Color.DARK_GRAY;

    public Frame() {
        frameTitle = new Label("New Frame");
        frameTitle.setFont(titleFont);
        frameTitle.setFontSize(9);
        frameTitle.setForeground(Color.WHITE);
        titleFont.setSize(9);
        frameTitle.setY(11 - (int) titleFont.getDescent());
        innerContainer = new Container() {
	        @Override
			public boolean receiveAllEvents() {
				return Frame.this.receiveAllEvents();
			}
        };
        innerContainer.setX(1);
        innerContainer.setY(12);
        super.addComponent(innerContainer);
        super.addComponent(frameTitle);
        setWidth(3);
        setHeight(14);
        setBackground(Color.LIGHT_GRAY);
    }

    public void setTitle(String title) {
        frameTitle.setText(title);
    }

    public String getTitle() {
        return frameTitle.getText();
    }

    public void setTitleBackColor(Color c) {
        this.titleBackColor = c;
    }

    public Color getTitleBackColor() {
        return this.titleBackColor;
    }

    @Override
    public void setWidth(int width) {
        width = Math.max(width, 3);
        super.setWidth(width);
        innerContainer.setWidth(width - 2);
        frameTitle.setX(width / 2 - frameTitle.getWidth() / 2);
    }

    @Override
    public void setHeight(int height) {
        height = Math.max(height, 14);
        super.setHeight(height);
        innerContainer.setHeight(height - 13);
    }
    
    public void setInnerWidth(int width) {
    	setWidth(width + 2);
    }
    
    public void setInnerHeight(int height) {
    	setHeight(height + 13);
    }

    @Override
    public void addComponent(Component c) {
        innerContainer.addComponent(c);
    }

    @Override
    public void removeComponent(Component c) {
        innerContainer.addComponent(c);
    }

    @Override
    public void clearComponents() {
        innerContainer.clearComponents();
    }

    @Override
    public List<Component> getComponents() {
        return innerContainer.getComponents();
    }

    @Override
    public void render() {
        this.pushClip();
        GL11.glPushMatrix();
        GL11.glTranslatef(getX(), getY(), 0);
        this.setSubClip(0, 0, getWidth(), getHeight(), getX(), getY());
        //this.fillRect(0, 0, getWidth(), getHeight(), getBackground());
        this.fillRect(0, 0, getWidth(), getHeight(), Color.BLACK);
        this.fillRect(1, 1, getWidth() - 2, 10, titleBackColor);
        this.fillRect(1, 12, getWidth() - 2, getHeight() - 13, getBackground());
        for (Component c : super.getComponents()) {
            if (c.isVisible()) {
                c.render();
            }
        }
        GL11.glPopMatrix();
        this.popClip();
    }

    public int getInnerWidth() {
        return innerContainer.getWidth();
    }

    public int getInnerHeight() {
        return innerContainer.getHeight();
    }
}

package me.ukl.hmod.map;

import java.util.Random;

import me.ukl.api.gui.Component;
import me.ukl.api.gui.Gui;
import me.ukl.api.gui.component.Button;
import me.ukl.api.gui.component.Frame;
import me.ukl.api.gui.component.Rectangle;
import me.ukl.api.gui.component.Slider;
import me.ukl.api.gui.component.TextField;
import me.ukl.api.gui.event.ActionEvent;
import me.ukl.api.gui.event.EventHandler;
import me.ukl.api.gui.event.SliderEvent;
import me.ukl.api.gui.event.mouse.MouseDownEvent;
import me.ukl.api.resource.TextFormat;
import me.ukl.api.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class GuiCreateWaypoint extends Gui {
	
	private TextField xfield;
	private TextField yfield;
	private TextField zfield;
	private TextField namefield;
	private Slider rslider;
	private Slider gslider;
	private Slider bslider;
	private Rectangle color;
	private Button confirmbtn;
	private Button cancelbtn;
	private Frame frm;
	private Minimap map;
	
	public GuiCreateWaypoint(int initX, int initY, int initZ, Minimap map) {
		this.map = map;
		xfield = new TextField();
		yfield = new TextField();
		zfield = new TextField();
		namefield = new TextField();
		
		rslider = new Slider();
		gslider = new Slider();
		bslider = new Slider();
		
		color = new Rectangle();
		
		confirmbtn = new Button("Create");
		cancelbtn = new Button("Cancel");
		
		rslider.setMin(0);
		gslider.setMin(0);
		bslider.setMin(0);
		
		rslider.setMax(255);
		gslider.setMax(255);
		bslider.setMax(255);
		
		color.setWidth(200);
		color.setHeight(16);
		
		xfield.setText(Integer.toString(initX));
		yfield.setText(Integer.toString(initY));
		zfield.setText(Integer.toString(initZ));
		namefield.setText("Waypoint Name");
		
		int height = 1;
		
		Component[] comps = new Component[] {
				xfield, yfield, zfield, namefield, rslider, gslider, bslider, color
		};
		
		frm = new Frame() {
			@Override
			public boolean receiveAllEvents() {
				return true;
			}
		};
		frm.setTitle("Create Waypoint");
		frm.setInnerWidth(153);
		
		for(Component c : comps) {
			c.setWidth(151);
			c.setX(1);
			c.setY(height);
			height += c.getHeight() + 1;
			frm.addComponent(c);
		}
		confirmbtn.setWidth(75);
		confirmbtn.setX(1 + 75 + 1);
		confirmbtn.setY(height);
		frm.addComponent(confirmbtn);
		
		cancelbtn.setWidth(75);
		cancelbtn.setX(1);
		cancelbtn.setY(height);
		frm.addComponent(cancelbtn);
		
		
//		pane.setHeight(height);
		frm.setBackground(Color.DARK_GRAY);
		frm.setInnerHeight(height + confirmbtn.getHeight() + 1);
		
		rslider.addEventListeners(this);
		gslider.addEventListeners(this);
		bslider.addEventListeners(this);
		namefield.addEventListeners(this);
		confirmbtn.addEventListeners(this);
		cancelbtn.addEventListeners(this);
		
		Random rand = new Random();
		
		rslider.setValue(rand.nextInt(256));
		gslider.setValue(rand.nextInt(256));
		bslider.setValue(rand.nextInt(256));
		
		setSliders();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.getRoot().addComponent(frm);
		frm.setX(width / 2 - frm.getWidth() / 2);
		frm.setY(height / 2 - frm.getHeight() / 2);
	}
	
	@EventHandler
	public void onSliderMove(SliderEvent e) {
		setSliders();
	}
	
	@EventHandler
	public void onBtnClick(ActionEvent e) {
		if(e.getSource() == confirmbtn) {
			try {
				int x = Integer.parseInt(xfield.getText());
				int y = Integer.parseInt(yfield.getText());
				int z = Integer.parseInt(zfield.getText());
				int color = rslider.getValue() << 16 | gslider.getValue() << 8 | bslider.getValue();
				String name = namefield.getText();
				if(namefield.getText().equals("Waypoint Name")) {
					name = "Waypoint";
				}
				
				Waypoint point = new Waypoint(name, x, y, z, color, Waypoint.TYPE_NORMAL);
				
				map.getWaypoints().add(point);
				map.saveCurrentWaypoints();
			} catch (Exception e1) {
				Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(TextFormat.DARK_RED + "Error creating waypoint!"));
			}
			Minecraft.getMinecraft().displayGuiScreen(null);
		} else if(e.getSource() == cancelbtn) {
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
	}
	
	@EventHandler
	public void onNameClick(MouseDownEvent e) {
		if(e.getSource() == namefield) {
			if(namefield.getText().equals("Waypoint Name")) {
				namefield.setText("");
			}
		}
	}
	
	private void setSliders() {
		color.setForeground(new Color(rslider.getValue(), gslider.getValue(), bslider.getValue()));
		rslider.setText("Red: " + rslider.getValue());
		gslider.setText("Green: " + gslider.getValue());
		bslider.setText("Blue: " + bslider.getValue());
	}

}

package me.ukl.hmod.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import me.ukl.api.gui.Container;
import me.ukl.api.gui.Gui;
import me.ukl.api.gui.component.Button;
import me.ukl.api.gui.component.CheckBox;
import me.ukl.api.gui.component.Frame;
import me.ukl.api.gui.component.Label;
import me.ukl.api.gui.component.Rectangle;
import me.ukl.api.gui.component.ScrollPane;
import me.ukl.api.gui.event.ActionEvent;
import me.ukl.api.gui.event.EventHandler;
import me.ukl.api.util.Color;
import net.minecraft.client.Minecraft;

public class GuiDeleteWaypoint extends Gui {
	
	private Map<CheckBox, Waypoint> waypointMap = new HashMap<CheckBox, Waypoint>();
	private List<CheckBox> checks = new ArrayList<CheckBox>();
	
	private Frame frame;
	private Button confirm;
	private Button cancel;
	private Minimap map;
	
	public GuiDeleteWaypoint(Minimap map) {
		this.map = map;
		frame = new Frame();
		frame.setTitle("Delete Waypoints");
		frame.setInnerWidth(203);
		frame.setInnerHeight(128);
		frame.setBackground(Color.DARK_GRAY);
		
		Container pane = new Container();
		ScrollPane scroll = new ScrollPane(pane);
		pane.setWidth(frame.getInnerWidth() - scroll.getVertScrollbarWidth() - 2);
		pane.setX(1);
		int height = 1;
		
		List<Waypoint> pts = new ArrayList<Waypoint>();
		pts.addAll(map.getWaypoints());
		pts.addAll(map.getDeathpoints());
		for(Waypoint point : pts) {
			
			Rectangle rect = new Rectangle(new Color(point.getColor()));
			Label lbl = new Label(String.format("[%d,%d,%d] %s", point.getX(), point.getY(), point.getZ(), point.getName()));
			CheckBox box = new CheckBox();
			box.setCheckedColor(new Color(0xFF4000));
			waypointMap.put(box, point);
			
			lbl.setX(0);
			lbl.setY(lbl.getFontSize());
			lbl.setWidth(pane.getWidth() - 16 - 16 - 1 - 1);
			lbl.setHeight(16);
			if(point.getType() == Waypoint.TYPE_DEATH) {
				lbl.setForeground(Color.RED);
			}
			
			box.setX(pane.getWidth() - 16);
			box.setWidth(16);
			box.setHeight(16);
			
			rect.setX(pane.getWidth() - 16 - 16 - 1);
			rect.setWidth(box.getWidth());
			rect.setHeight(box.getHeight());
			
			Container pointpane = new Container();
			pointpane.setWidth(pane.getWidth());
			pointpane.setHeight(box.getHeight());
			pointpane.addComponent(lbl);
			pointpane.addComponent(box);
			pointpane.addComponent(rect);
			pointpane.setY(height);
			height += pointpane.getHeight() + 1;
			pane.addComponent(pointpane);
		}
		
		pane.setHeight(height - 1);
		
		scroll.setX(0);
		scroll.setY(0);
		scroll.setWidth(frame.getInnerWidth());
		
		confirm = new Button("Delete");
		confirm.setWidth(100);
		confirm.setX(100 + 1 + 1);
		confirm.setY(frame.getInnerHeight() - 1 - confirm.getHeight());
		
		cancel = new Button("Cancel");
		cancel.setWidth(100);
		cancel.setX(1);
		cancel.setY(confirm.getY());
		
		confirm.addEventListeners(this);
		cancel.addEventListeners(this);
		
		scroll.setHeight(frame.getInnerHeight() - 1 - confirm.getHeight() - 1);
		
		frame.addComponent(scroll);
		frame.addComponent(confirm);
		frame.addComponent(cancel);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.getRoot().addComponent(frame);
		frame.setX(width / 2 - frame.getWidth() / 2);
		frame.setY(height / 2 - frame.getHeight() / 2);
	}
	
	@EventHandler
	public void onBtnClick(ActionEvent e) {
		if(e.getSource() == confirm) {
			for(Entry<CheckBox, Waypoint> pts : waypointMap.entrySet()) {
				if(pts.getKey().isChecked()) {
					Waypoint pt = pts.getValue();
					if(pt.getType() == Waypoint.TYPE_NORMAL) {
						map.getWaypoints().remove(pt);
					} else if(pt.getType() == Waypoint.TYPE_DEATH) {
						map.getDeathpoints().remove(pt);
					}
					try {
						map.saveCurrentWaypoints();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			Minecraft.getMinecraft().displayGuiScreen(null);
		} else if(e.getSource() == cancel) {
			Minecraft.getMinecraft().displayGuiScreen(null);
		}
	}

}

package com.potar.videoanalizer.gui.component;

import com.potar.AnalizadorVideo;

public abstract class Component {
	protected AnalizadorVideo app;
	protected float posX;
	protected float posY;
	
	public Component(AnalizadorVideo app, float posX, float posY) {
		this.app = app;
		this.posX = posX;
		this.posY = posY;
	}
	
	public abstract void draw();
}

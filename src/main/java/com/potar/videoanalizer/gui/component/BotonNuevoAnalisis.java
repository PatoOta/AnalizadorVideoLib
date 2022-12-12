package com.potar.videoanalizer.gui.component;

import com.potar.AnalizadorVideo;

import processing.core.PApplet;

public class BotonNuevoAnalisis extends Component implements Boton {
	private boolean overBotonNuevoAnalisis = false;
	private final int newWidth = 45;
	private final int newHeight = 90;
	private int newDoorAngle = 0;


	public BotonNuevoAnalisis(AnalizadorVideo app, float posX, float posY) {
		super(app, posX, posY);
	}

	@Override
	public boolean over(float x, float y) {
		if (posX <= x && x <= posX + newWidth && posY <= y && y <= posY + newHeight) {
			return overBotonNuevoAnalisis = true;
		} else {
			return overBotonNuevoAnalisis = false;
		}
	}
	

	@Override
	public void mousePressed() {
	}

	@Override
	public void draw() {
		app.pushStyle();

		//Puerta de nuevo fondo negro
		app.noStroke();
		app.fill(0);
		app.rect(posX, posY, newWidth, newHeight);

		//little open field
		app.fill(255);
		app.rect(posX+(newWidth/2.0f)-(newWidth/2.0f/3.0f), posY+newHeight/7.0f, newWidth/3.0f, newWidth/3.0f);

		//little cross maze
		app.rect(posX+(newWidth/2.0f)-(newWidth/2.0f/6.0f), posY+newHeight*3/7.0f, newWidth/6.0f, newWidth/3.0f);
		app.rect(posX+(newWidth/2.0f)-(newWidth/2.0f/3.0f), posY+newHeight*3/7.0f + newWidth/6.0f/2.0f, newWidth/3.0f, newWidth/6.0f);

		//little Y-maze
		app.noFill();
		app.stroke(255);
		app.strokeWeight(4);
		app.beginShape();
		app.vertex(posX+(newWidth/2.0f)-(newWidth/2.0f/3.0f), posY+newHeight*5/7.0f);
		app.vertex(posX+(newWidth/2.0f)-(newWidth/2.0f/3.0f) + newWidth/3.0f/2.0f, posY+newHeight*5/7.0f + newWidth/3.0f/2.0f);
		app.vertex(posX+(newWidth/2.0f)-(newWidth/2.0f/3.0f) + newWidth/3.0f, posY+newHeight*5/7.0f);
		app.vertex(posX+(newWidth/2.0f)-(newWidth/2.0f/3.0f) + newWidth/3.0f/2.0f, posY+newHeight*5/7.0f + newWidth/3.0f/2.0f);
		app.vertex(posX+(newWidth/2.0f)-(newWidth/2.0f/3.0f) + newWidth/3.0f/2.0f, posY+newHeight*5/7.0f + newWidth/3.0f);
		app.endShape();
		app.strokeWeight(1);

		//La puerta de nuevo
		app.pushMatrix();
		app.translate(posX + newWidth, posY);
		app.shearY(PApplet.radians(newDoorAngle));
		app.stroke(0);
		app.strokeWeight(1);
		app.fill(255);
		app.rect((newDoorAngle/2)-newWidth, 0, newWidth-(newDoorAngle/2), newHeight);

		newDoorAngle += overBotonNuevoAnalisis ? 1 : -1;
		if (newDoorAngle > 80) {
			newDoorAngle = 80;
		} else if (newDoorAngle < 0) {
			newDoorAngle = 0;
		}
		app.popMatrix();

		app.noStroke();
		app.fill(app.getGraphics().backgroundColor);
		app.rect(posX-newWidth, posY-newHeight, newWidth*3, newHeight);

		//El marco
		app.stroke(0);
		app.strokeWeight(1);
		app.noFill();
		app.rect(posX-2, posY-2, newWidth+4, newHeight+4);

		app.stroke(255);
		app.strokeWeight(3);
		app.noFill();
		app.rect(posX, posY, newWidth, newHeight);

		app.popStyle();
	}

}

package com.potar.videoanalizer.runtime.data;

import java.io.PrintWriter;

import com.potar.videoanalizer.utils.Pair;

import processing.core.PApplet;
import processing.core.PVector;

public class LineaInterna {
	PVector[] shapePoints;
	int aIndex;
	int bIndex;
	int aHaciaPuntoIndex;
	int bHaciaPuntoIndex;
	boolean editable;
	float porcentage = 0;// 0 es la linea entre a y b, y 1 es entre la linea ente los puntos aHacia y
	// bHacia, cualquier otro valor intermedio es la línea intermedia extrapolada
	// de a-b hacia aHacia-bHacia

	public LineaInterna(PVector[] shapePoints, int aPointIndex, int bPointIndex, float percentage, boolean editable) {
		this.shapePoints = shapePoints;
		this.aIndex = aPointIndex;
		this.bIndex = bPointIndex;
		this.aHaciaPuntoIndex = Math.floorMod(aPointIndex + 1, shapePoints.length);
		this.bHaciaPuntoIndex = Math.floorMod(bPointIndex - 1, shapePoints.length);
		this.porcentage = percentage;
		this.editable = editable;
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	
	public float getPorcentage() {
		return porcentage;
	}
	
	public void setPorcentage(float porcentage) {
		this.porcentage = porcentage;
	}

	public PVector getA() {
		return shapePoints[aIndex];
	}

	public PVector getB() {
		return shapePoints[bIndex];
	}

	public PVector getAHacia() {
		return shapePoints[aHaciaPuntoIndex];
	}

	public PVector getBHacia() {
		return shapePoints[bHaciaPuntoIndex];
	}

	public Pair<PVector, PVector> getABDesplazamiento() {
		PVector aPoint = getA();
		PVector bPoint = getB();
		PVector aHaciaPoint = getAHacia();
		PVector bHaciaPoint = getBHacia();
		PVector aPointDisplaced = PVector.sub(aHaciaPoint, aPoint).mult(porcentage).add(aPoint);
		PVector bPointDisplaced = PVector.sub(bHaciaPoint, bPoint).mult(porcentage).add(bPoint);
		return new Pair<PVector, PVector>(aPointDisplaced, bPointDisplaced);
	}

	public Pair<PVector, PVector> getABDisplaced(float newPercentage) {
		PVector aPoint = getA();
		PVector bPoint = getB();
		PVector aPointDisplaced = PVector.sub(getAHacia(), aPoint).mult(newPercentage).add(aPoint);
		PVector bPointDisplaced = PVector.sub(getBHacia(), bPoint).mult(newPercentage).add(bPoint);
		return new Pair<PVector, PVector>(aPointDisplaced, bPointDisplaced);
	}

	public Pair<PVector, PVector> getABDisplaced(PVector editingPoint, PVector newPoint) {
		PVector aPoint = editingPoint != getA() ? getA() : newPoint;
		PVector bPoint = editingPoint != getB() ? getB() : newPoint;
		PVector aPointDisplaced = PVector.sub(editingPoint != getAHacia() ? getAHacia() : newPoint, aPoint).mult(porcentage).add(aPoint);
		PVector bPointDisplaced = PVector.sub(editingPoint != getBHacia() ? getBHacia() : newPoint, bPoint).mult(porcentage).add(bPoint);
		return new Pair<PVector, PVector>(aPointDisplaced, bPointDisplaced);
	}

	public float getABDisplacedMinDistTo(PVector toPoint) {
		PVector aPoint = getA();
		PVector bPoint = getB();
		PVector aPointDisplaced = PVector.sub(getAHacia(), aPoint).mult(porcentage).add(aPoint);
		PVector bPointDisplaced = PVector.sub(getBHacia(), bPoint).mult(porcentage).add(bPoint);

		return PApplet.min(aPointDisplaced.dist(toPoint), bPointDisplaced.dist(toPoint));
	}
	
	public void print(PrintWriter output) {
		output.println(String.format(Parametros.locale, "%d\t%d\t%.2f\t%b", Integer.valueOf(aIndex), Integer.valueOf(bIndex), Float.valueOf(porcentage), Boolean.valueOf(editable)));		
	}
}
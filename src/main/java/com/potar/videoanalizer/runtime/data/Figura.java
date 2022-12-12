package com.potar.videoanalizer.runtime.data;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.potar.AnalizadorVideo;
import com.potar.videoanalizer.utils.Pair;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public abstract class Figura {
	private static final int MAXIMA_DISTANCIA_PARA_EDITAR_LINEA_INTERNA = 100;
	private static final int MAXIMA_DISTANCIA_PARA_EDITAR_PUNTO = 45;
	
	//El grosor de las lineas para enmascarar
	private int grosorLineasEnmascarado;

	// Mapea entre idRegion y la lista de puntos que forman la región (ordenados en sentido horario)
	protected Map<Integer, List<PVector>> regionPorIdRegion;
	
	// Mapea entre idRegion y el canvas dónde está "dibujada" solamente esta región. Se usa para verificar si el x,y cae en esta región o no
	protected Map<Integer, PGraphics> canvasPorIdRegion;

	public abstract PVector[] getPuntosFigura();
	public abstract LineaInterna[] getLineasInternas();
	protected abstract void extraRegionCalcInits();
	protected abstract void extraRegionCalcs(AnalizadorVideo app, Integer idRegion);
	protected abstract void extraRegionData(RegionData result, Integer idDeLaRegion);

	public int getGrosorLineasEnmascarado() {
		return grosorLineasEnmascarado;
	}
	
	public void setGrosorLineasEnmascarado(int grosorLineasEnmascarado) {
		this.grosorLineasEnmascarado = grosorLineasEnmascarado;
	}
	
	public void draw(AnalizadorVideo app) {
		drawForma(app);
		drawLineasInternas(app);
	}
	
	//Dibuja las líneas externas de la figura 
	private void drawForma(AnalizadorVideo app) {
		PVector[] puntosDeLaFigura = getPuntosFigura();
		app.pushStyle();
		app.noFill();
		if (grosorLineasEnmascarado > 0) {
			app.strokeWeight(grosorLineasEnmascarado);
			app.stroke(0, 0, 255);
		} else {
			app.strokeWeight(1);
			app.stroke(0, 200, 0);
		}
		app.beginShape();
		for (PVector puntoDeLaFigura : puntosDeLaFigura) {
			app.vertex(puntoDeLaFigura.x, puntoDeLaFigura.y);
		}
		app.endShape(PConstants.CLOSE);
		app.popStyle();

	}
	
	//Dibuja las líneas internas de la figura
	private void drawLineasInternas(AnalizadorVideo app) {
		app.pushStyle();
		if (grosorLineasEnmascarado > 0) {
			app.strokeWeight(grosorLineasEnmascarado);
		} else {
			app.strokeWeight(1);
		}

		for(LineaInterna line : getLineasInternas()) {
			Pair<PVector, PVector> points = line.getABDesplazamiento();
			PVector aPointDiff = points.getKey();
			PVector bPointDiff = points.getValue();

			if (grosorLineasEnmascarado > 0) {
				app.stroke(0, 0, 255);
			} else if (line.isEditable()) {
				app.stroke(220, 0, 0);
			} else {
				app.stroke(0, 220, 0);
			}

			app.line(aPointDiff.x, aPointDiff.y, bPointDiff.x, bPointDiff.y);
		}
		app.popStyle();
	}

	
	//Verifica que la figura no se superponga a sí misma con la modificación del punto que se está editando
	public boolean verificarFigura(PVector editingPoint, float newX, float newY) {
		//Se verifica que ninguna línea de la forma se auto intersecte
		PVector nuevoPunto = new PVector(newX, newY);
		
		//Se recorren todos los puntos y el siguiente, y se verifica que no se cruce con ninguna línea que forma la figura. Y que tampoco cruce ninguna línea interna
		for (int i = 0; i < getPuntosFigura().length; i++) {
			PVector iPoint = getPuntosFigura()[i];
			PVector aPoint = iPoint != editingPoint ? iPoint : nuevoPunto;

			PVector iPlusPoint = getPuntosFigura()[Math.floorMod(i+1, getPuntosFigura().length)];
			PVector bPoint = iPlusPoint != editingPoint ? iPlusPoint : nuevoPunto;

			for (int j = 0; j < getPuntosFigura().length; j++) {
				PVector jPoint = getPuntosFigura()[j];
				PVector cPoint = jPoint != editingPoint ? jPoint : nuevoPunto;

				PVector jPlusPoint = getPuntosFigura()[Math.floorMod(j+1, getPuntosFigura().length)];
				PVector dPoint = jPlusPoint != editingPoint ? jPlusPoint : nuevoPunto;

				if (aPoint == cPoint || bPoint == cPoint || aPoint == dPoint || bPoint == dPoint ) {
					continue;
				}
				PVector intersectionPoint = intersectionPoint(aPoint.x, aPoint.y, bPoint.x, bPoint.y, cPoint.x, cPoint.y, dPoint.x, dPoint.y);
				if (intersectionPoint != null) {
					return false;
				}
			}

			//Ahora se verifica que no se cruce con las líneas internas
			for(LineaInterna line : getLineasInternas()) {
				PVector interLinePointA = line.getA();
				PVector interLinePointB = line.getB();
				PVector aPointLineInternal = interLinePointA != editingPoint ? interLinePointA : nuevoPunto;
				PVector bPointLineInternal = interLinePointB != editingPoint ? interLinePointB : nuevoPunto;

				if (line.getPorcentage() == 0) {
					if (aPoint == aPointLineInternal || bPoint == aPointLineInternal || aPoint == bPointLineInternal || bPoint == bPointLineInternal ) {
						continue;
					}

					PVector intersectionPoint = intersectionPoint(aPoint.x, aPoint.y, bPoint.x, bPoint.y, aPointLineInternal.x, aPointLineInternal.y, bPointLineInternal.x, bPointLineInternal.y);
					if (intersectionPoint != null) {
						return false;
					}
				} else {
					//Tampoco debería cruzar ninguna línea desplazada

					Pair<PVector, PVector> points = line.getABDisplaced(editingPoint, nuevoPunto);
					PVector aPointDiff = points.getKey();
					PVector bPointDiff = points.getValue();

					PVector intersectionPoint = intersectionPoint(aPoint.x, aPoint.y, bPoint.x, bPoint.y, aPointDiff.x, aPointDiff.y, bPointDiff.x, bPointDiff.y, true);
					if (intersectionPoint != null) {
						return false;
					}
				}
			}

		}  
		return true;
	}
	
	//Verifica que la figura no se superponga a sí misma con la modificación de la línea que se está editando
	//editingLine == null signitica que se editan todos las líneas a la vez
	public boolean checkShape(LineaInterna editingLine, float newPorcentage) {
		//Se recorren todos los puntos y el siguiente, y se verifica que no se cruce con ninguna línea que forma la figura. Y que tampoco cruce ninguna línea interna
		for (int i = 0; i < getPuntosFigura().length; i++) {
			PVector aPoint = getPuntosFigura()[i];
			PVector bPoint = getPuntosFigura()[Math.floorMod(i+1, getPuntosFigura().length)];
			//Ahora se verifica que no se cruce con las líneas internas
			for(LineaInterna line : getLineasInternas()) {
				PVector aPointLineInternal = line.getA();
				PVector bPointLineInternal = line.getB();

				float porcentage = editingLine == line || editingLine == null ? newPorcentage : line.getPorcentage();

				if (porcentage == 0) {
					if (aPoint == aPointLineInternal || bPoint == aPointLineInternal || aPoint == bPointLineInternal || bPoint == bPointLineInternal ) {
						continue;
					}

					PVector intersectionPoint = intersectionPoint(aPoint.x, aPoint.y, bPoint.x, bPoint.y, aPointLineInternal.x, aPointLineInternal.y, bPointLineInternal.x, bPointLineInternal.y);
					if (intersectionPoint != null) {
						return false;
					}
				} else {
					//Tampoco debería cruzar ninguna línea desplazada

					Pair<PVector, PVector> points = editingLine == line || editingLine == null ? line.getABDisplaced(porcentage) : line.getABDesplazamiento();
					PVector aPointDiff = points.getKey();
					PVector bPointDiff = points.getValue();

					PVector intersectionPoint = intersectionPoint(aPoint.x, aPoint.y, bPoint.x, bPoint.y, aPointDiff.x, aPointDiff.y, bPointDiff.x, bPointDiff.y, true);
					if (intersectionPoint != null) {
						return false;
					}
				}
			}
		}

		return true;
	}


	// Calcula en qué lugar hay intersección entre dos líneas (no dos rectas, o sea si las líneas terminan antes de que se intersecten, devuelve null)
	private PVector intersectionPoint(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
		return intersectionPoint(x1, y1, x2, y2, x3, y3, x4, y4, false);

	}


	// Calcula en qué lugar hay intersección entre dos líneas (no dos rectas, o sea si las líneas terminan antes de que se intersecten, devuelve null)
	private PVector intersectionPoint(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, boolean conMargen) {

		// calculate the distance to intersection point
		float uA = ((x4-x3)*(y1-y3) - (y4-y3)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));
		float uB = ((x2-x1)*(y1-y3) - (y2-y1)*(x1-x3)) / ((y4-y3)*(x2-x1) - (x4-x3)*(y2-y1));

		// if uA and uB are between 0-1, lines are colliding
		float limitInf = -0.01f;
		float limitSup = 1.01f;
		if (conMargen) {
			limitInf = 0.01f;
			limitSup = 0.99f;
		}
		if (uA >= limitInf && uA <= limitSup && uB >= limitInf && uB <= limitSup) {

			//where the lines meet
			float intersectionX = x1 + (uA * (x2-x1));
			float intersectionY = y1 + (uA * (y2-y1));

			return new PVector(intersectionX, intersectionY);
		}
		return null;
	}

	//Dada la línea del punto 'a' al punto 'b', devuelve un punto que está en dicha línea que es perpendicular a un punto cualquiera 'p'. 
	//O sea, es el punto más cercano a 'p' que está en la recta a-b
	public PVector orthogonalProjection(PVector a, PVector b, PVector p) {

		// find nearest point alont a SEGMENT 
		PVector d1 = PVector.sub(b, a);
		PVector d2 = PVector.sub(p, a);
		float l1 = d1.mag();

		float dotp = PApplet.constrain(d2.dot(d1.normalize()), 0, l1);

		return PVector.add(a, d1.mult(dotp));  
	}



	public PVector getNearestPoint(float x, float y) {
		PVector result = null;
		float minDist = 65535;
		for (PVector aPoint : getPuntosFigura()) {
			float dist = PApplet.dist(aPoint.x, aPoint.y, x, y);
			if (dist < minDist) {
				minDist = dist;
				result = aPoint;
			}
		}

		//Si realmente está cerca se devuelve
		if (minDist < MAXIMA_DISTANCIA_PARA_EDITAR_PUNTO) {
			return result;
		} else {
			//Si no, no se eligió ninguna
			return null;
		}
	}

	public LineaInterna getNearestInternalLine(PVector toPoint) {
		LineaInterna result = null;

		float minDist = 65535;
		for(LineaInterna line : getLineasInternas()) {
			if (!line.isEditable()) {
				continue;
			}
			float dist = line.getABDisplacedMinDistTo(toPoint);
			if (dist < minDist) {
				minDist = dist;
				result = line;
			}
		}

		//Si realmente está cerca se devuelve
		if (minDist < MAXIMA_DISTANCIA_PARA_EDITAR_LINEA_INTERNA) {
			return result;
		} else {
			//Si no, no se eligió ninguna
			return null;
		}
	}
	
	public Map<Integer, List<PVector>> getRegiones() {
		List<List<PVector>> regiones = new LinkedList<List<PVector>>();

		//regionUno es la region completa de la figura (Por eso es importante que se defina la figura en sentido horario)
		//Todo este algoritmo espera que la figura esté definida en sentido horario
		List<PVector> regionTotal = new LinkedList<PVector>();
		for (PVector aPoint : getPuntosFigura()) {
			regionTotal.add(new PVector(aPoint.x, aPoint.y));
		}
		regiones.add(regionTotal);

		for(LineaInterna aLine : getLineasInternas()) {
			regiones = calcRegionesNuevas(aLine, regiones);
		}

		Map<Integer, List<PVector>> regionPorIdRegion = new HashMap<Integer, List<PVector>>();

		int regionId = 0;
		for (List<PVector> region : regiones) {
			regionPorIdRegion.put(Integer.valueOf(regionId), region);
			regionId++;
		}

		return regionPorIdRegion;
	}

	//Devuelve las regiones que se forman al dividir las regiones existentes por la línea. Si la línea no divide alguna región, la devuelve tal cual. 
	private List<List<PVector>> calcRegionesNuevas(LineaInterna aLine, List<List<PVector>> regiones) {
		List<List<PVector>> regionesNuevas = new LinkedList<List<PVector>>();
		for(List<PVector> region : regiones) {
			//Busco si esta región es partida por la línea
			//Si es partida, tiene que tener dos intersecciones con la periferia de la región
			PVector interseccion1 = null;
			PVector interseccion2 = null;
			List<PVector> regionNuevaA = new LinkedList<PVector>();
			List<PVector> regionNuevaB = new LinkedList<PVector>();
			List<PVector> regionNuevaActiva = regionNuevaA;
			List<PVector> regionNuevaLaOtra = regionNuevaB;
			for(int i = 0; region.size() >= 3 && i < region.size(); i++) {
				PVector pA = region.get(i);
				PVector pB = region.get(i+1 == region.size() ? 0 : i+1);//Con el punto siguiente o con el primero

				Pair<PVector, PVector> dispPoints = aLine.getABDesplazamiento();
				PVector interseccion;
				if (pB.dist(dispPoints.getKey()) == 0 || pB.dist(dispPoints.getValue()) == 0) {
					interseccion = null;
				} else {
					interseccion = intersectionPoint(dispPoints.getKey().x, dispPoints.getKey().y, dispPoints.getValue().x, dispPoints.getValue().y, pA.x, pA.y, pB.x, pB.y);
				}
				if (interseccion != null) {
					//pushStyle();
					//fill(255, 127);
					//strokeWeight(1);
					//stroke(0, 100, 0);
					//ellipse(interseccion.x, interseccion.y, 10,10);
					//popStyle();

					regionNuevaActiva.add(pA);
					if (!interseccion.equals(pA)) {
						regionNuevaActiva.add(interseccion);
					}

					regionNuevaLaOtra.add(interseccion);

					regionNuevaActiva = regionNuevaLaOtra;

					if (regionNuevaLaOtra == regionNuevaB) {
						regionNuevaLaOtra = regionNuevaA;
					} else {
						regionNuevaLaOtra = regionNuevaB;
					}


					//Ya se asignó a la intersección1? 
					if (interseccion1 == null) {
						//No, entonces se asigna
						interseccion1 = interseccion;
					} else {
						//Sí, entonces ya se encontró la segunda intersección que parte a la región
						interseccion2 = interseccion;
					}
				} else {
					regionNuevaActiva.add(pA);
				}
			}

			//Se encontraron dos intersecciones donde la línea parte a la región?
			if (interseccion1 != null && interseccion2 != null) {
				//Hay que dividir la región en dos nuevas (regionNuevaA y regionNuevaB)
				regionesNuevas.add(regionNuevaA);
				regionesNuevas.add(regionNuevaB);
			} else {
				//No, se mantiene esta región
				regionesNuevas.add(region);
			}
		}
		return regionesNuevas;
	}
	
	public void calcRegionesYExtrasParticulares(AnalizadorVideo app, boolean forzarRecalculo) {
		if (canvasPorIdRegion != null && !forzarRecalculo) {
			return;
		}
		regionPorIdRegion = getRegiones();
		canvasPorIdRegion = new HashMap<Integer, PGraphics>();
		
		extraRegionCalcInits();

		for (Map.Entry<Integer, List<PVector>> region : regionPorIdRegion.entrySet()) {
			Integer idRegion = region.getKey();
			//println("idRegion: " + idRegion + " value: " + region.getValue());
			PGraphics regionCanvas = app.createGraphics(AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, PConstants.JAVA2D);
			regionCanvas.beginDraw();
			regionCanvas.background(0);
			regionCanvas.pushStyle();
			regionCanvas.stroke(255);
			regionCanvas.fill(255);

			regionCanvas.beginShape();
			for (PVector aPoint : region.getValue()){
				regionCanvas.vertex(aPoint.x, aPoint.y);
			}
			regionCanvas.endShape(PConstants.CLOSE);


			regionCanvas.popStyle();
			regionCanvas.endDraw();

			canvasPorIdRegion.put(idRegion, regionCanvas);
			
			extraRegionCalcs(app, idRegion);
		}

		//Por último se calcula el canvas de la zona por fuera de la figura (para automáticamente enmascarar esta zona)
		PGraphics mascaraExteriorCanvas = app.createGraphics(AnalizadorVideo.WIDTH, AnalizadorVideo.HEIGHT, PConstants.JAVA2D);
		mascaraExteriorCanvas.beginDraw();
		mascaraExteriorCanvas.background(0);
		mascaraExteriorCanvas.pushStyle();
		mascaraExteriorCanvas.stroke(app.color(0,0,255));
		mascaraExteriorCanvas.fill(app.color(0,0,255));

		mascaraExteriorCanvas.beginShape();
		mascaraExteriorCanvas.vertex(0,0);
		mascaraExteriorCanvas.vertex(AnalizadorVideo.WIDTH,0);
		mascaraExteriorCanvas.vertex(AnalizadorVideo.WIDTH,AnalizadorVideo.HEIGHT);
		mascaraExteriorCanvas.vertex(0,AnalizadorVideo.WIDTH);
		mascaraExteriorCanvas.beginContour();
		for (int i = app.getCorrida().getFigura().getPuntosFigura().length - 1; i >= 0; i--) {
			//Se recorre antihorario para generar el hueco
			PVector aPoint = app.getCorrida().getFigura().getPuntosFigura()[i];
			mascaraExteriorCanvas.vertex(aPoint.x, aPoint.y);
		}
		mascaraExteriorCanvas.endContour();

		mascaraExteriorCanvas.endShape(PConstants.CLOSE);

		if (app.getCorrida().getFigura().getGrosorLineasEnmascarado() > 0) {
			mascaraExteriorCanvas.strokeWeight(app.getCorrida().getFigura().getGrosorLineasEnmascarado());
			for(LineaInterna line : app.getCorrida().getFigura().getLineasInternas()) {
				Pair<PVector, PVector> points = line.getABDesplazamiento();
				PVector aPointDiff = points.getKey();
				PVector bPointDiff = points.getValue();
				mascaraExteriorCanvas.line(aPointDiff.x, aPointDiff.y, bPointDiff.x, bPointDiff.y);
			}

		}
		mascaraExteriorCanvas.strokeWeight(0);
		mascaraExteriorCanvas.popStyle();

		mascaraExteriorCanvas.endDraw();
		
		app.getCorrida().setMascaraExteriorCanvas(mascaraExteriorCanvas);
	}
	

	public RegionData getRegionData(AnalizadorVideo app, float x, float y) {
		if (x < 0 || y < 0 || x > AnalizadorVideo.WIDTH || y > AnalizadorVideo.HEIGHT) {
			return null;
		}

		Pair<Integer, List<PVector>> region = getRegion(app, x, y);

		if (region == null) {
			return null;
		}

		RegionData result = new RegionData();
		Integer idDeLaRegion = region.getKey();
		result.setIdRegion(idDeLaRegion);
		result.setRegion(region.getValue());
		extraRegionData(result, idDeLaRegion);
		return result;
	}

	
	private Pair<Integer, List<PVector>> getRegion(AnalizadorVideo app, float x, float y) {
		calcRegionesYExtrasParticulares(app, false);
		int xInt = (int)x; 
		int yInt = (int)y; 
		for (Map.Entry<Integer, PGraphics> entry : canvasPorIdRegion.entrySet()) {
			PGraphics regionCanvas = entry.getValue();
			if (app.red(regionCanvas.pixels[xInt + AnalizadorVideo.WIDTH * yInt]) > 0) {
				List<PVector> resultRegion = regionPorIdRegion.get(entry.getKey());
				Pair<Integer, List<PVector>> resultado = new Pair<>(entry.getKey(), resultRegion);
				return resultado;
			}
		}

		return null;
	}

	public void print(PrintWriter output) {
		output.println("Shape Points:\nx\ty");
		for (PVector aPoint : getPuntosFigura()) {
			output.println(String.format(Parametros.locale, "%.4f\t%.4f", Float.valueOf(aPoint.x), Float.valueOf(aPoint.y)));
		}
		output.println("\nShape Internal Lines:\naPointIndex\tbPointIndex\tPorcentaje\tEditable");
		for(LineaInterna aLine : getLineasInternas()) {
			aLine.print(output);
		}
		output.println("\nGrosor Lineas Enmascarado");
		output.println(grosorLineasEnmascarado);
	}
	
	public void cambiarGrosorLineasEnmascarado() {
		grosorLineasEnmascarado++;
		if (grosorLineasEnmascarado > 10) {
			grosorLineasEnmascarado = 0;
		}
	}


}

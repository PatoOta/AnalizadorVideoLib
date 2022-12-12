package com.potar.videoanalizer.runtime.data;

import java.io.PrintWriter;
import java.util.*;

import processing.core.*;

public class Recorrido {
private TreeMap<Float, PVector> posiciones;//Las posiciones por las que estuvo la rata y en qué momento
private float ultimaPosicionTime;
private float ultimaPosicionX;
private float ultimaPosicionY;

public Recorrido() {
  posiciones = new TreeMap<Float, PVector>();
  ultimaPosicionTime=-1;
  ultimaPosicionX=-1;
  ultimaPosicionY=-1;
}

public float getUltimaPosicionTime() {
  return ultimaPosicionTime;
}
public float getUltimaPosicionX() {
  return ultimaPosicionX;
}
public float getUltimaPosicionY() {
  return ultimaPosicionY;
}

public void reset() {
  posiciones.clear();
  ultimaPosicionTime=-1;
  ultimaPosicionX=-1;
  ultimaPosicionY=-1;
}

@SuppressWarnings("boxing")
public void jumpTo(float elMomento) {
  posiciones.tailMap(elMomento).clear();
  
  Map.Entry<Float, PVector> lastEntry = posiciones.lastEntry();
  if (lastEntry != null && lastEntry.getValue() != null) {
    ultimaPosicionTime = lastEntry.getKey().floatValue();
    ultimaPosicionX = lastEntry.getValue().x;
    ultimaPosicionY = lastEntry.getValue().y;
  } else {
    ultimaPosicionTime=-1;
    ultimaPosicionX=-1;
    ultimaPosicionY=-1;
  }
}

/*
  Este método se lo utiliza para el momento en el que se comienza el enmascarado temporal.
  Se debe dejar la posición lista como si empezara desde ahí
 */
@SuppressWarnings("boxing")
public void softInit(float elMomento) {
  posiciones.put(elMomento, null);
  ultimaPosicionTime=-1;
  ultimaPosicionX=-1;
  ultimaPosicionY=-1;
}

public void agregarPosicion(float time, float x, float y) {
  posiciones.put(Float.valueOf(time), new PVector(x, y));
  ultimaPosicionTime = time;
  ultimaPosicionX = x;
  ultimaPosicionY = y;
}

public void drawRecorridoYUltimaPosicion(PGraphics gp) {
  drawRecorridoYUltimaPosicion(gp, true, true);
}
public void drawRecorrido(PGraphics gp) {
  drawRecorridoYUltimaPosicion(gp, true, false);
}
public void drawUltimaPosicion(PGraphics gp) {
  drawRecorridoYUltimaPosicion(gp, false, true);
}

private void drawRecorridoYUltimaPosicion(PGraphics gp, boolean drawRecorrido, boolean drawUltimaPosicion) {
  if (drawRecorrido && posiciones.size() > 3) {
    gp.pushStyle();  // Start a new style
    gp.stroke(0, 0, 140);
    gp.noFill();
    gp.beginShape();
    
    boolean cambioAMenosDe5Segundos = false;
    Queue<PVector> lastPoints = new LinkedList<PVector>();
    PVector lastDrawedPoint = null;
    for (Map.Entry<Float, PVector> entry : posiciones.entrySet()) {
      float posicionTime = entry.getKey().floatValue();
      PVector point = entry.getValue();
      
      if (point != null) {
        if (posicionTime < ultimaPosicionTime-5) {
            gp.curveVertex(point.x, point.y);
            lastDrawedPoint = point;
        } else {
          if (!cambioAMenosDe5Segundos) {
            gp.curveVertex(point.x, point.y);
            gp.endShape();
            cambioAMenosDe5Segundos = true;
            if (lastDrawedPoint != null) {
              lastPoints.add(lastDrawedPoint);
            }
          }
          lastPoints.add(point);
        }
      } else {
        if (!cambioAMenosDe5Segundos) {
          gp.endShape();
          gp.beginShape();
        } else {
          lastPoints.add(null);
        }
      }
    }
    if (lastPoints.size() > 3) {
      gp.stroke(0, 255, 140);
      gp.beginShape();
      for (PVector aPoint : lastPoints) {
        if (aPoint != null) {
          gp.curveVertex(aPoint.x, aPoint.y);
        } else {
          gp.endShape();
          gp.beginShape();
        }
      }
      gp.endShape();
    }

    gp.popStyle();
  }

  if (drawUltimaPosicion && ultimaPosicionTime > -1) {
    gp.ellipse(ultimaPosicionX, ultimaPosicionY, 10, 10);
  }
}

@SuppressWarnings("boxing")
public void print(PrintWriter output) {
  if (posiciones.size() > 0) {
    output.println("Recorrido");
    output.println("time\tx\ty");
    for (Map.Entry<Float, PVector> entry : posiciones.entrySet()) {
      Float time = entry.getKey();
      PVector point = entry.getValue();
      if (point != null) {
        output.println(String.format(Parametros.locale, "%.5f\t%.2f\t%.2f", time, point.x, point.y));
      } else {
        output.println(String.format(Parametros.locale, "%.5f\tnull\tnull", time));
      }
    }
  } else {
    output.println("No hay recorrido");
  }
}
}
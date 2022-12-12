package com.potar.videoanalizer.gui;

import com.potar.AnalizadorVideo;
import com.potar.videoanalizer.runtime.data.Figura;
import com.potar.videoanalizer.runtime.data.LineaInterna;
import com.potar.videoanalizer.runtime.data.RegionData;

import processing.core.PVector;

public class FiguraYMaze extends Figura {
	
//	private static final float yShapeX = 100;
//	private static final float yShapeY = 0;
//	private static final float yShapeAnchoBrazos = AnalizadorVideo.WIDTH / 10.0f;
//	private static final float yShapeLargoTotal = AnalizadorVideo.HEIGHT - 1;

	private PVector[] puntosFigura;
	private LineaInterna[] lineasInternas;


	public FiguraYMaze() {
		//puntosFigura = new PVector[]{new PVector(yShapeX, yShapeY),
		//                                 new PVector(yShapeX+yShapeAnchoBrazos, yShapeY),
		//                                 new PVector(yShapeX+(yShapeLargoTotal/2), yShapeY+(yShapeLargoTotal/2)),
		//                                 new PVector(yShapeX+yShapeLargoTotal-yShapeAnchoBrazos, yShapeY),
		//                                 new PVector(yShapeX+yShapeLargoTotal, yShapeY),
		//                                 new PVector(yShapeX+(yShapeLargoTotal/2)+(yShapeAnchoBrazos/2), yShapeY+(yShapeLargoTotal/2)+yShapeAnchoBrazos),
		//                                 new PVector(yShapeX+(yShapeLargoTotal/2)+(yShapeAnchoBrazos/2), yShapeY+yShapeLargoTotal),
		//                                 new PVector(yShapeX+(yShapeLargoTotal/2)-(yShapeAnchoBrazos/2), yShapeY+yShapeLargoTotal),
		//                                 new PVector(yShapeX+(yShapeLargoTotal/2)-(yShapeAnchoBrazos/2), yShapeY+(yShapeLargoTotal/2)+yShapeAnchoBrazos)
		//                               };

		puntosFigura = new PVector[]{new PVector(51,0),
				new PVector(190,0),
				new PVector(292,57),
				new PVector(395,0),
				new PVector(522,0),
				new PVector(326,110),
				new PVector(337,360),
				new PVector(257,360),
				new PVector(257,110)
		};

		lineasInternas = new LineaInterna[]{new LineaInterna(puntosFigura, 8, 2, 0.1f, true), 
				new LineaInterna(puntosFigura, 5, 8, 0.22f, false),
				new LineaInterna(puntosFigura, 2, 5, 0.1f, false)
		};

	}


	@Override
	public PVector[] getPuntosFigura() {
		return puntosFigura;
	}

	@Override
	public LineaInterna[] getLineasInternas() {
		return lineasInternas;
	}


	@Override
	protected void extraRegionCalcInits() {
	}


	@Override
	protected void extraRegionCalcs(AnalizadorVideo app, Integer idRegion) {
	}


	@Override
	protected void extraRegionData(RegionData result, Integer idDeLaRegion) {
	}

}

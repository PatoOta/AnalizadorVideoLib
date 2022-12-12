package com.potar.videoanalizer.runtime.data;

import com.potar.AnalizadorVideo;

import processing.core.PVector;

public class FiguraPlusMaze extends Figura {
//	private static final float plusShapeX = 100;
//	private static final float plusShapeY = 0;
//	private static final float plusShapeWidth = AnalizadorVideo.WIDTH / 2.0f;
//	private static final float plusShapeHeight = AnalizadorVideo.HEIGHT * 0.75f;
//	private static final float plusShapeArmWidth = 40;

	private PVector[] puntosFigura;
	private LineaInterna[] lineasInternas;


	public FiguraPlusMaze() {
		//puntosFigura = new PVector[]{new PVector(plusShapeX + plusShapeWidth / 2.0 - plusShapeArmWidth / 2.0, plusShapeY),
		//                                new PVector(plusShapeX + plusShapeWidth / 2.0 + plusShapeArmWidth / 2.0, plusShapeY),
		//                                new PVector(plusShapeX + plusShapeWidth / 2.0 + plusShapeArmWidth / 2.0, plusShapeY + plusShapeHeight / 2.0 - plusShapeArmWidth / 2.0),
		//                                new PVector(plusShapeX + plusShapeWidth                                , plusShapeY + plusShapeHeight / 2.0 - plusShapeArmWidth / 2.0),
		//                                new PVector(plusShapeX + plusShapeWidth                                , plusShapeY + plusShapeHeight / 2.0 + plusShapeArmWidth / 2.0),
		//                                new PVector(plusShapeX + plusShapeWidth / 2.0 + plusShapeArmWidth / 2.0, plusShapeY + plusShapeHeight / 2.0 + plusShapeArmWidth / 2.0),
		//                                new PVector(plusShapeX + plusShapeWidth / 2.0 + plusShapeArmWidth / 2.0, plusShapeY + plusShapeHeight),
		//                                new PVector(plusShapeX + plusShapeWidth / 2.0 - plusShapeArmWidth / 2.0, plusShapeY + plusShapeHeight),
		//                                new PVector(plusShapeX + plusShapeWidth / 2.0 - plusShapeArmWidth / 2.0, plusShapeY + plusShapeHeight / 2.0 + plusShapeArmWidth / 2.0),
		//                                new PVector(plusShapeX                                                 , plusShapeY + plusShapeHeight / 2.0 + plusShapeArmWidth / 2.0),
		//                                new PVector(plusShapeX                                                 , plusShapeY + plusShapeHeight / 2.0 - plusShapeArmWidth / 2.0),
		//                                new PVector(plusShapeX + plusShapeWidth / 2.0 - plusShapeArmWidth / 2.0, plusShapeY + plusShapeHeight / 2.0 - plusShapeArmWidth / 2.0),

		//                               };
		puntosFigura = new PVector[]{new PVector(274,10),
				new PVector(292,10),
				new PVector(294,159),
				new PVector(471,156),
				new PVector(472,178),
				new PVector(294,177),
				new PVector(297,341),
				new PVector(277,341),
				new PVector(277,176),
				new PVector(97,179),
				new PVector(97,156),
				new PVector(277,159)
		};

		lineasInternas = new LineaInterna[]{new LineaInterna(puntosFigura, 11, 2, 0.1f, true), 
				new LineaInterna(puntosFigura, 2, 5, 0.1f, false), 
				new LineaInterna(puntosFigura, 8, 11, 0.1f, false),
				new LineaInterna(puntosFigura, 5, 8, 0.1f, false)
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

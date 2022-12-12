package com.potar.videoanalizer.runtime.data;

public enum TipoTerreno {
	TERRENO_OPEN_FIELD(1), TERRENO_PLUS_MAZE(2), TERRENO_Y_MAZE(3);

	private int id;

	private TipoTerreno(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
	
}

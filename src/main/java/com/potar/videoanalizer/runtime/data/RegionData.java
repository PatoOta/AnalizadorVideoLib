package com.potar.videoanalizer.runtime.data;

import java.util.List;

import processing.core.PVector;

public class RegionData {
	private Integer idRegion;
	private List<PVector> region;
	private boolean periferia;
	private boolean esquina;

	public Integer getIdRegion() {
		return idRegion;
	}

	public void setIdRegion(Integer idRegion) {
		this.idRegion = idRegion;
	}

	public List<PVector> getRegion() {
		return region;
	}

	public void setRegion(List<PVector> region) {
		this.region = region;
	}

	public boolean isPeriferia() {
		return periferia;
	}

	public void setPeriferia(boolean periferia) {
		this.periferia = periferia;
	}

	public boolean isEsquina() {
		return esquina;
	}

	public void setEsquina(boolean esquina) {
		this.esquina = esquina;
	}

}
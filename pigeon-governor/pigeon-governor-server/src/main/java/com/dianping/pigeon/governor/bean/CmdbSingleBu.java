package com.dianping.pigeon.governor.bean;

public class CmdbSingleBu {

	private CmdbBuBean bu;

	public CmdbBuBean getBu() {
		return bu;
	}

	public void setBu(CmdbBuBean bu) {
		this.bu = bu;
	}

	@Override
	public String toString() {
		return "CmdbSingleBu [bu=" + bu + "]";
	}
	
}

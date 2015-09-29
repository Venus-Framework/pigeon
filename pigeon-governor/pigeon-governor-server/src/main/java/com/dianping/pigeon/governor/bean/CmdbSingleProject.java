package com.dianping.pigeon.governor.bean;

public class CmdbSingleProject {

	private CmdbProjectBean project;

	public CmdbProjectBean getProject() {
		return project;
	}

	public void setProject(CmdbProjectBean project) {
		this.project = project;
	}

	@Override
	public String toString() {
		return "CmdbSingleProject [project=" + project + "]";
	}
	
}

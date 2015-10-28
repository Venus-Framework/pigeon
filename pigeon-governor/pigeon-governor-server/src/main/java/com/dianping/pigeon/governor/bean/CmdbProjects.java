package com.dianping.pigeon.governor.bean;

import java.util.List;

public class CmdbProjects {

	private Integer total;

	private Integer numfound;

	private Integer page;

	private List<CmdbProjectBean> projects;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getNumfound() {
		return numfound;
	}

	public void setNumfound(Integer numfound) {
		this.numfound = numfound;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public List<CmdbProjectBean> getProjects() {
		return projects;
	}

	public void setProjects(List<CmdbProjectBean> projects) {
		this.projects = projects;
	}

	@Override
	public String toString() {
		return "CmdbProject{" +
				"total=" + total +
				", numfound=" + numfound +
				", page=" + page +
				", projects=" + projects +
				'}';
	}
}

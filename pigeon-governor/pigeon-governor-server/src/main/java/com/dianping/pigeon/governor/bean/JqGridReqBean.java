package com.dianping.pigeon.governor.bean;

public class JqGridReqBean {

	private boolean _search;
	
	private long nd;
	
	private int rows;
	
	private int page;
	
	private String sidx;
	
	private String sord;
	
	private String searchField;
	
	private String searchString;
	
	private String searchOper;
	
	private String filters;

	public boolean is_search() {
		return _search;
	}

	public long getNd() {
		return nd;
	}

	public int getRows() {
		return rows;
	}

	public int getPage() {
		return page;
	}

	public String getSidx() {
		return sidx;
	}

	public String getSord() {
		return sord;
	}

	public String getSearchField() {
		return searchField;
	}

	public String getSearchString() {
		return searchString;
	}

	public String getSearchOper() {
		return searchOper;
	}

	public String getFilters() {
		return filters;
	}

	public void set_search(boolean _search) {
		this._search = _search;
	}

	public void setNd(long nd) {
		this.nd = nd;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public void setSidx(String sidx) {
		this.sidx = sidx;
	}

	public void setSord(String sord) {
		this.sord = sord;
	}

	public void setSearchField(String searchField) {
		this.searchField = searchField;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public void setSearchOper(String searchOper) {
		this.searchOper = searchOper;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}
	
}

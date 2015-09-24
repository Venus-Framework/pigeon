package com.dianping.pigeon.governor.bean;

import java.util.List;

/**
 * 
 * @author chenchongze
 *
 */
public class JqGridRespBean {
	
	private List<?> data;
	
	private int currentPage;
	
	private int totalPages;
	
	private int totalRecords;

	public List<?> getData() {
		return data;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public int getTotalRecords() {
		return totalRecords;
	}

	public void setData(List<?> data) {
		this.data = data;
	}

	public void setCurrentPage(int page) {
		this.currentPage = page;
	}

	public void setTotalPages(int total) {
		this.totalPages = total;
	}

	public void setTotalRecords(int records) {
		this.totalRecords = records;
	}

}

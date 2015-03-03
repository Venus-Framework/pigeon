package com.dianping.pigeon.console.status;

import javax.servlet.ServletContext;

import com.dianping.phoenix.status.AbstractComponentStatus;

public class OverviewStatus extends AbstractComponentStatus {

	public static final String ID = "pigeon.overview";

	public OverviewStatus() {
		super(ID, "Pigeon Overview");
	}

	@Override
	public State getState() {
		return State.INITIALIZED;
	}

	protected void build(ServletContext ctx) {
	}

}

package com.edafa.web2sms.utils.alarm;

public enum AppErrors {
	GENERAL_ERROR, IO_ERROR, FAILED_TO_FORWARD_REQUEST, INVALID_REQUEST, FAILED_OPERATION, INVALID_OPERATION;
	int id;

	private AppErrors() {
	}

	private AppErrors(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		System.out.println("AppErrors.setId()" + " " + name() + " id=" + id);
		this.id = id;
	}
}

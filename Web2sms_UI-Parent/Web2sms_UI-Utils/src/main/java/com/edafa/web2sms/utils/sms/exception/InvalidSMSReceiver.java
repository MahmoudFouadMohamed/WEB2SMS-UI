package com.edafa.web2sms.utils.sms.exception;

public class InvalidSMSReceiver extends Exception {

	String receiver;
	
	private static final long serialVersionUID = -2080577803412983883L;

	public InvalidSMSReceiver(String receiver) {
		super();
		this.receiver = receiver;
	}

	public InvalidSMSReceiver(String receiver,String message) {
		super(message);
		this.receiver = receiver;
	}

	@Override
	public String toString() {
		return getMessage();
	}
	
	public String getReceiver() {
		return receiver;
	}
}

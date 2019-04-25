package com.edafa.web2sms.utils.sms.exception;

public class InvalidSMSSender extends Exception {

	String sender;

	private static final long serialVersionUID = -2080577803412983883L;

	public InvalidSMSSender(String sender) {
		super("Invalid sender: " + sender);
		this.sender = sender;
	}

	public InvalidSMSSender(String sender, String message) {
		super(message);
		this.sender = sender;
	}

	@Override
	public String toString() {
		return getMessage();
	}

	public String getSender() {
		return sender;
	}

}

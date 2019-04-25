package com.edafa.web2sms.utils.sms.exception;

import com.edafa.web2sms.utils.sms.MsisdnFormat;

public class InvalidAddressFormattingException extends Exception {

	private static final long serialVersionUID = 1482535210256730472L;
	protected MsisdnFormat sourceFormat;
	protected MsisdnFormat destFormat;

	public InvalidAddressFormattingException(MsisdnFormat sourceFormat, MsisdnFormat destFormat) {
		super("Cannot format address with " + sourceFormat.toString() + " format to " + destFormat.toString()
				+ " format");
		this.sourceFormat = sourceFormat;
		this.destFormat = destFormat;
	}

	public MsisdnFormat getSourceFormat() {
		return sourceFormat;
	}

	public MsisdnFormat getDestFormat() {
		return destFormat;
	}

}

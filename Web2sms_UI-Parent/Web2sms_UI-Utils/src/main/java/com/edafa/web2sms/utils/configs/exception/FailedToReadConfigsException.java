package com.edafa.web2sms.utils.configs.exception;

import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.WebFault;

@WebFault(name = "FailedToReadConfigsException", targetNamespace = "http://www.edafa.com/ws/utils/configs/exception")
@XmlType(name = "FailedToReadConfigsException", namespace = "http://www.edafa.com/ws/utils/configs/exception")
public class FailedToReadConfigsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5740490877794247352L;

	public FailedToReadConfigsException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FailedToReadConfigsException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public FailedToReadConfigsException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public FailedToReadConfigsException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}

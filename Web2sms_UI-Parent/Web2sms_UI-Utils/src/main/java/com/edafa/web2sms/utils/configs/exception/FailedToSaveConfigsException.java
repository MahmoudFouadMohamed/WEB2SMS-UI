package com.edafa.web2sms.utils.configs.exception;

import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.WebFault;

@WebFault(name = "FailedToSaveConfigsException", targetNamespace = "http://www.edafa.com/ws/utils/configs/exception")
@XmlType(name = "FailedToSaveConfigsException", namespace = "http://www.edafa.com/ws/utils/configs/exception")
public class FailedToSaveConfigsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6376309910051394926L;

	public FailedToSaveConfigsException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedToSaveConfigsException(String message) {
		super(message);
	}

	public FailedToSaveConfigsException(Throwable cause) {
		super(cause);
	}

}

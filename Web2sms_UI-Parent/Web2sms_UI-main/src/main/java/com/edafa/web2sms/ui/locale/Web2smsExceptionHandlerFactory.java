package com.edafa.web2sms.ui.locale;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class Web2smsExceptionHandlerFactory extends ExceptionHandlerFactory
{
	private ExceptionHandlerFactory parent;

	public Web2smsExceptionHandlerFactory(ExceptionHandlerFactory parent)
	{
		this.parent = parent;
	}// end of default constructor

	@Override
	public ExceptionHandler getExceptionHandler()
	{
		ExceptionHandler handler = new Web2smsExceptionHandler(parent.getExceptionHandler());
		return handler;
	}// end of method getExceptionHandler

}// end of class PgwExceptionHandlerFactory

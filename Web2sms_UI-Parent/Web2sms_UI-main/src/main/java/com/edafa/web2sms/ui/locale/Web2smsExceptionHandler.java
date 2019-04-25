package com.edafa.web2sms.ui.locale;

import java.io.IOException;
import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

public class Web2smsExceptionHandler extends ExceptionHandlerWrapper {
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());

	private ExceptionHandler wrapped;

	Web2smsExceptionHandler(ExceptionHandler exception) {
		this.wrapped = exception;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return wrapped;
	}

	@Override
	public void handle() throws FacesException {
		String errorPageLocation = "";
		for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
			ExceptionQueuedEvent event = i.next();
			ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
			Throwable t = context.getException();
			if (t instanceof Throwable) {
				try {
					if ((boolean) Configs.ENABLE_DIRECT_LOGIN.getValue())
						errorPageLocation = (String) Configs.DIRECT_LOGIN_REDIRECT_URL.getValue();
					else 
						errorPageLocation = (String) Configs.LOGIN_REDIRECT_URL.getValue();

					FacesContext.getCurrentInstance().getExternalContext().redirect(errorPageLocation);
				} catch (IOException e) {
					userLogger.error("IOException while redirecting in case of view expiring exception the url="
							+ errorPageLocation, e);
				} catch (Exception e) {
					userLogger.error("Exception while redirecting to url="
							+ errorPageLocation, e);
				} finally {
					i.remove();
				}
			}
		}

		// At this point, the queue will not contain any ViewExpiredEvents.
		// Therefore, let the parent handle them.

		if (wrapped != null || getWrapped() != null) {
			getWrapped().handle();
		}
	}// end of method handle

}// end of class Web2smsExceptionHandler

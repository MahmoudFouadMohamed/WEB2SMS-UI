package com.edafa.web2sms.ui.locale;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

@SuppressWarnings("serial")
public class Web2smsCacheControlPhaseListener implements PhaseListener {

	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());

	public PhaseId getPhaseId() {
		return PhaseId.RENDER_RESPONSE;
	}

	public void afterPhase(PhaseEvent event) {
	}

	public void beforePhase(PhaseEvent event) {
		try {
			FacesContext facesContext = event.getFacesContext();
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			String[] pageArray = request.getRequestURI().split("/");
			userLogger.info("The page requested contains argument....");

			if (pageArray[pageArray.length - 1].equals("uploadFile.xhtml")) {
				userLogger
						.info("user trying to upload file to create list the request will contain a same_orgin header");
				response.addHeader("X-FRAME-OPTIONS", "SAMEORIGIN");
			} else if (pageArray[pageArray.length - 1].equals("upload_CustomizeList_File.xhtml")) {
				userLogger
						.info("user trying to upload file to create list the request will contain a same_orgin header");
				response.addHeader("X-FRAME-OPTIONS", "SAMEORIGIN");
			} else {
				userLogger.info("not allowed request for a page, the request will contain a deny header");
				response.addHeader("X-FRAME-OPTIONS", "DENY");
			}
			response.addHeader("Pragma", "no-cache");
			response.addHeader("Cache-Control", "no-cache");
			response.addHeader("Cache-Control", "no-store");
			response.addHeader("Cache-Control", "must-revalidate");
			response.addHeader("Expires", "Mon, 8 Aug 1700 10:00:00 GMT");
			response.addHeader("X-XSS-Protection", "1; mode=block");
		} catch (Exception e) {
			userLogger.error("Error while setting response headers", e);
		}
	}// end of method beforePhase
}// end of class PgwCacheControlPhaseListener
package com.edafa.web2sms.ui.locale;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

@ManagedBean(name = "general")
@ApplicationScoped
public class AppBean {
	public String getFacesSeverity() {
		String severityStr = "";
		if (FacesContext.getCurrentInstance().getMaximumSeverity() != null) {
			severityStr = FacesContext.getCurrentInstance().getMaximumSeverity().toString();
			if (severityStr.contains("INFO")) {
				severityStr = "disclaimer success";
			} else if (severityStr.contains("ERROR")) {
				severityStr = "disclaimer error";
			} else {
				severityStr = "";
			}
		}
		return severityStr;
	}
}

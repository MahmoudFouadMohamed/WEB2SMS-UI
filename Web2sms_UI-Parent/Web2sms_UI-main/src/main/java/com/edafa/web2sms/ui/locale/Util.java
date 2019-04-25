package com.edafa.web2sms.ui.locale;

import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

public class Util implements Serializable{
	private static final long serialVersionUID = 8759725244478701565L;
		
	private static String getLocalizedLabel(String key, String bundlePath) {
		ResourceBundle bundle = null;

		String message = "";
		Locale locale = FacesContext.getCurrentInstance().getViewRoot()
				.getLocale();
		try {
			bundle = ResourceBundle.getBundle(bundlePath, locale);
		} catch (MissingResourceException e) {
		}// end catch

		if (bundle == null) {
			return null;
		}
		try {
			message = bundle.getString(key);
		} catch (Exception e) {
		}
		return message;
	}// end of method getLocalizedLabel

	public static String getLocalizedLabel(String key) {
		return getLocalizedLabel(key, "com.edafa.web2sms.ui.locale.message");
	}// end of method getLocalizedLabel

}
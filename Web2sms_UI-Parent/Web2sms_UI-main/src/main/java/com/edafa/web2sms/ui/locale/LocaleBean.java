package com.edafa.web2sms.ui.locale;

import com.edafa.web2sms.ui.login.LoginBean;
import com.edafa.web2sms.utils.StringUtils;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import javax.ejb.EJB;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@ManagedBean(name = "localeBean")
@ViewScoped
public class LocaleBean implements Serializable {
	private static final long serialVersionUID = -2129781143784837418L;

        @EJB
        AppErrorManagerAdapter appErrorManagerAdapter;
        
	private String language = "en";
	private String direction = "ltr";
	private boolean langEnFlag = false;
	private boolean langArFlag = false;
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
    static final String LANGUAGE = (String) Configs.LANGUAGE_ATTRIBUTE.getValue();
    public boolean directLoginFlag = (boolean) Configs.ENABLE_DIRECT_LOGIN.getValue();
    
	public LocaleBean() {
		String lan = "";
		try {
			//System.out.println("directLoginFlag: " + directLoginFlag);
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

			lan = (String) request.getSession().getAttribute(LANGUAGE);
			if (lan != null) {
				if (lan.equals("en")) {
					userLogger.info("Setting english language from session");
					activateEnglish();
				} else {
					userLogger.info("Setting arabic language from session");
					activateArabic();
				}
			}else{
                            userLogger.info("No language saved in session, Reading language from saved cookies");
                            readLangFromCookies();
                        }
		} catch (Exception e) {
                        switchToEnglish();
                        userLogger.error(StringUtils.concatenate("Error while setting language=", lan, ", english language will be set", e.getMessage()));
                        appLogger.error(StringUtils.concatenate("Error while setting language=" , lan , ", english language will be set"),e);
                        appErrorManagerAdapter.raiseError("", AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public String activateEnglish() {
		setLanguage("en");
		langArFlag = false;
		langEnFlag = true;
		return null;
	}// end of method activateEnglish

	public String activateArabic() {
		setLanguage("ar");
		setDirection("rtl");
		langArFlag = true;
		langEnFlag = false;
		return null;
	}// end of method activateKorean

	public String switchToEnglish() {
		userLogger.info("Setting english language in app, session, and cookies");
                activateEnglish();
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
                request.getSession().setAttribute(LANGUAGE,language);
                addLangCookie();
                FacesContext.getCurrentInstance().getViewRoot().getViewMap().clear();
		return request.getRequestURI()+"?faces-redirect=true";
	}

	public String switchToArabic() {
		userLogger.debug("Setting arabic language in app, session, and cookies");
		activateArabic();
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
                request.getSession().setAttribute(LANGUAGE,language);
                addLangCookie();
                FacesContext.getCurrentInstance().getViewRoot().getViewMap().clear();
		return request.getRequestURI()+"?faces-redirect=true";
	}
        
        
        private void readLangFromCookies() {
            try {
                boolean cookieFound=false;
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
                Cookie[] cookies = request.getCookies();
                if(cookies!=null){
                    for (int i = 0; i < cookies.length; i++) {
                        Cookie cookie = cookies[i];
                        if(cookie.getName().equals(LANGUAGE)){
                            cookieFound=true;
                            String lang=cookie.getValue();
                            if (lang.equals("ar")) {
                                userLogger.info("Setting arabic language from cookies");
                                switchToArabic();
                            } else {
                                userLogger.info("Setting english language from cookies");
                                switchToEnglish();
                            }
                            break;
                        }
                    }
                }
                if(!cookieFound){
                    userLogger.info("No language saved in cookies, english language will be set");
                    switchToEnglish();
                }
            } catch (Exception e) {
                switchToEnglish();
                userLogger.error(StringUtils.concatenate("Error while setting language from cookies english language will be set", e.getMessage()));
                appLogger.error("Error while setting language from cookies english language will be set",e);
                appErrorManagerAdapter.raiseError("", AppErrors.GENERAL_ERROR, "Generic failure");
            }
        }
        
        private void addLangCookie(){
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            Cookie langCookie=new Cookie(LANGUAGE, language);
            langCookie.setMaxAge(Integer.MAX_VALUE);
            langCookie.setPath("/");
            response.addCookie(langCookie);
            userLogger.debug("Add language to cookies "+language);
        }


	// Getters and Setters-------------------------------------------

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public boolean isLangEnFlag() {
		return langEnFlag;
	}

	public void setLangEnFlag(boolean langEnFlag) {
		this.langEnFlag = langEnFlag;
	}

	public boolean isLangArFlag() {
		return langArFlag;
	}

	public void setLangArFlag(boolean langArFlag) {
		this.langArFlag = langArFlag;
	}

	public boolean isDirectLoginFlag() {
		return directLoginFlag;
	}

	public void setDirectLoginFlag(boolean directLoginFlag) {
		this.directLoginFlag = directLoginFlag;
	}

	
}

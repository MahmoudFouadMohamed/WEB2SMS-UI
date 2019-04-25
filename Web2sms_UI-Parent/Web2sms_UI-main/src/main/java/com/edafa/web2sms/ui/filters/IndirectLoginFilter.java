package com.edafa.web2sms.ui.filters;

import com.edafa.web2sms.ui.locale.UserAccount;

import static com.edafa.web2sms.utils.StringUtils.concatenate;
import static com.edafa.web2sms.utils.StringUtils.userLogInfo;

import java.io.IOException;

import javax.ejb.EJB;
import javax.faces.application.ResourceHandler;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.spi.ThreadContextMap;

import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.ConfigsManagerBean;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.configs.enums.ModulesEnum;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsListener;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsManagerBeanLocal;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;

@WebFilter("/*")
public class IndirectLoginFilter implements Filter, ConfigsListener {
	
	@EJB
	AppErrorManagerAdapter appErrorManagerAdapter;
	
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
	
	static String USER_KEY;
	static String REDIRECT_URL;
	static String CAMPAIGNS_URL;
	static String REDIRECT_RELATIVE_URL;
	static String USER_ACTIONS;
	static String LANGUAGE;
	static String REQUEST_TOKEN;
	static String ACTIVE_PASSWORD;
	static String LOGIN_ID;
	static final String isDirectLogin = "isLoginDirect";
	static boolean INACTIVE_PASS_REDIRECT_TO_CHANGE_PASS;
	static boolean ENABLE_DIRECT_LOGIN;
	
	@EJB
	ConfigsManagerBeanLocal configsManagerBean;
	
	public IndirectLoginFilter() {
	
	}
	
	public void destroy() {
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		try {
			if (!ENABLE_DIRECT_LOGIN) {
				if (req.getRequestURI().startsWith(req.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER) || req.getRequestURI().contains("resources")) {
					chain.doFilter(req, res);
					return;
				}
				
				// to prevent caching in all requests expect library resources requests
				if (!req.getRequestURI().startsWith(req.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER)) {
					res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
					res.setHeader("Pragma", "no-cache");
					res.setDateHeader("Expires", 0);
				}
				
				// userLogger.trace("IndirectLoginFilter: dofiltler() requested path "+req.getRequestURI());
				
				// to bypass login requests
				if (req.getRequestURI().contains("login")) {
					if (req.getSession().getAttribute(IndirectLoginFilter.USER_KEY) != null && req.getSession().getAttribute(IndirectLoginFilter.USER_ACTIONS) != null
							&& req.getSession().getAttribute(IndirectLoginFilter.LANGUAGE) != null && req.getSession().getAttribute(IndirectLoginFilter.ACTIVE_PASSWORD) != null) {
						String loginId = (String) req.getSession().getAttribute(LOGIN_ID);
						if (loginId != null && !loginId.trim().isEmpty()) {
							ThreadContext.put(LOGIN_ID, loginId);
						}
						userLogger.info("User has session, with active password, tries to access login, redirect to campaigns page");
						res.sendRedirect(CAMPAIGNS_URL);
					} else {
						userLogger.trace("login request");
						chain.doFilter(req, res);
					}
				}
				// to check user key, actions, language, request token in session in all other
				// requests
				else
					if (req.getSession().getAttribute(IndirectLoginFilter.USER_KEY) != null && req.getSession().getAttribute(IndirectLoginFilter.USER_ACTIONS) != null
							&& req.getSession().getAttribute(IndirectLoginFilter.LANGUAGE) != null && req.getSession().getAttribute(isDirectLogin) != null) {
						String loginId = (String) req.getSession().getAttribute(LOGIN_ID);
						if (loginId != null && !loginId.trim().isEmpty()) {
							ThreadContext.put(LOGIN_ID, loginId);
						}
						// TODO check login type in session
						// should be direct login or invalidate the session
						if ((boolean) req.getSession().getAttribute(isDirectLogin) != true) {
							
							UserAccount userAccount = (UserAccount) req.getSession().getAttribute(IndirectLoginFilter.USER_KEY);
							String userLogInfo = userLogInfo(userAccount.getAccount().getAccountId(), userAccount.getUsername());
							if (req.getSession().getAttribute(IndirectLoginFilter.ACTIVE_PASSWORD) != null) {
								userLogger.debug("User has running session, with active password");
								
								chain.doFilter(req, res);
							} else {
								userLogger.info("User has session, without active password");
								appErrorManagerAdapter.raiseError(userLogInfo, AppErrors.INVALID_REQUEST, "User is not authenticated");
								if (INACTIVE_PASS_REDIRECT_TO_CHANGE_PASS) {
									userLogger.debug("forward customer to change password");
									request.setAttribute("change-password", true);
									request.getRequestDispatcher(REDIRECT_RELATIVE_URL).forward(request, response);
								} else {
									userLogger.debug("redirect customer to login after invalidate his session");
									req.getSession().invalidate();
									res.sendRedirect(REDIRECT_URL);
								}
							}
						} else {
							// it's direct login, while direct login is disabled, So will invalidate
							// the session\
							userLogger.trace("User have session by direct login, while it's disabled now, will invalidate it");
							req.getSession().invalidate();
							res.sendRedirect(REDIRECT_URL);
						}
					} else {
						userLogger.info("User doesn't have session, redirect to login");
						appErrorManagerAdapter.raiseError("", AppErrors.INVALID_REQUEST, "User is not authinticated");
						res.sendRedirect(REDIRECT_URL);
					}
			} else {
				userLogger.trace("direct login is enabled, pass login filter");
				chain.doFilter(req, res);
			}
		} catch (Exception e) {
			userLogger.error("Exception in IndirectLoginFilter : " + e.getMessage());
			appLogger.error("Exception in IndirectLoginFilter : ", e);
			res.sendRedirect(req.getContextPath() + "/error.html");
			appErrorManagerAdapter.raiseError("", AppErrors.GENERAL_ERROR, "Generic failure");
		} finally {
			ThreadContext.remove(LOGIN_ID);
			
		}
	}
	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		userLogger.debug("register indirect login filter on configListener for UIManagement module ");
		configsManagerBean.registerConfigsListener(ModulesEnum.UIManagement, this);
		initConfigKeys();
	}
	
	private void initConfigKeys() {
		USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
		REDIRECT_URL = (String) Configs.LOGIN_REDIRECT_URL.getValue();
		CAMPAIGNS_URL = (String) Configs.CAMPAIGNS_URL.getValue();
		REDIRECT_RELATIVE_URL = (String) Configs.REDIRECT_RELATIVE_URL.getValue();
		USER_ACTIONS = (String) Configs.USER_ACTIONS.getValue();
		LANGUAGE = (String) Configs.LANGUAGE_ATTRIBUTE.getValue();
		ACTIVE_PASSWORD = (String) Configs.ACTIVE_PASSWORD.getValue();
		INACTIVE_PASS_REDIRECT_TO_CHANGE_PASS = (boolean) Configs.INACTIVE_PASS_REDIRECT_TO_CHANGE_PASS.getValue();
		LOGIN_ID = (String) Configs.LOGIN_ID.getValue();
		ENABLE_DIRECT_LOGIN = (boolean) Configs.ENABLE_DIRECT_LOGIN.getValue();
	}
	
	public String userLogInfo(String id, String userName) {
		StringBuilder sb = new StringBuilder();
		sb.append("User");
		sb.append("(");
		sb.append(id);
		sb.append(",");
		sb.append(userName);
		sb.append("): ");
		return sb.toString();
	}

	@Override
	public void configurationRefreshed(ModulesEnum module) {
		initConfigKeys();		
	}

	@Override
	public void configurationRefreshed() {
		initConfigKeys();		
	}
}

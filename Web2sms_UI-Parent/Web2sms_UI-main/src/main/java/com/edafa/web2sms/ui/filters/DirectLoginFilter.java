package com.edafa.web2sms.ui.filters;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.edafa.web2sms.service.acc_manag.account.model.AccountResultFullInfo;
import com.edafa.web2sms.service.acc_manag.account.user.UserLoginService;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.acc_manag.model.AccountUserTrxInfo;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.configs.enums.ModulesEnum;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsListener;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsManagerBeanLocal;

@WebFilter("/*")
public class DirectLoginFilter implements Filter, ConfigsListener {
	
	private UserLoginService userLoginService;
	static String USER_ACTIONS;
	static String LANGUAGE;
	static String ACTIVE_PASSWORD;
	static String USER_KEY;
	static boolean ENABLE_DIRECT_LOGIN;
	static String LOGIN_ID;
	static final String isDirectLogin = "isLoginDirect";
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	static String redirectUrl;
	
	@EJB
	WSClients portObj;
	
	@EJB
	AppErrorManagerAdapter appErrorManagerAdapter;
	
	@EJB
	ConfigsManagerBeanLocal configsManagerBean;
	
	@Override
	public void destroy() {
		
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		try {
			if (ENABLE_DIRECT_LOGIN) {
				Object attr = "";
				String paramValue = "";
				String paramName = "";
				String language = "";
				
				Map<String, String> map = new HashMap<String, String>();
				Map<String, String> map1 = new HashMap<String, String>();
				
				Enumeration headerNames = req.getHeaderNames();
				while (headerNames.hasMoreElements()) {
					String key = (String) headerNames.nextElement();
					String value = req.getHeader(key);
					map.put(key, value);
					if (key.equals((String) Configs.USER_ATTRIBUTE.getValue())) {
						attr = value;
					}
					
					if (key.equals((String) Configs.LANGUAGE_ATTRIBUTE.getValue())) {
						language = value;
					}
				}
				userLogger.info("Request parameters : " + map1);
				if (req.getRequestURI().startsWith(req.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER) || req.getRequestURI().contains("resources")) {
					chain.doFilter(req, res);
					return;
				}
				userLoginService = portObj.getUserLoginPort();
				// skip the resources library
				if (!req.getRequestURI().startsWith(req.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER)) {
					res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP
																							// 1.1.
					res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
					res.setDateHeader("Expires", 0); // Proxies.
				}
				
				userLogger.trace("Request header contains " + map);
				userLogger.info("Request url : " + req.getRequestURL());
				
				Enumeration<String> parameterNames = req.getParameterNames();
				while (parameterNames.hasMoreElements()) {
					paramName = parameterNames.nextElement();
					String[] paramValues = req.getParameterValues(paramName);
					for (int i = 0; i < paramValues.length; i++) {
						paramValue = paramValues[i];
					}
					map1.put(paramName, paramValue);
				}
				
				
				if (attr == null || attr.equals("")) {
					if (((String) req.getParameter((String) Configs.USER_ATTRIBUTE.getValue()) == null)) {
						if (req.getSession().getAttribute(USER_KEY) != null) {
							if ((req.getSession().getAttribute(isDirectLogin) != null) 
									&& ((boolean) req.getSession().getAttribute(DirectLoginFilter.isDirectLogin) == true)) {
								userLogger.info("User is in session");
								chain.doFilter(req, res);
								return;
							} else {
								userLogger.info("ssesion authenticated through indirect login, and it's disabled now, so will invalidate the session");
								req.getSession().invalidate();
								res.sendRedirect(redirectUrl);
							}
						}
						userLogger.info("User attribute is not found");
					} else {
						attr = req.getParameter((String) Configs.USER_ATTRIBUTE.getValue());
						userLogger.info("User attribute (" + (String) Configs.USER_ATTRIBUTE.getValue() + ") value=\"" + (String) attr + "\"");
					}
				}
				
				if (language == null || language.trim().isEmpty()) {
					if (((String) req.getParameter((String) Configs.LANGUAGE_ATTRIBUTE.getValue()) == null)
							|| ((String) req.getParameter((String) Configs.LANGUAGE_ATTRIBUTE.getValue())).trim().isEmpty()) {
						language = "en";
					} else {
						language = (String) req.getParameter((String) Configs.LANGUAGE_ATTRIBUTE.getValue());
					}
				}
				
				if (attr != null && language != null && !attr.equals("") && !language.trim().isEmpty()) {
					// check user in db
					String userAdmin = (String) attr;
					String[] adminArr = userAdmin.split("@");
					String companyDomainMain = adminArr[1];
					String companyDomain = companyDomainMain.toLowerCase();
					String userNameMain = adminArr[0];
					String userName = userNameMain.toLowerCase();
					
					userLogger.debug("User name formatted to lower case, userName=" + userName + ", Company domain formatted to lower case, company domain=" + companyDomain);
					String trxId = TrxId.getTrxId();
					AccountUserTrxInfo accountTrxInfo = new AccountUserTrxInfo();
					accountTrxInfo.setTrxId(trxId);
					req.getSession().setAttribute(LOGIN_ID, trxId);
					AccManagUser accountUser = new AccManagUser();
					accountUser.setUsername(userName);
					accountTrxInfo.setUser(accountUser);
					UserAccount userAccount = new UserAccount();
					
					if (req.getSession().getAttribute(USER_KEY) == null) {
						AccountResultFullInfo result = userLoginService.directUserLogin(accountTrxInfo, companyDomain);
						if (result.getStatus().equals(com.edafa.web2sms.service.acc_manag.enums.ResponseStatus.SUCCESS)
								&& (result.getAccount().getLoginUser().getUserActions() != null && !result.getAccount().getLoginUser().getUserActions().isEmpty() && result.getAccount().getLoginUser()
										.getUserActions().size() > 0)) {
							userAccount.setAccount(result.getAccount());
							userAccount.setAccountId(result.getAccount().getAccountId());
							List<String> sendersList = result.getAccount().getSender();
							if (sendersList.size() > 1) {
								String sendersStrings = "";
								for (String s : sendersList) {
									sendersStrings = sendersStrings + s + " , ";
								}
								userLogger.debug(userName + " has multi senders=[" + sendersStrings + "]");
							} else {
								userLogger.debug(userName + " has one sender_name=" + result.getAccount().getSender().get(0));
							}
							
							userAccount.setUsername(userName);
							
							req.getSession().setAttribute(LANGUAGE, language);
							req.getSession().setAttribute(USER_KEY, userAccount);
							req.getSession().setAttribute(USER_ACTIONS, result.getAccount().getLoginUser().getUserActions());
							req.getSession().setAttribute(isDirectLogin, true);
							StringBuilder userActionsStr = new StringBuilder("[ ");
							for (int i = 0; i < result.getAccount().getLoginUser().getUserActions().size(); i++) {
								if (i == 0) {
									userActionsStr.append(result.getAccount().getLoginUser().getUserActions().get(i).toString());
								} else {
									userActionsStr.append(", ").append(result.getAccount().getLoginUser().getUserActions().get(i).toString());
								}
							}
							userActionsStr.append(" ].");
							userLogger.info("Session is set for user " + userAccount.getUsername() + " with id " + userAccount.getAccountId() + " and language " + language
									+ ", and list of actions : " + userActionsStr.toString());
							chain.doFilter(req, res);
						} else {
							userLogger.info("User is not found in the database");
							// User is not logged in, so redirect to index.
							res.sendRedirect(redirectUrl);
						}
					} else {
						userAccount = ((UserAccount) (req.getSession().getAttribute(USER_KEY)));
						userLogger.info(userLogInfo(userAccount.getAccount().getAccountId(), userAccount.getUsername()) + "have a running session");
						userLogger.debug("Already exsisting session has company domain=" + userAccount.getAccount().getCompanyDomain() + " and user name=" + userAccount.getUsername()
								+ " ,the new request has company domain=" + companyDomain + " and user name=" + userName);
						if (userAccount.getAccount().getCompanyDomain().equals(companyDomain) && userAccount.getUsername().equals(userName)) {
							req.removeAttribute(LANGUAGE);
							req.getSession().setAttribute(LANGUAGE, language);
							userLogger.info(userLogInfo(userAccount.getAccount().getAccountId(), userAccount.getUsername()) + "user with language " + language
									+ " is already have a session and will be redirected");
							String loginId = (String) req.getSession().getAttribute(LOGIN_ID);
							if (loginId != null && !loginId.trim().isEmpty()) {
								ThreadContext.put(LOGIN_ID, loginId);
							}
							// TODO check login type in session
							// should be direct login or invalidate the session
							if (req.getSession().getAttribute(DirectLoginFilter.isDirectLogin) != null && ((boolean) req.getSession().getAttribute(DirectLoginFilter.isDirectLogin) == true)) {
								chain.doFilter(req, res);
							} else {
								// it's indirect login, while indirect login is disabled, So will
								// invalidate the session\
								userLogger.trace("User have session by indirect login, while it's disabled now, will invalidate it");
								req.getSession().invalidate();
								res.sendRedirect(redirectUrl);
							}
						} else {
							userLogger.info(userLogInfo(userAccount.getAccount().getAccountId(), userAccount.getUsername()) + "is not the owner of the running session and will be redirected to "
									+ redirectUrl);
							res.sendRedirect(redirectUrl);
						}
					}
				} else {
					userLogger.info("User is not logged in");
					// User is not logged in, so redirect to index.
					res.sendRedirect(redirectUrl);
				}
			} else {
				// direct login isn't enabled, redirect to login url ?
				userLogger.trace(" direct login isn't enabled, pass the filter");
				chain.doFilter(req, res);
			}
		} catch (Exception e) {
			userLogger.error("Error while logging in ", e);
			res.sendRedirect(req.getContextPath() + "/error.html");
			appErrorManagerAdapter.raiseError("", AppErrors.GENERAL_ERROR, "Generic failure");
		} finally {
			ThreadContext.remove(LOGIN_ID);
			
		}
	}
	
	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		userLogger.debug("register direct login filter on configListener for UIManagement module ");
		configsManagerBean.registerConfigsListener(ModulesEnum.UIManagement, this);
		initConfigKeys();
	}
	
	private void initConfigKeys() {
		USER_ACTIONS = (String) Configs.USER_ACTIONS.getValue();
		LANGUAGE = (String) Configs.LANGUAGE_ATTRIBUTE.getValue();
		ACTIVE_PASSWORD = (String) Configs.ACTIVE_PASSWORD.getValue();
		USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
		redirectUrl = (String) Configs.DIRECT_LOGIN_REDIRECT_URL.getValue();
		ENABLE_DIRECT_LOGIN = (boolean) Configs.ENABLE_DIRECT_LOGIN.getValue();
		LOGIN_ID = (String) Configs.LOGIN_ID.getValue();
		
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

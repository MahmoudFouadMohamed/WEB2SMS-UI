package com.edafa.web2sms.ui.filters;

import com.edafa.web2sms.utils.configs.enums.Configs;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

/**
 * Filters Http requests and removes malicious characters/strings (i.e. XSS)
 * from the Query String
 */
@WebFilter("/*")
public class XSSPreventionFilter implements Filter {
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());

	class XSSRequestWrapper extends HttpServletRequestWrapper {

		public XSSRequestWrapper(HttpServletRequest servletRequest) {
			super(servletRequest);
		}

		@Override
		public String[] getParameterValues(String parameter) {
			String[] values = super.getParameterValues(parameter);

			if (values == null) {
				return null;
			}

			int count = values.length;
			String[] encodedValues = new String[count];
			for (int i = 0; i < count; i++) {
				encodedValues[i] = stripXSS(values[i]);
			}

			return encodedValues;
		}

		@Override
		public String getParameter(String parameter) {
			// userLogger.debug("getParameter : " + parameter );
			String value = super.getParameter(parameter);

			return stripXSS(value);
		}

		@Override
		public String getHeader(String name) {
			// userLogger.debug("getHeader : " + name );

			String value = super.getHeader(name);
			return stripXSS(value);
		}

		private String stripXSS(String value) {
			// userLogger.debug("stripXSS : " + value );

			if (value != null) {
				// NOTE: It's highly recommended to use the ESAPI library and
				// uncomment the following line to
				// avoid encoded attacks.
//				 value = ESAPI.encoder().canonicalize(value);

				// Avoid null characters
				value = value.replaceAll("", "");

				// Avoid anything between script tags
				Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
				value = scriptPattern.matcher(value).replaceAll("");

				// Avoid anything in a src='...' type of expression
				scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE
						| Pattern.MULTILINE | Pattern.DOTALL);
				value = scriptPattern.matcher(value).replaceAll("");

				scriptPattern = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE
						| Pattern.MULTILINE | Pattern.DOTALL);
				value = scriptPattern.matcher(value).replaceAll("");

				// Remove any lonesome </script> tag
				scriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
				value = scriptPattern.matcher(value).replaceAll("");

				// Remove any lonesome <script ...> tag
				scriptPattern = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
						| Pattern.DOTALL);
				value = scriptPattern.matcher(value).replaceAll("");

				// Avoid eval(...) expressions
				scriptPattern = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
						| Pattern.DOTALL);
				value = scriptPattern.matcher(value).replaceAll("");

				// Avoid expression(...) expressions
				scriptPattern = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
						| Pattern.DOTALL);
				value = scriptPattern.matcher(value).replaceAll("");

				// Avoid javascript:... expressions
				scriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
				value = scriptPattern.matcher(value).replaceAll("");

				// Avoid vbscript:... expressions
				scriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
				value = scriptPattern.matcher(value).replaceAll("");

				// Avoid onload= expressions
				scriptPattern = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
						| Pattern.DOTALL);
				value = scriptPattern.matcher(value).replaceAll("");

				// remove <
				scriptPattern = Pattern.compile("<*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
				value = scriptPattern.matcher(value).replaceAll("");
			}
			// userLogger.debug("stripXSS return value : " + value);
			return value;
		}
	}

	@Override
	public void destroy() {
		// userLogger.debug("XSSPreventionFilter: destroy()");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		//  change to the configuration
		String redirectURL;
		if( (boolean)Configs.ENABLE_DIRECT_LOGIN.getValue())
			redirectURL = (String) Configs.DIRECT_LOGIN_REDIRECT_URL.getValue();
		else 
			redirectURL = (String) Configs.LOGIN_REDIRECT_URL.getValue();
//		String redirectURL = "http://cloudportal.vodafone.com.eg/portal/selfservice";
		if (req.getMethod().equals("GET") || req.getMethod().equals("POST")) {
			XSSRequestWrapper wrapper = new XSSRequestWrapper(req);
			chain.doFilter(wrapper, response);
		} else {
			userLogger.debug("invalid method, user will be redirected, and method will be invalidated session");
			HttpSession session = ((HttpServletRequest) request).getSession(false);
			session.invalidate();
			res.sendRedirect(redirectURL);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// userLogger.debug("XSSPreventionFilter: init()");
	}
}
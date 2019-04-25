package com.edafa.web2sms.ui.filters;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.tangosol.net.security.UsernameAndPassword;

public class SessionListener implements HttpSessionListener {
	
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
	@Resource(name = "java:app/env/basedirfiles")
	String proBaseDir;
	
	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		
	}
	
	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		deleteUsersFiles(arg0.getSession());
	}
	
	private void deleteUsersFiles(HttpSession session) {
		String userName = "notDefined";
		try {
			
			String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
			UserAccount userAcc = (UserAccount) session.getAttribute(USER_KEY);
			if (userAcc != null) {
				userName = userAcc.getUsername();
			}
			List<String> filesNames = (List<String>) session.getAttribute("filesNames");
			if (filesNames != null && !filesNames.isEmpty()) {
				for (String fileName : filesNames) {
					
					String file = proBaseDir + "/" + fileName;
					try {
						Files.deleteIfExists(Paths.get(file));
					} catch (NoSuchFileException e) {
						appLogger.error("NoSuchFileException while deleting file (" + fileName + ") related to this (" + userName +") user", e);
					} catch (DirectoryNotEmptyException e) {
						appLogger.error("DirectoryNotEmptyException while deleting file (" + fileName+ ") related to this (" + userName +") user", e);
					} catch (IOException e) {
						appLogger.error("IOException while deleting file (" + fileName + ") related to this (" + userName +") user", e);
					} catch (Exception e) {
						appLogger.error("Exception while deleting file (" + fileName+ ") related to this (" + userName +") user", e);
						
					}
				}
			}
		} catch (Exception e) {
			appLogger.error("Exception while deleteing ("+userName+") files before destoying his session", e);
			
		}
		
	}
	
}

package com.edafa.web2sms.utils.logging.interfaces;

import javax.ejb.Local;

@Local
public interface LoggingManagerLocal extends LoggingManager {

	void resetLoggerFiles();

	// void refreshLoggers();

	// void updateLoggersConfigs(Map<LoggersEnum, String> newLogLevels);
}

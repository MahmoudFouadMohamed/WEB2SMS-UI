package com.edafa.web2sms.utils.configs.enums;

public enum LoggersEnum {
	APP_UTILS(ModulesEnum.UIUtils, "web2sms_app"),
	WEB_MODULE(ModulesEnum.UIManagement,"web2sms_web")
	;

	private ModulesEnum module;
	private String logFileName;

	LoggersEnum(ModulesEnum module, String logFileName) {
		this.module = module;
		this.logFileName = logFileName;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public ModulesEnum getModule() {
		return module;
	}
}

package com.edafa.web2sms.utils.configs.enums;

public enum AppSettings {
	BaseDir("java:app/env/basedir", "web2sms-ui/");

	public final String envEnteryName;
	public String envEntryValue;

	AppSettings(String envEnteryName) {
		this.envEnteryName = envEnteryName;
		this.envEntryValue = null;
	}

	AppSettings(String envEnteryName, String envEntryValue) {
		this.envEntryValue = envEntryValue;
		this.envEnteryName = envEnteryName;
	}

	public void setEnvEntryValue(String envEntryValue) {
		this.envEntryValue = envEntryValue;
	}

	public String getEnvEnteryName() {
		return envEnteryName;
	}

	public String getEnvEntryValue() {
		return envEntryValue;
	}
}

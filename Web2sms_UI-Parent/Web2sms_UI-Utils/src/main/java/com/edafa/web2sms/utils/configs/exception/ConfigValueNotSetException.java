package com.edafa.web2sms.utils.configs.exception;

import com.edafa.web2sms.utils.configs.enums.ModulesEnum;

public class ConfigValueNotSetException extends RuntimeException {

	private static final long serialVersionUID = -8455213227404007715L;

	ModulesEnum module;
	String property;

	public ConfigValueNotSetException(ModulesEnum module, String property) {
		super("No value set for property " + property + " in module " + module.getName()
				+ " and there is no default value for it.");
		this.module = module;
		this.property = property;
	}

	public ModulesEnum getModule() {
		return module;
	}

	public void setModule(ModulesEnum module) {
		this.module = module;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

}

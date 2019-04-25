package com.edafa.web2sms.utils.configs.enums;

public enum ModulesEnum {

	UIConfigs, UIUtils, UIManagement

	;

	public String name;
	private boolean configsApplied = true;

	ModulesEnum() {
		this.name = name();
	}

	ModulesEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the configsApplied
	 */
	public boolean isConfigsApplied() {
		return configsApplied;
	}

	/**
	 * @param configsApplied
	 *            the configsApplied to set
	 */
	public void setConfigsApplied(boolean configsApplied) {
		this.configsApplied = configsApplied;
	}

	@Override
	public String toString() {
		return name;
	}
}

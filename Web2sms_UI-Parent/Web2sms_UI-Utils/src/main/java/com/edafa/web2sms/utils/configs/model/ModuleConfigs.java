package com.edafa.web2sms.utils.configs.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ModuleConfigs", namespace = "http://www.edafa.com/ws/utils/configs/model/")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModuleConfigs {

	@XmlElement(required = true, nillable = false)
	private String module;
	@XmlElement(required = true, nillable = false)
	private boolean configsApplied;
	@XmlElement(required = true, nillable = false)
	private List<Config> configList;

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public List<Config> getConfigList() {
		return configList;
	}

	public void setConfigList(List<Config> configList) {
		this.configList = configList;
	}

	public boolean isConfigsApplied() {
		return configsApplied;
	}

	public void setConfigsApplied(boolean configsApplied) {
		this.configsApplied = configsApplied;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ModuleConfigs (module=" + module + ", configsApplied=" + configsApplied + ", configList=" + configList
				+ ")";
	}

	public boolean isValid() {
		return !(module == null || configList == null || configList.isEmpty());
	}

}

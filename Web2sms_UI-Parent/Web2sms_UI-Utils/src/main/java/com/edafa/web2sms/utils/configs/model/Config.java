package com.edafa.web2sms.utils.configs.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.edafa.web2sms.utils.configs.enums.ConfigType;

@XmlType(name = "Config", namespace = "http://www.edafa.com/ws/utils/configs/model/")
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

	@XmlElement(required = true, nillable = false)
	private int id;
	@XmlElement(required = true, nillable = false)
	private String key;
	@XmlElement(required = true, nillable = false)
	private String value;
	@XmlElement(required = true, nillable = true)
	private String description;
	@XmlElement(required = true, nillable = false)
	private String module;
	// @XmlElement(required = true, nillable = false)
	// private Boolean editFlag;
	@XmlElement(required = true, nillable = true)
	private String runningConfig;
	@XmlElement(required = true, nillable = true)
	private ConfigType configType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	// public Boolean getEditFlag() {
	// return editFlag;
	// }
	//
	// public void setEditFlag(Boolean editFlag) {
	// this.editFlag = editFlag;
	// }

	public String getRunningConfig() {
		return runningConfig;
	}

	public void setRunningConfig(String runningConfig) {
		this.runningConfig = runningConfig;
	}

	public ConfigType getConfigType() {
		return configType;
	}

	public void setConfigType(ConfigType configType) {
		this.configType = configType;
	}

}

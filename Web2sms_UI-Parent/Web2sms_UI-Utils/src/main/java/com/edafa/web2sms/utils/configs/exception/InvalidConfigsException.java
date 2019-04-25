package com.edafa.web2sms.utils.configs.exception;

import java.util.List;

import javax.xml.bind.annotation.XmlType;
import javax.xml.ws.WebFault;

import com.edafa.web2sms.utils.configs.model.Config;

@WebFault(name = "InvalidConfigsException", targetNamespace = "http://www.edafa.com/ws/utils/configs/exception")
@XmlType(name = "InvalidConfigsException", namespace = "http://www.edafa.com/ws/utils/configs/exception")
public class InvalidConfigsException extends Exception {

	private static final long serialVersionUID = -2191893588503593781L;
	private String module;
	private List<Config> invalidConfigs;

	public InvalidConfigsException(String module, List<Config> invalidConfigs) {
		super("Some or all configuration of module " + module + " are invalid");
		this.invalidConfigs = invalidConfigs;
		this.module = module;
	}

	public String getModule() {
		return module;
	}

	public List<Config> getInvalidConfigs() {
		return invalidConfigs;
	}

}

package com.edafa.web2sms.utils.configs.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * @author tmohamed, akhalifah
 * 
 */
@XmlType(name = "ConfigType", namespace = "http://www.edafa.com/ws/utils/configs/enums/")
@XmlEnum
public enum ConfigType {
	STRING, BOOLEAN, INTEGER, LOG_LEVEL("trace", "debug", "info", "warn", "error", "fatal"), EMAIL_ADDRESS, IP_ADDRESS, ENCRYPTED_PASSWORD, LIST(
			"\\|");

	private String[] acceptedValues;

	private ConfigType() {
	}

	private ConfigType(String... acceptedValues) {
		this.acceptedValues = acceptedValues;
	}

	public String[] getAcceptedValues() {
		return acceptedValues;
	}

	public boolean validateType(Object value) {
		boolean valid = false;

		try {
			switch (this) {
			case BOOLEAN:
				if (String.valueOf(value).matches("(?i)true|false"))
					valid = true;
				break;
			case INTEGER:
				if (String.valueOf(value).matches(("-?\\d+")))
					valid = true;
				break;
			case EMAIL_ADDRESS:
				if (String.valueOf(value).matches(
						"^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$"))
					valid = true;
				break;
			case LOG_LEVEL:
				for (String level : LOG_LEVEL.acceptedValues) {
					if (level.equalsIgnoreCase(String.valueOf(value))) {
						valid = true;
					}
				}
				break;
			case LIST:
				String del = acceptedValues.length > 0 ? acceptedValues[0] : "\\|";
				valid = ((String) value).split(del).length > 0;
				break;
			default:
				valid = true;

			}
		} catch (Exception e) {
			valid = false;
		}
		return valid;
	}
}
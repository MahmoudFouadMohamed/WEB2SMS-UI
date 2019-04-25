package com.edafa.web2sms.utils.sms;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Language", namespace = "http://www.edafa.com/web2sms/service/model/enums/")
@XmlEnum
public enum LanguageNameEnum {
	ENGLISH(1), ARABIC(2), UNKNOWN;

	private int languageId;

	private LanguageNameEnum() {

	}

	private LanguageNameEnum(int languageId) {
		this.languageId = languageId;
	}

	public void setLanguageId(int languageId) {
		this.languageId = languageId;
	}

	public int getLanguageId() {
		return languageId;
	}

	static public LanguageNameEnum getLanguageEnum(int id) {
		LanguageNameEnum[] languages = values();
		for (LanguageNameEnum languageNameEnum : languages) {
			if (languageNameEnum.getLanguageId() == id)
				return languageNameEnum;
		}
		return null;
	}
}

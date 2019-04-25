package com.edafa.web2sms.service.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "UserReportRequest", namespace = "http://www.edafa.com/web2sms/service/model/")
@XmlRootElement(name = "UserReportRequest", namespace = "http://www.edafa.com/web2sms/service/model/")

public class UserReportRequest implements Serializable {

	private static final long serialVersionUID = 7554870662961823648L;

	@XmlElement(required = true, nillable = false)
	private String trx;

	@XmlElement(required = true, nillable = false)
	private String fileToken;

	public UserReportRequest() {}

	public String getTrx() {
		return trx;
	}

	public void setTrx(String trx) {
		this.trx = trx;
	}

	public String getFileToken() {
		return fileToken;
	}

	public void setFileToken(String fileToken) {
		this.fileToken = fileToken;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{trx=").append(trx).append(", fileToken=").append(fileToken).append("}");
		return builder.toString();
	}

}

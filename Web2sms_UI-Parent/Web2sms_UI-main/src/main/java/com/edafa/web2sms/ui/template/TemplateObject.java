package com.edafa.web2sms.ui.template;

import com.edafa.web2sms.service.model.Template;

public class TemplateObject {
	private Template template;
	private int numOfMessages;
	private int numOfChar;

	public Template getTemplate() {
		return template;
	}
	public void setTemplate(Template template) {
		this.template = template;
	}
	public int getNumOfMessages() {
		return numOfMessages;
	}
	public void setNumOfMessages(int numOfMessages) {
		this.numOfMessages = numOfMessages;
	}
	public int getNumOfChar() {
		return numOfChar;
	}
	public void setNumOfChar(int numOfChar) {
		this.numOfChar = numOfChar;
	}

}

package com.edafa.web2sms.utils.alarm;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The persistent class for the ALARMS database table.
 * 
 */
@XmlType(name = "AppErrorRemote")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AppErrorRemote")
public class AppErrorRemote {

        @XmlElement(name = "appErrorId")        
	private Integer appErrorId;
        
        @XmlElement(name = "name")
	private String name;
        
        @XmlElement(name = "description")
	private String description;
        
        @XmlElement(name = "errorDefinitionEntities")
	private List<ErrorDefinitionRemote> errorDefinitionEntities;

	public AppErrorRemote() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAppErrorId() {
		return this.appErrorId;
	}

	public void setAppErrorId(Integer alarmId) {
		this.appErrorId = alarmId;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ErrorDefinitionRemote> getAlarmDefinitions() {
		return this.errorDefinitionEntities;
	}

	public void setAlarmDefinitions(List<ErrorDefinitionRemote> errorDefinitionEntities) {
		this.errorDefinitionEntities = errorDefinitionEntities;
	}

	public ErrorDefinitionRemote addAlarmDefinition(ErrorDefinitionRemote errorDefinitionEntity) {
		getAlarmDefinitions().add(errorDefinitionEntity);
		errorDefinitionEntity.setAlarm(this);

		return errorDefinitionEntity;
	}

	public ErrorDefinitionRemote removeAlarmDefinition(ErrorDefinitionRemote errorDefinitionEntity) {
		getAlarmDefinitions().remove(errorDefinitionEntity);
		errorDefinitionEntity.setAlarm(null);

		return errorDefinitionEntity;
	}

}
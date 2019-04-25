/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.edafa.web2sms.utils.alarm;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author mahmoud
 */
@XmlType(name = "RemoteAlarmsConfig")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RemoteAlarmsConfig")
public class RemoteAlarmsConfig {

    @XmlElement(name = "ErrorDefinitionRemote")
    ArrayList<ErrorDefinitionRemote> errorsDefinition;

    public ArrayList<ErrorDefinitionRemote> getErrorsDefinition() {
        return errorsDefinition;
    }

    public void setErrorsDefinition(ArrayList<ErrorDefinitionRemote> errorsDefinition) {
        this.errorsDefinition = errorsDefinition;
    }

}

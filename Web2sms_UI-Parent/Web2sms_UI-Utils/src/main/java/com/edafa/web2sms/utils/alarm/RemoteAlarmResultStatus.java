/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.edafa.web2sms.utils.alarm;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author mahmoud
 */
@XmlRootElement(name = "RemoteErrorResultStatus")
@XmlEnum
public enum RemoteAlarmResultStatus {

    SUCCESS, INVALID_REQUEST, INTERNAL_SERVER_ERROR, GENERIC_ERROR;
}

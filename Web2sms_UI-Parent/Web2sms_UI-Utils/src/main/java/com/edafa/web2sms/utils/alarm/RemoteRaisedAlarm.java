/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.edafa.web2sms.utils.alarm;

import com.edafa.jee.apperr.Severity;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author mahmoud
 */
@XmlType(name = "RemoteRaisedAlarm")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "RemoteRaisedAlarm")
public class RemoteRaisedAlarm {

    @XmlElement(name = "trxid", required = true, nillable = false)
    protected String trxId;
    @XmlElement(name = "alarmIdentifier")
    protected String alarmIdentifier;
    @XmlElement(name = "errorsource", required = true, nillable = false)
    protected String errorsource;
    @XmlElement(name = "error", required = true, nillable = false)
    protected int error;
    @XmlElement(name = "timestamp", required = true, nillable = false)
    protected Date timestamp;
    @XmlElement(name = "severity")
    protected Severity severity;
    @XmlElement(name = "msg")
    protected String msg;

    public RemoteRaisedAlarm() {
    }

    public RemoteRaisedAlarm(String trxId, String alarmIdentifier, String errorsource, int error, Severity severity, String msg, Date timestamp) {
        this.trxId = trxId;
        this.errorsource = errorsource;
        this.error = error;
        this.severity = severity;
        this.msg = msg;
        this.timestamp = timestamp;
        this.alarmIdentifier = alarmIdentifier;
    }

    public RemoteRaisedAlarm(String trxId, String alarmIdentifier, String errorsource, int error, String msg, Date timestamp) {
        this.trxId = trxId;
        this.errorsource = errorsource;
        this.error = error;
        this.msg = msg;
        this.timestamp = timestamp;
        this.alarmIdentifier = alarmIdentifier;

    }

    public String getAlarmIdentifier() {
        return alarmIdentifier;
    }

    public String getErrorsource() {
        return errorsource;
    }

    public int getError() {
        return error;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMsg() {
        return msg;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTrxId() {
        return trxId;
    }

    public void setAlarmIdentifier(String alarmIdentifier) {
        this.alarmIdentifier = alarmIdentifier;
    }

    public void setErrorsource(String errorsource) {
        this.errorsource = errorsource;
    }

    public void setError(int error) {
        this.error = error;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setTrxId(String trxId) {
        this.trxId = trxId;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("RemoteRaisedAlarm{trxId=");
        str = str.append(trxId)
        .append(", alarmIdentifier=")
        .append(alarmIdentifier)
        .append(", errorsource=")
        .append(errorsource)
        .append(", error=")
        .append(error)
        .append(", severity=")
        .append(severity)
        .append(", msg=")
        .append(msg)
        .append(", timestamp=")
        .append(timestamp)
        .append('}');
        return str.toString();
    }
}

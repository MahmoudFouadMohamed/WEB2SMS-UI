package com.edafa.web2sms.utils.alarm;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import com.edafa.jee.apperr.alarm.Alarm;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.remote.XmlHttpClient;
import com.sun.jersey.api.client.ClientResponse;
import java.util.Date;
import javax.ejb.EJB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Session Bean implementation class AlarmRasingBean
 */
@Stateless
@LocalBean
public class AlarmRasingBean implements com.edafa.jee.apperr.alarm.AlarmRasingBean {

    @EJB
    XmlHttpClient httpClient;

    Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());
    Logger smsLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());

    /**
     * Default constructor.
     */
    public AlarmRasingBean() {
        // TODO Auto-generated constructor stub
    }

    @Override
    @Asynchronous
    public void raiseAlarm(Alarm appError) {
        try {
            smsLogger.debug("Alarm raising error: " + appError.toString());
            String trxId = TrxId.getTrxId();
            RemoteRaisedAlarm remoteRaisedAlarm = new RemoteRaisedAlarm(trxId, (String)Configs.ALARMING_IDENTIFIER.getValue(), (String)appError.getSource(), appError.getAlarmId(), appError.getSeverity(), appError.getInfo(), new Date());
            smsLogger.info("Raising alarm: " + remoteRaisedAlarm);
            String errorsRaisingServiceURI = (String) Configs.ERRORS_RAISING_SERVICE_URI.getValue();
            
            ClientResponse cr = httpClient.sendHttpXmlRequest(errorsRaisingServiceURI, remoteRaisedAlarm);
            if (cr != null) {
                if (cr.getStatus() == 200) {
                    RemoteAlarmResultStatus resultStatus = (RemoteAlarmResultStatus) cr.getEntity(RemoteAlarmResultStatus.class);
                    smsLogger.info("Alarm raising response: " + resultStatus);
                } else {
                    smsLogger.error("Alarm raising response status: " + cr.getStatus());
                }
            } else {
                smsLogger.error("Alarm raising response is null");
            }

        } catch (Exception e) {
            smsLogger.error("Unhandled Exception while raising error ");
            appLogger.error("Unhandled Exception while raising error ", e);
        }
    }

}

package com.edafa.web2sms.utils.alarm;

import javax.ejb.Local;

@Local
public interface AppErrorManagerLocal {

	boolean refreshAlarmDefinitions();

}

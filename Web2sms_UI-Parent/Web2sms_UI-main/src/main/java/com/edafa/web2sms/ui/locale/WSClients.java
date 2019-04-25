package com.edafa.web2sms.ui.locale;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.edafa.web2sms.service.acc_manag.account.AccountManegementService;
import com.edafa.web2sms.service.acc_manag.account.AccountManegementService_Service;
import com.edafa.web2sms.service.acc_manag.account.group.GroupManagementService;
import com.edafa.web2sms.service.acc_manag.account.group.GroupManagementService_Service;
import com.edafa.web2sms.service.acc_manag.account.user.UserLoginService;
import com.edafa.web2sms.service.acc_manag.account.user.UserLoginService_Service;
import com.edafa.web2sms.service.acc_manag.account.user.UserManegementService;
import com.edafa.web2sms.service.acc_manag.account.user.UserManegementService_Service;
import com.edafa.web2sms.service.campaign.CampaignManagementService;
import com.edafa.web2sms.service.campaign.CampaignManagementService_Service;
import com.edafa.web2sms.service.lists.ListsManegementService;
import com.edafa.web2sms.service.lists.ListsManegementService_Service;
import com.edafa.web2sms.service.prov.ServiceProvisioning;
import com.edafa.web2sms.service.prov.ServiceProvisioningImplService;
import com.edafa.web2sms.service.report.ReportManagementService;
import com.edafa.web2sms.service.report.ReportManagementService_Service;
import com.edafa.web2sms.service.template.TemplateManegementService;
import com.edafa.web2sms.service.template.TemplateManegementService_Service;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.configs.enums.ModulesEnum;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsListener;
import com.edafa.web2sms.utils.configs.interfaces.ConfigsManagerBeanLocal;
import javax.xml.ws.handler.Handler;
import java.util.List;
import java.util.ArrayList;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

@Singleton
@LocalBean
@Startup
public class WSClients implements ConfigsListener {
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());

	@EJB
	ConfigsManagerBeanLocal configsManagerBean;

	AccountManegementService_Service accountManagementService;
	// AccountManegementService accountServicePort;

	CampaignManagementService_Service campaignManagementService;
	// CampaignManagementService campaignServicePort;

	ListsManegementService_Service listsManegementService_Service;
	// ListsManegementService listsManegementServicePort;

	TemplateManegementService_Service templateManegmentService_Service;
	// TemplateManegementService templateManegmentServicePort;

	ReportManagementService_Service reportManagementService;
	// ReportManagementService reportServicePort;

	ServiceProvisioningImplService serviceProvisioningService;
	// ServiceProvisioning serviceProvisioningPort;
        
        UserManegementService_Service userManagementService;
        
        GroupManagementService_Service groupManagementService;
        
        UserLoginService_Service userLoginService;

	ThreadLocal<AccountManegementService> accountServicePorts;

	ThreadLocal<CampaignManagementService> campaignServicePorts;

	ThreadLocal<ListsManegementService> listsManegementServicePorts;

	ThreadLocal<TemplateManegementService> templateManegmentServicePorts;

	ThreadLocal<ReportManagementService> reportManagementServicePorts;

	ThreadLocal<ServiceProvisioning> serviceProvisioningPorts;
        
	ThreadLocal<UserManegementService> userManagementServicePorts;
        
	ThreadLocal<GroupManagementService> groupManagementServicePorts;
        
	ThreadLocal<UserLoginService> userLoginServicePorts;

	private void initBindingProvider(BindingProvider bindingProvider, String url) {
		int requestTimeout = (int) Configs.WS_REQUEST_TIMEOUT.getValue();
		int connectTimeout = (int) Configs.WS_CONNECT_TIMEOUT.getValue();
		appLogger.debug("Setting binding properties for " + bindingProvider.getClass().getSimpleName() + " port");
		bindingProvider.getRequestContext().put("com.sun.xml.ws.request.timeout", requestTimeout);
		bindingProvider.getRequestContext().put("com.sun.xml.ws.connect.timeout", connectTimeout);
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
//		appLogger.debug("Binding properties for " + bindingProvider.getClass().getName() + ": requestTimeout="
//				+ requestTimeout + ", connectTimeout=" + connectTimeout + ", endpointAddress=\"" + url + "\"");
	}

	public WSClients() {

	}

	@PostConstruct
	public void init() {
		loadURLs();
		configsManagerBean.registerConfigsListener(ModulesEnum.UIManagement, this);

	}

	public AccountManegementService getAccountServicePort() {
		return accountServicePorts.get();
	}

	public CampaignManagementService getCampaignServicePort() throws WebServiceException {
		return campaignServicePorts.get();
	}

	public ListsManegementService getListsManegementServicePort() {
		return listsManegementServicePorts.get();
	}

	public TemplateManegementService getTemplateManegmentServicePort() {
		return templateManegmentServicePorts.get();
	}

	public ReportManagementService getReportServicePort() {
		return reportManagementServicePorts.get();
	}

	public ServiceProvisioning getServiceProvisioningPort() {
		return serviceProvisioningPorts.get();
	}

        public UserManegementService getUserManagementPorts() {
            return userManagementServicePorts.get();
        }

        public GroupManagementService getGroupManagementPorts() {
            return groupManagementServicePorts.get();
        }

        public UserLoginService getUserLoginPort() {
            return userLoginServicePorts.get();
        }

	@Override
	public void configurationRefreshed(ModulesEnum module) {
		appLogger.info("Configuration refreshed for module " + module + ", will reinitalize web service clients");
		loadURLs();
		appLogger.info("Web service clients reinitalized");
	}

	@Override
	public void configurationRefreshed() {
		appLogger.info("Configuration refreshed, will reinitalize web service clients");
		loadURLs();
		appLogger.info("Web service clients reinitalized");
	}
	
	@Lock(LockType.WRITE)
	private void loadURLs() {
		try {
			serviceProvisioningPorts = new ThreadLocal<ServiceProvisioning>() {

				AtomicInteger instCount = new AtomicInteger();

				protected ServiceProvisioning initialValue() {
					String serviceProvisioningURL = (String) Configs.SERVICE_PROVISIONING_WEBSERVICE_URL.getValue();
					if (serviceProvisioningService == null) {
						appLogger.info("Initializing ServiceProvisioning client");
						serviceProvisioningService = new ServiceProvisioningImplService();
					}
					appLogger.info("Getting ServiceProvisioning port");
					ServiceProvisioning serviceProvisioningPort = serviceProvisioningService.getServiceProvisioning();
					initBindingProvider((BindingProvider) serviceProvisioningPort, serviceProvisioningURL);
//					appLogger.info("ServiceProvisioning port is intialized with url " + serviceProvisioningURL
//							+ ", instances count=" + instCount.incrementAndGet());

					return serviceProvisioningPort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("ServiceProvisioning port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh serviceProvisioningPorts : " + e.getMessage(), e);
		}

		try {
			accountServicePorts = new ThreadLocal<AccountManegementService>() {

				AtomicInteger instCount = new AtomicInteger();

				protected AccountManegementService initialValue() {
					String accountServiceURL = (String) Configs.ACCOUNT_WEBSERVICE_URL.getValue();
					if (accountManagementService == null) {
						appLogger.info("Initializing AccountManegementService client");
						accountManagementService = new AccountManegementService_Service();
					}
					appLogger.info("Getting AccountManegementService port");
					AccountManegementService accountServicePort = accountManagementService
							.getAccountManegementServicePort();
					initBindingProvider((BindingProvider) accountServicePort, accountServiceURL);
//					appLogger.info("AccountManegementService port is intialized with url " + accountServiceURL
//							+ ", instances count=" + instCount.incrementAndGet());
					return accountServicePort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("AccountManegementService port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh accountServicePorts : " + e.getMessage(), e);
		}

		try {
			campaignServicePorts = new ThreadLocal<CampaignManagementService>() {

				AtomicInteger instCount = new AtomicInteger();

				protected CampaignManagementService initialValue() {
					String campaignServiceURL = (String) Configs.CAMPAIGN_WEBSERVICE_URL.getValue();
					if (campaignManagementService == null) {
						appLogger.info("Initializing CampaignManagementService client");
						campaignManagementService = new CampaignManagementService_Service();
					}
					appLogger.info("Getting CampaignManagementService port");
					CampaignManagementService campaignServicePort = campaignManagementService
							.getCampaignManagementServicePort();
					initBindingProvider((BindingProvider) campaignServicePort, campaignServiceURL);
//					appLogger.info("CampaignManagementService port is intialized with url " + campaignServiceURL
//							+ ", instances count=" + instCount.incrementAndGet());
					return campaignServicePort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("CampaignManagementService port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh campaignServicePorts : " + e.getMessage(), e);
		}

		try {
			listsManegementServicePorts = new ThreadLocal<ListsManegementService>() {

				AtomicInteger instCount = new AtomicInteger();

				protected ListsManegementService initialValue() {
					String listServiceURL = (String) Configs.LIST_WEBSERVICE_URL.getValue();
					if (listsManegementService_Service == null) {
						appLogger.info("Initializing ListsManegementService client");
						listsManegementService_Service = new ListsManegementService_Service();
					}
					appLogger.info("Getting ListsManegementService port");
					ListsManegementService listsManegementServicePort = listsManegementService_Service
							.getListsManegementServicePort();
					initBindingProvider((BindingProvider) listsManegementServicePort, listServiceURL);
//					appLogger.info("ListsManegementService port is intialized with url " + listServiceURL
//							+ ", instances count=" + instCount.incrementAndGet());

					return listsManegementServicePort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("ListsManegementService port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh listsManegementServicePorts : " + e.getMessage(), e);
		}

		try {
			templateManegmentServicePorts = new ThreadLocal<TemplateManegementService>() {

				AtomicInteger instCount = new AtomicInteger();

				protected TemplateManegementService initialValue() {
					String templateServiceURL = (String) Configs.TEMPALTE_WEBSERVICE_URL.getValue();
					if (templateManegmentService_Service == null) {
						appLogger.info("Initializing TemplateManegementService client");
						templateManegmentService_Service = new TemplateManegementService_Service();
					}
					appLogger.info("Getting TemplateManegementService port");
					TemplateManegementService templateManegmentServicePort = templateManegmentService_Service
							.getTemplateManegementServicePort();
					initBindingProvider((BindingProvider) templateManegmentServicePort, templateServiceURL);
//					appLogger.info("TemplateManegementService port is intialized with url " + templateServiceURL
//							+ ", instances count=" + instCount.incrementAndGet());
					return templateManegmentServicePort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("TemplateManegementService port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh templateManegmentServicePorts : " + e.getMessage(), e);
		}

		try {
			reportManagementServicePorts = new ThreadLocal<ReportManagementService>() {

				AtomicInteger instCount = new AtomicInteger();

				protected ReportManagementService initialValue() {
					String reportServiceURL = (String) Configs.REPORT_WEBSERVICE_URL.getValue();
					if (reportManagementService == null) {
						appLogger.info("Initializing ReportManagementService client");
						reportManagementService = new ReportManagementService_Service();
					}
					appLogger.info("Getting ReportManagementService port");
					ReportManagementService reportServicePort = reportManagementService
							.getReportManagementServicePort();
					initBindingProvider((BindingProvider) reportServicePort, reportServiceURL);
//					appLogger.info("ReportManagementService port is intialized with url " + reportServiceURL
//							+ ", instances count=" + instCount.incrementAndGet());

					return reportServicePort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("ReportManagementService port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh reportManagementServicePorts : " + e.getMessage(), e);
		}

		try {
			userManagementServicePorts = new ThreadLocal<UserManegementService>() {

				AtomicInteger instCount = new AtomicInteger();

				protected UserManegementService initialValue() {
					String userManaegementServiceURL = (String) Configs.USER_MANAGEMENT_WEBSERVICE_URL.getValue();
					if (userManagementService == null) {
						appLogger.info("Initializing UserManagementService client");
						userManagementService = new UserManegementService_Service();
                                        }
					appLogger.info("Getting UserManegementService port");
					UserManegementService userServicePort = userManagementService.getUserManegementServicePort();
					initBindingProvider((BindingProvider) userServicePort, userManaegementServiceURL);
//					appLogger.info("UserManegementService port is intialized with url " + userManaegementServiceURL
//							+ ", instances count=" + instCount.incrementAndGet());
					return userServicePort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("UserManegementService port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh userManagementServicePorts : " + e.getMessage(), e);
		}

		try {
			groupManagementServicePorts = new ThreadLocal<GroupManagementService>() {

				AtomicInteger instCount = new AtomicInteger();

				protected GroupManagementService initialValue() {
					String groupManagementServiceURL = (String) Configs.GROUP_MANAGEMENT_WEBSERVICE_URL.getValue();
					if (groupManagementService == null) {
						appLogger.info("Initializing GroupManagementService client");
						groupManagementService = new GroupManagementService_Service();
					}
					appLogger.info("Getting GroupManagementService port");
					GroupManagementService groupServicePort = groupManagementService.getGroupManagementServicePort();
					initBindingProvider((BindingProvider) groupServicePort, groupManagementServiceURL);
//					appLogger.info("GroupManagementService port is intialized with url " + groupManagementServiceURL
//							+ ", instances count=" + instCount.incrementAndGet());
					return groupServicePort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("GroupManagementService port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh groupManagementServicePorts : " + e.getMessage(), e);
		}

		try {
			userLoginServicePorts = new ThreadLocal<UserLoginService>() {

				AtomicInteger instCount = new AtomicInteger();

				protected UserLoginService initialValue() {
					String userLoginServiceURL = (String) Configs.USER_LOGIN_WEBSERVICE_URL.getValue();
					if (userLoginService == null) {
						appLogger.info("Initializing UserLoginService client");
						userLoginService = new UserLoginService_Service();
					}
					appLogger.info("Getting UserLoginService port");
					UserLoginService userLoginServicePort = userLoginService.getUserLoginServiceImplPort();
					initBindingProvider((BindingProvider) userLoginServicePort, userLoginServiceURL);
//					appLogger.info("UserLoginService port is intialized with url " + userLoginServiceURL
//							+ ", instances count=" + instCount.incrementAndGet());
					return userLoginServicePort;
				}

				public void remove() {
					super.remove();
					appLogger.trace("UserLoginService port instance count=" + instCount.decrementAndGet());
				};
			};
		} catch (Exception e) {
			appLogger.error("Error during refresh groupManagementServicePorts : " + e.getMessage(), e);
		}

	}
}

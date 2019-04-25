package com.edafa.web2sms.ui.template;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.web2sms.encyrptionutil.Interfaces.EncyrptionUtilInterface;
import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.model.ResultStatus;
import com.edafa.web2sms.service.model.Template;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.model.enums.Language;
import com.edafa.web2sms.service.template.TemplateManegementService;
import com.edafa.web2sms.service.template.TemplatesResultSet;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.Util;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;

@ManagedBean(name = "templateBean")
@ViewScoped
public class TemplateBean {
	FacesContext facesContext = FacesContext.getCurrentInstance();
	HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
	UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
	User user = new User();

	TemplateManegementService templateManegmentServicePort;

	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());

	private List<TemplateObject> templateList;
	private List<TemplateObject> adminTemplateList;
	private String tempalteName;
	private String templateText;
	private String language = "ENGLISH";
	private TemplateObject templateObj;
	private TemplateObject templateDelObj;
	private boolean viewTemplateTitles = true;
	private boolean viewTemlateAdminTitle = true;

	public static final int engMsgMaxLen = 160;
	public static final int nonEngMsgMaxLen = 70;
	public static final int engConcMsgLen = 153;
	public static final int nonEngConcMsgLen = 67;

	private boolean goToHome = false; // TODO change to false;

	@EJB
	WSClients portObj;
	@EJB
	AppErrorManagerAdapter appErrorManagerAdapter;

	@EJB
	EncyrptionUtilInterface encyrptionUtil;
	// Setters and getters ---------------------------------------

	public boolean isViewTemlateAdminTitle() {
		return viewTemlateAdminTitle;
	}

	public boolean isGoToHome() {
		return goToHome;
	}

	public void setGoToHome(boolean goToHome) {
		this.goToHome = goToHome;
	}

	public void setViewTemlateAdminTitle(boolean viewTemlateAdminTitle) {
		this.viewTemlateAdminTitle = viewTemlateAdminTitle;
	}

	public boolean isViewTemplateTitles() {
		return viewTemplateTitles;
	}

	public void setViewTemplateTitles(boolean viewTemplateTitles) {
		this.viewTemplateTitles = viewTemplateTitles;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getTemplateText() {
		return templateText;
	}

	public void setTemplateText(String templateText) {
		this.templateText = templateText;
	}

	public String getTempalteName() {
		return tempalteName;
	}

	public void setTempalteName(String tempalteName) {
		this.tempalteName = tempalteName;
	}

	public List<TemplateObject> getTemplateList() {
		return templateList;
	}

	public void setTemplateList(List<TemplateObject> templateList) {
		this.templateList = templateList;
	}

	public List<TemplateObject> getAdminTemplateList() {
		return adminTemplateList;
	}

	public TemplateObject getTemplateObj() {
		return templateObj;
	}

	public void setTemplateObj(TemplateObject templateObj) {
		this.templateObj = templateObj;
	}

	public TemplateObject getTemplateDelObj() {
		return templateDelObj;
	}

	public void setTemplateDelObj(TemplateObject templateDelObj) {
		this.templateDelObj = templateDelObj;
	}

	public void setAdminTemplateList(List<TemplateObject> adminTemplateList) {
		this.adminTemplateList = adminTemplateList;
	}

	// Constructor ----------------------------------------------

	public TemplateBean() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());

			templateList = new ArrayList<TemplateObject>();
			adminTemplateList = new ArrayList<TemplateObject>();

		} catch (Exception e) {
			userLogger.error("Error while setting user account ", e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	@PostConstruct
	private void postMethod() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			userInfo.setTrxId(TrxId.getTrxId());
			userInfo.setUser(user);

			templateManegmentServicePort = portObj.getTemplateManegmentServicePort();

			populateList();
		} catch (Exception e) {
			userLogger.error("Error while getting template managment service port", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public String logTrxId(String trxId) {
		StringBuilder sb = new StringBuilder();
		sb.append("UserTrx");
		sb.append("(");
		sb.append(trxId);
		sb.append("): ");
		return sb.toString();
	}

	// Action methods-------------------------------------------------

	public void populateList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			userLogger.info("populating all template lists");

			TemplatesResultSet tempResult = templateManegmentServicePort.getUserTemplates(userInfo);

			ResponseStatus status = tempResult.getStatus();

			userLogger.info( " getting template for account id:( " +user.getAccountId()+ ") "+"finished with status:(" + status+ ").");
			
			switch (status) {
			case SUCCESS:
				userLogger.info( "user template count: ("+tempResult.getTemplateList().size()+").");
				templateList.clear();
				List<Template> temp = tempResult.getTemplateList();
				TemplateObject obj;
				for (Template template : temp) {
					obj = new TemplateObject();
					obj.setTemplate(template);
					obj.setNumOfChar(calcCharCount(template.getLanguage(), template.getText()));
					obj.setNumOfMessages(calcSegCount(template.getLanguage(), template.getText()));
					templateList.add(obj);
				}
				userLogger.info( " templates is populated successfully");
				break;
			case FAIL:
                                FacesContext.getCurrentInstance().addMessage(
                                                null,
                                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                .getLocalizedLabel("loading_failed")), null));
				userLogger.error( "Error while populating user templates");
				break;
			case INELIGIBLE_ACCOUNT:
                                FacesContext.getCurrentInstance().addMessage(
                                                null,
                                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                .getLocalizedLabel("ineligible_view_templates")), null));
				userLogger.warn( " not allowed to display his templates");
				break;
			case TEMPLATES_NOT_FOUND:
                                FacesContext.getCurrentInstance().addMessage(
                                                null,
                                                new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
                                                                .getLocalizedLabel("no_templates")), null));
				userLogger.warn("Templates are not found");
				break;
			default:
                                FacesContext.getCurrentInstance().addMessage(
                                                null,
                                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                                .getLocalizedLabel("loading_failed")), null));
				userLogger.error( "Undefined result status=" + status + " while getting user templates");
				break;
			}

			TemplatesResultSet adminTempResult = templateManegmentServicePort.getAdminTemplates(userInfo);

			ResponseStatus status1 = adminTempResult.getStatus();
                        Iterator<FacesMessage> msgIterator;
			switch (status1) {
			case SUCCESS:
				adminTemplateList.clear();
				List<Template> adminTemp = adminTempResult.getTemplateList();
				TemplateObject obj;
				for (Template template : adminTemp) {
					obj = new TemplateObject();
					obj.setTemplate(template);
					obj.setNumOfChar(calcCharCount(template.getLanguage(), template.getText()));
					obj.setNumOfMessages(calcSegCount(template.getLanguage(), template.getText()));
					adminTemplateList.add(obj);
				}
				userLogger.info( "ready templates is populated successfully");
				break;
			case FAIL:
//                                msgIterator = FacesContext.getCurrentInstance().getMessages();
//                                while (msgIterator.hasNext()) {
//                                        msgIterator.next();
//                                        msgIterator.remove();
//                                }
//
//                                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("loading_failed")), null));
			
				userLogger.error( "Error while populating ready templates ");
				break;
			case INELIGIBLE_ACCOUNT:
//                                msgIterator = FacesContext.getCurrentInstance().getMessages();
//                                while (msgIterator.hasNext()) {
//                                        msgIterator.next();
//                                        msgIterator.remove();
//                                }
//                                FacesContext.getCurrentInstance().addMessage(
//                                                null,
//                                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
//                                                                .getLocalizedLabel("ineligible_view_templates")), null));

				userLogger.warn(" not allowed to display ready templates");
				break;
			case TEMPLATES_NOT_FOUND:
//                                msgIterator = FacesContext.getCurrentInstance().getMessages();
//                                while (msgIterator.hasNext()) {
//                                        msgIterator.next();
//                                        msgIterator.remove();
//                                }
//                                FacesContext.getCurrentInstance().addMessage(
//                                                null,
//                                                new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
//                                                                .getLocalizedLabel("no_templates")), null));

				userLogger.warn("Ready templates are not found");
				break;
			default:
//                                msgIterator = FacesContext.getCurrentInstance().getMessages();
//                                while (msgIterator.hasNext()) {
//                                        msgIterator.next();
//                                        msgIterator.remove();
//                                }
//                                FacesContext.getCurrentInstance().addMessage(
//                                                null,
//                                                new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
//                                                                .getLocalizedLabel("loading_failed")), null));

				userLogger.error( "Undefined result status=" + status1 + " while getting ready templates");
				break;
			}

			if (templateList == null || templateList.isEmpty()) {
				userLogger.info( "No templates found");
				viewTemplateTitles = false;
			}

			if (adminTemplateList == null || adminTemplateList.isEmpty()) {
				userLogger.info( "No ready templates are found");
				viewTemlateAdminTitle = false;
			}

		} catch (Exception e) {
//                        Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
//			while (msgIterator.hasNext()) {
//				msgIterator.next();
//				msgIterator.remove();
//			}
//			
//			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util.getLocalizedLabel("unexcepected_error")), null));
//			
			userLogger.error("Error while populating admin and user templates list", e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	private boolean validateLenght() {
		int smsTextLength = (Integer) Configs.SMS_TEXT_LENGTH.getValue();
		int temNameLength = (Integer) Configs.TEMP_NAME_LENGTH.getValue();
		if (tempalteName.length() > temNameLength) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("template_name_length")), null));
			userLogger.info( " entered too long template name more than system configured length=" + temNameLength);
			return false;
		} else if (templateText.length() > smsTextLength) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("template_text_length")), null));
			userLogger.info( " entered too long template text more than system configured length=" + smsTextLength);
			return false;
		}

		return true;
	}

	private boolean validateRequired() {
		if (tempalteName.trim().isEmpty() || templateText.trim().isEmpty()) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("all_required_template_filed")), null));
			userLogger.info( " didn't enter template name or template text");
			return false;
		}
		return true;
	}

	public String createTempalte() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			if (language.equals("true")) {
				language = "ENGLISH";
			} else {
				language = "ARABIC";
			}

			if (validateRequired()) {
				if (validateLenght()) {
					userLogger.info( "Creating template");
					Template template = new Template();
					template.setTemplateName(tempalteName);
					template.setTemplateType(false);
					template.setText(templateText);
					template.setLanguage(Language.valueOf(language));

					userLogger.debug( "creating template [template_name=" + template.getTemplateName()
							+ ",template_type=false,template_text=" + encyrptionUtil.encrypt(template.getText()) + ",template_language="
							+ template.getLanguage() + "]");

					ResultStatus status = templateManegmentServicePort.createTemplate(userInfo, template);

					ResponseStatus res = status.getStatus();


					switch (res) {
					
					case SUCCESS:
//						populateList();

						userLogger.info( "created template "
								+ tempalteName + " successfully");
						goToHome = true;
						tempalteName = "";
						templateText = "";

						break;
					case INELIGIBLE_ACCOUNT:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("ineligible_create_template")), null));
						userLogger.warn( " not allowed to create template");
						break;
					case FAIL:
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("template_create_failure")), null));
						userLogger.error( "Error while creating template " + tempalteName);
						break;
					default:
						userLogger.error( "undefined result status="
								+ res + " while creating new template");
						break;
					}
				}
			}
		} catch (Exception e) {
			userLogger.error( "Error while creating template " + tempalteName, e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
		if (goToHome)
			return "campaign";
		else
			return "stay";
	}

	public String editTemplate() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			int smsTextLength = (Integer) Configs.SMS_TEXT_LENGTH.getValue();
			userLogger.info(" the maximum length of template sms text=" + smsTextLength);

			userLogger.info( "Editing template");

			userLogger.debug( "Editing template [template_id=" + templateObj.getTemplate().getTemplateId() + ",tempalte_name="
					+ templateObj.getTemplate().getTemplateName() + ",text=" +encyrptionUtil.encrypt(templateObj.getTemplate().getText())
					+ ",language=" + templateObj.getTemplate().getLanguage() + ",number_of_charcters="
					+ templateObj.getNumOfChar() + ",number_of_messages=" + templateObj.getNumOfMessages() + "]");

			if (templateObj.getTemplate().getText().trim().isEmpty()) {

				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("template_text_required")), null));

				userLogger.info( "the text of template with id=" + templateObj.getTemplate().getTemplateId() + " is empty");

			} else if (templateObj.getTemplate().getText().length() > smsTextLength) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("template_text_length")), null));
				userLogger.info( "text of template with id=" + templateObj.getTemplate().getTemplateId()
						+ " more than the accepted limit=" + smsTextLength);
			} else {
				ResultStatus status = templateManegmentServicePort.editTemplate(userInfo, templateObj.getTemplate());
				ResponseStatus response = status.getStatus();
				userLogger.debug( "Editing template response: " + response);
				switch (response) {
				case INVALID_REQUEST:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("edit_template_failure")), null));
					userLogger.warn( "Invalid request for creating template");
					break;
				case INELIGIBLE_ACCOUNT:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("ineligible_edit_template")), null));
					userLogger.warn("not allowed to edit template "
							+ templateObj.getTemplate().getTemplateId());
					break;
				case FAIL:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("edit_template_failure")), null));
					userLogger.error("Error while editing template "
							+ templateObj.getTemplate().getTemplateId());

					break;
				case SUCCESS:
					userLogger.info(" edited template " + templateObj.getTemplate().getTemplateId()
							+ " successfully");
					userLogger.info( "Tempalte with id="
							+ templateObj.getTemplate().getTemplateId() + " is edited successfully");
					break;

				default:
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("edit_template_failure")), null));
					userLogger
							.error( "undefined response status=" + response + " while editing template with id="
									+ templateObj.getTemplate().getTemplateId());
					break;
				}
			}

		} catch (Exception e) {
			userLogger.error("Error while editing template " + templateObj.getTemplate().getTemplateId(), e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
		return "";
	}

	public String deleteTemplate() {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info( "Deleting template id=" + templateDelObj.getTemplate().getTemplateId());

			ResultStatus status = templateManegmentServicePort.deleteTemplate(userInfo, templateDelObj.getTemplate()
					.getTemplateId());
			ResponseStatus response = status.getStatus();
			switch (response) {
			case INVALID_REQUEST:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("delete_template_failure")), null));
				userLogger.warn("Invalid request for deleting template id=" + templateDelObj.getTemplate().getTemplateId());
				break;
			case INELIGIBLE_ACCOUNT:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("ineligible_delete_template")), null));
				userLogger.warn( "not allowed to delete template " + templateDelObj.getTemplate().getTemplateId());
				break;
			case FAIL:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("delete_template_failure")), null));
				userLogger.error( "Error while deleting template " + templateDelObj.getTemplate().getTemplateId());
				break;
			case SUCCESS:
				templateList.remove(templateDelObj);
				userLogger.info( "deleted template " + templateDelObj.getTemplate().getTemplateId() + " successfully");
				populateList();
				break;

			default:
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("delete_template_failure")), null));
				userLogger.error( "undefined response status=" + response + " while deleting template id="
						+ templateDelObj.getTemplate().getTemplateId());
				break;
			}

		} catch (Exception e) {
			userLogger.error( "Error while deleting template " + templateDelObj.getTemplate().getTemplateId(), e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
		return "";
	}

	public static int calcSegCount(Language lang, String smsText) {
		String gsm2bitsChars = "\f^{}\\[~]|€";

		int totalSegments = 1;
		int msgMaxLen = 0;
		int msgLength = smsText.length();
		int extraMsgLen = 0;

		switch (lang) {
		case ENGLISH: {
			for (int i = 0; i < msgLength; i++) {
				char testChar = smsText.charAt(i);
				if ((gsm2bitsChars.indexOf(testChar) > -1)) {
					extraMsgLen++;
				}// end if
			}// end for

			msgLength += extraMsgLen;

			msgMaxLen = engMsgMaxLen;
			if (msgLength > msgMaxLen) {
				msgMaxLen = engConcMsgLen;
				totalSegments = (int) Math.ceil((double) msgLength / msgMaxLen);
			}

			break;
		}// end case

		default: {
			msgMaxLen = nonEngMsgMaxLen;
			if (msgLength > msgMaxLen) {
				msgMaxLen = nonEngConcMsgLen;
				totalSegments = (int) Math.ceil((double) msgLength / msgMaxLen);
			}
		}
		}// end switch

		return totalSegments;
	}

	public static int calcCharCount(Language lang, String smsText) {
		String gsm2bitsChars = "\f^{}\\[~]|€";

		int msgLength = smsText.length();
		int extraMsgLen = 0;

		switch (lang) {
		case ENGLISH: {
			for (int i = 0; i < msgLength; i++) {
				char testChar = smsText.charAt(i);
				if ((gsm2bitsChars.indexOf(testChar) > -1)) {
					extraMsgLen++;
				}// end if
			}// end for

			msgLength += extraMsgLen;

			break;
		}// end case
		default: {

		}// end default
		}// end switch

		return msgLength;
	}// end of method calcCharCount

	public String userLogInfo(String id, String userName) {
		StringBuilder sb = new StringBuilder();
		sb.append("User");
		sb.append("(");
		sb.append(id);
		sb.append(",");
		sb.append(userName);
		sb.append("): ");
		return sb.toString();
	}
    
        public String getEmptyString(){
            return "";
        }
}
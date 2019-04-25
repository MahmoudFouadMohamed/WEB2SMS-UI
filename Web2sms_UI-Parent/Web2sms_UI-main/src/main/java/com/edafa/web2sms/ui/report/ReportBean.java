package com.edafa.web2sms.ui.report;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.service.campaign.model.CampaignResult;
import com.edafa.web2sms.service.acc_manag.account.AccountManegementService;
import com.edafa.web2sms.service.acc_manag.account.model.QuotaHistoryResult;
import com.edafa.web2sms.service.acc_manag.model.AccManagUser;
import com.edafa.web2sms.service.acc_manag.model.AccountUserTrxInfo;
import com.edafa.web2sms.service.acc_manag.model.QuotaHistory;
import com.edafa.web2sms.service.campaign.CampaignManagementService;
import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.model.CampaignAggregationReport;
import com.edafa.web2sms.service.model.GetCountResult;
import com.edafa.web2sms.service.model.HistoryReportSearch;
import com.edafa.web2sms.service.model.ReportsResult;
import com.edafa.web2sms.service.model.ResultStatus;
import com.edafa.web2sms.service.model.SMSReport;
import com.edafa.web2sms.service.model.SummaryReport;
import com.edafa.web2sms.service.model.User;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.report.ReportManagementService;
import com.edafa.web2sms.service.report.ReportResultSet;
import com.edafa.web2sms.service.report.Reports;
import com.edafa.web2sms.service.report.SmsReportResultSet;
import com.edafa.web2sms.ui.locale.UserAccount;
import com.edafa.web2sms.ui.locale.Util;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.TrxId;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.file.DownloadedFileInfo;

@ManagedBean(name = "reportBean")
@ViewScoped
public class ReportBean {
	ReportManagementService reportServicePort;
	AccountManegementService accountServicePort;
	CampaignManagementService campaignServicePort;

	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	Logger appLogger = LogManager.getLogger(LoggersEnum.APP_UTILS.name());

	FacesContext facesContext = FacesContext.getCurrentInstance();
	HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        static final String USER_KEY = (String) Configs.USER_ATTRIBUTE.getValue();
	UserAccount userAcc = (UserAccount) request.getSession().getAttribute(USER_KEY);
	User user = new User();
	AccManagUser acctManagUser = new AccManagUser();
	
	List<ReportObject> reportsList;
	List<ReportObject> reportsHistoryList = new ArrayList<ReportObject>();
	List<Reports> requestedReports = new ArrayList<Reports>();
	
	boolean tableFlag = true;
	boolean requestedReportsTableFlag = true;
	@EJB
	WSClients portObj;
	@EJB
	AppErrorManagerAdapter appErrorManagerAdapter;

	int totalRows;
	int firstRow;
	int rowsPerPage;
	int pageRange;
	Integer[] pages;
	int currentPage;
	int totalPages;

	int totalRepHistoryRows;
	int firstRepHistoryRow;
	int rowsRepHistoryPerPage;
	int pageRepHistoryRange;
	Integer[] pagesRepHistory;
	int currentRepHistoryPage;
	int totalRepHistoryPages;

	InputStream input;
	String campaignId;
	DownloadedFileInfo result;

	String campaignNameSearch;
	String fromDate;
	String toDate;
	Date finalFromDate;
	Date finalToDate;
	SummaryReport grandTotal;

	// List<String> reportsHistoryList;

	boolean reportHistoryFlag;
	QuotaHistory quotaHistory = new QuotaHistory();

	// SMS API variables
	private String smsFromDateStr;
	private String smsToDateStr;
	private String smsSearchField;
	private String smsSearchValue;
	private Date smsFromDate;
	private Date smsToDate;

	private int totalSmsReportHistoryRows;
	private int firstSmsReportHistoryRow;
	private int rowsSmsReportHistoryPerPage;
	private int pageSmsReportHistoryRange;
	private Integer[] pagesSmsReportHistory;
	private int currentSmsReportHistoryPage;
	private int totalSmsReportHistoryPages;

	private boolean smsReportHistoryFlag;
	private SummaryReport smsReportGrandTotal;

	private List<SmsReportObject> smsReportsHistoryList = new ArrayList<SmsReportObject>();

	// exporting sms api
	HistoryReportSearch historyReportSearch = null;

	// Resending failed
	boolean resendFlag = true;

	int requestedReportsTotalRows;
	int requestedReportsFirstRow;
	int requestedReportsRowsPerPage;
	int requestedReportsPageRange;
	Integer[] requestedReportsPages;
	int requestedReportsCurrentPage;
	int requestedReportsTotalPages;
	// Setters and getters -----------------------------------------------------

	public boolean isTableFlag() {
		return tableFlag;
	}

	public List<Reports> getRequestedReports() {
		return requestedReports;
	}

	public void setRequestedReports(List<Reports> requestedReports) {
		this.requestedReports = requestedReports;
	}

	public boolean isRequestedReportsTableFlag() {
		return requestedReportsTableFlag;
	}

	public void setRequestedReportsTableFlag(boolean requestedReportsTableFlag) {
		this.requestedReportsTableFlag = requestedReportsTableFlag;
	}

	public int getRequestedReportsTotalRows() {
		return requestedReportsTotalRows;
	}

	public void setRequestedReportsTotalRows(int requestedReportsTotalRows) {
		this.requestedReportsTotalRows = requestedReportsTotalRows;
	}

	public int getRequestedReportsFirstRow() {
		return requestedReportsFirstRow;
	}

	public void setRequestedReportsFirstRow(int requestedReportsFirstRow) {
		this.requestedReportsFirstRow = requestedReportsFirstRow;
	}

	public int getRequestedReportsRowsPerPage() {
		return requestedReportsRowsPerPage;
	}

	public void setRequestedReportsRowsPerPage(int requestedReportsRowsPerPage) {
		this.requestedReportsRowsPerPage = requestedReportsRowsPerPage;
	}

	public int getRequestedReportsPageRange() {
		return requestedReportsPageRange;
	}

	public void setRequestedReportsPageRange(int requestedReportsPageRange) {
		this.requestedReportsPageRange = requestedReportsPageRange;
	}

	public Integer[] getRequestedReportsPages() {
		return requestedReportsPages;
	}

	public void setRequestedReportsPages(Integer[] requestedReportsPages) {
		this.requestedReportsPages = requestedReportsPages;
	}

	public int getRequestedReportsCurrentPage() {
		return requestedReportsCurrentPage;
	}

	public void setRequestedReportsCurrentPage(int requestedReportsCurrentPage) {
		this.requestedReportsCurrentPage = requestedReportsCurrentPage;
	}

	public int getRequestedReportsTotalPages() {
		return requestedReportsTotalPages;
	}

	public void setRequestedReportsTotalPages(int requestedReportsTotalPages) {
		this.requestedReportsTotalPages = requestedReportsTotalPages;
	}

	public boolean isResendFlag() {
		return resendFlag;
	}

	public void setResendFlag(boolean resendFlag) {
		this.resendFlag = resendFlag;
	}

	public String getFromDate() {
		return fromDate;
	}

	public QuotaHistory getQuotaHistory() {
		return quotaHistory;
	}

	public void setQuotaHistory(QuotaHistory quotaHistory) {
		this.quotaHistory = quotaHistory;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public SummaryReport getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(SummaryReport grandTotal) {
		this.grandTotal = grandTotal;
	}

	public String getToDate() {
		return toDate;
	}

	public boolean isReportHistoryFlag() {
		return reportHistoryFlag;
	}

	public void setReportHistoryFlag(boolean reportHistoryFlag) {
		this.reportHistoryFlag = reportHistoryFlag;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public List<ReportObject> getReportsHistoryList() {
		return reportsHistoryList;
	}

	public void setReportsHistoryList(List<ReportObject> reportsHistoryList) {
		this.reportsHistoryList = reportsHistoryList;
	}

	public int getTotalRepHistoryRows() {
		return totalRepHistoryRows;
	}

	public void setTotalRepHistoryRows(int totalRepHistoryRows) {
		this.totalRepHistoryRows = totalRepHistoryRows;
	}

	public int getFirstRepHistoryRow() {
		return firstRepHistoryRow;
	}

	public void setFirstRepHistoryRow(int firstRepHistoryRow) {
		this.firstRepHistoryRow = firstRepHistoryRow;
	}

	public int getRowsRepHistoryPerPage() {
		return rowsRepHistoryPerPage;
	}

	public void setRowsRepHistoryPerPage(int rowsRepHistoryPerPage) {
		this.rowsRepHistoryPerPage = rowsRepHistoryPerPage;
	}

	public int getPageRepHistoryRange() {
		return pageRepHistoryRange;
	}

	public void setPageRepHistoryRange(int pageRepHistoryRange) {
		this.pageRepHistoryRange = pageRepHistoryRange;
	}

	public Integer[] getPagesRepHistory() {
		return pagesRepHistory;
	}

	public void setPagesRepHistory(Integer[] pagesRepHistory) {
		this.pagesRepHistory = pagesRepHistory;
	}

	public int getTotalRepHistoryPages() {
		return totalRepHistoryPages;
	}

	public void setTotalRepHistoryPages(int totalRepHistoryPages) {
		this.totalRepHistoryPages = totalRepHistoryPages;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public String getCampaignNameSearch() {
		return campaignNameSearch;
	}

	public void setCampaignNameSearch(String campaignNameSearch) {
		this.campaignNameSearch = campaignNameSearch;
	}

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public int getRowsPerPage() {
		return rowsPerPage;
	}

	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	public int getPageRange() {
		return pageRange;
	}

	public void setPageRange(int pageRange) {
		this.pageRange = pageRange;
	}

	public Integer[] getPages() {
		return pages;
	}

	public void setPages(Integer[] pages) {
		this.pages = pages;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public List<ReportObject> getReportsList() {
		return reportsList;
	}

	public void setReportsList(List<ReportObject> reportsList) {
		this.reportsList = reportsList;
	}

	public void setTableFlag(boolean tableFlag) {
		this.tableFlag = tableFlag;
	}

	// Pagination part-------------------------------------------------------

	public void pageFirst() {
		page(0);
	}

	public void pageNext() {
		page(firstRow + rowsPerPage);
	}

	public void pagePrevious() {
		page(firstRow - rowsPerPage);
	}

	public void pageLast() {
		page(totalRows - ((totalRows % rowsPerPage != 0) ? totalRows % rowsPerPage : rowsPerPage));
	}

	private void page(int firstRow) {
		this.firstRow = firstRow;
		populateList(); // Load requested page.
	}

	public void page(ActionEvent event) {
		page(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsPerPage);
	}

	/*
	 * start pagination of Campaign report history
	 */

	public void pageRepHisFirst() {
		pageRepHis(0);
	}

	public void pageRepHisNext() {
		pageRepHis(firstRepHistoryRow + rowsRepHistoryPerPage);
	}

	public void pageRepHisPrevious() {
		pageRepHis(firstRepHistoryRow - rowsRepHistoryPerPage);
	}

	public void pageRepHisLast() {
		pageRepHis(totalRepHistoryRows
				- ((totalRepHistoryRows % rowsRepHistoryPerPage != 0) ? totalRepHistoryRows % rowsRepHistoryPerPage
						: rowsRepHistoryPerPage));
	}

	private void pageRepHis(int firstRow) {
		this.firstRepHistoryRow = firstRow;
		loadReportsHistory(); // Load requested page.
	}

	public void pageRepHis(ActionEvent event) {
		pageRepHis(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * rowsRepHistoryPerPage);
	}

	/*
	 * end pagination of Campaign report history
	 */

	/*
	 * start pagination of requested reports
	 */

	public void pageRequestedReportsFirst() {
		pageRequestedReports(0);
	}

	public void pageRequestedReportsNext() {
		pageRequestedReports(requestedReportsFirstRow + requestedReportsRowsPerPage);
	}

	public void pageRequestedReportsPrevious() {
		pageRequestedReports(requestedReportsFirstRow - requestedReportsRowsPerPage);
	}

	public void pageRequestedReportsLast() {
		pageRequestedReports(requestedReportsTotalRows
				- ((requestedReportsTotalRows % requestedReportsRowsPerPage != 0) ? requestedReportsTotalRows % requestedReportsRowsPerPage
						: requestedReportsRowsPerPage));
	}

	private void pageRequestedReports(int firstRow) {
		this.requestedReportsFirstRow = firstRow;
		getRequestedReport(); // Load requested page.
	}

	public void pageRequestedReports(ActionEvent event) {
		pageRequestedReports(((Integer) ((UICommand) event.getComponent()).getValue() - 1) * requestedReportsRowsPerPage);
	}
	// Constructor------------------------------------

	public ReportBean() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
	        ThreadContext.push(trxId);
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());
			userInfo.setUser(user);

			rowsPerPage = 6; // Default rows per page (max amount of rows to be
			// displayed at once).
			pageRange = 10;

			rowsRepHistoryPerPage = 6;
			pageRepHistoryRange = 10;

			rowsSmsReportHistoryPerPage = 6;
			pageSmsReportHistoryRange = 10;
			
			requestedReportsRowsPerPage=6;
			requestedReportsPageRange=10;
		
		} catch (Exception e) {
			userLogger.error( "error while populating report service port", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}

	public int getCurrentRepHistoryPage() {
		return currentRepHistoryPage;
	}

	public void setCurrentRepHistoryPage(int currentRepHistoryPage) {
		this.currentRepHistoryPage = currentRepHistoryPage;
	}

	public String logTrxId(String trxId) {
		StringBuilder sb = new StringBuilder();
		sb.append("UserTrx");
		sb.append("(");
		sb.append(trxId);
		sb.append("): ");
		return sb.toString();
	}

	@PostConstruct
	public void init() {
	AccountUserTrxInfo userInfo = new AccountUserTrxInfo();
		try {
			userInfo.setTrxId(TrxId.getTrxId());
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());
			acctManagUser.setAccountId(userAcc.getAccount().getAccountId());
			acctManagUser.setUsername(userAcc.getUsername());
			
			userInfo.setUser(acctManagUser);
		       
			accountServicePort = portObj.getAccountServicePort();
			QuotaHistoryResult result = accountServicePort.getQuotaHistory(userInfo, user.getAccountId());
			com.edafa.web2sms.service.acc_manag.enums.ResponseStatus status = result.getStatus();

			switch (status) {
			case FAIL:
				userLogger.error( "Fail while finiding quota history");
				break;
			case ACCT_NOT_EXIST:
				userLogger.warn( "Account not exist while finiding quota history");
				break;
			case SUCCESS:
				userLogger.info( "Quota history found successfully");
				quotaHistory = result.getQuotaHistory();
				break;

			default:
				userLogger.error( "Error while finding quota history with status=" + result.getStatus());
				break;
			}
			populateList();
			getRequestedReport();
			

		} catch (WebServiceException ex) {
                        Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
                        FacesContext.getCurrentInstance().addMessage(
                                        null,
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                        .getLocalizedLabel("report_view_failure")), null));
			userLogger.error( "Error while filling ui report graph", ex);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.FAILED_OPERATION, "WebService failure");
		} catch (Exception e) {
                        Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();
			while (msgIterator.hasNext()) {
				msgIterator.next();
				msgIterator.remove();
			}
                        FacesContext.getCurrentInstance().addMessage(
                                        null,
                                        new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
                                                        .getLocalizedLabel("report_view_failure")), null));
			userLogger.error("Error while filling ui report graph", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

	public void populateList() {
		UserTrxInfo userInfo = new UserTrxInfo();
		reportsList = new ArrayList<ReportObject>();
		try {
			Calendar c = Calendar.getInstance();
			c.clear(Calendar.SECOND);
			c.clear(Calendar.MILLISECOND);
			Calendar endDate = c;

			Calendar s = (Calendar) endDate.clone();
			s.set(Calendar.DAY_OF_MONTH, -(int) Configs.REPORTS_PERIOD.getValue());
			Date startDate = s.getTime();
			String trxId = TrxId.getTrxId();
	        ThreadContext.push(trxId);
			userInfo.setTrxId(trxId);
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());
			userInfo.setUser(user);

			userLogger.info( "Getting report service port instance");

			reportServicePort = portObj.getReportServicePort();

			userLogger.debug( "Getting reports list paginated [start_date=" + startDate + ",end_date=" + endDate.getTime()
					+ ",get_from_row_num=" + firstRow + ",max_num_of_rows=" + rowsPerPage + "]");

					ReportResultSet result = reportServicePort.getReportsViewWithPagination(userInfo,
					fromDateToXMLGregorianCalendar(startDate), fromDateToXMLGregorianCalendar(endDate.getTime()),
					firstRow, rowsPerPage);
			ResponseStatus status = result.getStatus();

			switch (status) {
			case SUCCESS:
				reportsList.clear();
				List<CampaignAggregationReport> list = result.getReports();

				for (CampaignAggregationReport campaignAggregationReport : list) {
					ReportObject obj = new ReportObject();
					obj.setCampaignReportObj(campaignAggregationReport);
					
					obj.setRemaingSmsRatio(((((double) campaignAggregationReport.getSubmittedSMSCount() / (double) campaignAggregationReport
							.getSmsCount())) * 100) / 2);
					// false in campaignAggregationReport.isResentFailedFlag()
					// means it is resend before.
					// ui resendFlag true to view the button
					if (!campaignAggregationReport.isResentFailedFlag()) {
						userLogger.debug( "failed SMSs count for campaign id:(" +campaignAggregationReport.getCampaignId()+") is :("+ campaignAggregationReport.getFailedSMSCount()
								+").");
						if (campaignAggregationReport.getFailedSMSCount() >0)
							// CR on 24-Dec	+ campaignAggregationReport.getUnDeliverdSMSCount() > 0)
							resendFlag = true;
						else
							resendFlag = false;

						userLogger.debug(logTrxId(userInfo.getTrxId()) +  "resendFlag for campaign id:(" + campaignAggregationReport.getCampaignId() +") is : (" +resendFlag +").");
					} else {
						resendFlag = false;
					}
					obj.getCampaignReportObj().setResentFailedFlag(resendFlag);
					reportsList.add(obj);
				}

				userLogger.info(logTrxId(userInfo.getTrxId())+ "report list is populated successfully with row count=" + reportsList.size());
				break;

			case FAIL:
				userLogger.error( "error while getting report list");
				tableFlag = false;
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("report_view_failure")), null));
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.warn( "can't view his reports list");
				tableFlag = false;
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("ineligible_view_reports")), null));
				break;
			default:
				tableFlag = false;
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("report_view_failure")), null));
				userLogger.error( "undefined status response=" + status + " while populating report list");
				break;
			}

			GetCountResult reportsCountRespose = reportServicePort.countReportsView(userInfo, fromDateToXMLGregorianCalendar(startDate),
					fromDateToXMLGregorianCalendar(endDate.getTime()));
			switch (reportsCountRespose.getStatus()) {
				case SUCCESS:
					totalRows = reportsCountRespose.getCount();
					break;
				
				default:
					userLogger.error(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "can't get reports count, set to default");
					break;
			}
			userLogger.info(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername())
					+ "total reports list rows=" + totalRows);

			// Set currentPage, totalPages and pages.
			currentPage = (totalRows / rowsPerPage) - ((totalRows - firstRow) / rowsPerPage) + 1;
			totalPages = (totalRows / rowsPerPage) + ((totalRows % rowsPerPage != 0) ? 1 : 0);

			int pagesLength = Math.min(pageRange, totalPages);
			pages = new Integer[pagesLength];

			// firstPage must be greater than 0 and lesser than
			// totalPages-pageLength.
			int firstPage = Math.min(Math.max(0, currentPage - (pageRange / 2)), totalPages - pagesLength);

			// Create pages (page numbers for page links).
			for (int i = 0; i < pagesLength; i++) {
				pages[i] = ++firstPage;
				if (pagesLength > 10) {
					pagesLength = 10;
				}
			}

			if (reportsList == null || totalRows == 0 || reportsList.size() == 0) {
				tableFlag = false;
				userLogger.info( "No reports found in the database");
			}

		} catch (Exception e) {
			tableFlag = false;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("report_view_failure")), null));
			userLogger.error( "error while populating report list", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}

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

	// Methods-----------------------------------------------------------------

	public XMLGregorianCalendar fromDateToXMLGregorianCalendar(Date date) {
		XMLGregorianCalendar date2 = null;
		try {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(date);
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (Exception e) {
			userLogger.error( "Erorr while parsing date from data object to XML gregorian calender", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
		return date2;
	}

	public boolean checkFlag(String status) {
		if (status.equals("NEW") || status.equals("FINISHED")) {
			return true;
		} else {
			return false;
		}
	}

	// Export report------------------------------------------------------------

	public void getCampaignIdSelected(String campaignIdSel) {
		campaignId = campaignIdSel;
	}

	public void readFileFromServer() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
//			userLogger.debug( "Setting session attributes ([name=campaignReportId,value=" + campaignId
//					+ "][name=UserInfo,value=" + userInfo.getTrxId() + ",details=[user_name="
//					+ userInfo.getUser().getUsername() + ",account_id=" + userInfo.getUser().getAccountId() + "]])");
	        userLogger.debug( "Setting session attributes ([name=campaignReportId,value=" + campaignId
					+ "])");

			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
			session.setAttribute("campaignReportId", campaignId);
			session.setAttribute("UserInfo", userInfo);

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("export_report_failure")), null));
			userLogger.error( e.getMessage(), e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	public void PDF() {
		UserTrxInfo userInfo = new UserTrxInfo();
		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			/*userLogger.debug(userLogInfo(user.getAccountId(), user.getUsername())
					+ "Setting session attributes ([name=UserInfo,value=" + userInfo.getTrxId()
					+ ",details=[user_name=" + userInfo.getUser().getUsername() + ",account_id="
					+ userInfo.getUser().getAccountId() + "]])");
*/
			userLogger.debug("Setting session attributes ([name=UserInfo], details=[user_name,account_id ])");

			/*
			 * Calendar c = Calendar.getInstance(); c.clear(Calendar.SECOND);
			 * c.clear(Calendar.MILLISECOND); Calendar endDate = c;
			 * 
			 * Calendar s = (Calendar) endDate.clone();
			 * s.set(Calendar.DAY_OF_MONTH, -(int)
			 * Configs.REPORTS_PERIOD.getValue()); Date startDate = s.getTime();
			 */

			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
			session.setAttribute("UserInfo2", userInfo);
			session.setAttribute("fromDate", fromDate);
			session.setAttribute("endDate", toDate);
			session.setAttribute("CampName", campaignNameSearch.toLowerCase());

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("export_report_failure")), null));
			userLogger.error( e.getMessage(), e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	public SearchFilterStatus checkSearchFilter() {
		try {
			if ((fromDate == null || fromDate.trim().isEmpty()) && (toDate == null || toDate.trim().isEmpty())
					&& (campaignNameSearch == null || campaignNameSearch.trim().isEmpty())) {
				// all filters are empty
				userLogger.debug( "fromDate=" + fromDate
						+ " is empty or null, also " + "toDate=" + toDate + " is empty or null"
						+ " and the campaign name=" + campaignNameSearch + " is empty or null");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("date_campaign_name_empty")), null));
				return SearchFilterStatus.EMPTY;
			}// end if
			else {
				// campaign name or dates are not empty
				if (campaignNameSearch == null || campaignNameSearch.trim().isEmpty()) {
					// searching with date only
					userLogger.debug( "campaign name filter is empty to view report history");

					if (fromDate == null || fromDate.trim().isEmpty()) {
						// from date is empty
						userLogger.debug( "fromDate=" + fromDate
								+ " is empty or null");
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("from_date_required")), null));

						return SearchFilterStatus.EMPTY;
					}// end if
					else if (toDate == null || toDate.trim().isEmpty()) {
						// to date is empty
						userLogger.debug( "toDate=" + toDate
								+ " is empty or null");
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("to_date_required")), null));
						return SearchFilterStatus.EMPTY;
					}// end if
					else {
						// searching with date only
						if (checkDate()) {
							// parsing strings to dates successfully
							userLogger.debug( "Dates are parsed successfully");
							return SearchFilterStatus.DATE_ONLY;
						}// end if
						else {
							// dates are not valid
							userLogger.debug( "Dates are not valid");
							return SearchFilterStatus.ERROR;
						}// end else
					}// end else
				}// end if
				else {
					// campaign name is filled
					// check if no date just campaign only
					if ((fromDate == null || fromDate.trim().isEmpty()) && (toDate == null || toDate.trim().isEmpty())) {
						userLogger.debug( "searching report history with only campaign name ,campName=" + campaignNameSearch);
						return SearchFilterStatus.CAMPAIGN_NAME_ONLY;
					}// end if
					else {
						// there is date selected with campaign name
						// check if both dates is filled
						if ((fromDate != null || !fromDate.trim().isEmpty())
								&& (toDate != null || !toDate.trim().isEmpty())) {
							// both is filled , searching with both date and
							// campaign
							userLogger.debug( "searching report history with campaign name ,campName=" + campaignNameSearch
									+ " and with fromDate=" + fromDate + " with toDate=" + toDate);
							if (checkDate()) {
								// strings are parsed to dates successfully
								userLogger.debug( "Dates are parsed successfully");

								return SearchFilterStatus.DATE_CAMP;
							}// end if
							else {
								// dates are not valid
								userLogger.debug( "Dates are not valid");

								return SearchFilterStatus.ERROR;
							}// end else
						}// end if
						else {
							// one date is filled other is not
							userLogger.debug( "searching report history with campaign name ,campName=" + campaignNameSearch
									+ " and with only fromDate=" + fromDate + " or toDate=" + toDate);
							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("date_campaign_name_empty")), null));
							return SearchFilterStatus.ERROR;
						}// end else
					}// end else
				}// end else
			}// end else
		}// end try
		catch (Exception e) {
			userLogger.error( "Error while parsing string to date", e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("campaign_history_error")), null));
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			return SearchFilterStatus.ERROR;
		}// end catch
	}// end of method checkSearchFilter

	private boolean checkDate() {
		try {
			SimpleDateFormat fromDateformatter = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat toDateformatter = new SimpleDateFormat("dd/MM/yyyy");
			String tempStartDate = fromDate;
			String tempEndDate = toDate;

			userLogger.debug( "Parsing string to date ,"
					+ "toDate=" + toDate + " ,fromDate=" + fromDate);

			finalFromDate = fromDateformatter.parse(tempStartDate);
			finalToDate = toDateformatter.parse(tempEndDate);

			// check if to date is before from date
			if (finalToDate.before(finalFromDate)) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("to_date_before_from_date")), null));
				userLogger.debug( "toDate=" + finalToDate
						+ " is before fromDate=" + finalFromDate);
				return false;
			}// end if

			return true;
		}// end try
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("campaign_history_error")), null));
			userLogger.error("Error while parsing string to date", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			return false;
		}// end catch
	}// end of method checkDate

	public String loadReportsHistory() {
		UserTrxInfo userInfo = new UserTrxInfo();

		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info( "Getting report service port instance");

			reportServicePort = portObj.getReportServicePort();
			ReportResultSet result = null;
			ResponseStatus status = null;

			switch (checkSearchFilter()) {
			case DATE_ONLY: {
				userLogger.debug( "Getting reports history list paginated [start_date=" + finalFromDate + ",end_date="
						+ finalToDate + ",get_from_row_num=" + firstRepHistoryRow + ",max_num_of_rows="
						+ rowsRepHistoryPerPage + "]");
				result = reportServicePort.getPDFReportsPaginated(userInfo,
						fromDateToXMLGregorianCalendar(finalFromDate), fromDateToXMLGregorianCalendar(finalToDate),
						firstRepHistoryRow, rowsRepHistoryPerPage);
				status = result.getStatus();

				switch (status) {
				case INVALID_REQUEST: {
					userLogger.warn( "Invalid request while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
					break;
				}// end case

				case SUCCESS: {
					reportsHistoryList.clear();
					for (CampaignAggregationReport rep : result.getReports()) {
						ReportObject obj = new ReportObject();
						obj.setCampaignReportObj(rep);
						reportsHistoryList.add(obj);
					}

					grandTotal = result.getSummary();

					userLogger.info( "Successfully getting report history list with size=" + reportsHistoryList.size());
					reportHistoryFlag = true;

					GetCountResult reportsCountRespose = reportServicePort.countReportsWithinDateRangeForPDF(userInfo,
							fromDateToXMLGregorianCalendar(finalFromDate), fromDateToXMLGregorianCalendar(finalToDate));
					switch (reportsCountRespose.getStatus()) {
						case SUCCESS:
							totalRepHistoryRows = reportsCountRespose.getCount();
							break;
						
						default:
							userLogger.error(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "can't get total reports history -By date range- count, set to default");
							break;
					}
					userLogger.info(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername())
							+ "total reports history -by date Range- count list rows=" + totalRepHistoryRows);

					
					break;
				}// end case

				case FAIL: {
					userLogger.warn( "FAIL while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
					break;
				}// end case

				case INELIGIBLE_ACCOUNT: {
					userLogger.warn( "INELIGIBLE_ACCOUNT while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("ineligible_view_reports")), null));
					break;
				}// end case

				case CAMPAIGN_NOT_FOUND: {
					userLogger.warn( "CAMPAIGN_NOT_FOUND while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("report_view_failure")), null));
					break;
				}// end case

				default: {
					userLogger.warn( "Undefined status=" + status
							+ " while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
				}// end default
				}// end switch

				break;
			}// end case

			case CAMPAIGN_NAME_ONLY: {
				userLogger
						.debug( "Getting reports history list paginated [campaign_name=" + campaignNameSearch
								+ ",get_from_row_num=" + firstRepHistoryRow + ",max_num_of_rows="
								+ rowsRepHistoryPerPage + "]");
				result = reportServicePort.getReportsByCampNamePaginated(userInfo, campaignNameSearch.toLowerCase(),
						firstRepHistoryRow, rowsRepHistoryPerPage);
				status = result.getStatus();

				switch (status) {
				case INVALID_REQUEST: {
					userLogger.warn( "Invalid request while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
					break;
				}// end case

				case SUCCESS: {
					reportsHistoryList.clear();
					for (CampaignAggregationReport rep : result.getReports()) {
						ReportObject obj = new ReportObject();
						obj.setCampaignReportObj(rep);
						obj.setGrandTotal(result.getSummary());
						reportsHistoryList.add(obj);
					}

					grandTotal = result.getSummary();

					userLogger.info( "Successfully getting report history list with size=" + reportsHistoryList.size());

					reportHistoryFlag = true;

					GetCountResult reportsCountRespose = reportServicePort.countReportsByCampName(userInfo,
							campaignNameSearch.toLowerCase());
					switch (reportsCountRespose.getStatus()) {
						case SUCCESS:
							totalRepHistoryRows = reportsCountRespose.getCount();
							break;
						
						default:
							userLogger.error(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "can't get total reports history -byCampaign name- count, set to default");
							break;
					}
					userLogger.info(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername())
							+ "total reports history -byCampaign name- count list rows=" + totalRepHistoryRows);

					
					
					break;
				}// end case

				case FAIL: {
					userLogger.warn( "FAIL while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
					break;
				}// end case

				case INELIGIBLE_ACCOUNT: {
					userLogger.warn( "INELIGIBLE_ACCOUNT while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("ineligible_view_reports")), null));
					break;
				}// end case

				case CAMPAIGN_NOT_FOUND: {
					userLogger.warn( "CAMPAIGN_NOT_FOUND while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("report_view_failure")), null));
					break;
				}// end case

				default: {
					userLogger.warn( "Undefined status=" + status
							+ " while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
				}// end default
				}// end switch

				break;
			}// end case

			case DATE_CAMP: {
				userLogger.debug( "Getting reports history list paginated [start_date=" + finalFromDate + ",end_date="
						+ finalToDate + ",campaign_name=" + campaignNameSearch + ",get_from_row_num="
						+ firstRepHistoryRow + ",max_num_of_rows=" + rowsRepHistoryPerPage + "]");
				result = reportServicePort.getPDFReportsWithinDateAndCampNamePaginated(userInfo,
						fromDateToXMLGregorianCalendar(finalFromDate), fromDateToXMLGregorianCalendar(finalToDate),
						campaignNameSearch.toLowerCase(), firstRepHistoryRow, rowsRepHistoryPerPage);
				status = result.getStatus();

				switch (status) {
				case INVALID_REQUEST: {
					userLogger.warn( "Invalid request while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
					break;
				}// end case

				case SUCCESS: {
					reportsHistoryList.clear();
					for (CampaignAggregationReport rep : result.getReports()) {
						ReportObject obj = new ReportObject();
						obj.setCampaignReportObj(rep);
						obj.setGrandTotal(result.getSummary());
						reportsHistoryList.add(obj);
					}

					grandTotal = result.getSummary();

					userLogger.info("Successfully getting report history list with size=" + reportsHistoryList.size());
					reportHistoryFlag = true;

					GetCountResult reportsCountRespose  = reportServicePort.countReportsWithinDateRangeAndCampNameForPDF(
							userInfo, fromDateToXMLGregorianCalendar(finalFromDate),
							fromDateToXMLGregorianCalendar(finalToDate), campaignNameSearch.toLowerCase());
					switch (reportsCountRespose.getStatus()) {
						case SUCCESS:
							totalRepHistoryRows = reportsCountRespose.getCount();
							break;
						
						default:
							userLogger.error(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "can't get total reports history -By date range campaign name- count, set to default");
							break;
					}
					userLogger.info(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername())
							+ "total reports history -by date Range and campaign name- count list rows=" + totalRepHistoryRows);

				
					
					
					break;
				}// end case

				case FAIL: {
					userLogger.warn("FAIL while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
					break;
				}// end case

				case INELIGIBLE_ACCOUNT: {
					userLogger.warn("INELIGIBLE_ACCOUNT while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("ineligible_view_reports")), null));
					break;
				}// end case

				case CAMPAIGN_NOT_FOUND: {
					userLogger.warn("CAMPAIGN_NOT_FOUND while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("report_view_failure")), null));
					break;
				}// end case

				default: {
					userLogger.warn( "Undefined status=" + status
							+ " while getting report history list");
					reportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("campaign_history_error")), null));
				}// end default
				}// end switch

				break;
			}// end case

			case EMPTY: {
				reportHistoryFlag = false;
				userLogger.info( "Can't view report history");
				break;
			}// end case

			case ERROR: {
				reportHistoryFlag = false;
				userLogger.info( "Can't view report history");
				break;
			}// end case

			default: {
				reportHistoryFlag = false;
				userLogger.info( "Can't view report history");
				break;
			}// end default
			}// end switch

			userLogger.info( "total reports list rows=" + totalRepHistoryRows);

			if (reportsHistoryList == null || totalRepHistoryRows == 0 || reportsHistoryList.size() == 0) {
				reportHistoryFlag = false;
				userLogger.info( "No reports found in the database");
			}// end if
			else {
				// Set currentPage, totalPages and pages.
				currentRepHistoryPage = (totalRepHistoryRows / rowsRepHistoryPerPage)
						- ((totalRepHistoryRows - firstRepHistoryRow) / rowsRepHistoryPerPage) + 1;
				totalRepHistoryPages = (totalRepHistoryRows / rowsRepHistoryPerPage)
						+ ((totalRepHistoryRows % rowsRepHistoryPerPage != 0) ? 1 : 0);

				int pagesLength = Math.min(pageRepHistoryRange, totalRepHistoryPages);
				pagesRepHistory = new Integer[pagesLength];

				// firstPage must be greater than 0 and lesser than
				// totalPages-pageLength.
				int firstPage = Math.min(Math.max(0, currentRepHistoryPage - (pageRepHistoryRange / 2)),
						totalRepHistoryPages - pagesLength);

				// Create pages (page numbers for page links).
				for (int i = 0; i < pagesLength; i++) {
					pagesRepHistory[i] = ++firstPage;
					if (pagesLength > 10) {
						pagesLength = 10;
					}// end if
				}// end for
			}// end else

		}// end try
		catch (Exception e) {
			reportHistoryFlag = false;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("campaign_history_error")), null));
			userLogger.error( e.getMessage(), e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}// end catch
		finally{
            ThreadContext.pop();
        }
		return null;
	}// end of method loadReportsHistory

	public String loadSmsReportsHistory() {
		UserTrxInfo userInfo = new UserTrxInfo();

		try {
			String trxId = TrxId.getTrxId();
			userInfo.setTrxId(trxId);
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());
			userInfo.setUser(user);
	        ThreadContext.push(trxId);
			userLogger.info("Getting sms api report service port instance");

			reportServicePort = portObj.getReportServicePort();
			SmsReportResultSet result = null;
			ResponseStatus status = null;
			historyReportSearch = null;

			boolean validSearchCriteria = validateSearchCriteria(smsFromDateStr, smsToDateStr, smsSearchValue);

			if (validSearchCriteria) {
				historyReportSearch = new HistoryReportSearch();

				if (smsFromDateStr != null && !smsFromDateStr.trim().isEmpty()) {
					historyReportSearch.setDateFrom(date2String(smsFromDate));
				}// end if

				if (smsToDateStr != null && !smsToDateStr.trim().isEmpty()) {
					historyReportSearch.setDateTo(date2String(smsToDate));
				}// end if

				if (smsSearchValue != null && !smsSearchValue.trim().isEmpty()) {
					if (smsSearchField.equals(ReportSearchFields.SENDER_NAME.toString())) {
						historyReportSearch.setSenderName(smsSearchValue);
					}// end if
				}// end if

				userLogger.debug( "Getting sms api reports history list paginated [start_date=" + smsFromDate + ",end_date="
						+ smsToDate + ",search_field=" + smsSearchField + ",search_value=" + smsSearchValue
						+ ",get_from_row_num=" + firstSmsReportHistoryRow + ",max_num_of_rows="
						+ rowsSmsReportHistoryPerPage + "]");

				result = reportServicePort.getSMSReportsView(userInfo, historyReportSearch, firstSmsReportHistoryRow,
						rowsSmsReportHistoryPerPage);
				status = result.getStatus();

				switch (status) {
				case INVALID_REQUEST: {
					userLogger.warn( "Invalid request while getting sms api report history list");
					smsReportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("sms_api_history_error")), null));
					break;
				}// end case

				case SUCCESS: {
					smsReportsHistoryList.clear();
					for (SMSReport smsReport : result.getReports()) {
						SmsReportObject obj = new SmsReportObject();
						obj.setSmsReportObj(smsReport);
						smsReportsHistoryList.add(obj);
					}// end for

					smsReportGrandTotal = result.getSummaryReport();

					userLogger.info( "Successfully getting sms api report history list with size="
							+ smsReportsHistoryList.size());

					smsReportHistoryFlag = true;
					GetCountResult reportsCountRespose =reportServicePort.countSMSReportsView(userInfo,
							historyReportSearch);
					switch (reportsCountRespose.getStatus()) {
						case SUCCESS:
							totalSmsReportHistoryRows = reportsCountRespose.getCount();
							break;
						
						default:
							userLogger.error(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername()) + "can't get total sms reports count, set to default");
							break;
					}
					userLogger.info(logTrxId(userInfo.getTrxId()) + userLogInfo(user.getAccountId(), user.getUsername())
							+ "total sms api reports count list rows=" + totalSmsReportHistoryRows);

					

					break;
				}// end case

				case FAIL: {
					userLogger.warn( "FAIL while getting sms api report history list");
					smsReportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("sms_api_history_error")), null));
					break;
				}// end case

				case INELIGIBLE_ACCOUNT: {
					userLogger.warn( "INELIGIBLE_ACCOUNT while getting sms api report history list");
					smsReportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("ineligible_view_reports")), null));
					break;
				}// end case

				default: {
					userLogger.warn("Undefined status=" + status
							+ " while getting sms api report history list");
					smsReportHistoryFlag = false;
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("sms_api_history_error")), null));
				}// end default
				}// end switch
			}// end if
			else {
				smsReportHistoryFlag = false;
			}// end else

			userLogger.info( "total sms api reports list rows=" + totalSmsReportHistoryRows);

			if (smsReportsHistoryList == null || smsReportsHistoryList.isEmpty() || totalSmsReportHistoryRows == 0) {
				smsReportHistoryFlag = false;
				userLogger.info( "No sms api reports found in the database");
			}// end if
			else {
				// Set currentPage, totalPages and pages.
				currentSmsReportHistoryPage = (totalSmsReportHistoryRows / rowsSmsReportHistoryPerPage)
						- ((totalSmsReportHistoryRows - firstSmsReportHistoryRow) / rowsSmsReportHistoryPerPage) + 1;
				totalSmsReportHistoryPages = (totalSmsReportHistoryRows / rowsSmsReportHistoryPerPage)
						+ ((totalSmsReportHistoryRows % rowsSmsReportHistoryPerPage != 0) ? 1 : 0);

				int pagesLength = Math.min(pageSmsReportHistoryRange, totalSmsReportHistoryPages);
				pagesSmsReportHistory = new Integer[pagesLength];

				// firstPage must be greater than 0 and lesser than
				// totalPages-pageLength.
				int firstPage = Math.min(Math.max(0, currentSmsReportHistoryPage - (pageSmsReportHistoryRange / 2)),
						totalSmsReportHistoryPages - pagesLength);

				// Create pages (page numbers for page links).
				for (int i = 0; i < pagesLength; i++) {
					pagesSmsReportHistory[i] = ++firstPage;
					if (pagesLength > 10) {
						pagesLength = 10;
					}// end if
				}// end for
			}// end else

		}// end try
		catch (Exception e) {
			smsReportHistoryFlag = false;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("sms_api_history_error")), null));
			userLogger.error(e.getMessage(), e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}// end catch
		finally{
            ThreadContext.pop();
        }

		return null;
	}// end of method loadSmsReportsHistory

	private boolean validateSearchCriteria(String fromDateStr, String toDateStr, String searchValue) {
		boolean validSearchCriteria = true;

		try {
			// check at least 1 filter is provided
			if ((fromDateStr == null || fromDateStr.trim().isEmpty())
					&& (toDateStr == null || toDateStr.trim().isEmpty())
					&& (searchValue == null || searchValue.trim().isEmpty())) {
				// all filters are empty
				userLogger.debug( "fromDate=" + fromDateStr
						+ " is empty or null, also " + "toDate=" + toDateStr + " is empty or null" + ", searchField="
						+ smsSearchField + " and the search value=" + searchValue + " is empty or null");
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("date_search_value_empty")), null));

				validSearchCriteria = false;
			}// end if
			else {
				// search value or dates are not empty
				if (searchValue == null || searchValue.trim().isEmpty()) {
					// searching with date only
					userLogger.debug(" searchField="
							+ smsSearchField + ", and search value filter is empty to view sms api report history");

					if (fromDateStr == null || fromDateStr.trim().isEmpty()) {
						// from date is empty
						userLogger.debug("fromDate="
								+ fromDateStr + " is empty or null");
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("from_date_required")), null));

						validSearchCriteria = false;
					}// end if
					else if (toDateStr == null || toDateStr.trim().isEmpty()) {
						// to date is empty
						userLogger.debug("toDate=" + toDateStr
								+ " is empty or null");
						FacesContext.getCurrentInstance().addMessage(
								null,
								new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
										.getLocalizedLabel("to_date_required")), null));

						validSearchCriteria = false;
					}// end if
					else {
						// searching with date only
						if (checkSmsApiDate(fromDateStr, toDateStr)) {
							// parsing strings to dates successfully
							userLogger.debug( "Dates are parsed successfully");

							validSearchCriteria = true;
						}// end if
						else {
							// dates are not valid
							userLogger.debug( "Dates are not valid");

							validSearchCriteria = false;
						}// end else
					}// end else
				}// end if
				else {
					// search value is filled
					// check if no date just campaign only
					if ((fromDateStr == null || fromDateStr.trim().isEmpty())
							&& (toDateStr == null || toDateStr.trim().isEmpty())) {
						userLogger.debug( "searching sms api report history with only search attribute ,searchField="
								+ smsSearchField + ", searchValue=" + searchValue);

						validSearchCriteria = true;
					}// end if
					else {
						// there is date selected with search field
						// check if both dates is filled
						if ((fromDateStr != null && !fromDateStr.trim().isEmpty())
								&& (toDateStr != null && !toDateStr.trim().isEmpty())) {
							// both is filled , searching with both date and
							// search value
							userLogger.debug( "searching report history with search field ,searchField=" + smsSearchField
									+ ", searchValue=" + searchValue + " and with fromDate=" + fromDateStr
									+ " with toDate=" + toDateStr);

							if (checkSmsApiDate(fromDateStr, toDateStr)) {
								// strings are parsed to dates successfully
								userLogger.debug( "Dates are parsed successfully");

								validSearchCriteria = true;
							}// end if
							else {
								// dates are not valid
								userLogger.debug( "Dates are not valid");

								validSearchCriteria = false;
							}// end else
						}// end if
						else {
							// one date is filled other is not
							userLogger.debug( "searching sms api report history with search field ,searchField="
									+ smsSearchField + ", searchValue=" + searchValue + " and with only fromDate="
									+ fromDateStr + " or toDate=" + toDateStr);

							FacesContext.getCurrentInstance().addMessage(
									null,
									new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
											.getLocalizedLabel("date_search_value_empty")), null));

							validSearchCriteria = false;
						}// end else
					}// end else
				}// end else
			}// end else

		}// end try
		catch (Exception e) {
			userLogger.error( "Error while validating sms api search criteria ", e);
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("sms_api_history_error")), null));
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

			validSearchCriteria = false;
		}// end catch
		
		return validSearchCriteria;
	}// end of method validateSearchCriteria

	private boolean checkSmsApiDate(String fromDateStr, String toDateStr) {
		try {
			SimpleDateFormat fromDateformatter = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat toDateformatter = new SimpleDateFormat("dd/MM/yyyy");

			userLogger.debug( "Parsing string to date ,"
					+ " fromDate=" + fromDateStr + ", toDate=" + toDateStr);

			smsFromDate = fromDateformatter.parse(fromDateStr);
			smsToDate = toDateformatter.parse(toDateStr);

			// check if to date is before from date
			if (smsToDate.before(smsFromDate)) {
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("to_date_before_from_date")), null));

				userLogger.debug( "toDate=" + smsToDate
						+ " is before fromDate=" + smsFromDate);

				return false;
			}// end if

			return true;
		}// end try
		catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("sms_api_history_error")), null));

			userLogger.error( "Error while parsing string to date", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			return false;
		}// end catch
	}// end of method checkSmsApiDate

	public String date2String(Date date) {
		try {
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			String dateStr = df.format(date);
			return dateStr;
		}// end try
		catch (Exception e) {
			userLogger.error( "Error while parsing from date="
					+ date + " to string", e);
                        appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
			return null;
		}// end catch
	}// end of method date2String

	/*
	 * start pagination of Sms api report history
	 */

	public void pageSmsReportHisFirst() {
		pageSmsReportHistory(0);
	}

	public void pageSmsReportHistoryNext() {
		pageSmsReportHistory(firstSmsReportHistoryRow + rowsSmsReportHistoryPerPage);
	}

	public void pageSmsReportHistoryPrevious() {
		pageSmsReportHistory(firstSmsReportHistoryRow - rowsSmsReportHistoryPerPage);
	}

	public void pageSmsReportHistoryLast() {
		pageSmsReportHistory(totalSmsReportHistoryRows
				- ((totalSmsReportHistoryRows % rowsSmsReportHistoryPerPage != 0) ? totalSmsReportHistoryRows
						% rowsSmsReportHistoryPerPage : rowsSmsReportHistoryPerPage));
	}

	private void pageSmsReportHistory(int firstRow) {
		this.firstSmsReportHistoryRow = firstRow;
		loadSmsReportsHistory(); // Load requested page.
	}

	public void pageSmsReportHistory(ActionEvent event) {
		pageSmsReportHistory(((Integer) ((UICommand) event.getComponent()).getValue() - 1)
				* rowsSmsReportHistoryPerPage);
	}

	/*
	 * end pagination of Sms api report history
	 */

	// Getters && Setters
	public String getSmsFromDateStr() {
		return smsFromDateStr;
	}

	public void setSmsFromDateStr(String smsFromDateStr) {
		this.smsFromDateStr = smsFromDateStr;
	}

	public String getSmsToDateStr() {
		return smsToDateStr;
	}

	public void setSmsToDateStr(String smsToDateStr) {
		this.smsToDateStr = smsToDateStr;
	}

	public String getSmsSearchField() {
		return smsSearchField;
	}

	public void setSmsSearchField(String smsSearchField) {
		this.smsSearchField = smsSearchField;
	}

	public String getSmsSearchValue() {
		return smsSearchValue;
	}

	public void setSmsSearchValue(String smsSearchValue) {
		this.smsSearchValue = smsSearchValue;
	}

	public Date getSmsFromDate() {
		return smsFromDate;
	}

	public void setSmsFromDate(Date smsFromDate) {
		this.smsFromDate = smsFromDate;
	}

	public Date getSmsToDate() {
		return smsToDate;
	}

	public void setSmsToDate(Date smsToDate) {
		this.smsToDate = smsToDate;
	}

	public List<SmsReportObject> getSmsReportsHistoryList() {
		return smsReportsHistoryList;
	}

	public void setSmsReportsHistoryList(List<SmsReportObject> smsReportsHistoryList) {
		this.smsReportsHistoryList = smsReportsHistoryList;
	}

	public int getTotalSmsReportHistoryRows() {
		return totalSmsReportHistoryRows;
	}

	public void setTotalSmsReportHistoryRows(int totalSmsReportHistoryRows) {
		this.totalSmsReportHistoryRows = totalSmsReportHistoryRows;
	}

	public int getFirstSmsReportHistoryRow() {
		return firstSmsReportHistoryRow;
	}

	public void setFirstSmsReportHistoryRow(int firstSmsReportHistoryRow) {
		this.firstSmsReportHistoryRow = firstSmsReportHistoryRow;
	}

	public int getRowsSmsReportHistoryPerPage() {
		return rowsSmsReportHistoryPerPage;
	}

	public void setRowsSmsReportHistoryPerPage(int rowsSmsReportHistoryPerPage) {
		this.rowsSmsReportHistoryPerPage = rowsSmsReportHistoryPerPage;
	}

	public int getPageSmsReportHistoryRange() {
		return pageSmsReportHistoryRange;
	}

	public void setPageSmsReportHistoryRange(int pageSmsReportHistoryRange) {
		this.pageSmsReportHistoryRange = pageSmsReportHistoryRange;
	}

	public Integer[] getPagesSmsReportHistory() {
		return pagesSmsReportHistory;
	}

	public void setPagesSmsReportHistory(Integer[] pagesSmsReportHistory) {
		this.pagesSmsReportHistory = pagesSmsReportHistory;
	}

	public int getCurrentSmsReportHistoryPage() {
		return currentSmsReportHistoryPage;
	}

	public void setCurrentSmsReportHistoryPage(int currentSmsReportHistoryPage) {
		this.currentSmsReportHistoryPage = currentSmsReportHistoryPage;
	}

	public int getTotalSmsReportHistoryPages() {
		return totalSmsReportHistoryPages;
	}

	public void setTotalSmsReportHistoryPages(int totalSmsReportHistoryPages) {
		this.totalSmsReportHistoryPages = totalSmsReportHistoryPages;
	}

	public boolean isSmsReportHistoryFlag() {
		return smsReportHistoryFlag;
	}

	public void setSmsReportHistoryFlag(boolean smsReportHistoryFlag) {
		this.smsReportHistoryFlag = smsReportHistoryFlag;
	}

	public SummaryReport getSmsReportGrandTotal() {
		return smsReportGrandTotal;
	}

	public void setSmsReportGrandTotal(SummaryReport smsReportGrandTotal) {
		this.smsReportGrandTotal = smsReportGrandTotal;
	}

	public void requestExportSMSapi() {

		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		String trxIdLog= logTrxId(trxId);

		try {
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			userLogger.debug(trxIdLog + "Reuqsting detailed report for sms api with historyReportSearch: " +historyReportSearch);
			reportServicePort = portObj.getReportServicePort();
			ResultStatus result = reportServicePort.offlineGenerateDetailedSMSAPIReport(userInfo, historyReportSearch);
			ResponseStatus status = result.getStatus();
			switch (status) {
				case SUCCESS:
					userLogger.debug(trxIdLog + "detailed smsapi report requested successfully.");
					getRequestedReport();
					break;
				
				default:
					userLogger.error(trxIdLog + "error while requesting detaield smsapi report.");
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("request_detailed_report_fail")), null));
					break;
			}
		} catch (Exception e) {
			userLogger.error(trxIdLog + "exception while requesting detaield smsapi report: "+ e.getMessage());
			appLogger.error(trxIdLog + "exception while requesting detaield smsapi report: "+ e.getMessage());

			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("request_detailed_report_fail")), null));
			
			appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}
	public void downloadSMSapi(String token) {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		try {
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			userLogger.debug(trxId+ "| "+ userLogInfo(user.getAccountId(), user.getUsername())
					+ "Set session attributes ([name=UserInfo,value=" + userInfo.getTrxId() + ",details=[user_name="
					+ userInfo.getUser().getUsername() + ",account_id=" + userInfo.getUser().getAccountId() + "], file token["+ token+"]])");

			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
			session.setAttribute("SMS_report_token", token);
			session.setAttribute("UserInfo", userInfo);

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("export_report_failure")), null));
			userLogger.error( e.getMessage(), e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	public void resendFailedSMSs(String campId) {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		userInfo.setTrxId(trxId);
		userInfo.setUser(user);
		try{
	        ThreadContext.push(trxId);
	
		campaignServicePort = portObj.getCampaignServicePort();
		CampaignResult result = campaignServicePort.createResentCampaign(userInfo, campId);
		ResponseStatus status = result.getStatus();
		userLogger.info( "Resending Campaign Result: (" + status +").");

		switch (status) {

		case FAIL:
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("campaign_resending_failure")), null));

			userLogger.info( "Error while trying to resend faild of campaign_id=" + campId +","+ result.getErrorMessage());

			break;

		case INVALID_CAMPAIGN:
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("campaign_resending_failure")), null));

			userLogger.info("Trying to resend faild of campaign_id=" + campId + " with uneditable status");

			break;

		case ACCOUNT_QUOTA_NOT_FOUND:
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("account_quota_not_found")), null));

			userLogger.info( "account quota record not found for this account ");

			break;

		case ACCOUNT_QUOTA_EXCEEDED:
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("account_quota_exceed")), null));

			userLogger.info( "account_quota_exceed while resending campaing id= " + campId);
			break;

		case SUCCESS:
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
							.getLocalizedLabel("Resending_faild_Campaign")), null));
			userLogger.info( "Resending failed for campaign id=" + campId + " done successfully.");
			break;
		case INELIGIBLE_ACCOUNT:
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("ineligible_user")), null));

			userLogger.info( "INELIGIBLE_ACCOUNT ,not allowed to resend campaign id=" + campId);

			break;

		case INVALID_CAMPAIGN_LIST:
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("invalid_campaign_list")), null));

			userLogger.info( "Error while resend campaign id=" + campId + " because of empty list");

			break;

		default:
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("campaign_resending_failure")), null));

			userLogger.info( "Unknown response=" + status + " while trying to resende campaign id=" + campId);

			break;
			}
		} finally {
			populateList();

			ThreadContext.pop();
		}
		// reportsList.get(0).getCampaignReportObj().g
	}
	
	
	
	public void getRequestedReport() {
		UserTrxInfo userInfo = new UserTrxInfo();
		requestedReports = new ArrayList<Reports>();
		String trxId = TrxId.getTrxId();
		String trxIdLog= logTrxId(trxId);

		try {
			userInfo.setTrxId(trxId);
			user.setAccountId(userAcc.getAccount().getAccountId());
			user.setUsername(userAcc.getUsername());
			userInfo.setUser(user);
			userLogger.info( trxIdLog + "getting requested reports paginated get_from_row_num=" + requestedReportsFirstRow + ",max_num_of_rows=" + requestedReportsRowsPerPage);

			reportServicePort = portObj.getReportServicePort();
			ReportsResult result = reportServicePort.getAllReports(userInfo, requestedReportsFirstRow, requestedReportsRowsPerPage);
			ResponseStatus status = result.getStatus();

			switch (status) {
			case SUCCESS:
				requestedReports.clear();
				requestedReports = result.getReports();

				userLogger.info(trxIdLog+ "requested report list is populated successfully with row count=" + requestedReports.size());	
				break;
 
			case FAIL:
				userLogger.error(trxIdLog+ "retriving requested list status is FAIL");
				requestedReportsTableFlag = false;
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("report_view_failure")), null));
				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.warn(trxIdLog+ "can't view his reports list");
				requestedReportsTableFlag = false;
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("ineligible_view_reports")), null));
				break;
			default:
				requestedReportsTableFlag = false;
				FacesContext.getCurrentInstance().addMessage(
						null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
								.getLocalizedLabel("report_view_failure")), null));
				userLogger.error(trxIdLog+ "undefined status response=" + status + " while populating requested reports list");
				break;
			}

			GetCountResult reportsCountRespose = reportServicePort.getReportsCount(userInfo);
			switch (reportsCountRespose.getStatus()) {
				case SUCCESS:
					requestedReportsTotalRows = reportsCountRespose.getCount();
					break;
				
				default:
					userLogger.error(trxIdLog + "can't get requested reports count, set it to default");
					break;
			}
			userLogger.info(trxIdLog + "total requested reports list rows=" + requestedReportsTotalRows);

			// Set currentPage, totalPages and pages.
			requestedReportsCurrentPage = (requestedReportsTotalRows / requestedReportsRowsPerPage) - ((requestedReportsTotalRows - requestedReportsFirstRow) / requestedReportsRowsPerPage) + 1;
			requestedReportsTotalPages = (requestedReportsTotalRows / requestedReportsRowsPerPage) + ((requestedReportsTotalRows % requestedReportsRowsPerPage != 0) ? 1 : 0);

			int pagesLength = Math.min(requestedReportsPageRange, requestedReportsTotalPages);
			requestedReportsPages = new Integer[pagesLength];

			// firstPage must be greater than 0 and lesser than
			// totalPages-pageLength.
			int firstPage = Math.min(Math.max(0, requestedReportsCurrentPage - (requestedReportsPageRange / 2)), requestedReportsTotalPages - pagesLength);

			// Create pages (page numbers for page links).
			for (int i = 0; i < pagesLength; i++) {
				requestedReportsPages[i] = ++firstPage;
				if (pagesLength > 10) {
					pagesLength = 10;
				}
			}

			if (requestedReports == null || requestedReportsTotalRows == 0 || requestedReports.size() == 0) {
				requestedReportsTableFlag = false;
				userLogger.info(userLogInfo(user.getAccountId(), user.getUsername())
						+ "No requestedReports found in the database");
				/*FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, String.format(Util
								.getLocalizedLabel("no_data_found")), null));*/
			}

		} catch (Exception e) {
			requestedReportsTableFlag = false;
			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("report_view_failure")), null));
			userLogger.error(trxIdLog+ "error while populating requested report list", e);
            appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}
	}
	
	public void requestDetailedReportForCampaign(String campaignId) {
		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		String trxIdLog= logTrxId(trxId);

		try {
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			userLogger.debug(trxIdLog + "Reuqsting detailed report for campaign with id["+campaignId+"].");
			reportServicePort = portObj.getReportServicePort();
			ResultStatus result = reportServicePort.offlineGenerateDetailedCampaignReport(userInfo, campaignId);
			ResponseStatus status = result.getStatus();
			switch (status) {
				case SUCCESS:
					userLogger.debug(trxIdLog + "detailed report requested successfully.");
					getRequestedReport();
					break;
				
				default:
					userLogger.error(trxIdLog + "error while requesting detaield report.");
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("request_detailed_report_fail")), null));
					break;
			}
		} catch (Exception e) {
			userLogger.error(trxIdLog + "exception while requesting detaield report: "+ e.getMessage());
			appLogger.error(trxIdLog + "exception while requesting detaield report: "+ e.getMessage());

			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("request_detailed_report_fail")), null));
			
			appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}

	}

	public void cancelReport(int reportId){

		UserTrxInfo userInfo = new UserTrxInfo();
		String trxId = TrxId.getTrxId();
		String trxIdLog= logTrxId(trxId);

		try {
			userInfo.setTrxId(trxId);
			userInfo.setUser(user);

			userLogger.debug(trxIdLog + "cancel the requested report  with id["+reportId+"].");
			reportServicePort = portObj.getReportServicePort();
			ResultStatus result = reportServicePort.cancelReport(userInfo, reportId);			
			ResponseStatus status = result.getStatus();
			switch (status) {
				case SUCCESS:
					userLogger.debug(trxIdLog + "  The requested report is canceled successfully.");
					getRequestedReport();
					break;
				
				default:
					userLogger.error(trxIdLog + "  error while cancelling report.");
					FacesContext.getCurrentInstance().addMessage(
							null,
							new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
									.getLocalizedLabel("request_detailed_report_fail")), null));
					break;
			}
		} catch (Exception e) {
			userLogger.error(trxIdLog + " exception while cancelling report: "+ e.getMessage());
			appLogger.error(trxIdLog + "exception while cancelling report: "+ e.getMessage());

			FacesContext.getCurrentInstance().addMessage(
					null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(Util
							.getLocalizedLabel("request_detailed_report_fail")), null));
			
			appErrorManagerAdapter.raiseError(userLogInfo(user.getAccountId(), user.getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}

	
		
	}

}// end of class ReportBean
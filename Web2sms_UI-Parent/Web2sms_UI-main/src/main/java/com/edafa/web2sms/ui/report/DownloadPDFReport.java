package com.edafa.web2sms.ui.report;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.model.CampaignAggregationReport;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.service.report.ReportManagementService;
import com.edafa.web2sms.service.report.ReportResultSet;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.file.DownloadedFileInfo;

/**
 * Servlet implementation class DownloadReport
 */
@WebServlet("/DownloadPDFReport")
public class DownloadPDFReport extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	ReportManagementService reportServicePort;
	@EJB
	WSClients portObj;
        @EJB
        AppErrorManagerAdapter appErrorManagerAdapter;
	DownloadedFileInfo result;
	InputStream input;
	UserTrxInfo userInfo = null;
	JasperPrint jasperPrint = new JasperPrint();
	String startDate;
	String endDate;
	String campName;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DownloadPDFReport() {
		super();
	}

	public void initReport() throws JRException, MalformedURLException {
		JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(getAllTrxLogView());
		// String path = getServletContext().getRealPath(".");
		// System.out.println(path);
		// req.getContextPath().concat("/ui/jasperreports/CampaignReport.jasper");
		// FacesContext.getCurrentInstance().getExternalContext().getResource("/CampaignReport.jasper")
		// .getPath();			
			URL url = getServletContext().getResource("/WEB-INF/CampaignReport.jasper");
//			userLogger.info("jasper file path=" + url);
			// url = Class.class.getResource(".");
			// System.out.println(url);
			JasperReport campaignReport = (JasperReport) JRLoader.loadObject(url);
			jasperPrint = JasperFillManager.fillReport(url.getPath(), new HashMap(), jrBeanCollectionDataSource);
		

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String campaignId = "";
		try {
			HttpSession session = request.getSession();
			userInfo = (UserTrxInfo) session.getAttribute("UserInfo2");
			startDate = (String) session.getAttribute("fromDate");
			endDate = (String) session.getAttribute("endDate");
			campName = (String) session.getAttribute("CampName");
			String trxId = userInfo.getTrxId();
			ThreadContext.push(trxId);

			userLogger.info( "Get report service port, to generate detailed report of campaign id=" + campaignId);

			initReport();

			String filename = "CampaignReport.pdf";

			response.setContentType("application/pdf");
			response.setHeader("Content-disposition", "attachment; filename=" + filename);
			JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
		} catch (JRException e) {
			userLogger.error(e.getMessage(), e);
			e.printStackTrace();
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.FAILED_OPERATION, "JR failure");
		} catch (IOException e) {
			userLogger.error(e.getMessage(), e);
			e.printStackTrace();
			appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.IO_ERROR, "IO failure");
		} catch (Exception e) {
			userLogger.error(e.getMessage(), e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
	}

	public String logTrxId(String trxId) {
		StringBuilder sb = new StringBuilder();
		sb.append("UserTrx");
		sb.append("(");
		sb.append(trxId);
		sb.append("): ");
		return sb.toString();
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

	public ArrayList<UploadedPDFReport> getAllTrxLogView() {
		ArrayList<UploadedPDFReport> arrayList = new ArrayList<UploadedPDFReport>();
		try {
			userLogger.info( "Getting report service port instance");

			reportServicePort = portObj.getReportServicePort();

			userLogger.debug( "Getting reports list paginated [start_date=" + startDate + ",end_date=" + endDate + "]");

			ReportResultSet result = null;

			if (endDate != null 
				&& !endDate.trim().isEmpty()  
				&& startDate != null
				&& !startDate.trim().isEmpty()) 
			{
				if (campName == null || campName.trim().isEmpty()) 
				{
					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					Date start = formatter.parse(startDate);
					Date end = formatter.parse(endDate);
					result = reportServicePort.getPDFReports(userInfo, fromDateToXMLGregorianCalendar(start),
							fromDateToXMLGregorianCalendar(end));
				}
				else 
				{
					SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
					Date start = formatter.parse(startDate);
					Date end = formatter.parse(endDate);
					result = reportServicePort.getPDFReportsWithinDateAndCampName(userInfo,
							fromDateToXMLGregorianCalendar(start), fromDateToXMLGregorianCalendar(end),
							campName.toLowerCase());
				}

			} 
			else 
			{
				result = reportServicePort.getReportsByCampName(userInfo, campName.toLowerCase());
			}

			ResponseStatus status = result.getStatus();

			switch (status) {
			case SUCCESS:
				List<CampaignAggregationReport> list = result.getReports();
				for (CampaignAggregationReport campaignAggregationReport : list) {
					UploadedPDFReport obj = new UploadedPDFReport();
					obj.setCampaignReportObj(campaignAggregationReport);
					obj.setGrandTotal(result.getSummary());
					arrayList.add(obj);
					StringBuilder listName = new StringBuilder();
					if (campaignAggregationReport.getContactListName().size() > 0) {
						for (int index = 0; index < campaignAggregationReport.getContactListName().size(); index++) {
							listName.append(campaignAggregationReport.getContactListName().get(index));

							if (index < campaignAggregationReport.getContactListName().size() - 1) {
								listName.append(",");
							}
							obj.setListName(listName.toString());
						}
					} else {
						obj.setListName("");
					}
				}
				userLogger.info( "report list is populated successfully with row count=" + arrayList.size());
				break;

			case FAIL:
				userLogger.error( "error while getting report pdf");

				break;
			case INELIGIBLE_ACCOUNT:
				userLogger.warn( "can't view his reports list");

				break;
			default:

				userLogger.error( "undefined status response=" + status + " while populating report list");
				break;
			}
		} catch (Exception e) {
			userLogger.error( "error while populating report list", e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

		}// end catch

		return arrayList;
	}

	public XMLGregorianCalendar fromDateToXMLGregorianCalendar(Date date) {
		XMLGregorianCalendar date2 = null;
		try {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(date);
			date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (Exception e) {
			userLogger.error("Erorr while parsing date from data object to XML gregorian calender", e);
             appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");

		}
		return date2;
	}
}

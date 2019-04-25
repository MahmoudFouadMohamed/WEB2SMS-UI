package com.edafa.web2sms.ui.list;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.edafa.web2sms.service.enums.ResponseStatus;
import com.edafa.web2sms.service.lists.ListsManegementService;
import com.edafa.web2sms.service.model.FileTokenResult;
import com.edafa.web2sms.service.model.UserTrxInfo;
import com.edafa.web2sms.ui.locale.WSClients;
import com.edafa.web2sms.utils.alarm.AppErrorManagerAdapter;
import com.edafa.web2sms.utils.alarm.AppErrors;
import com.edafa.web2sms.utils.configs.enums.Configs;
import com.edafa.web2sms.utils.configs.enums.LoggersEnum;
import com.edafa.web2sms.utils.file.DownloadedFileInfo;
import com.edafa.web2sms.utils.file.FileDownloadClient;

/**
 * Servlet implementation class DownloadLists
 */
@WebServlet("/DownloadLists")
public class DownloadLists extends HttpServlet {
	private static final long serialVersionUID = 1L;
	Logger userLogger = LogManager.getLogger(LoggersEnum.WEB_MODULE.name());
	ListsManegementService listsManegementServicePort;
	@EJB
	WSClients portObj;
        
        @EJB
        AppErrorManagerAdapter appErrorManagerAdapter;
        
	DownloadedFileInfo result;
	InputStream input;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DownloadLists() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserTrxInfo userInfo = null;
		Integer listId = 0;
		try {
			PrintWriter out = null;

			HttpSession session = request.getSession();
			listId = (Integer) session.getAttribute("listId");
			userInfo = (UserTrxInfo) session.getAttribute("UserInfo");
			String trxId = userInfo.getTrxId();
	        ThreadContext.push(trxId);
			userLogger.info( " Getting list management service port, for exporting list id=" + listId);

			listsManegementServicePort = portObj.getListsManegementServicePort();

			FileTokenResult token = listsManegementServicePort.exportListToCSVFile(userInfo, listId);
			ResponseStatus status = token.getStatus();
			String exportListTitle= "Export List";
			String error = "Error";
			String failErrorMessage= "Error while exporting list";
			String ineligibleMessage = "You are ineligible to export lists.";
			
			switch (status) {
				case SUCCESS :
					exportList(userInfo, token, response);
					userLogger.info( " generated list file of list id=" + listId + " successfully");
					break;
				case FAIL :
					userLogger.error( "Error could not get the file token " + status + token.getErrorMessage() + " to export list id=" + listId);
					out = response.getWriter();
					out.print("<!DOCTYPE html><html><head><title>"+exportListTitle+"</title></head><body><h2 ><font color='red'>"+error+"</font></h2><h3>"
							+ failErrorMessage + "</h3></body></html>");
				
					break;
				case INELIGIBLE_ACCOUNT :
					userLogger.warn( "not allowed to export list id=" + listId);
					out = response.getWriter();
					out.print("<!DOCTYPE html><html><head><title>"+exportListTitle+"</title></head><body><h2 ><font color='red'>"+error+"</font></h2><h3>"
							+ ineligibleMessage + "</h3></body></html>");
					break;
				
				default :
					userLogger.error("Undefined response status=" + status + " to generate detailed report of list id=" + listId);
					out = response.getWriter();
					out.print("<!DOCTYPE html><html><head><title>"+exportListTitle+"</title></head><body><h2 ><font color='red'>"+error+"</font></h2><h3>"
							+ failErrorMessage + "</h3></body></html>");
				
					break;
			}

		} catch (Exception e) {
			userLogger.error( "Error while exporting list id=" + listId, e);
            appErrorManagerAdapter.raiseError(userLogInfo(userInfo.getUser().getAccountId(), userInfo.getUser().getUsername()), AppErrors.GENERAL_ERROR, "Generic failure");
		}finally{
            ThreadContext.pop();
        }
	}
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	public void exportList(UserTrxInfo userInfo, FileTokenResult token, HttpServletResponse response) throws IOException {
		OutputStream output = null;
		try {
			FileDownloadClient fileDownload = new FileDownloadClient();
			result = fileDownload.downloadFile(userInfo.getTrxId(), token.getFileToken(), (String) Configs.DOWNLOAD_LIST_SERVER_LINK.getValue());
			input = result.getFileInputStream();

			userLogger.info("file token contains the file name is successfully returned " + token.getFileToken());
			userLogger.info( "Exporting list");

			response.setHeader("Content-Type", "application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + result.getFileName() + "\"");

			output = response.getOutputStream();
			int octet;

			while ((octet = input.read()) != -1)
				output.write(octet);

			input.close();
			output.flush();
			output.close();

			userLogger.info( "Done exporting list file " + result.getFileName());

		} catch (Exception e) {
			input.close();
			output.flush();
			output.close();
			userLogger.error("Error while exporting list file=" + result.getFileName(), e);
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
}

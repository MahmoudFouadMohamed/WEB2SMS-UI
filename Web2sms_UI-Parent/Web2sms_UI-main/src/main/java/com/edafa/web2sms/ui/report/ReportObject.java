package com.edafa.web2sms.ui.report;

import com.edafa.web2sms.service.model.CampaignAggregationReport;
import com.edafa.web2sms.service.model.SummaryReport;

public class ReportObject {

	private CampaignAggregationReport campaignReportObj;
	private double remaingSmsRatio;
	private SummaryReport grandTotal;
//	private boolean resentFailedFlag;
//
//	public boolean isResentFailedFlag() {
//		return resentFailedFlag;
//	}
//
//	public void setResentFailedFlag(boolean resentFailedFlag) {
//		this.resentFailedFlag = resentFailedFlag;
//	}

	public CampaignAggregationReport getCampaignReportObj() {
		return campaignReportObj;
	}

	public void setCampaignReportObj(CampaignAggregationReport campaignReportObj) {
		this.campaignReportObj = campaignReportObj;
	}

	public double getRemaingSmsRatio() {
		return remaingSmsRatio;
	}

	public void setRemaingSmsRatio(double remaingSmsRatio) {
		this.remaingSmsRatio = remaingSmsRatio;
	}

	public SummaryReport getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(SummaryReport grandTotal) {
		this.grandTotal = grandTotal;
	}

	@Override
	public String toString() {
		return "campaignId " + campaignReportObj.getCampaignId() + ", resentFailedFlag : ("  +campaignReportObj.isResentFailedFlag()+ ")";
	}

}

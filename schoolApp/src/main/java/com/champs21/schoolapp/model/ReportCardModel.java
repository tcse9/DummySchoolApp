/**
 * 
 */
package com.champs21.schoolapp.model;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * @author Amit
 *
 */
public class ReportCardModel {
	@SerializedName("term_report")
	private ArrayList<TermReportItem> termReportItemList;
	@SerializedName("class_test_report")
	private ArrayList<ClassTestReportItem> classtestReportItemList ;
	
	public ReportCardModel(){
		termReportItemList= new ArrayList<TermReportItem>();
		classtestReportItemList = new ArrayList<ClassTestReportItem>();
	}
	
	public void setClasstestReportItemList(
			ArrayList<ClassTestReportItem> classtestReportItemList) {
		this.classtestReportItemList = classtestReportItemList;
	}
	public ArrayList<ClassTestReportItem> getClasstestReportItemList() {
		return classtestReportItemList;
	}
	
	public void setTermReportItemList(
			ArrayList<TermReportItem> termReportItemList) {
		this.termReportItemList = termReportItemList;
	}
	public ArrayList<TermReportItem> getTermReportItemList() {
		return termReportItemList;
	}
}

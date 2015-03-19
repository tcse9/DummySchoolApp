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
public class CTReportSubjectExam {
	@SerializedName("class_test")
	private ArrayList<ClassTestItem> classtestList;
	@SerializedName("project")
	private ArrayList<ClassTestItem> projectList;
	
	public void setClasstestList(ArrayList<ClassTestItem> classtestList) {
		this.classtestList = classtestList;
	}
	public ArrayList<ClassTestItem> getClasstestList() {
		return classtestList;
	}
	
	public void setProjectList(ArrayList<ClassTestItem> projectList) {
		this.projectList = projectList;
	}
	public ArrayList<ClassTestItem> getProjectList() {
		return projectList;
	}
}

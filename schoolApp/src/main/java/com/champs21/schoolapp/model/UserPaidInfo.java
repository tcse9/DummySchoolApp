package com.champs21.schoolapp.model;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class UserPaidInfo {

	
	@SerializedName("id")
	private String id;
	@SerializedName("full_name")
	private String fullName;
	
	@SerializedName("is_admin")
	private boolean admin;
	@SerializedName("is_student")
	private boolean student;
	@SerializedName("is_teacher")
	private boolean teacher;
	@SerializedName("is_parent")
	private boolean parent;
	@SerializedName("school_id")
	private String schoolId;
	@SerializedName("secret")
	private String secret;
	@SerializedName("batch_id")
	private String batchId;
	@SerializedName("profile_id")
	private String profileId;
	@SerializedName("terms")
	private ArrayList<Term> termList;
	@SerializedName("course_name")
	private String course_name;
	@SerializedName("section_name")
	private String section_name;
	@SerializedName("batch_name")
	private String batch_name;
	@SerializedName("profile_image")
	private String profile_image;
	@SerializedName("school_name")
	private String school_name;
	
	
	public String getSchool_name() {
		return school_name;
	}

	public void setSchool_name(String school_name) {
		this.school_name = school_name;
	}

	public String getCourse_name() {
		return course_name;
	}

	public void setCourse_name(String course_name) {
		this.course_name = course_name;
	}

	public String getSection_name() {
		return section_name;
	}

	public void setSection_name(String section_name) {
		this.section_name = section_name;
	}

	public String getBatch_name() {
		return batch_name;
	}

	public void setBatch_name(String batch_name) {
		this.batch_name = batch_name;
	}

	public String getProfile_image() {
		return profile_image;
	}

	public void setProfile_image(String profile_image) {
		this.profile_image = profile_image;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setParent(boolean parent) {
		this.parent = parent;
	}

	public boolean isParent() {
		return parent;
	}

	public void setStudent(boolean student) {
		this.student = student;
	}

	public boolean isStudent() {
		return student;
	}

	public void setTeacher(boolean teacher) {
		this.teacher = teacher;
	}

	public boolean isTeacher() {
		return teacher;
	}

	public void setSchoolId(String schoolId) {
		this.schoolId = schoolId;
	}

	public String getSchoolId() {
		return schoolId;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getSecret() {
		return secret;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getBatchId() {
		return batchId;
	}
	
	public void setProfileId(String profileId) {
		this.profileId = profileId;
	}

	public String getProfileId() {
		return profileId;
	}

	public void setTermList(ArrayList<Term> termList) {
		this.termList = termList;
	}

	public ArrayList<Term> getTermList() {
		return termList;
	}
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

}

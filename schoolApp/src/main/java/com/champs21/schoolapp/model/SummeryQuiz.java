package com.champs21.schoolapp.model;

import com.google.gson.annotations.SerializedName;

public class SummeryQuiz {
	@SerializedName("id")
	private String id;
	@SerializedName("name")
	private String name;
	@SerializedName("icon")
	private String icon;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
}

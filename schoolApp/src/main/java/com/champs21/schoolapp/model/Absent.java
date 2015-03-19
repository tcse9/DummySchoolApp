package com.champs21.schoolapp.model;

import com.champs21.schoolapp.adapters.CalendarAdapter.CalenderEventType;
import com.google.gson.annotations.SerializedName;

public class Absent implements CalenderEvent{
	@SerializedName("date")
	private String date;
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public CalenderEventType getType() {
		// TODO Auto-generated method stub
		return CalenderEventType.ABSENT;
	}

	
}

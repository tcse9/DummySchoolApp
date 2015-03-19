package com.champs21.freeversion;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.model.AcademicCalendarDataItem;
import com.champs21.schoolapp.model.HomeworkData;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SingleCalendarEvent extends ChildContainerActivity {
	
	
	
	private UIHelper uiHelper;
	private Gson gson;
	private String id;
	
	private AcademicCalendarDataItem data;
	//private TextView txtEventCatName;
	private TextView txtEventTitle;
	private TextView txtStartDate;
	private TextView txtEndDate;
	private TextView txtDescription;
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        homeBtn.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
	}
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_calendar_event);
		
		uiHelper = new UIHelper(SingleCalendarEvent.this);
		gson = new Gson();
		
		if(getIntent().getExtras() != null)
			this.id = getIntent().getExtras().getString(AppConstant.ID_SINGLE_CALENDAR_EVENT);
		
		/*JsonObject objHomework = modelContainer.getData().get("homework").getAsJsonObject();
		data = gson.fromJson(objHomework.toString(), HomeworkData.class);*/
		
		
		Log.e("SINGLE_EVENT", "id: "+id);
		
		initView();
		
		initApiCall();
		
	}
	
	
	private void initView()
	{
		//this.txtEventCatName = (TextView)this.findViewById(R.id.txtEventCatName);
		this.txtEventTitle = (TextView)this.findViewById(R.id.txtEventTitle);
		this.txtStartDate = (TextView)this.findViewById(R.id.txtStartDate);
		this.txtEndDate = (TextView)this.findViewById(R.id.txtEndDate);
		this.txtDescription = (TextView)this.findViewById(R.id.txtDescription);
	}
	
	
	private void initApiCall()
	{
		RequestParams params = new RequestParams();

		//app.showLog("adfsdfs", app.getUserSecret());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put("id", this.id);
		
		AppRestClient.post(URLHelper.URL_SINGLE_CALENDAR_EVENT, params, singleCalendarEvent);
	}
	
	private void initAction()
	{
		//this.txtEventCatName.setText(data.getEventCategoryName());
		this.txtEventTitle.setText(data.getEventTitle());
		this.txtStartDate.setText(data.getEventDate());
		this.txtEndDate.setText(data.getEventEndDate());
		this.txtDescription.setText(data.getEventDescription());
	}
	
	
	
	AsyncHttpResponseHandler singleCalendarEvent = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}
		};

		@Override
		public void onStart() {
			
				uiHelper.showLoadingDialog("Please wait...");
			

		};

		@Override
		public void onSuccess(int arg0, String responseString) {
			

			uiHelper.dismissLoadingDialog();


			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);

			if (modelContainer.getStatus().getCode() == 200) {
				
				JsonObject objEvent = modelContainer.getData().get("events").getAsJsonObject();
				data = gson.fromJson(objEvent.toString(), AcademicCalendarDataItem.class);
				
				initAction();
				
			}
			
			else {

			}
			
			

		};
	};
	
	
	

}

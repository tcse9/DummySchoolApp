package com.champs21.freeversion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.champs21.schoolapp.BatchSelectionChangedBroadcastReceiver;
import com.champs21.schoolapp.BatchSelectionChangedBroadcastReceiver.onBatchIdChangeListener;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.fragments.LeaveApplicationFragment;
import com.champs21.schoolapp.model.BaseType;
import com.champs21.schoolapp.model.Batch;
import com.champs21.schoolapp.model.Picker;
import com.champs21.schoolapp.model.Picker.PickerItemSelectedListener;
import com.champs21.schoolapp.model.PickerType;
import com.champs21.schoolapp.model.StudentParent;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.CustomDateTimePicker;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class CreateMeetingRequest extends HomeBaseActivity {
	
	
	
	private String batchId="42";
	
	private UIHelper uiHelper;
	
	private TextView txtCurrentDate;
	
	private ImageButton btnSelectBatch;
	private TextView txtSelectBatch;
	private ImageButton btnSelectParent;
	private TextView txtSelectParent;
	private ImageButton btnSelectDateTime;
	private TextView txtSelectDateTime;
	private EditText txtDescription;
	
	private String selectedId = "";
	private String selectedBatchId = "";
	
	private List<StudentParent> listStudentParent = new ArrayList<StudentParent>();
	
	private ImageButton btnGoBack;
	private ImageButton btnSendMeetingRequest;
	
	private LinearLayout layoutSelectBatchSegmnent;
	private TextView txtHeaderParent;
	
	private LinearLayout layoutSelectBatchActionHolder;
	private LinearLayout layoutSelectParentActionHolder;
	private LinearLayout layoutSelectSelectDateTimeActionHolder;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_create_meeting_request);
		
		uiHelper = new UIHelper(this);
		
		
		
		initView();
		initAction();
	}
	
	
	private void initView() {
		// TODO Auto-generated method stub
		this.txtCurrentDate = (TextView)this.findViewById(R.id.txtCurrentDate);
		
		this.btnSelectBatch = (ImageButton)this.findViewById(R.id.btnSelectBatch);
		this.txtSelectBatch = (TextView)this.findViewById(R.id.txtSelectBatch);
		
		this.btnSelectParent = (ImageButton)this.findViewById(R.id.btnSelectParent);
		this.txtSelectParent = (TextView)this.findViewById(R.id.txtSelectParent);
		
		this.btnSelectDateTime = (ImageButton)this.findViewById(R.id.btnSelectDateTime);
		this.txtSelectDateTime = (TextView)this.findViewById(R.id.txtSelectDateTime);
		
		this.txtDescription = (EditText)this.findViewById(R.id.txtDescription);
		
		this.btnGoBack = (ImageButton)this.findViewById(R.id.btnGoBack);
		this.btnSendMeetingRequest = (ImageButton)this.findViewById(R.id.btnSendMeetingRequest);
		
		this.layoutSelectBatchSegmnent = (LinearLayout)this.findViewById(R.id.layoutSelectBatchSegmnent);
		this.txtHeaderParent = (TextView)this.findViewById(R.id.txtHeaderParent);
		
		
		this.layoutSelectBatchActionHolder = (LinearLayout)this.findViewById(R.id.layoutSelectBatchActionHolder);
		this.layoutSelectParentActionHolder = (LinearLayout)this.findViewById(R.id.layoutSelectParentActionHolder);
		this.layoutSelectSelectDateTimeActionHolder = (LinearLayout)this.findViewById(R.id.layoutSelectSelectDateTimeActionHolder);
		
	}

	private void initAction() {
		// TODO Auto-generated method stub
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			this.layoutSelectBatchSegmnent.setVisibility(View.GONE);
			this.txtHeaderParent.setText("Select Teacher");
			this.txtSelectParent.setHint("Select Teacher");//Select Parent by Student Name
			
			initApiCallParent();
		}
		else
		{
			this.layoutSelectBatchSegmnent.setVisibility(View.VISIBLE);
			this.txtHeaderParent.setText("Select Parent");
			this.txtSelectParent.setHint("Select Parent by Student Name");
		}
		
		
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
		this.txtCurrentDate.setText(currentDateTimeString);
		
		
		this.btnSelectBatch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//showBatchPicker(PickerType.TEACHER_BATCH);
				
				
				RequestParams params=new RequestParams();
				params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
				AppRestClient.post(URLHelper.URL_GET_TEACHER_BATCH, params, getBatchEventsHandler);
			
				
			}
		});
		
		
		// this layout also consumes the click event same as this.btnSelectBatch
		this.layoutSelectBatchActionHolder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//showBatchPicker(PickerType.TEACHER_BATCH);
				
				RequestParams params=new RequestParams();
				params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
				AppRestClient.post(URLHelper.URL_GET_TEACHER_BATCH, params, getBatchEventsHandler);
			
				
			}
		});
		
		
		this.btnSelectParent.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(TextUtils.isEmpty(txtSelectBatch.getText().toString()))
				{
					Toast.makeText(CreateMeetingRequest.this, "Select a batch first", Toast.LENGTH_SHORT).show();
				}
				
				PopupMenu popup = new PopupMenu(CreateMeetingRequest.this, btnSelectParent);
				//popup.getMenuInflater().inflate(R.menu.popup_menu_medium, popup.getMenu());  
				for(int i=0;i<listStudentParent.size();i++)
					popup.getMenu().add(listStudentParent.get(i).getName());
				 
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
		             public boolean onMenuItemClick(MenuItem item) {  
		              //Toast.makeText(SchoolSearchFragment.this.getActivity(),"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();  
		              
		            	 txtSelectParent.setText(item.getTitle().toString());
		            	 
		            	 for(StudentParent sp : listStudentParent)
		            	 {
		            		 if(item.getTitle().toString().equalsIgnoreCase(sp.getName()))
		            			 selectedId = sp.getId();
		            	 }
		            	 
		            	 Log.e("ITEM_ID", "id: "+selectedId);
		              
		              return true;  
		             }  
		            });  

		        popup.show();
			}
		});
		
		// this layout also consumes the click event same as this.btnSelectParent
		this.layoutSelectParentActionHolder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(TextUtils.isEmpty(txtSelectBatch.getText().toString()))
				{
					Toast.makeText(CreateMeetingRequest.this, "Select a batch first", Toast.LENGTH_SHORT).show();
				}

				// TODO Auto-generated method stub
				PopupMenu popup = new PopupMenu(CreateMeetingRequest.this, btnSelectParent);
				//popup.getMenuInflater().inflate(R.menu.popup_menu_medium, popup.getMenu());  
				for(int i=0;i<listStudentParent.size();i++)
					popup.getMenu().add(listStudentParent.get(i).getName());
				 
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {  
		             public boolean onMenuItemClick(MenuItem item) {  
		              //Toast.makeText(SchoolSearchFragment.this.getActivity(),"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();  
		              
		            	 txtSelectParent.setText(item.getTitle().toString());
		            	 
		            	 for(StudentParent sp : listStudentParent)
		            	 {
		            		 if(item.getTitle().toString().equalsIgnoreCase(sp.getName()))
		            			 selectedId = sp.getId();
		            	 }
		            	 
		            	 Log.e("ITEM_ID", "id: "+selectedId);
		              
		              return true;  
		             }  
		            });  

		        popup.show();
			
			}
		});
		
		
		this.btnSelectDateTime.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDateTimePicker();
			}
		});
		
		
		// this layout also consumes the click event same as this.btnSelectDateTime
		this.layoutSelectSelectDateTimeActionHolder.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDateTimePicker();
			}
		});
		
		
		
		this.btnGoBack.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		this.btnSendMeetingRequest.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(isValidate())
					initApiCallSendRequest();
			}
		});
		
	}
	
	
	AsyncHttpResponseHandler getBatchEventsHandler=new AsyncHttpResponseHandler()
	{

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			//uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		}

		@Override
		public void onStart() {
			super.onStart();
			
			if(!uiHelper.isDialogActive())
				uiHelper.showLoadingDialog(getString(R.string.loading_text));
			else
				uiHelper.updateLoadingDialog(getString(R.string.loading_text));
			
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);
			uiHelper.dismissLoadingDialog();
			Log.e("Response", responseString);
			Wrapper wrapper=GsonParser.getInstance().parseServerResponse(responseString);
			if(wrapper.getStatus().getCode()==AppConstant.RESPONSE_CODE_SUCCESS)
			{
				PaidVersionHomeFragment.isBatchLoaded=true;
				PaidVersionHomeFragment.batches.clear();
				String data=wrapper.getData().get("batches").toString();
				PaidVersionHomeFragment.batches.addAll(GsonParser.getInstance().parseBatchList(data));
				//showPicker(PickerType.TEACHER_BATCH);
				showBatchPicker(PickerType.TEACHER_BATCH);
			}
			
		}
		
	};
	
	
	
	private void showDateTimePicker() {
		// TODO Auto-generated method stub
		CustomDateTimePicker custom = new CustomDateTimePicker(this,
				new CustomDateTimePicker.ICustomDateTimeListener() {

					@Override
					public void onCancel() {

					}

					@Override
					public void onSet(Dialog dialog, Calendar calendarSelected,
							Date dateSelected, int year, String monthFullName,
							String monthShortName, int monthNumber, int date,
							String weekDayFullName, String weekDayShortName,
							int hour24, int hour12, int min, int sec,
							String AM_PM) {
						// TODO Auto-generated method stub

						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
						String dateStr = format.format(dateSelected);
						Log.e("DATE_SELECTED", "is: "+dateStr);
						
						txtSelectDateTime.setText(dateStr);
						
					}
				});
		/**
		 * Pass Directly current time format it will return AM and PM if you set
		 * false
		 */
		custom.set24HourFormat(false);
		/**
		 * Pass Directly current data and time to show when it pop up
		 */
		custom.setDate(Calendar.getInstance());
		custom.showDialog();
	}
	
	
	
	private void showBatchPicker(PickerType type) {

		Picker picker = Picker.newInstance(0);
		picker.setData(type, PaidVersionHomeFragment.batches, PickerCallback , "Select Batch");
		picker.show(this.getSupportFragmentManager(), null);
	}

	PickerItemSelectedListener PickerCallback = new PickerItemSelectedListener() {

		@Override
		public void onPickerItemSelected(BaseType item) {

			switch (item.getType()) {
			case TEACHER_BATCH:
				Batch selectedBatch=(Batch)item;
				PaidVersionHomeFragment.selectedBatch=selectedBatch;
				Intent i = new Intent("com.champs21.schoolapp.batch");
                i.putExtra("batch_id", selectedBatch.getId());
                CreateMeetingRequest.this.sendBroadcast(i);
				
				
				Log.e("BATCH", "name: "+selectedBatch.getName());
				Log.e("BATCH", "id: "+selectedBatch.getId());
				
				txtSelectBatch.setText(selectedBatch.getName());
				
				selectedBatchId = selectedBatch.getId();
				initApiCall(selectedBatch.getId());
				
				break;
			default:
				break;
			}

		}
	};
	
	
	
	private void initApiCall(String batchId) {

		RequestParams params = new RequestParams();

		params.put("batch_id", batchId);
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			

		AppRestClient.post(URLHelper.URL_MEETING_GETSTUDENTPARENT, params,
				getStudentParentHandler);
	}
	
	
	private void initApiCallParent() {

		RequestParams params = new RequestParams();
		
		
			
		params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			
		Log.e("BATCH_ID", "is: "+userHelper.getUser().getSelectedChild().getBatchId());
		
		AppRestClient.post(URLHelper.URL_MEETING_GETTEACHERPARENT, params,
				getStudentParentHandler);
	}
	
	
	
	
	
	private AsyncHttpResponseHandler getStudentParentHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) 
		{
			uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		};

		@Override
		public void onStart() 
		{
			uiHelper.showLoadingDialog("Please wait...");
		};

		@Override
		public void onSuccess(String responseString) {
			
			uiHelper.dismissLoadingDialog();
			Log.e("RES_PARENT", "is: "+responseString);

			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);

			
			if (modelContainer.getStatus().getCode() == 200) {

				//do parsing
				JsonArray array = modelContainer.getData().get("student").getAsJsonArray();
				
				for (int i = 0; i < parseStudentParent(array.toString()).size(); i++) 
				{
					listStudentParent.add(parseStudentParent(array.toString()).get(i));
				}
				
				

			}

			else {

			}
		};
	};
	
	
	private boolean isValidate()
	{
		boolean isValid = true;
		if(layoutSelectBatchSegmnent.getVisibility() == View.VISIBLE && TextUtils.isEmpty(txtSelectBatch.getText().toString()))
		{
			Toast.makeText(this, "Select batch", Toast.LENGTH_SHORT).show();
			isValid = false;
		}
			
		
		else if(TextUtils.isEmpty(txtSelectParent.getText().toString()))
		{
			if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
			{
				Toast.makeText(this, "Select teacher name", Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(this, "Select parent by student name", Toast.LENGTH_SHORT).show();
			isValid = false;
		}
			
		
		else if(TextUtils.isEmpty(txtSelectDateTime.getText().toString()))
		{
			Toast.makeText(this, "Select date and time", Toast.LENGTH_SHORT).show();
			isValid = false;
		}
			
		
		else if(TextUtils.isEmpty(txtDescription.getText().toString()))
		{
			Toast.makeText(this, "Enter meeting description", Toast.LENGTH_SHORT).show();
			isValid = false;
		}
			
		
		return isValid;
	}
	
	private void initApiCallSendRequest() {

		RequestParams params = new RequestParams();

		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}
		else{
			params.put("batch_id", selectedBatchId);
			
		}
		
		
		params.put("parent_id", selectedId);
		params.put("description", txtDescription.getText().toString());
		params.put("datetime", txtSelectDateTime.getText().toString());
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
		}
		
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			
		

		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			AppRestClient.post(URLHelper.URL_MEETING_SEND_REQUEST_PARENT, params,
					sendMeetingRequestHandler);
		}
		
		else
		{
			AppRestClient.post(URLHelper.URL_MEETING_SEND_REQUEST, params,
					sendMeetingRequestHandler);
		}
		
	}
	
	private AsyncHttpResponseHandler sendMeetingRequestHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) 
		{
			uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		};

		@Override
		public void onStart() 
		{
			uiHelper.showLoadingDialog("Please wait...");
		};

		@Override
		public void onSuccess(String responseString) {
			
			uiHelper.dismissLoadingDialog();
			

			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);

			
			if (modelContainer.getStatus().getCode() == 200) {

				//do parsing
				Toast.makeText(CreateMeetingRequest.this, "Meeting request sent sucessfully", Toast.LENGTH_SHORT).show();
				
				CreateMeetingRequest.this.finish();

			}

			else {

			}
		};
	};
	
	
	
	public ArrayList<StudentParent> parseStudentParent(String object) {
		ArrayList<StudentParent> data = new ArrayList<StudentParent>();
		data = new Gson().fromJson(object,
				new TypeToken<ArrayList<StudentParent>>() {
				}.getType());
		return data;
	}


	
	

}

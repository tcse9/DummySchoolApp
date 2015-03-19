package com.champs21.schoolapp.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.fragments.DatePickerFragment.DatePickerOnSetDateListener;
import com.champs21.schoolapp.model.BaseType;
import com.champs21.schoolapp.model.LeaveType;
import com.champs21.schoolapp.model.Picker;
import com.champs21.schoolapp.model.Picker.PickerItemSelectedListener;
import com.champs21.schoolapp.model.PickerType;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.AppUtility;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class LeaveApplicationFragment extends Fragment implements
		OnClickListener {

	View rootView;
	private UIHelper uiHelper;
	EditText  leaveDescriptionEditText;
	private List<BaseType> subjectCats;
	TextView leaveReasonTextView, choosenFileTextView;
	private String leaveId = "", homeworkTypeId = "1";
	private String selectedFilePath;
	private TextView chooseStartDateTextView,chooseEndDateTextView;
	private final static int REQUEST_CODE_FILE_CHOOSER = 101;
	private String startDateFormatServerString = "";
	private String endDateFormatServerString = "";

	
	private UserHelper userHelper;
	
	private LinearLayout layoutReasonHolder;
	
	private EditText etLeaveSubject;
	
	private TextView txtDate;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		subjectCats = new ArrayList<BaseType>();
		uiHelper = new UIHelper(getActivity());
		userHelper = new UserHelper(getActivity());
	}

	private boolean isFormValid() {
		
		
		if (userHelper.getUser().getType() != UserTypeEnum.PARENTS && leaveId.equals("")) {
			Toast.makeText(getActivity(), "Please choose a reason type!",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if(etLeaveSubject.getText().toString().trim().equals(""))
		{
			Toast.makeText(getActivity(),
					"Application subject can't be empty!", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		
		if (leaveDescriptionEditText.getText().toString().trim().equals("")) {
			Toast.makeText(getActivity(),
					"Leave description can't be Empty!", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		if (startDateFormatServerString.equals("")) {
			Toast.makeText(getActivity(),
					"Please Choose a start date for Homework!",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		if (endDateFormatServerString.equals("")) {
			Toast.makeText(getActivity(),
					"Please Choose an end date for Homework!",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	public void RequestForLeave() {

		RequestParams params = new RequestParams();

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		
		if (userHelper.getUser().getType() != UserTypeEnum.PARENTS) 
		{
			params.put(RequestKeyHelper.EMPLOYEE_LEAVE, leaveId);
		}
		
		if(userHelper.getUser().getType() == UserTypeEnum.PARENTS)
		{
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
		}
		
		
		//params.put(RequestKeyHelper.EMPLOYEE_LEAVE, leaveId);
		params.put("leave_subject", etLeaveSubject.getText().toString());
		params.put(RequestKeyHelper.REASON, leaveDescriptionEditText
				.getText().toString());
		params.put(RequestKeyHelper.START_DATE, startDateFormatServerString);
		params.put(RequestKeyHelper.END_DATE, endDateFormatServerString);
//		params.put(RequestKeyHelper.HOMEWORK_DUEDATE, s);
		
		
		
		if(userHelper.getUser().getType() == UserTypeEnum.PARENTS)
		{
			AppRestClient.post(URLHelper.URL_PARENT_LEAVE, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onStart() {
							if (!uiHelper.isDialogActive())
								uiHelper.showLoadingDialog("Loading..");
							super.onStart();
						}

						@Override
						public void onFailure(Throwable arg0, String response) {
							if (uiHelper.isDialogActive())
								uiHelper.dismissLoadingDialog();
							Log.e("POST HOMEWORK FAILED", response);
							super.onFailure(arg0, response);
						}

						@Override
						public void onSuccess(int arg0, String responseString) {
							if (uiHelper.isDialogActive())
								uiHelper.dismissLoadingDialog();

							Log.e("SERVERRESPONSE", responseString);
							Wrapper wrapper = GsonParser.getInstance()
									.parseServerResponse(responseString);
							if (wrapper.getStatus().getCode() == AppConstant.RESPONSE_CODE_SUCCESS) {
								Toast.makeText(getActivity(),
										"Successfully posted Homework!",
										Toast.LENGTH_SHORT).show();
								leaveDescriptionEditText.setText("");
								leaveId="";
								startDateFormatServerString="";
								endDateFormatServerString = "";
							} else
								Toast.makeText(
										getActivity(),
										"Failed to post Homework! Please try again.",
										Toast.LENGTH_SHORT).show();
							super.onSuccess(arg0, responseString);
						}
					});
		}
		else
		{
			AppRestClient.post(URLHelper.URL_TEACHER_LEAVE, params,
					new AsyncHttpResponseHandler() {
						@Override
						public void onStart() {
							if (!uiHelper.isDialogActive())
								uiHelper.showLoadingDialog("Loading..");
							super.onStart();
						}

						@Override
						public void onFailure(Throwable arg0, String response) {
							if (uiHelper.isDialogActive())
								uiHelper.dismissLoadingDialog();
							Log.e("POST HOMEWORK FAILED", response);
							super.onFailure(arg0, response);
						}

						@Override
						public void onSuccess(int arg0, String responseString) {
							if (uiHelper.isDialogActive())
								uiHelper.dismissLoadingDialog();

							Log.e("SERVERRESPONSE", responseString);
							Wrapper wrapper = GsonParser.getInstance()
									.parseServerResponse(responseString);
							if (wrapper.getStatus().getCode() == AppConstant.RESPONSE_CODE_SUCCESS) {
								Toast.makeText(getActivity(),
										"Successfully posted Homework!",
										Toast.LENGTH_SHORT).show();
								leaveDescriptionEditText.setText("");
								leaveId="";
								startDateFormatServerString="";
								endDateFormatServerString = "";
							} else
								Toast.makeText(
										getActivity(),
										"Failed to post Homework! Please try again.",
										Toast.LENGTH_SHORT).show();
							super.onSuccess(arg0, responseString);
						}
					});
		}
		
		
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.leave_application_layout,
				container, false);
		intiviews(rootView);
		fetchSubject();
		return rootView;
	}

	

	private void fetchSubject() {

		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		AppRestClient.post(URLHelper.URL_TEACHER_GET_SUBJECT, params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onFailure(Throwable arg0, String response) {
						super.onFailure(arg0, response);
						Log.e("GET_SUBJECT_RESPONSE_FAIL", response+"");
					}

					@Override
					public void onSuccess(int arg0, String response) {
						super.onSuccess(arg0, response);
						Log.e("GET_SUBJECT_RESPONSE_SUCCESS", response);
						Wrapper wrapper = GsonParser.getInstance()
								.parseServerResponse(response);
						if (wrapper.getStatus().getCode() == AppConstant.RESPONSE_CODE_SUCCESS) {
							subjectCats.clear();
							subjectCats.addAll(GsonParser.getInstance()
									.parseSubject(
											wrapper.getData().get("subjects")
													.toString()));
						}
					}
				});
	}

	private void intiviews(View view) {
		
		leaveDescriptionEditText = (EditText) view
				.findViewById(R.id.et_leave_description);
		leaveReasonTextView = (TextView) view
				.findViewById(R.id.tv_leave_reason);
		choosenFileTextView = (TextView) view
				.findViewById(R.id.tv_teacher_ah_choosen_file_name);
		chooseStartDateTextView = (TextView) view
				.findViewById(R.id.tv_start_date);
		chooseEndDateTextView = (TextView) view.findViewById(R.id.tv_end_date);
		((ImageButton) view.findViewById(R.id.btn_leave_reason))
				.setOnClickListener(this);
		((ImageButton) view.findViewById(R.id.btn_leave_end_date))
				.setOnClickListener(this);
		((ImageButton) view.findViewById(R.id.btn_leave_start_date))
		.setOnClickListener(this);
		((Button) view.findViewById(R.id.btn_leave_apply))
				.setOnClickListener(this);
		
		this.layoutReasonHolder = (LinearLayout)view.findViewById(R.id.layoutReasonHolder);
		
		if(userHelper.getUser().getType() == UserTypeEnum.PARENTS)
		{
			this.layoutReasonHolder.setVisibility(View.GONE);
		}
		else
		{
			this.layoutReasonHolder.setVisibility(View.VISIBLE);
		}
		
		etLeaveSubject = (EditText)view.findViewById(R.id.et_leave_subject);

		
		/*Date cDate = new Date();
		String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
		txtDate = (TextView)rootView.findViewById(R.id.date_text);
		txtDate.setText(AppUtility.getDateString(fDate, AppUtility.DATE_FORMAT_APP, AppUtility.DATE_FORMAT_SERVER));*/ 
		
		txtDate = (TextView)view.findViewById(R.id.date_text);
		
		Date cDate = new Date();
		String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
		txtDate.setText(AppUtility.getDateString(fDate, AppUtility.DATE_FORMAT_APP, AppUtility.DATE_FORMAT_SERVER));
		
		
	}

	public void showPicker(PickerType type, List<BaseType> cats, String title) {

		Picker picker = Picker.newInstance(0);
		picker.setData(type, cats, new PickerItemSelectedListener() {

			@Override
			public void onPickerItemSelected(BaseType item) {

				switch (item.getType()) {
				case TEACHER_SUBJECT:
					/*Subject mdata = (Subject) item;
					subjectNameTextView.setText(mdata.getName());
					subjectId = mdata.getId();*/
					break;
				case TEACHER_HOMEWORKTYPE:
					/*TypeHomeWork data = (TypeHomeWork) item;
					leaveReasonTextView.setText(data.getTypeName());
					homeworkTypeId = data.getTypeId();*/
					break;
				default:
					break;
				}

			}
		}, title);
		picker.show(getChildFragmentManager(), null);
	}
	
	public void showStudentPicker(PickerType type) {

		CustomPickerWithLoadData picker = CustomPickerWithLoadData.newInstance(0);
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		picker.setData(PickerType.LEAVE,params,URLHelper.URL_GET_LEAVE_TYPE, new PickerItemSelectedListener() {
			
			@Override
			public void onPickerItemSelected(BaseType item) {
				LeaveType reasonType =  (LeaveType)item;
				leaveReasonTextView.setText(reasonType.getText());
				leaveId = reasonType.getId();
			}
		} , "Select Leave Type");
		picker.show(getChildFragmentManager(), null);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_leave_start_date:
			showStartDatepicker();
			break;
		case R.id.btn_leave_end_date:
			showEndDatepicker();
			break;
		case R.id.btn_leave_apply:
			if (isFormValid()) {
				RequestForLeave();
			}
			break;
		case R.id.btn_leave_reason:
			showStudentPicker(PickerType.LEAVE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_FILE_CHOOSER:
			if (resultCode == getActivity().RESULT_OK) {
				if (data != null) {
					// Get the URI of the selected file
					final Uri uri = data.getData();
					if (uri.getLastPathSegment().endsWith("doc")
							|| uri.getLastPathSegment().endsWith("docx")
							|| uri.getLastPathSegment().endsWith("pdf")) {
						try {
							// Get the file path from the URI
							final String path = FileUtils.getPath(
									getActivity(), uri);
							selectedFilePath = path;
							choosenFileTextView
									.setText(getFileNameFromPath(selectedFilePath));

						} catch (Exception e) {
							Log.e("FileSelectorTestActivity",
									"File select error", e);
						}
					} else {
						Toast.makeText(getActivity(), "Invalid file type",
								Toast.LENGTH_SHORT).show();
					}
					Log.e("File", "Uri = " + uri.toString());
				}
			}
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showChooser() {
		Intent target = FileUtils.createGetContentIntent();
		Intent intent = Intent.createChooser(target,
				getString(R.string.chooser_title));
		try {
			startActivityForResult(intent, REQUEST_CODE_FILE_CHOOSER);
		} catch (ActivityNotFoundException e) {
			// The reason for the existence of aFileChooser
		}
	}

	private String getFileNameFromPath(String path) {
		String[] tokens = path.split("/");
		return tokens[tokens.length - 1];
	}

	private void showStartDatepicker() {
		DatePickerFragment picker = new DatePickerFragment();
		picker.setCallbacks(startDatePickerCallback);
		picker.show(getFragmentManager(), "startdatePicker");
	}
	private void showEndDatepicker() {
		DatePickerFragment picker = new DatePickerFragment();
		picker.setCallbacks(endDatePickerCallback);
		picker.show(getFragmentManager(), "enddatePicker");
	}
	DatePickerOnSetDateListener startDatePickerCallback = new DatePickerOnSetDateListener() {

		@Override
		public void onDateSelected(int month, String monthName, int day,
				int year, String dateFormatServer, String dateFormatApp,
				Date date) {
			// TODO Auto-generated method stub
			chooseStartDateTextView.setText(dateFormatApp);
			startDateFormatServerString = dateFormatServer;
		}
	};
	DatePickerOnSetDateListener endDatePickerCallback = new DatePickerOnSetDateListener() {

		@Override
		public void onDateSelected(int month, String monthName, int day,
				int year, String dateFormatServer, String dateFormatApp,
				Date date) {
			// TODO Auto-generated method stub
			chooseEndDateTextView.setText(dateFormatApp);
			endDateFormatServerString = dateFormatServer;
		}
	};
}

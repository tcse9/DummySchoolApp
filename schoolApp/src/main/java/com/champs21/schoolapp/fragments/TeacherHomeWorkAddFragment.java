package com.champs21.schoolapp.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.fragments.DatePickerFragment.DatePickerOnSetDateListener;
import com.champs21.schoolapp.model.BaseType;
import com.champs21.schoolapp.model.Picker;
import com.champs21.schoolapp.model.Picker.PickerItemSelectedListener;
import com.champs21.schoolapp.model.PickerType;
import com.champs21.schoolapp.model.Subject;
import com.champs21.schoolapp.model.TypeHomeWork;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.AppUtility;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.viewhelpers.CustomButton;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class TeacherHomeWorkAddFragment extends Fragment implements
		OnClickListener {

	View rootView;
	private UIHelper uiHelper;
	EditText subjectEditText, homeworkDescriptionEditText;
	private List<BaseType> subjectCats;
	private List<BaseType> homeworkTypeCats;
	TextView subjectNameTextView, homeWorkTypeTextView, choosenFileTextView;
	private String subjectId="", homeworkTypeId="1";
	private String selectedFilePath;
	private TextView choosenDateTextView;
	private final static int REQUEST_CODE_FILE_CHOOSER = 101;
	private String dateFormatServerString = "";
	
	private LinearLayout layoutDate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		subjectCats = new ArrayList<BaseType>();
		uiHelper = new UIHelper(getActivity());
	}

	private boolean isFormValid() {
		if(subjectId.equals("")){
			Toast.makeText(getActivity(), "Please choose a subject", Toast.LENGTH_SHORT).show();
			return false;
		}
		if(subjectEditText.getText().toString().trim().equals("")){
			Toast.makeText(getActivity(), "Subject title cannot be Empty!", Toast.LENGTH_SHORT).show();
			return false;
		}
		if(homeworkDescriptionEditText.getText().toString().trim().equals("")){
			Toast.makeText(getActivity(), "Homework description cannot be Empty!", Toast.LENGTH_SHORT).show();
			return false;
		}
		if(dateFormatServerString.equals("")){
			Toast.makeText(getActivity(), "Please Choose a due date for Homework!", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	public void PublishHomeWork() {

		RequestParams params = new RequestParams();

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put(RequestKeyHelper.SUBJECT_ID, subjectId);
		params.put(RequestKeyHelper.CONTENT, homeworkDescriptionEditText
				.getText().toString());
		params.put(RequestKeyHelper.SUBJECT_TITLE, subjectEditText.getText()
				.toString());
		params.put(RequestKeyHelper.TYPE, homeworkTypeId);
		params.put(RequestKeyHelper.HOMEWORK_DUEDATE, dateFormatServerString);
		AppRestClient.post(URLHelper.URL_TEACHER_ADD_HOMEWORK, params,
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
						} else
							Toast.makeText(
									getActivity(),
									"Failed to post Homework! Please try again.",
									Toast.LENGTH_SHORT).show();
						super.onSuccess(arg0, responseString);
					}
				});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_teacher_add_homework,
				container, false);
		intiviews(rootView);
		createHomeworkTypeCats();
		fetchSubject();
		return rootView;
	}

	private void createHomeworkTypeCats() {
		homeworkTypeCats = new ArrayList<BaseType>();
		homeworkTypeCats.add(new TypeHomeWork("Regular", "1"));
		homeworkTypeCats.add(new TypeHomeWork("Project", "2"));
	}

	private void fetchSubject() {

		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		AppRestClient.post(URLHelper.URL_TEACHER_GET_SUBJECT, params,
				new AsyncHttpResponseHandler() {
					@Override
					public void onFailure(Throwable arg0, String response) {
						super.onFailure(arg0, response);
						Log.e("GET_SUBJECT_RESPONSE_FAIL", response);
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
		subjectEditText = (EditText) view
				.findViewById(R.id.et_teacher_ah_subject_name);
		homeworkDescriptionEditText = (EditText) view
				.findViewById(R.id.et_teacher_ah_homework_description);
		subjectNameTextView = (TextView) view
				.findViewById(R.id.tv_teacher_ah_subject_name);
		homeWorkTypeTextView = (TextView) view
				.findViewById(R.id.tv_teacher_ah_homework_type);
		choosenFileTextView = (TextView) view
				.findViewById(R.id.tv_teacher_ah_choosen_file_name);
		choosenDateTextView = (TextView) view
				.findViewById(R.id.tv_teacher_ah_date);
		((ImageButton) view.findViewById(R.id.btn_subject_name))
				.setOnClickListener(this);
		((ImageButton) view.findViewById(R.id.btn_homework_type))
				.setOnClickListener(this);
		((CustomButton) view.findViewById(R.id.btn_teacher_ah_attach_file))
				.setOnClickListener(this);
		((CustomButton) view.findViewById(R.id.btn_teacher_ah_due_date))
				.setOnClickListener(this);
		((ImageButton) view.findViewById(R.id.btn_publish_homework))
				.setOnClickListener(this);

		layoutDate = (LinearLayout)view.findViewById(R.id.layoutDate);
		layoutDate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDatepicker();
			}
		});
		
		
		Date cDate = new Date();
		String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
		
		choosenDateTextView.setText(AppUtility.getDateString(fDate, AppUtility.DATE_FORMAT_APP, AppUtility.DATE_FORMAT_SERVER));
	}

	public void showPicker(PickerType type, List<BaseType> cats, String title) {

		Picker picker = Picker.newInstance(0);
		picker.setData(type, cats, new PickerItemSelectedListener() {

			@Override
			public void onPickerItemSelected(BaseType item) {

				switch (item.getType()) {
				case TEACHER_SUBJECT:
					Subject mdata = (Subject) item;
					subjectNameTextView.setText(mdata.getName());
					subjectId = mdata.getId();
					break;
				case TEACHER_HOMEWORKTYPE:
					TypeHomeWork data = (TypeHomeWork) item;
					homeWorkTypeTextView.setText(data.getTypeName());
					homeworkTypeId = data.getTypeId();
					break;
				default:
					break;
				}

			}
		}, title);
		picker.show(getChildFragmentManager(), null);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_subject_name:
			showPicker(PickerType.TEACHER_SUBJECT, subjectCats,
					"Select your Subject");
			break;
		case R.id.btn_homework_type:
			showPicker(PickerType.TEACHER_HOMEWORKTYPE, homeworkTypeCats,
					"Select Homework type");
			break;
		case R.id.btn_teacher_ah_attach_file:
			showChooser();
			break;
		case R.id.btn_teacher_ah_due_date:
			showDatepicker();
			break;
		case R.id.btn_publish_homework:
			if (isFormValid()) {
				PublishHomeWork();
			}
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

	private void showDatepicker() {
		DatePickerFragment picker = new DatePickerFragment();
		picker.setCallbacks(datePickerCallback);
		picker.show(getFragmentManager(), "datePicker");
	}

	DatePickerOnSetDateListener datePickerCallback = new DatePickerOnSetDateListener() {

		@Override
		public void onDateSelected(int month, String monthName, int day,
				int year, String dateFormatServer, String dateFormatApp,
				Date date) {
			// TODO Auto-generated method stub
			choosenDateTextView.setText(dateFormatApp);
			dateFormatServerString = dateFormatServer;
		}

		/*
		 * @Override public void onDateSelected(String monthName, int day, int
		 * year) { // TODO Auto-generated method stub Date date; try { date =
		 * new SimpleDateFormat("MMMM").parse(monthName); Calendar cal =
		 * Calendar.getInstance(); cal.setTime(date); String dateString = day +
		 * "-" + cal.get(Calendar.MONTH) + "-" + year;
		 * choosenDateTextView.setText(dateString); } catch (ParseException e) {
		 * // TODO Auto-generated catch block Log.e("ERROR", e.toString()); } }
		 */
	};
}

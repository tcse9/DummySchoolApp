/**
 * 
 */
package com.champs21.schoolapp.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.adapters.TeacherRoutineAdapter;
import com.champs21.schoolapp.model.BaseType;
import com.champs21.schoolapp.model.Picker;
import com.champs21.schoolapp.model.Picker.PickerItemSelectedListener;
import com.champs21.schoolapp.model.PickerType;
import com.champs21.schoolapp.model.RoutineTimeTable;
import com.champs21.schoolapp.model.WeekDay;
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
import com.google.gson.JsonElement;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 *
 */
public class StudentClassRoutineFragment extends UserVisibleHintFragment implements OnClickListener{

	

	private View view;
	private ListView classList;
	private TextView currentDateText,noDataText,nextClassNameText,nextSubjectNameText,nextClassDateText,nextClassTimeText;
	private TextView listHeaderTextView;
	private ImageButton nextClassRefreshBtn;
	private UserHelper userHelper;
	private UIHelper uiHelper;
	private Context con;
	private RelativeLayout selectDayLayout;
	private ProgressBar nextClassPanelRefreshPb,listPb;
	private RoutineTimeTable nextClass;
	private String currentDateServer;
	private List<RoutineTimeTable> weekDayClasses;
	private List<BaseType> days;
	private String currentWeekDay;
	private String selectedWeekDayId="current";
	private TeacherRoutineAdapter adapter;

	
	public void showPicker(PickerType type) {

		Picker picker = Picker.newInstance(0);
		picker.setData(type, days, PickerCallback , "Select day");
		picker.show(getChildFragmentManager(), null);
	}
	
	PickerItemSelectedListener PickerCallback = new PickerItemSelectedListener() {

		@Override
		public void onPickerItemSelected(BaseType item) {

			switch (item.getType()) {
			case WEEK_DAYS:
				selectedWeekDayId=((WeekDay)item).getId();
				fetchWeekDayDataFromServer(selectedWeekDayId);
				break;
			
			default:
				break;
			}

		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.teacher_routine_fragment_layout,
				container, false);
		currentDateText=(TextView)view.findViewById(R.id.curr_date_text);
		nextClassNameText=(TextView)view.findViewById(R.id.tv_class_batch_name);
		nextSubjectNameText=(TextView)view.findViewById(R.id.tv_subject_name);
		nextClassDateText=(TextView)view.findViewById(R.id.tv_date);
		nextClassTimeText=(TextView)view.findViewById(R.id.tv_time);
		nextClassRefreshBtn=(ImageButton)view.findViewById(R.id.next_class_refresh_btn);
		nextClassRefreshBtn.setOnClickListener(this);
		nextClassPanelRefreshPb=(ProgressBar)view.findViewById(R.id.next_class_panel_refresh_pb);
		listHeaderTextView=(TextView)view.findViewById(R.id.textview1);
		classList=(ListView)view.findViewById(R.id.listview);
		listPb=(ProgressBar)view.findViewById(R.id.list_pb);
		selectDayLayout=(RelativeLayout)view.findViewById(R.id.select_day_layout);
		selectDayLayout.setOnClickListener(this);
		noDataText=(TextView)view.findViewById(R.id.no_data_text);
		return view;
	}

	
	private void fetchWeekDayDataFromServer(String weekDayId) {
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		/*params.put(RequestKeyHelper.SCHOOL, userHelper.getUser().getPaidInfo()
				.getSchoolId());*/
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}
		
		params.put(RequestKeyHelper.DAILY, "0");
		if(!weekDayId.equalsIgnoreCase(""))
		{
			params.put(RequestKeyHelper.DAY_ID, weekDayId);
		}
		
		AppRestClient.post(URLHelper.URL_GET_WEEK_DAY_STUDENT_CLASSES, params,
				getWeekDayClassesHandler);
	}
	
	
	private void getNextClassData() {

		if (AppUtility.isInternetConnected()) {
			fetchNextClassDataFromServer();
		} else
			uiHelper.showMessage(con.getString(R.string.internet_error_text));

	}

	private void fetchNextClassDataFromServer() {
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}
		AppRestClient.post(URLHelper.URL_GET_NEXT_CLASS_STUDENT, params,
				getNextClassHandler);
	}

	private void updateNextClassPanel(RoutineTimeTable nextClass)
	{
		currentDateText.setText(AppUtility.getDateString(currentDateServer,AppUtility.DATE_FORMAT_APP, AppUtility.DATE_FORMAT_SERVER ));
		
		String dayName = nextClass.getWeekDayText().trim();
		dayName.toLowerCase(); 
		dayName = dayName.substring(0,1).toUpperCase() + dayName.substring(1).toLowerCase();
	
		//nextClassDateText.setText(nextClass.getWeekDayText());
		nextClassDateText.setText(dayName);
		
		
		nextClassTimeText.setText(nextClass.getClassStartTime()+"-"+nextClass.getClassEndTime());
		nextClassNameText.setText(nextClass.getTeacher_full_name());
		nextSubjectNameText.setText(nextClass.getSubjectName());
	}
	
	
	AsyncHttpResponseHandler getWeekDayClassesHandler = new AsyncHttpResponseHandler() {
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			listPb.setVisibility(View.GONE);
		}

		@Override
		public void onStart() {
			super.onStart();
			listPb.setVisibility(View.VISIBLE);
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);
			listPb.setVisibility(View.GONE);
			Wrapper wrapper=GsonParser.getInstance().parseServerResponse(responseString);
			if(wrapper.getStatus().getCode()==AppConstant.RESPONSE_CODE_SUCCESS)
			{
				weekDayClasses.clear();
				weekDayClasses.addAll(GsonParser.getInstance().parseClassList(wrapper.getData().get("time_table").toString()));
				days.clear();
				days.addAll(GsonParser.getInstance().parseWeekDays(wrapper.getData().get("weekdays").toString()));
				currentWeekDay=GsonParser.getInstance().parseGsonToString(wrapper.getData().get("cur_week"));
				
				if(selectedWeekDayId.equalsIgnoreCase("current"))
				{
					String weekDayName=days.get(Integer.parseInt(currentWeekDay)).getText();
					
					weekDayName.toLowerCase(); 
					weekDayName = weekDayName.substring(0,1).toUpperCase() + weekDayName.substring(1).toLowerCase();
					
					Resources res = getActivity().getResources();
					String text = String.format(res.getString(R.string.teacher_routine_title), weekDayName);
					
					
					listHeaderTextView.setText(text);
					String noClassText = String.format(res.getString(R.string.no_class_title), weekDayName);
					noDataText.setText(noClassText);
				}
				else
				{
					String weekDayName="";
					/*if(selectedWeekDayId.equals("current")){
						weekDayName = "current";
					}else {*/
						weekDayName=days.get(Integer.parseInt(selectedWeekDayId)).getText();
					//}
						
					weekDayName.toLowerCase(); 
					weekDayName = weekDayName.substring(0,1).toUpperCase() + weekDayName.substring(1).toLowerCase();
						
						
					Resources res = getActivity().getResources();
					String text = String.format(res.getString(R.string.teacher_routine_title), weekDayName);
					
					
					
					listHeaderTextView.setText(text);
					String noClassText = String.format(res.getString(R.string.no_class_title), weekDayName);
					noDataText.setText(noClassText);
				}
				adapter.notifyDataSetChanged();
				if(weekDayClasses.size()>0)
				{
					classList.setVisibility(View.VISIBLE);
					noDataText.setVisibility(View.GONE);
				}
				else
				{
					classList.setVisibility(View.GONE);
					noDataText.setVisibility(View.VISIBLE);
				}
				
			}
		}
	};
	
	
	AsyncHttpResponseHandler getNextClassHandler = new AsyncHttpResponseHandler() {
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			nextClassPanelRefreshPb.setVisibility(View.GONE);
			nextClassRefreshBtn.setVisibility(View.VISIBLE);
		}

		@Override
		public void onStart() {
			super.onStart();
			nextClassPanelRefreshPb.setVisibility(View.VISIBLE);
			nextClassRefreshBtn.setVisibility(View.GONE);
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);
			nextClassPanelRefreshPb.setVisibility(View.GONE);
			nextClassRefreshBtn.setVisibility(View.VISIBLE);
			Wrapper wrapper=GsonParser.getInstance().parseServerResponse(responseString);
			if(wrapper.getStatus().getCode()==AppConstant.RESPONSE_CODE_SUCCESS)
			{
				currentDateServer=GsonParser.getInstance().parseGsonToString(wrapper.getData().get("today"));
				Log.e("Date", currentDateServer);
				JsonElement e = wrapper.getData().get("time_table");
				if(!e.isJsonArray()){
					nextClass=GsonParser.getInstance().parseRoutineTimeTable(e.toString());
					updateNextClassPanel(nextClass);
				}
				
			}
			Log.e("NextClass", responseString);
		}
	};

	private void setUpList() {
		adapter = new TeacherRoutineAdapter(getActivity(), 0, weekDayClasses);
		classList.setAdapter(adapter);
	}
	private void init() {
		con = getActivity();
		uiHelper = new UIHelper(getActivity());
		userHelper = new UserHelper(con);
		weekDayClasses=new ArrayList<RoutineTimeTable>();
		days=new ArrayList<BaseType>();
	}

	

	@Override
	protected void onVisible() {
		getNextClassData();
		fetchWeekDayDataFromServer(selectedWeekDayId);
		setUpList();
	}

	@Override
	protected void onInvisible() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.next_class_refresh_btn:
			fetchNextClassDataFromServer();
			break;
		case R.id.select_day_layout:
			if(days.size()>0)
				showPicker(PickerType.WEEK_DAYS);
		default:
			break;
		}
		
	}

}

/**
 * 
 */
package com.champs21.schoolapp.fragments;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.champs21.freeversion.AssesmentActivity;
import com.champs21.freeversion.AssesmentHomeworkActivity;
import com.champs21.freeversion.AssessmentLeaderBoardActivity;
import com.champs21.freeversion.SingleHomeworkActivity;
import com.champs21.freeversion.SingleItemShowActivity;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.model.AssessmentHomework;
import com.champs21.schoolapp.model.BaseType;
import com.champs21.schoolapp.model.City;
import com.champs21.schoolapp.model.HomeWorkSubject;
import com.champs21.schoolapp.model.HomeworkData;
import com.champs21.schoolapp.model.ModelContainer;
import com.champs21.schoolapp.model.Picker;
import com.champs21.schoolapp.model.PickerType;
import com.champs21.schoolapp.model.UserAuthListener;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.model.Picker.PickerItemSelectedListener;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.AppUtility;
import com.champs21.schoolapp.utils.CustomDateTimePicker;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.ReminderHelper;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.champs21.schoolapp.viewhelpers.CustomButton;
import com.champs21.schoolapp.viewhelpers.CustomButtonTest;
import com.champs21.schoolapp.viewhelpers.CustomTabButton;
import com.champs21.schoolapp.viewhelpers.PopupDialogHomeworkAssessmentResult;
import com.champs21.schoolapp.viewhelpers.PopupDialogSingleItemAssessmentInvok;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class HomeworkFragment extends Fragment implements OnClickListener,UserAuthListener{

	boolean _areContentLoaded = false;
	
	//SchoolApp app;
	UIHelper uiHelper;
	UserHelper userHelper;

	//	ArrayList<HomeworkData> homeworkList;
	EfficientAdapter homeworkAdapter;
	EfficientAdapter projectAdapter;
	EfficientAdapter previousAdapter;

	EfficientAdapter currentAdapter;
	PullToRefreshListView listviewHomework;
	String currentTabKey = "1";

	boolean hasNext = false;
	int type = 1;

	CustomTabButton tabHomework, tabProject, current;
	
	
	CustomTabButton tabAssessment;
	private List<AssessmentHomework> listAssessmentHomework;
	private PullToRefreshListView listViewAssessment;
	private AssessmentAdapter assessmentAdapter;
	
	private TextView txtNoData;
	
	
	private boolean hasNextAssessment = false;
	private int pageNumber = 1;
	private boolean isRefreshing = false;
	private boolean loading = false;
	private boolean stopLoadingData = false;
	
	private LinearLayout layoutAssessmentHolder;
	
	private LinearLayout layoutFilter;
	
	private String selectedSubjectId;
	private String selectedDate;
	
	
	private List<BaseType> homeWorkSubject;
	
	
	private boolean isFilterClicked = false;
	
	private ImageView imgFilter;
	
	private LinearLayout layoutMidPanel;
	
	private LinearLayout layoutSubject;
	private LinearLayout layoutDate;
	
	
	/*@Override
	 public void setUserVisibleHint(boolean isVisibleToUser) {
	     super.setUserVisibleHint(isVisibleToUser);
	     if (isVisibleToUser && !_areContentLoaded ) {
	      
	    	 _areContentLoaded = true;
	    	 Log.e("HomeWork", "Visible hoise ar Content loaded");
	    	 processFetchHomework(URLHelper.URL_HOMEWORK);
	     }
	 }*/
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		if (!_areContentLoaded ) {
		      
	    	 _areContentLoaded = true;
	    	 Log.e("HomeWork", "Visible hoise ar Content loaded");
	    	 processFetchHomework(URLHelper.URL_HOMEWORK);
	     }
		
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		//app = (SchoolApp) getActivity().getApplicationContext();
		uiHelper=new UIHelper(getActivity());
		
		homeWorkSubject = new ArrayList<BaseType>();

		initAdapters();
		currentAdapter = homeworkAdapter;
		userHelper=new UserHelper(this, getActivity());
	}

	private void initAdapters() {
		// TODO Auto-generated method stub
		ArrayList<HomeworkData> homeWorkItemList = new ArrayList<HomeworkData>();
		ArrayList<HomeworkData> projectItemList = new ArrayList<HomeworkData>();
		ArrayList<HomeworkData> previousItemList = new ArrayList<HomeworkData>();

		homeworkAdapter = new EfficientAdapter(getActivity(), homeWorkItemList);
		projectAdapter = new EfficientAdapter(getActivity(), projectItemList);
		previousAdapter = new EfficientAdapter(getActivity(), previousItemList);

		homeworkAdapter.setRefreshing(false);
		homeworkAdapter.setRefreshing(false);

		projectAdapter.setRefreshing(false);
		projectAdapter.setStopLoadingData(false);

		previousAdapter.setRefreshing(false);
		previousAdapter.setStopLoadingData(false);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.fragment_homework, container, false);

		initView(view);

		listviewHomework = (PullToRefreshListView) view.findViewById(R.id.listView_homework);

		// Set a listener to be invoked when the list should be refreshed.
		listviewHomework.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				Mode m = listviewHomework.getCurrentMode();
				if(m==Mode.PULL_FROM_START)
				{
					currentAdapter.setStopLoadingData(false);
					currentAdapter.setRefreshing(true);
					currentAdapter.resetPageNumber();
					if (type == 1) {
						processFetchHomework(URLHelper.URL_HOMEWORK);
					} else {
						processFetchHomework(URLHelper.URL_PROJECT);
					}

				}
				else if(!currentAdapter.isStopLoadingData())
				{
					currentAdapter.addPageNumber();
					if (type == 1) {
						processFetchHomework(URLHelper.URL_HOMEWORK);
					} else {
						processFetchHomework(URLHelper.URL_PROJECT);
					}
				}
				else
				{
					new NoDataTask().execute();
				}
			}
		});

		listviewHomework.setMode(Mode.BOTH);

		listviewHomework.setAdapter(currentAdapter);
		
		
		
		
		
		layoutFilter = (LinearLayout)view.findViewById(R.id.layoutFilter);
		imgFilter = (ImageView)view.findViewById(R.id.imgFilter);
		layoutMidPanel = (LinearLayout)view.findViewById(R.id.layoutMidPanel);		
		
		layoutSubject = (LinearLayout)view.findViewById(R.id.layoutSubject);
		layoutDate = (LinearLayout)view.findViewById(R.id.layoutDate);
		
		
		if (userHelper.getUser().getType() == UserTypeEnum.TEACHER) 
		{
			layoutFilter.setVisibility(View.GONE);
		}
		else
		{
			layoutFilter.setVisibility(View.VISIBLE);
		}
		
		layoutFilter.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				isFilterClicked = !isFilterClicked;
				
				if(isFilterClicked)
				{
					layoutFilter.setBackgroundColor(Color.parseColor("#b1b8ba"));
					imgFilter.setImageResource(R.drawable.filter_tap);
					
					layoutMidPanel.setVisibility(View.VISIBLE);
					
					//layoutMidPanel.animate().translationY(layoutMidPanel.getHeight());
				}
				
				else
				{
					layoutFilter.setBackgroundColor(Color.WHITE);
					imgFilter.setImageResource(R.drawable.filter_normal);
					
					layoutMidPanel.setVisibility(View.GONE);
					
					
					if (type == 1) {
						processFetchHomework(URLHelper.URL_HOMEWORK);
					} else {
						processFetchHomework(URLHelper.URL_PROJECT);
					}
					
					
				}
				
				
				//initApiCallSubject();
			}
		});
		
		
		
		layoutSubject.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				initApiCallSubject();
			}
		});
		
		layoutDate.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDateTimePicker(getActivity());
			}
		});
		
		
		
		initListAction();

		return view;
	}
	
	
	private void showDateTimePicker(final Context context) {
		CustomDateTimePicker custom = new CustomDateTimePicker(context,
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

						SimpleDateFormat format = new SimpleDateFormat(
								"yyyy-MM-dd");

						String dateStr = format.format(dateSelected);

						selectedDate = dateStr;
						Log.e("Date Selected", selectedDate);
						
						
						currentAdapter.clearList();
						
						if (type == 1) {
							processFetchHomeworkDate(URLHelper.URL_HOMEWORK, true);
						} else {
							processFetchHomeworkDate(URLHelper.URL_PROJECT, true);
						}
						
						
						
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
	
	
	
	
	
	private void initApiCallSubject()
	{
		RequestParams params = new RequestParams();

		//app.showLog("adfsdfs", app.getUserSecret());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		
		AppRestClient.post(URLHelper.URL_HOMEWORK_SUBJECT, params, subjectHandler);
		
		
	}
	
	
	AsyncHttpResponseHandler subjectHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}
		};

		@Override
		public void onStart() {
			if (currentAdapter.getPageNumber() == 1 && !currentAdapter.isRefreshing()) {
				uiHelper.showLoadingDialog("Please wait...");
			}

		};

		@Override
		public void onSuccess(int arg0, String responseString) {
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}

			homeWorkSubject.clear();
			
			Log.e("Response", responseString);
			//app.showLog("Response", responseString);
			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);
			if (modelContainer.getStatus().getCode() == 200) {

				JsonArray array = modelContainer.getData().get("subject").getAsJsonArray();
				
				List<HomeWorkSubject> list = new ArrayList<HomeWorkSubject>();
				for(int i=0;i<array.size();i++)
				{
					
					list.add(new HomeWorkSubject(array.get(i).getAsJsonObject().get("name").getAsString(), array.get(i).getAsJsonObject().get("id").getAsString()));
					
					
				}
				
				homeWorkSubject.addAll(list);

				showSubjectPicker();
			}

			else {

			}
		};
	};
	
	
	private void showSubjectPicker() {
		Picker picker = Picker.newInstance(0);
		picker.setData(PickerType.HOMEWORK_SUBJECT, homeWorkSubject, PickerCallback,
				"Choose your subject");
		picker.show(getChildFragmentManager(), null);
	}
	
	PickerItemSelectedListener PickerCallback = new PickerItemSelectedListener() {

		@Override
		public void onPickerItemSelected(BaseType item) {

			switch (item.getType()) {
			case HOMEWORK_SUBJECT:
				HomeWorkSubject hs = (HomeWorkSubject) item;
				selectedSubjectId = hs.getId();
				
					
				currentAdapter.clearList();
				
				if (type == 1) {
					processFetchHomework(URLHelper.URL_HOMEWORK, true);
				} else {
					processFetchHomework(URLHelper.URL_PROJECT, true);
				}

				
				
				
				break;
			default:
				break;
			}

		}
	};
	
	
	

	private class NoDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			currentAdapter.notifyDataSetChanged();

			// Call onRefreshComplete when the list has been refreshed.
			listviewHomework.onRefreshComplete();

			super.onPostExecute(result);
		}
	}

	private void initView(View view) {
		// TODO Auto-generated method stub
		this.layoutAssessmentHolder = (LinearLayout)view.findViewById(R.id.layoutAssessmentHolder);
		

		HashMap<String, Integer> tabHomeworkTag = new HashMap<String, Integer>();

		tabHomework = (CustomTabButton) view.findViewById(R.id.tab_homework);
		tabHomework.setOnClickListener(this);

		tabHomeworkTag.put(AppConstant.NORMAL, R.drawable.homeworks_tab_gray);
		tabHomeworkTag.put(AppConstant.SELECTED, R.drawable.homeworks_tab_black);

		tabHomework.setTag(tabHomeworkTag);

		tabProject = (CustomTabButton) view
				.findViewById(R.id.tab_project);
		tabProject.setOnClickListener(this);

		HashMap<String, Integer> tabProjectTag = new HashMap<String, Integer>();
		tabProjectTag.put(AppConstant.NORMAL, R.drawable.project_gray);
		tabProjectTag.put(AppConstant.SELECTED, R.drawable.project_black);

		tabProject.setTag(tabProjectTag);
		
		
		tabAssessment = (CustomTabButton)view.findViewById(R.id.tab_assessment);
		HashMap<String, Integer> tabAssessmentTag = new HashMap<String, Integer>();
		tabAssessmentTag.put(AppConstant.NORMAL, R.drawable.asses_gray);
		tabAssessmentTag.put(AppConstant.SELECTED, R.drawable.asses_black);
		tabAssessment.setTag(tabAssessmentTag);
		
		
		listAssessmentHomework = new ArrayList<AssessmentHomework>();
		assessmentAdapter = new AssessmentAdapter();
		listViewAssessment = (PullToRefreshListView)view.findViewById(R.id.listViewAssessment);
		//listViewAssessment.setAdapter(assessmentAdapter);
		setUpList();
		
		
		/*if (userHelper.getUser().getType() == UserTypeEnum.STUDENT) 
		{
			layoutAssessmentHolder.setVisibility(View.VISIBLE);
		}
		else
		{
			layoutAssessmentHolder.setVisibility(View.GONE);
		}*/
		
		//layoutAssessmentHolder is hidden as we are not providing any quizes at homework page rather the quiz section is moved to QuizFragment.java class
		//but the code is still here and not commented as for further use.
		
		layoutAssessmentHolder.setVisibility(View.GONE);
		
		tabAssessment.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("TABB", "clicking assessment");
				setTabSelectedForAssessment(v, tabAssessment);
				
				initApiCallAssessment(pageNumber);
			}
		});
		
		/*listViewAssessment.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, 
					long id) {
				// TODO Auto-generated method stub
				AssessmentHomework data = (AssessmentHomework)assessmentAdapter.getItem(position);
				Intent intent = new Intent(getActivity(), AssesmentHomeworkActivity.class);
				intent.putExtra("ASSESSMENT_HOMEWORK_ID", data.getId());
				startActivity(intent);
				
			}
		});*/
		
		
		
		
		

		tabHomework.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.homeworks_tab_black);

		current = tabHomework;
		
		txtNoData = (TextView)view.findViewById(R.id.txtNoData);
		
		
		

	}
	
	private void initListAction()
	{
		
		listviewHomework.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				
				HomeworkData  data = (HomeworkData)currentAdapter.getItem(position-1);
				
				Intent intent = new Intent(getActivity(), SingleHomeworkActivity.class);
				intent.putExtra(AppConstant.ID_SINGLE_HOMEWORK, data.getId());
				startActivityForResult(intent, 50);
				
				
				Log.e("DATA_CLICKED", "is: "+data.getId());
				
				
				
			}
		});
		
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 50)
		{
		
			currentAdapter.clearList();
			processFetchHomework(URLHelper.URL_HOMEWORK);
		}
		
	}
	
	

	private void processFetchHomework(String url) {
		// TODO Auto-generated method stub

		RequestParams params = new RequestParams();

		//app.showLog("adfsdfs", app.getUserSecret());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}

		params.put(RequestKeyHelper.PAGE_NUMBER, currentAdapter.getPageNumber()+"");
		params.put(RequestKeyHelper.PAGE_SIZE, AppConstant.PAGE_SIZE+"");

		Log.e("Params", params.toString());

		AppRestClient.post(url, params, homeworkHandler);
	}
	
	
	private void processFetchHomework(String url, boolean isFilterApply) {
		// TODO Auto-generated method stub

		RequestParams params = new RequestParams();

		//app.showLog("adfsdfs", app.getUserSecret());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}

		
		if(isFilterApply == true)
		{
			params.put("subject_id", selectedSubjectId);
		}
		
		params.put(RequestKeyHelper.PAGE_NUMBER, currentAdapter.getPageNumber()+"");
		params.put(RequestKeyHelper.PAGE_SIZE, AppConstant.PAGE_SIZE+"");

		Log.e("Params", params.toString());

		AppRestClient.post(url, params, homeworkHandler);
	}
	
	
	private void processFetchHomeworkDate(String url, boolean isFilterApply) {
		// TODO Auto-generated method stub

		RequestParams params = new RequestParams();

		//app.showLog("adfsdfs", app.getUserSecret());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}

		
		if(isFilterApply == true)
		{
			params.put("duedate", selectedDate);
		}
		
		params.put(RequestKeyHelper.PAGE_NUMBER, currentAdapter.getPageNumber()+"");
		params.put(RequestKeyHelper.PAGE_SIZE, AppConstant.PAGE_SIZE+"");

		Log.e("Params", params.toString());

		AppRestClient.post(url, params, homeworkHandler);
	}
	
	
	
	
	
	

	AsyncHttpResponseHandler homeworkHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}
		};

		@Override
		public void onStart() {
			if (currentAdapter.getPageNumber() == 1 && !currentAdapter.isRefreshing()) {
				uiHelper.showLoadingDialog("Please wait...");
			}

		};

		@Override
		public void onSuccess(int arg0, String responseString) {
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}

			if (currentAdapter.getPageNumber() == 1) {
				currentAdapter.getList().clear();
			}
			Log.e("Response", responseString);
			//app.showLog("Response", responseString);
			ModelContainer modelContainer = GsonParser.getInstance().parseGson(responseString);

			if (modelContainer.getStatus().getCode() == 200) {
				hasNext = modelContainer.getData().isHasNext();

				if (!hasNext) {
					currentAdapter.setStopLoadingData(true);
				}

				currentAdapter.getList().addAll(modelContainer.getData().getHomeworkList());
				currentAdapter.notifyDataSetChanged();


				// Call onRefreshComplete when the list has been refreshed.
				if(currentAdapter.getPageNumber() != 1 || currentAdapter.isRefreshing())
				{
					listviewHomework.onRefreshComplete();
				}
				
				if(modelContainer.getData().getHomeworkList().size() <= 0)
				{
					Toast.makeText(getActivity(), "No data found!", Toast.LENGTH_SHORT).show();
				}
			}
		};
	};


	private class EfficientAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		//        ViewHolder holder;

		private ArrayList<HomeworkData> list;
		private int pageNumber = 1;
		private boolean stopLoadingData = false;
		private boolean isRefreshing = false;

		public EfficientAdapter(Context context, ArrayList<HomeworkData> list) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);
			this.list = list;
		}

		/**
		 * The number of items in the list is determined by the number of speeches
		 * in our array.
		 *
		 * @see android.widget.ListAdapter#getCount()
		 */
		public int getCount() {
			return list.size();
		}

		/**
		 * Since the data comes from an array, just returning the index is
		 * sufficent to get at the data. If we were using a more complex data
		 * structure, we would return whatever object represents one row in the
		 * list.
		 *
		 * @see android.widget.ListAdapter#getItem(int)
		 */
		public Object getItem(int position) {
			return list.get(position);
		}

		/**
		 * Use the array index as a unique id.
		 *
		 * @see android.widget.ListAdapter#getItemId(int)
		 */
		public long getItemId(int position) {
			return position;
			//dfsdf
		}
		
		
		public void clearList()
		{
			list.clear();
		}

		/**
		 * Make a view to hold each row.
		 *
		 * @see android.widget.ListAdapter#getView(int, android.view.View,
		 *      android.view.ViewGroup)
		 */
		//		public View getView(int position, View convertView, ViewGroup parent) {
		//
		//
		//		}


		class ViewHolder {
			TextView tvSubject;
			TextView tvDate;
			TextView tvLesson;
			TextView section;
			CustomButton btnDone;
			CustomButton btnReminder;
			ImageView ivSubjectIcon;
			TextView txtAssignDate;
			TextView txtAttachment;
			
			LinearLayout bottmlay;
			//CustomButton btnPrevious;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final int i = position;
			// A ViewHolder keeps references to children views to avoid unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;
			// When convertView is not null, we can reuse it directly, there is no need
			// to reinflate it. We only inflate a new View when the convertView supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.item_homework_feed, null);

				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.tvSubject = (TextView) convertView.findViewById(R.id.tv_teacher_feed_subject_name);
				holder.tvDate = (TextView) convertView.findViewById(R.id.tv_teacher_homewrok_feed_date);
				holder.tvLesson = (TextView) convertView.findViewById(R.id.tv_homework_content);
				holder.section = (TextView) convertView.findViewById(R.id.tv_teavher_homework_feed_section);
				holder.btnDone = (CustomButton) convertView.findViewById(R.id.btn_done);
				holder.ivSubjectIcon = (ImageView) convertView.findViewById(R.id.imgViewCategoryMenuIcon);
				
				holder.bottmlay = (LinearLayout)convertView.findViewById(R.id.bottmlay);
				
				holder.txtAssignDate = (TextView)convertView.findViewById(R.id.txtAssignDate);
				holder.txtAttachment = (TextView)convertView.findViewById(R.id.txtAttachment);
				

				//holder.btnPrevious = (CustomButton) convertView.findViewById(R.id.btn_previous);

				/*holder.btnPrevious.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String url;
						if (type == 1) {
							url = URLHelper.URL_HOMEWORK;
						} else {
							url = URLHelper.URL_PROJECT;
						}

						//app.showLog("previous", v.getTag().toString());

						processPreviousButton((CustomButton) v, url);
					}
				});*/

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}
			
			
			holder.btnDone.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					processDoneButton(i, (CustomButton) v);
				}
			});

			holder.btnReminder = (CustomButton) convertView.findViewById(R.id.btn_reminder);

			
			 if (ReminderHelper.getInstance().reminder_map.containsKey(AppConstant.KEY_HOMEWORK+list.get(i).getId())){
					setButtonState(holder.btnReminder, R.drawable.btn_reminder_tap, false, "Reminder");
					
				}else {
					setButtonState(holder.btnReminder, R.drawable.btn_reminder_normal, true, "Reminder");
				}
			
			holder.btnReminder.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CustomButton btn = (CustomButton) v;
					
					AppUtility.showDateTimePicker(AppConstant.KEY_HOMEWORK+list.get(i).getId(), list.get(i).getSubject()+ ": " + AppConstant.NOTIFICATION_HOMEWORK, list.get(i).getName(), HomeworkFragment.this.getActivity());
				
					
				}
			});
			

			// Bind the data efficiently with the holder.
			holder.tvSubject.setText( list.get(position).getSubject());

			String[] parts =  list.get(position).getDueDate().split(" ");
			String part1 = parts[0];
			holder.tvDate.setText(part1);
			//holder.tvLesson.setText(Html.fromHtml(list.get(position).getContent(), null, new MyTagHandler()));
			holder.tvLesson.setText(list.get(position).getName());
			holder.section.setText(list.get(position).getTeacherName());
			holder.ivSubjectIcon.setImageResource(AppUtility.getImageResourceId(list.get(position).getSubject_icon_name(), getActivity()));
			if ( list.get(position).getIsDone().equalsIgnoreCase(AppConstant.ACCEPTED) || 
					list.get(position).getIsDone().equalsIgnoreCase(AppConstant.SUBMITTED)) {
				holder.btnDone.setImage(R.drawable.done_tap);
				holder.btnDone.setTitleColor(HomeworkFragment.this.getActivity().getResources().getColor(R.color.maroon));

				holder.btnDone.setEnabled(false);
			} else {

				holder.btnDone.setImage(R.drawable.done_normal);
				holder.btnDone.setTitleColor(HomeworkFragment.this.getActivity().getResources().getColor(R.color.gray_1));

				if (userHelper.getUser().getType() == UserTypeEnum.STUDENT) {
					holder.btnDone.setEnabled(true);
				}
				if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
					holder.btnDone.setEnabled(false);
				}

				holder.btnDone.setTag( list.get(position).getId());
			}
			
			
			String[] parts2 =  list.get(position).getAssign_date().split(" ");
			String part2 = parts[0];
			holder.tvDate.setText(part2);
			holder.txtAssignDate.setText(part2);
			
			

			/*holder.btnPrevious.setEnabled(true);
			holder.btnPrevious.setTag( list.get(position).getSubjectId());*/
			
			if(list.get(position).getTimeOver() == 0)
			{
				holder.bottmlay.setVisibility(View.VISIBLE);
			}
			else if(list.get(position).getTimeOver() == 1)
			{
				holder.bottmlay.setVisibility(View.GONE);
			}
			
			if(!TextUtils.isEmpty(list.get(position).getAttachmentFileName()))
			{
				holder.txtAttachment.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.txtAttachment.setVisibility(View.GONE);
			}

			return convertView;
		}

		public ArrayList<HomeworkData> getList() {
			return list;
		}

		public int getPageNumber() {
			return pageNumber;
		}
		public void addPageNumber() {
			this.pageNumber++;
		}
		public void resetPageNumber() {
			pageNumber = 1;
		}

		public void setRefreshing(boolean isRefreshing) {
			this.isRefreshing = isRefreshing;
		}
		public boolean isRefreshing() {
			return isRefreshing;
		}

		public void setStopLoadingData(boolean stopLoadingData) {
			this.stopLoadingData = stopLoadingData;
		}
		public boolean isStopLoadingData() {
			return stopLoadingData;
		}
		
		
		
		@SuppressLint("ResourceAsColor")
		private void setButtonState(CustomButton btn, int imgResId, boolean enable , String btnText) {
			
			btn.setImage(imgResId);
			btn.setTitleText(btnText);
			btn.setEnabled(enable);
			if(enable) {
				setBtnTitleColor(btn, R.color.gray_1); 
			} else {
				setBtnTitleColor(btn, R.color.maroon); 
			}
		}
		private void setBtnTitleColor(CustomButton btn, int colorId) {
			btn.setTitleColor(getActivity().getResources().getColor(colorId));
		}
		
		
	}




	AsyncHttpResponseHandler doneBtnHandler = new AsyncHttpResponseHandler() {
		public void onFailure(Throwable arg0, String arg1) {
			Log.e("button", "failed");
			uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		};

		public void onStart() {
			Log.e("button", "onstart");
			uiHelper.showLoadingDialog("Please wait...");
		};

		public void onSuccess(int arg0, String response) {
			Log.e("response", response);
			Log.e("button", "success");
			uiHelper.dismissLoadingDialog();

			ModelContainer modelContainer = GsonParser.getInstance().parseGson(response);

			if (modelContainer.getStatus().getCode() == 200) {
				currentAdapter.getList().get(itemPosition).setIsDone(AppConstant.ACCEPTED);
			} else {
				uiHelper.showMessage("Error in operation!");
			}


			currentAdapter.notifyDataSetChanged();

			Log.e("status code", modelContainer.getStatus().getCode() + "");
		};
	};

	CustomButton button;
	int itemPosition;

	protected void processDoneButton(int position, CustomButton button) {
		// TODO Auto-generated method stub

		itemPosition = position;

		this.button = button;

		RequestParams params = new RequestParams();

		Log.e("User secret", UserHelper.getUserSecret());
		Log.e("Ass_ID", button.getTag().toString());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put(RequestKeyHelper.ASSIGNMENT_ID, button.getTag().toString());

		AppRestClient.post(URLHelper.URL_HOMEWORK_DONE, params, doneBtnHandler);
	}

	protected void processPreviousButton(CustomButton v, String url) {
		// TODO Auto-generated method stub
		this.button = v;

		currentAdapter = previousAdapter;
		type = 3;

		listviewHomework.setAdapter(currentAdapter);

		RequestParams params = new RequestParams();

		Log.e("User secret", UserHelper.getUserSecret());
		Log.e("subject id", button.getTag().toString());

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put(RequestKeyHelper.SUBJECT_ID, button.getTag().toString());

		AppRestClient.post(url, params, homeworkHandler);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.tab_homework:
			type = 1;
			currentAdapter = homeworkAdapter;
			setTabSelected(v, tabHomework, "1");
			break;
		case R.id.tab_project:
			Log.e("TABBbb", "clicking assessment");
			type = 2;
			currentAdapter = projectAdapter;
			setTabSelected(v, tabProject, "2");
			break;
		
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void setTabSelected(View v, CustomTabButton currentCustom,
			String homeworkTypeId) {
		// TODO Auto-generated method stub
		listViewAssessment.setVisibility(View.GONE);
		listviewHomework.setVisibility(View.VISIBLE);
		
		
		
		CustomTabButton btn = (CustomTabButton) v;
		HashMap<String, Integer> btnTag = (HashMap<String, Integer>) current.getTag();
		current.setButtonSelected(false, getResources().getColor(R.color.black), btnTag.get(AppConstant.NORMAL));
		HashMap<String, Integer> clickedBtnTag = (HashMap<String, Integer>) btn.getTag();
		btn.setButtonSelected(true, getResources().getColor(R.color.black), clickedBtnTag.get(AppConstant.SELECTED));
		current = currentCustom;
		currentTabKey = homeworkTypeId;

		listviewHomework.setAdapter(currentAdapter);

		if (currentAdapter.getList().isEmpty()) {
			if (type == 1) {
				processFetchHomework(URLHelper.URL_HOMEWORK);
			} else {
				processFetchHomework(URLHelper.URL_PROJECT);
			}

		}


		//		projectAdapter.notifyDataSetChanged();
	}
	
	
	private void initApiCallAssessment(int pageNumber)
	{
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		params.put("page_size", "10");
		params.put("page_number", String.valueOf(pageNumber));
		

		AppRestClient.post(URLHelper.URL_HOMEWORK_ASSESSMENT_LIST, params,
				assessmentHomeworkHandler);
	}
	
	private AsyncHttpResponseHandler assessmentHomeworkHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		};

		@Override
		public void onStart() {
			uiHelper.showLoadingDialog("Please wait...");
		};

		@Override
		public void onSuccess(String responseString) {
			

			uiHelper.dismissLoadingDialog();

			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);
			
			hasNextAssessment = modelContainer.getData().get("has_next").getAsBoolean();
			
			
			if (pageNumber == 1)
			{
				assessmentAdapter.clearList();
			}
				
			if (!hasNextAssessment) 
			{
				stopLoadingData = true;
			}
			
			

			if (modelContainer.getStatus().getCode() == 200) {
				JsonArray arraHomework = modelContainer.getData().get("homework").getAsJsonArray();
				
				//listAssessmentHomework = parseAssessmentList(arraHomework.toString());
				
				
				for (int i = 0; i < parseAssessmentList(arraHomework.toString())
						.size(); i++) {
					listAssessmentHomework.add(parseAssessmentList(arraHomework.toString()).get(i));
				}
				
				
				if (pageNumber != 0 || isRefreshing) 
				{
					listViewAssessment.onRefreshComplete();
					loading = false;
				}
				
				assessmentAdapter.notifyDataSetChanged();
				
			}
			
			else {

			}
			
			
			if(listAssessmentHomework.size() <= 0)
			{
				txtNoData.setVisibility(View.VISIBLE);
				//listViewAssessment.setVisibility(View.GONE);
			}
			else
			{
				txtNoData.setVisibility(View.GONE);
				//listViewAssessment.setVisibility(View.VISIBLE);
			}

		};

	};
	
	
	private void initApiCallAssessmentResult(String id)
	{
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		params.put("id", id);
		

		AppRestClient.post(URLHelper.URL_HOMEWORK_ASSESSMENT_RESULT, params,
				assessmentHomeworkResultHandler);
	}
	
	
	private AsyncHttpResponseHandler assessmentHomeworkResultHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		};

		@Override
		public void onStart() {
			uiHelper.showLoadingDialog("Please wait...");
		};

		@Override
		public void onSuccess(String responseString) {
			

			uiHelper.dismissLoadingDialog();

			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);
			

			if (modelContainer.getStatus().getCode() == 200) {
				
				JsonObject obj = modelContainer.getData().get("assesment").getAsJsonObject();
				
				String nameText = obj.get("name").getAsString();
				String subjectText = obj.get("subject_name").getAsString();
				String totalStudent = obj.get("total_student").getAsString();
				String totaltotalParticipated = obj.get("total_participated").getAsString();
				String maxScore = obj.get("max_score").getAsString();
				String minScore = obj.get("min_score").getAsString();
				String totalTimeTaken = obj.get("total_time_taken").getAsString();
				
				String totalMarkText = obj.get("total_mark").getAsString();
				String isPassedText = obj.get("is_passed").getAsString();
				String totalScoreText = obj.get("total_score").getAsString();
				
				
				String studentCountText = totaltotalParticipated+"/"+totalStudent;
				
				
				showCustomDialogHomeworkAssessmentOk("QUIZ", nameText, subjectText, studentCountText, maxScore, minScore, totalMarkText, totalTimeTaken, isPassedText, totalScoreText, R.drawable.assessment_icon_popup, getActivity());
				
			}
			
			else {

			}
			
			

		};

	};
	
	
	
	
	
	public List<AssessmentHomework> parseAssessmentList(String object) {

		List<AssessmentHomework> tags = new ArrayList<AssessmentHomework>();
		Type listType = new TypeToken<List<AssessmentHomework>>() {
		}.getType();
		tags = (List<AssessmentHomework>) new Gson().fromJson(object, listType);
		return tags;
	}
	
	
	public List<HomeWorkSubject> parseHomeworkSubject(String object) {

		List<HomeWorkSubject> tags = new ArrayList<HomeWorkSubject>();
		Type listType = new TypeToken<List<HomeWorkSubject>>() {
		}.getType();
		tags = (List<HomeWorkSubject>) new Gson().fromJson(object, listType);
		return tags;
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void setTabSelectedForAssessment(View v, CustomTabButton currentCustom) {
		// TODO Auto-generated method stub
		listViewAssessment.setVisibility(View.VISIBLE);
		listviewHomework.setVisibility(View.GONE);
		
		
		CustomTabButton btn = (CustomTabButton) v;
		
		tabHomework.setButtonSelected(false, getResources().getColor(R.color.black), ((HashMap<String, Integer>) tabHomework.getTag()).get(AppConstant.NORMAL));
		tabProject.setButtonSelected(false, getResources().getColor(R.color.black), ((HashMap<String, Integer>) tabProject.getTag()).get(AppConstant.NORMAL));
		HashMap<String, Integer> clickedBtnTag = (HashMap<String, Integer>) btn.getTag();
		btn.setButtonSelected(true, getResources().getColor(R.color.black), clickedBtnTag.get(AppConstant.SELECTED));
		current = currentCustom;
		listviewHomework.setAdapter(null);


		
	}
	
	private class AssessmentAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listAssessmentHomework.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listAssessmentHomework.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		public void clearList() {
			listAssessmentHomework.clear();
		}
		
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolderAssessment holder;
			if (convertView == null) {
				holder = new ViewHolderAssessment();

				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_assessment_homework, parent, false);

				holder.txtPosition = (TextView)convertView.findViewById(R.id.txtPosition);
				holder.txtName = (TextView)convertView.findViewById(R.id.txtName);
				holder.txtStartDate = (TextView)convertView.findViewById(R.id.txtStartDate);
				holder.txtEndDate = (TextView)convertView.findViewById(R.id.txtEndDate);
				holder.txtMaximumTime = (TextView)convertView.findViewById(R.id.txtMaximumTime);
				holder.txtPassPercentage = (TextView)convertView.findViewById(R.id.txtPassPercentage);
				holder.btnPlay = (Button)convertView.findViewById(R.id.btnPlay);
				
				convertView.setTag(holder);

			} else {
				holder = (ViewHolderAssessment)convertView.getTag();
			}
			
			holder.txtPosition.setText(String.valueOf(position+1)+". ");
			
			holder.txtName.setText(listAssessmentHomework.get(position).getName());
			holder.txtStartDate.setText("Start Date: "+listAssessmentHomework.get(position).getStartDate());
			holder.txtEndDate.setText("Due Date: "+listAssessmentHomework.get(position).getEndDate());
			holder.txtMaximumTime.setText("Maximum Time: "+listAssessmentHomework.get(position).getMaximumTime());
			holder.txtPassPercentage.setText("Pass Percentage: "+listAssessmentHomework.get(position).getPassPercentage());
			holder.btnPlay.setTag(position);
			
			if(listAssessmentHomework.get(position).getTimeover() == 0 && listAssessmentHomework.get(position).getExamGiven() == 0)
			{
				holder.btnPlay.setText("Play");
				
				holder.btnPlay.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Button btn = ((Button)v);
						
						Intent intent = new Intent(getActivity(), AssesmentHomeworkActivity.class);
						intent.putExtra("ASSESSMENT_HOMEWORK_ID", listAssessmentHomework.get((Integer) btn.getTag()).getId());
						startActivity(intent);
						
					}
				});
				
			}
			
			if(listAssessmentHomework.get(position).getExamGiven() == 1)
			{
				holder.btnPlay.setText("Result");
				
				holder.btnPlay.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Log.e("CCC", "clicked from result");
						Button btn = ((Button)v);
						
						initApiCallAssessmentResult(listAssessmentHomework.get((Integer) btn.getTag()).getId());
						
					}
				});
			}
			if(listAssessmentHomework.get(position).getTimeover() == 1 && listAssessmentHomework.get(position).getExamGiven() == 0)
			{
				holder.btnPlay.setText("Time Over");
			}
			
			
			
			
			
			
			return convertView;
		}
		
		
	}
	
	
	private void showCustomDialogHomeworkAssessmentOk(String headerText, String nameText, String subjectNameText, String studentCountText, String maxScoreText, String minScoreText, String totalMarkText, String totalTimeTakenText, String isPassedText, String totalScoreText, int iconResId,
			Context context) {

			PopupDialogHomeworkAssessmentResult picker = PopupDialogHomeworkAssessmentResult.newInstance(0);
			//picker.setData(headerText, nameText, totalMarkText, isPassedText, totalScoreText, iconResId, context, new PopupDialogHomeworkAssessmentResult.IOkButtonClick(){

			picker.setData(headerText, nameText, subjectNameText, studentCountText, maxScoreText, minScoreText, totalMarkText, totalTimeTakenText, isPassedText, totalScoreText, iconResId, context, new PopupDialogHomeworkAssessmentResult.IOkButtonClick(){
			
				@Override
				public void onOkButtonClick() {
					// TODO Auto-generated method stub
					
				}});
			
			
			
			picker.show(getActivity().getSupportFragmentManager(), null);
	}
	
	
	
	
	private void initializePageing() {
		pageNumber = 1;
		isRefreshing = false;
		loading = false;
		stopLoadingData = false;
	}
	
	private void setUpList() {

		initializePageing();
		listViewAssessment.setMode(Mode.PULL_FROM_END);
		// Set a listener to be invoked when the list should be refreshed.
		listViewAssessment.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				Mode m = listViewAssessment.getCurrentMode();
				if (m == Mode.PULL_FROM_START) {
					stopLoadingData = false;
					isRefreshing = true;
					pageNumber = 1;
					loading = true;
					/*processFetchPost(URLHelper.URL_FREE_VERSION_CATEGORY,
							currentCategoryId);*/
					initApiCallAssessment(pageNumber);
				} else if (!stopLoadingData) {
					pageNumber++;
					loading = true;
					/*processFetchPost(URLHelper.URL_FREE_VERSION_CATEGORY,
							currentCategoryId);*/
					initApiCallAssessment(pageNumber);
					
				} else {
					new NoDataTaskAssessment().execute();
				}
			}
		});

		
		
		this.assessmentAdapter = new AssessmentAdapter();
		this.assessmentAdapter.clearList();
		this.listViewAssessment.setAdapter(assessmentAdapter);
	}
	
	
	
	private class NoDataTaskAssessment extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {

			assessmentAdapter.notifyDataSetChanged();

			// Call onRefreshComplete when the list has been refreshed.
			listViewAssessment.onRefreshComplete();

			super.onPostExecute(result);
		}
	}
	
	class ViewHolderAssessment {
		
		TextView txtPosition;
		TextView txtName;
		TextView txtStartDate;
		TextView txtEndDate;
		TextView txtMaximumTime;
		TextView txtPassPercentage;
		Button btnPlay;
	}
	
	
	
	
	

	@Override
	public void onAuthenticationStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAuthenticationSuccessful() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAuthenticationFailed(String msg) {
		// TODO Auto-generated method stub

	}


	@Override
	public void onPaswordChanged() {
		// TODO Auto-generated method stub
		
	}
}

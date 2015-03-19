/**
 * 
 */
package com.champs21.schoolapp.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.champs21.freeversion.SingleCalendarEvent;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.adapters.AcademicCalendarListAdapter;
import com.champs21.schoolapp.model.AcademicCalendarDataItem;
import com.champs21.schoolapp.model.UserAuthListener;
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
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 *
 */
public class AcademicCalendarHolidays extends UserVisibleHintFragment implements UserAuthListener{
	
	
	private View view;
	private ListView examList;
	private UserHelper userHelper;
	private UIHelper uiHelper;
	private List<AcademicCalendarDataItem> items;
	private Context con;
	private AcademicCalendarListAdapter adapter;
	private TextView gridTitleText;
	private LinearLayout pbs;
	private static final String TAG="Academic Calendar holidays";
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.e("HAAHHAHA", "Oncreate!!!");
		init();
	}

	

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		view = inflater.inflate(R.layout.fragment_academic_calendar_exam, container, false);
		examList = (ListView) view.findViewById(R.id.exam_listview);
		gridTitleText=(TextView)view.findViewById(R.id.grid_title_textview);
		gridTitleText.setText(getString(R.string.title_holidays_calendar_tab));
		pbs = (LinearLayout)view.findViewById(R.id.pb);
		setUpList();
		loadDataInToList();
		return view;
	}
	
	private void loadDataInToList() {
		
		if(AppUtility.isInternetConnected())
		{
			fetchDataFromServer();
		}
		else
			uiHelper.showMessage(con.getString(R.string.internet_error_text));
		
	}

	private void fetchDataFromServer() {
		
		RequestParams params=new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET,UserHelper.getUserSecret());
		params.put(RequestKeyHelper.SCHOOL,userHelper.getUser().getPaidInfo().getSchoolId());
		params.put(RequestKeyHelper.PAGE_NUMBER,"1");
		params.put(RequestKeyHelper.PAGE_SIZE, "500");
		params.put(RequestKeyHelper.ORIGIN, "2");
		//params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getPaidInfo().getBatchId());
		if(userHelper.getUser().getType()==UserTypeEnum.PARENTS){
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
//			/params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
		}else {
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser()
					.getPaidInfo().getBatchId());
		}
		
		//params.put("category", pageSize+"");
		
		AppRestClient.post(URLHelper.URL_GET_ACADEMIC_CALENDAR_EVENTS, params, getAcademicEventsHandler);
		
		
	}
	
	AsyncHttpResponseHandler getAcademicEventsHandler=new AsyncHttpResponseHandler()
	{
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			pbs.setVisibility(View.GONE);
		}

		@Override
		public void onStart() {
			super.onStart();
			/*if(uiHelper.isDialogActive())
				uiHelper.updateLoadingDialog(getString(R.string.loading_text));
			else
				uiHelper.showLoadingDialog(getString(R.string.loading_text));*/
			pbs.setVisibility(View.VISIBLE);
			
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);
//			uiHelper.dismissLoadingDialog();
			pbs.setVisibility(View.GONE);
			Wrapper wrapper=GsonParser.getInstance().parseServerResponse(responseString);
			if(wrapper.getStatus().getCode()==AppConstant.RESPONSE_CODE_SUCCESS)
			{
				items.clear();
				items.addAll(GsonParser.getInstance().parseAcademicCalendarData(wrapper.getData().getAsJsonArray("events").toString()));
				adapter.notifyDataSetChanged();
			}
			else if(wrapper.getStatus().getCode()==AppConstant.RESPONSE_CODE_SESSION_EXPIRED)
			{
				//userHelper.doLogIn();
			}
			Log.e("Events", responseString);
			
			initListActionClick();
		}
		
	};
	
	
	private void initListActionClick()
	{
		examList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				AcademicCalendarDataItem data = (AcademicCalendarDataItem)adapter.getItem(position);
				Intent intent = new Intent(getActivity(), SingleCalendarEvent.class);
				intent.putExtra(AppConstant.ID_SINGLE_CALENDAR_EVENT, data.getEventId());
				startActivity(intent);
			}
		});
	}

	private void setUpList() {
		adapter=new AcademicCalendarListAdapter(con, items);
		examList.setAdapter(adapter);
		
	}

	private void init() {
		con=getActivity();
		items=new ArrayList<AcademicCalendarDataItem>();
		uiHelper=new UIHelper(getActivity());
		userHelper=new UserHelper(this, con);
		
	}

	@Override
	public void onAuthenticationStart() {
		
		if(uiHelper.isDialogActive())
			uiHelper.updateLoadingDialog(getString(R.string.authenticating_text));
		else
			uiHelper.showLoadingDialog(getString(R.string.authenticating_text));
		
	}

	@Override
	public void onAuthenticationSuccessful() {
		fetchDataFromServer();
	}

	@Override
	public void onAuthenticationFailed(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onVisible() {
		fetchDataFromServer();
	}

	@Override
	protected void onInvisible() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPaswordChanged() {
		// TODO Auto-generated method stub
		
	}

	
}
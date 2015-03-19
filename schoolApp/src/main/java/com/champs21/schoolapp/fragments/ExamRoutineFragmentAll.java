/**
 * 
 */
package com.champs21.schoolapp.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.champs21.freeversion.SingleCalendarEvent;
import com.champs21.freeversion.SingleExamRoutine;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.adapters.AcademicCalendarListAdapter;
import com.champs21.schoolapp.adapters.ExamRoutineListAdapter;
import com.champs21.schoolapp.model.AcademicCalendarDataItem;
import com.champs21.schoolapp.model.ExamRoutine;
import com.champs21.schoolapp.model.UserAuthListener;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.AppUtility;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserAccessType;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 *
 */
public class ExamRoutineFragmentAll extends UserVisibleHintFragment implements
		UserAuthListener {

	private View view;
	private ListView examList;
	private UserHelper userHelper;
	private UIHelper uiHelper;
	private List<ExamRoutine> items;
	private Context con;
	private ExamRoutineListAdapter adapter;
	private TextView gridTitleText;
	private LinearLayout pbs;
	private static final String TAG = "Exam Routine";

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
		view = inflater.inflate(R.layout.fragment_academic_calendar_exam,
				container, false);
		((LinearLayout) view.findViewById(R.id.top_panel_for_others))
				.setVisibility(View.VISIBLE);
		examList = (ListView) view.findViewById(R.id.exam_listview);
		gridTitleText = (TextView) view.findViewById(R.id.grid_title_textview);
		gridTitleText.setText("Exam");
		pbs = (LinearLayout) view.findViewById(R.id.pb);
		setUpList();
		loadDataInToList();
		return view;
	}

	private void loadDataInToList() {

		if (AppUtility.isInternetConnected()) {
			fetchDataFromServer();
		} else
			uiHelper.showMessage(con.getString(R.string.internet_error_text));

	}

	private void fetchDataFromServer() {

		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) {
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser()
					.getPaidInfo().getSchoolId());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser()
					.getSelectedChild().getProfileId());
			/*Log.e("SCHOOL_BATCH_ID_STUDENT_ID", userHelper.getUser()
					.getPaidInfo().getSchoolId()
					+ " "
					+ userHelper.getUser().getPaidInfo().getBatchId()
					+ " "
					+ userHelper.getUser().getSelectedChild().getProfileId());*/
		}

		/*
		 * params.put(RequestKeyHelper.PAGE_NUMBER,"1");
		 * params.put(RequestKeyHelper.PAGE_SIZE, "500");
		 * params.put(RequestKeyHelper.ORIGIN, "1");
		 */

		// params.put(RequestKeyHelper.BATCH_ID,
		// userHelper.getUser().getPaidInfo().getBatchId());

		// params.put("category", pageSize+"");

		AppRestClient.post(URLHelper.URL_GET_EXAM_ROUTINE_STUDENT_PARENT,
				params, getAcademicEventsHandler);

	}

	AsyncHttpResponseHandler getAcademicEventsHandler = new AsyncHttpResponseHandler() {
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			pbs.setVisibility(View.GONE);
		}

		@Override
		public void onStart() {
			super.onStart();
			/*
			 * if(uiHelper.isDialogActive())
			 * uiHelper.updateLoadingDialog(getString(R.string.loading_text));
			 * else
			 * uiHelper.showLoadingDialog(getString(R.string.loading_text));
			 */
			pbs.setVisibility(View.VISIBLE);

		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);
			// uiHelper.dismissLoadingDialog();
			pbs.setVisibility(View.GONE);
			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					responseString);
			if (wrapper.getStatus().getCode() == AppConstant.RESPONSE_CODE_SUCCESS) {
				items.clear();
				items.addAll(GsonParser.getInstance()
						.parseExamRoutine(
								wrapper.getData().getAsJsonArray("all_exam")
										.toString()));
				adapter.notifyDataSetChanged();
			} else if (wrapper.getStatus().getCode() == AppConstant.RESPONSE_CODE_SESSION_EXPIRED) {
				// userHelper.doLogIn();
			}
			Log.e("Events", responseString);

			initListActionClick();

		}

	};

	private void initListActionClick() {
		examList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				ExamRoutine data = (ExamRoutine) adapter.getItem(position);
				Intent intent = new Intent(getActivity(),
						SingleExamRoutine.class);
				intent.putExtra(AppConstant.ID_SINGLE_CALENDAR_EVENT,
						data.getId());
				intent.putExtra("term_name", data.getName());
				startActivity(intent);
			}
		});
	}

	private void setUpList() {
		adapter = new ExamRoutineListAdapter(con, items);
		examList.setAdapter(adapter);
	}

	private void init() {
		con = getActivity();
		items = new ArrayList<ExamRoutine>();
		uiHelper = new UIHelper(getActivity());
		userHelper = new UserHelper(this, con);

	}

	@Override
	public void onAuthenticationStart() {

		if (uiHelper.isDialogActive())
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
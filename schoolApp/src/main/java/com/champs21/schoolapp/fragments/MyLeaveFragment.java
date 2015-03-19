package com.champs21.schoolapp.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.fragments.StudentLeaveFragment.StudentLeaveListAdapter;
import com.champs21.schoolapp.fragments.StudentLeaveFragment.StudentLeaveListAdapter.ViewHolder;
import com.champs21.schoolapp.model.BaseType;
import com.champs21.schoolapp.model.Batch;
import com.champs21.schoolapp.model.StudentAttendance;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.model.Picker.PickerItemSelectedListener;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppUtility;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class MyLeaveFragment extends  UserVisibleHintFragment{


	
	private View rootView;
	private ListView studentListView;
	private LinearLayout pbLayout;
	private ArrayList<StudentAttendance> arraylist = new ArrayList<StudentAttendance>();
	private StudentLeaveListAdapter adapter;
	
	
	private UserHelper userHelper;
	
	private TextView txtDate;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//fetchData();
		userHelper = new UserHelper(getActivity());
	}

	private void fetchData() {

		RequestParams params = new RequestParams();
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
			AppRestClient.post(URLHelper.URL_GET_PARENT_LEAVE_LIST, params,
					getStudentHandler);
		}
		else
		{
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			AppRestClient.post(URLHelper.URL_GET_TEACHER_LEAVE_LIST, params,
					getStudentHandler);
		}
		
		
		

	}

	AsyncHttpResponseHandler getStudentHandler = new AsyncHttpResponseHandler() {
		public void onFailure(Throwable arg0, String arg1) {
			pbLayout.setVisibility(View.GONE);
			Log.e("error", arg1);
		};

		public void onStart() {
			pbLayout.setVisibility(View.VISIBLE);
			arraylist.clear();
			if(adapter!=null)
			adapter.notifyDataSetChanged();
		};

		public void onSuccess(int arg0, String responseString) {
			arraylist.clear();

			pbLayout.setVisibility(View.GONE);
			Log.e("Menu", responseString);
			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					responseString);
			arraylist.addAll(GsonParser.getInstance().parseStudentList(
					(wrapper.getData().get("leaves")).toString()));
			adapter = new StudentLeaveListAdapter(getActivity(), arraylist);
			studentListView.setAdapter(adapter);
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_leave_approval_layout, container,
				false);
		studentListView = (ListView) rootView.findViewById(R.id.listview);
		pbLayout = (LinearLayout) rootView.findViewById(R.id.pb);
		// Capture Text in EditText
		
		txtDate = (TextView)rootView.findViewById(R.id.date_text);
		Date cDate = new Date();
		String fDate = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
		txtDate.setText(AppUtility.getDateString(fDate, AppUtility.DATE_FORMAT_APP, AppUtility.DATE_FORMAT_SERVER));
		
		return rootView;
	}
	
	
	private Batch selectedBatch;
	PickerItemSelectedListener PickerCallback = new PickerItemSelectedListener() {

		@Override
		public void onPickerItemSelected(BaseType item) {

			switch (item.getType()) {
			case TEACHER_BATCH:
				selectedBatch=(Batch)item;
				fetchData();
				break;
			
			default:
				break;
			}

		}
	};
	@Override
	protected void onVisible() {
		fetchData();
	}

	@Override
	protected void onInvisible() {
		// TODO Auto-generated method stub
		
	}
	
	
	public class StudentLeaveListAdapter extends BaseAdapter {

		// Declare Variables
		Context mContext;
		LayoutInflater inflater;
		private List<StudentAttendance> studentlist = null;
		private ArrayList<StudentAttendance> arraylist;

		public StudentLeaveListAdapter(Context context, List<StudentAttendance> studentlist) {
			mContext = context;
			this.studentlist = studentlist;
			inflater = LayoutInflater.from(mContext);
			this.arraylist = new ArrayList<StudentAttendance>();
			this.arraylist.addAll(studentlist);
		}

		public class ViewHolder {
			TextView namebatch,applydate,startend,status, txtLeaveSubject;
			
		}

		@Override
		public int getCount() {
			return studentlist.size();
		}

		@Override
		public StudentAttendance getItem(int position) {
			return studentlist.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View view, ViewGroup parent) {
			final ViewHolder holder;
			if (view == null) {
				holder = new ViewHolder();
				view = inflater.inflate(R.layout.item_leave_teacher, null);
				// Locate the TextViews in listview_item.xml
				holder.namebatch = (TextView) view.findViewById(R.id.tv_name_and_batch);
				holder.applydate = (TextView) view.findViewById(R.id.tv_apply_date);
				holder.startend = (TextView) view.findViewById(R.id.tv_start_date_and_end_date);
				holder.status = (TextView)view.findViewById(R.id.tv_leave_status);
				holder.txtLeaveSubject = (TextView)view.findViewById(R.id.txtLeaveSubject);
				
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			// Set the results into TextViews
			//holder.population.setText(worldpopulationlist.get(position).getPopulation());
			if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
			{
				holder.txtLeaveSubject.setVisibility(View.VISIBLE);
				holder.namebatch.setVisibility(View.GONE);
			}
			else
			{
				holder.txtLeaveSubject.setVisibility(View.GONE);
				holder.namebatch.setVisibility(View.VISIBLE);
			}
			
			
			
			holder.txtLeaveSubject.setText(studentlist.get(position).getLeave_subject());
			
			holder.namebatch.setText(studentlist.get(position).getLeave_type());
			holder.applydate.setText("Apply Date: "+studentlist.get(position).getCreateDate());
			// Listen for ListView Item Click
			holder.startend.setText("Duration: "+studentlist.get(position).getLeaveStartDate()+" to "+studentlist.get(position).getLeaveEndDate());
			switch (studentlist.get(position).getTeacherLeaveStatus()) {
			case 0:
				holder.status.setText("declined");
				holder.status.setTextColor(getResources().getColor(R.color.white));
				holder.status.setBackgroundColor(getActivity().getResources().getColor(R.color.red));
				break;
			case 1:
				holder.status.setTextColor(getResources().getColor(R.color.white));
				holder.status.setText("accepted");
				holder.status.setBackgroundColor(getActivity().getResources().getColor(R.color.accepted_color));
				break;
			case 2:
				holder.status.setTextColor(getResources().getColor(R.color.black));
				holder.status.setText("pending");
				holder.status.setBackgroundColor(getActivity().getResources().getColor(R.color.gray_2));
				break;
			default:
				break;
			}
			return view;
		}

		

	}

}

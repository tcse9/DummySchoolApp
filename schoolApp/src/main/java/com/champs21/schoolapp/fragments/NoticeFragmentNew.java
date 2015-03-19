package com.champs21.schoolapp.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.champs21.freeversion.SingleNoticeActivity;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.model.MeetingStatus;
import com.champs21.schoolapp.model.ModelContainer;
import com.champs21.schoolapp.model.Notice;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.MyTagHandler;
import com.champs21.schoolapp.utils.ReminderHelper;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.viewhelpers.CustomButton;
import com.champs21.schoolapp.viewhelpers.CustomTabButton;
import com.champs21.schoolapp.viewhelpers.ExpandableTextView;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class NoticeFragmentNew extends Fragment implements View.OnClickListener{
	
	
	private View view;
	private CustomTabButton currentButton;
	
	private CustomTabButton btnNoticeGeneral;
	private CustomTabButton btnNoticeCircular;
	private CustomTabButton btnNoticeAnnouncement;
	
	private List<CustomTabButton> listBtn;
	
	private UIHelper uiHelper;
	private UserHelper userHelper;
	
	private PullToRefreshListView listViewNotice;
	
	private List<Notice> listNotice;
	
	private NoticeAdapter adapter;
	
	
	private boolean hasNext = false;
	private int pageNumber = 1;
	private boolean isRefreshing = false;
	private boolean loading = false;
	private boolean stopLoadingData = false;
	private String noticeType = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		listBtn = new ArrayList<CustomTabButton>();
		listNotice = new ArrayList<Notice>();
		
		
		uiHelper = new UIHelper(getActivity());
		userHelper = new UserHelper(getActivity());
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.fragment_notice_new, container, false);
		initView(view);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		initApiCall(pageNumber, noticeType);
	}
	
	
	private void initView(View view)
	{
		btnNoticeGeneral = (CustomTabButton)view.findViewById(R.id.btnNoticeGeneral);
		btnNoticeCircular = (CustomTabButton)view.findViewById(R.id.btnNoticeCircular);
		btnNoticeAnnouncement = (CustomTabButton)view.findViewById(R.id.btnNoticeAnnouncement);
		listBtn.add(btnNoticeGeneral);
		listBtn.add(btnNoticeCircular);
		listBtn.add(btnNoticeAnnouncement);
		
		
		
		currentButton = btnNoticeGeneral;
		noticeType = "1";
		btnNoticeGeneral.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.eye_black);
		
		btnNoticeGeneral.setOnClickListener(this);
		btnNoticeCircular.setOnClickListener(this);
		btnNoticeAnnouncement.setOnClickListener(this);
		
		listViewNotice = (PullToRefreshListView)view.findViewById(R.id.listViewNotice);
		setUpList();
		
		listViewNotice.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Notice data = (Notice) adapter.getItem(position-1);
				Log.e("NOTICE_ID", "id: "+data.getNoticeId());
				Intent intent = new Intent(getActivity(), SingleNoticeActivity.class);
				intent.putExtra(AppConstant.ID_SINGLE_NOTICE, data.getNoticeId());
				startActivityForResult(intent, 65);
			}
		});
		
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 65)
		{
			initializePageing();
			initApiCall(pageNumber, noticeType);
		}
	}
	
	private void initializePageing() {
		pageNumber = 1;
		isRefreshing = false;
		loading = false;
		stopLoadingData = false;
	}
	
	private void setUpList() {

		initializePageing();
		listViewNotice.setMode(Mode.PULL_FROM_END);
		// Set a listener to be invoked when the list should be refreshed.
		listViewNotice.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getActivity(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME
								| DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);

				Mode m = listViewNotice.getCurrentMode();
				if (m == Mode.PULL_FROM_START) {
					stopLoadingData = false;
					isRefreshing = true;
					pageNumber = 1;
					loading = true;
					/*processFetchPost(URLHelper.URL_FREE_VERSION_CATEGORY,
							currentCategoryId);*/
					initApiCall(pageNumber, noticeType);
				} else if (!stopLoadingData) {
					pageNumber++;
					loading = true;
					/*processFetchPost(URLHelper.URL_FREE_VERSION_CATEGORY,
							currentCategoryId);*/
					initApiCall(pageNumber, noticeType);
					
				} else {
					new NoDataTask().execute();
				}
			}
		});

		
		
		adapter = new NoticeAdapter();
		adapter.clearList();
		listViewNotice.setAdapter(adapter);
	}
	
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

			adapter.notifyDataSetChanged();

			// Call onRefreshComplete when the list has been refreshed.
			listViewNotice.onRefreshComplete();

			super.onPostExecute(result);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnNoticeGeneral:
			currentButton = btnNoticeGeneral;
			btnNoticeGeneral.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.eye_black);
			noticeType = "1";
			updateButtonUi();
			
			initializePageing();
			initApiCall(pageNumber, noticeType);
			
			break;
			
		case R.id.btnNoticeCircular:
			currentButton = btnNoticeCircular;
			btnNoticeCircular.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.circular_black);
			noticeType = "2";
			updateButtonUi();
			
			initializePageing();
			initApiCall(pageNumber, noticeType);
			
			break;
			
		case R.id.btnNoticeAnnouncement:
			currentButton = btnNoticeAnnouncement;
			btnNoticeAnnouncement.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.annaouncment_black);
			noticeType = "3";
			updateButtonUi();
			
			initializePageing();
			initApiCall(pageNumber, noticeType);
			
			break;

		default:
			break;
		}
	}
	
	private void updateButtonUi()
	{
		for(CustomTabButton btn : listBtn)
		{
			if(!btn.equals(currentButton))
			{
				if(btn.equals(btnNoticeGeneral))
				{
					btn.setButtonSelected(false, getResources().getColor(R.color.black), R.drawable.eye_gray);
				}
				if(btn.equals(btnNoticeCircular))
				{
					btn.setButtonSelected(false, getResources().getColor(R.color.black), R.drawable.circular_gray);
				}
				if(btn.equals(btnNoticeAnnouncement))
				{
					btn.setButtonSelected(false, getResources().getColor(R.color.black), R.drawable.annaouncment_gray);
				}
			}
			
		}
	}
	
	
	private void initApiCall(int pageNumber, String noticeType) {

		RequestParams params = new RequestParams();

		params.put("page_size", "10");
		params.put("page_number", String.valueOf(pageNumber));
		
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put("notice_type", noticeType);
			
		

		AppRestClient.post(URLHelper.URL_GET_NOTICE, params,
				noticeHandler);
	}
	
	AsyncHttpResponseHandler noticeHandler = new AsyncHttpResponseHandler() {

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
			
			hasNext = modelContainer.getData().get("has_next").getAsBoolean();
			Log.e("HAS_NEXT_MEETING", "is: "+hasNext);
			
			
			if (pageNumber == 1)
			{
				adapter.clearList();
			}
				
			if (!hasNext) 
			{
				stopLoadingData = true;
			}

			if (modelContainer.getStatus().getCode() == 200) {
				
				JsonArray array = modelContainer.getData().get("notice").getAsJsonArray();
				
				
				for (int i = 0; i < array.size(); i++) 
				{
					listNotice.add(parseNotice(array.toString()).get(i));
				}
				
				if (pageNumber != 0 || isRefreshing) 
				{
					listViewNotice.onRefreshComplete();
					loading = false;
				}
				
				Log.e("listnotice size", "is: "+listNotice.size());
				
				adapter.notifyDataSetChanged();
				
			}
			
			else {

			}
			
			

		};
	};
	
	private CustomButton clickedAckBtn;
	private CustomButton clickedReminderBtn;
	private void requestAcknowledge(String tag, CustomButton btn) {
		// TODO Auto-generated method stub
		this.clickedAckBtn = btn;

		RequestParams params = new RequestParams();

		

		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put(RequestKeyHelper.NOTICE_ID, btn.getTag().toString());

		AppRestClient.post(URLHelper.URL_NOTICE_ACKNOWLEDGE, params,
				ackBtnHandler);
	}
	
	AsyncHttpResponseHandler ackBtnHandler = new AsyncHttpResponseHandler() {
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

			ModelContainer modelContainer = GsonParser.getInstance().parseGson(
					response);

			// arrangeHomeworkData(modelContainer);

			// adapter.notifyDataSetChanged();

			// Log.e("status code", modelContainer.getStatus().getCode() + "");
			if (modelContainer.getData().getNotice_ack()
					.getAcknowledge_status().equals("1")) {
				clickedAckBtn.setImage(R.drawable.done_tap);
				clickedAckBtn.setTitleColor(NoticeFragmentNew.this.getActivity()
						.getResources().getColor(R.color.maroon));
				clickedAckBtn.setTitleText("Acknowledged");
				clickedAckBtn.setEnabled(false);
			}

		};
	};
	
	private ArrayList<Notice> parseNotice(String object) {
		ArrayList<Notice> data = new ArrayList<Notice>();
		data = new Gson().fromJson(object,
				new TypeToken<ArrayList<Notice>>() {
				}.getType());
		return data;
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
	
	
	
	private class NoticeAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listNotice.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listNotice.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
		
		public void clearList() {
			listNotice.clear();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			
			ViewHolder holder;
			
			Log.e("calling", "1");
			
			if (convertView == null) {
				holder = new ViewHolder();

				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_notice_list, null, false);
				holder.tvTitle = (ExpandableTextView) convertView.findViewById(R.id.txt_notice_header);
				//holder.tvContent = (ExpandableTextView) convertView.findViewById(R.id.txt_notice_content);
				holder.tvDate = (TextView) convertView.findViewById(R.id.txt_notice_date);
				//holder.acknowledgeBtn = (CustomButton) convertView.findViewById(R.id.btn_notice_acknowledge);
				holder.reminderBtn = (CustomButton) convertView.findViewById(R.id.btn_notice_reminder);
				
				Log.e("calling", "2");
				
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			Log.e("calling", "3");
			
			holder.tvTitle.setText(" "+ listNotice.get(position).getNoticeTitle());

			//holder.tvContent.setText(" "+ Html.fromHtml(listNotice.get(position).getNoticeContent(), null, new MyTagHandler()));
			holder.tvDate.setText(" "+ listNotice.get(position).getPublishedAt());
			//holder.acknowledgeBtn.setTag(listNotice.get(position).getNoticeId());
			holder.reminderBtn.setTag(listNotice.get(position));
			
			//holder.acknowledgeBtn.setOnClickListener(NoticeFragmentNew.this);
			//holder.reminderBtn.setOnClickListener(NoticeFragmentNew.this);
			
			
			
			/*if (listNotice.get(position).getAllAck().size() != 0) {
				if (listNotice.get(position).getAllAck().get(0).getAcknowledge_status()
						.equals("1")) {

					setButtonState(holder.acknowledgeBtn, R.drawable.done_tap, false, "Acknowledged");
					
				} else if (listNotice.get(position).getAllAck().get(0).getAcknowledge_status()
						.equals("0")) {
					setButtonState(holder.acknowledgeBtn, R.drawable.done_normal, true, "Acknowledge");
				}
			} else {
				setButtonState(holder.acknowledgeBtn, R.drawable.done_normal, false, "Acknowledge");
				holder.acknowledgeBtn.setTitleColor(NoticeFragmentNew.this
						.getActivity().getResources().getColor(R.color.gray_1));
			}*/
			Log.e("NOTICE PUBLISH", listNotice.get(position).getPublishedAt()+"");
			if (ReminderHelper.getInstance().reminder_map.containsKey(listNotice.get(position).getPublishedAt())){
				setButtonState(holder.reminderBtn, R.drawable.btn_reminder_tap, false, "Reminder");
				
			}else {
				setButtonState(holder.reminderBtn, R.drawable.btn_reminder_normal, true, "Reminder");
			}
			
			
			/*holder.acknowledgeBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					requestAcknowledge(v.getTag().toString(), (CustomButton) v);
				}
			});*/
			
			holder.reminderBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CustomButton reminderBtn = (CustomButton) v;
					Notice rmNotice = (Notice) reminderBtn.getTag();
					reminderBtn.setImage(R.drawable.btn_reminder_tap);
					reminderBtn.setTitleColor(NoticeFragmentNew.this.getActivity()
							.getResources().getColor(R.color.maroon));
					reminderBtn.setEnabled(false);
					String content = ""+Html.fromHtml(rmNotice.getNoticeContent());
					ReminderHelper.getInstance().setReminder(rmNotice.getPublishedAt(),
							rmNotice.getNoticeTitle(), content,
							rmNotice.getPublishedAt(), getActivity());
				}
			});
			
			
			
			Log.e("calling", "4");
			return convertView;
		}
		
		
		
	}
	

	class ViewHolder {
		ExpandableTextView tvTitle;
		//ExpandableTextView tvContent;
		TextView tvDate;
		CustomButton acknowledgeBtn;
		CustomButton reminderBtn;
	}

}

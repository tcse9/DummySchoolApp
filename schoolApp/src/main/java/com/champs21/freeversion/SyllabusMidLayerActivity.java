package com.champs21.freeversion;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.model.Syllabus;
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

public class SyllabusMidLayerActivity extends ChildContainerActivity{
	
	
	private UIHelper uiHelper;
	private UserHelper userHelper;
	
	private ListView listViewSyllabus;
	
	private String termId = "";
	private ArrayList<Syllabus> syllabusList;
	
	private EfficientAdapter adapter;
	
	private TextView txtMessage;
	
	private String selectedBatchId = "";

	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
        homeBtn.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
	}
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_syllabus_new);
		
		syllabusList = new ArrayList<Syllabus>();
		
		uiHelper = new UIHelper(this);
		userHelper = new UserHelper(this);
		
		if(getIntent().getExtras() != null)
		{
			termId = getIntent().getExtras().getString(AppConstant.ID_TERM);
			
			if(userHelper.getUser().getType() == UserTypeEnum.TEACHER)
			{
				
				selectedBatchId = getIntent().getExtras().getString(AppConstant.ID_BATCH);
			}
		}
		
		initView();
		initApiCall(termId);
		
	}
	
	
	
	private void initView()
	{
		txtMessage = (TextView)this.findViewById(R.id.txtMessage);
		
		listViewSyllabus= (ListView)this.findViewById(R.id.listViewSyllabus);
		adapter = new EfficientAdapter(this);
		listViewSyllabus.setAdapter(adapter);
	}
	
	
	private void initAction()
	{
		listViewSyllabus.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Syllabus data = (Syllabus)adapter.getItem(position);
				
				Intent intent = new Intent(SyllabusMidLayerActivity.this, SingleSyllabus.class);
				intent.putExtra(AppConstant.ID_SINGLE_SYLLABUS, data.getId());
				startActivity(intent);
			}
		});
	}
	
	private void initApiCall(String termId)
	{
		RequestParams params = new RequestParams();
		
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser().getPaidInfo().getSchoolId());
			params.put(RequestKeyHelper.TERM_ID, termId);
		}
		
		if(userHelper.getUser().getType() == UserTypeEnum.STUDENT)
		{
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getPaidInfo().getBatchId());
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser().getPaidInfo().getSchoolId());
			params.put(RequestKeyHelper.TERM_ID, termId);
		}
		
		if(userHelper.getUser().getType() == UserTypeEnum.TEACHER)
		{
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			params.put(RequestKeyHelper.BATCH_ID, selectedBatchId);
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser().getPaidInfo().getSchoolId());
			params.put(RequestKeyHelper.TERM_ID, termId);
		}
		
		
		
		
		
		AppRestClient.post(URLHelper.URL_SYLLABUS, params, singleTermHandler);
	}
	
	
	AsyncHttpResponseHandler singleTermHandler = new AsyncHttpResponseHandler() {

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
			super.onSuccess(arg0, responseString);
			// uiHelper.dismissLoadingDialog();
			uiHelper.dismissLoadingDialog();
			Log.e("SINGLE TERM RESPONSE", responseString);
			
			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);

			if (modelContainer.getStatus().getCode() == 200) {
				
				syllabusList = GsonParser.getInstance().parseTermSyllabus(
						modelContainer.getData().getAsJsonArray("syllabus")
								.toString());
				
				if(syllabusList.size() <= 0)
				{
					txtMessage.setVisibility(View.VISIBLE);
				}
				else
				{
					txtMessage.setVisibility(View.GONE);
				}
				
				adapter.notifyDataSetChanged();
				
				initAction();
				
			}
			
			else {

			}
			
		}
	};
	
	
	private class EfficientAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public EfficientAdapter(Context context) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);
		}

		/**
		 * The number of items in the list is determined by the number of
		 * speeches in our array.
		 * 
		 * @see android.widget.ListAdapter#getCount()
		 */
		public int getCount() {
			// return syllabusMap.get(currentTabKey).size();
			return syllabusList.size();
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
			return syllabusList.get(position);
		}

		/**
		 * Use the array index as a unique id.
		 * 
		 * @see android.widget.ListAdapter#getItemId(int)
		 */
		public long getItemId(int position) {
			return position;
			// dfsdf
		}

		/**
		 * Make a view to hold each row.
		 * 
		 * @see android.widget.ListAdapter#getView(int, android.view.View,
		 *      android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid
			// unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;
			Syllabus item = syllabusList.get(position);

			// When convertView is not null, we can reuse it directly, there is
			// no need
			// to reinflate it. We only inflate a new View when the convertView
			// supplied
			// by ListView is null.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.row_syllabus_list,
						null);

				// Creates a ViewHolder and store references to the two children
				// views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.tvTitle = (TextView) convertView
						.findViewById(R.id.txt_syllabus_header);
				/*
				 * holder.tvContent = (ExpandableTextView) convertView
				 * .findViewById(R.id.txt_syllabus_content);
				 */
				holder.tvDate = (TextView) convertView
						.findViewById(R.id.txt_syllabus_date);
				holder.subjectIcon = (ImageView) convertView
						.findViewById(R.id.iv_syllabus_subject_icon);

				convertView.setTag(holder);
			} else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			holder.tvTitle.setText(item.getSubject_name());
			holder.subjectIcon.setImageResource(AppUtility.getImageResourceId(item.getSubject_icon(), SyllabusMidLayerActivity.this));
			/*
			 * holder.tvContent.setText(" " +
			 * Html.fromHtml(item.getSyllabus_text(), null, new
			 * MyTagHandler()));
			 */
			holder.tvDate.setText(item.getLast_updated());
			return convertView;
		}

		class ViewHolder {
			TextView tvTitle;
			// ExpandableTextView tvContent;
			TextView tvDate;
			ImageView subjectIcon;
		}
	}


	

}

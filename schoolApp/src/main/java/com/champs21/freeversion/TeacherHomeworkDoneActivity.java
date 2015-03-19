package com.champs21.freeversion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.model.FeesDue;
import com.champs21.schoolapp.model.HomeWorkStatus;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
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

public class TeacherHomeworkDoneActivity extends ChildContainerActivity {
	
	
	private UIHelper uiHelper;
	private UserHelper userHelper;
	private String id;
	
	private ListView listView;
	
	private List<HomeWorkStatus> listStatus;
	private TeacherDoneAdapter adapter;
	
	private TextView txtMessage;
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        homeBtn.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teacherhomeworkdone);
		
		listStatus = new ArrayList<HomeWorkStatus>();
		
		uiHelper = new UIHelper(this);
		userHelper = new UserHelper(this);
		
		if(getIntent().getExtras() != null)
			this.id = getIntent().getExtras().getString(AppConstant.ID_TEACHER_HOMEWORK_DONE);
		
		initView();
		initApiCall();
	}
	
	
	private void initView()
	{
		listView = (ListView)this.findViewById(R.id.listView);
		adapter = new TeacherDoneAdapter();
		listView.setAdapter(adapter);
		
		txtMessage = (TextView)this.findViewById(R.id.txtMessage);
	}
	
	private void initApiCall()
	{

		RequestParams params = new RequestParams();

	
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put("assignment_id", this.id);
		
		AppRestClient.post(URLHelper.URL_HOMEWORK_STATUS, params, homeworkStatusHandler);
	
	}
	
	AsyncHttpResponseHandler homeworkStatusHandler = new AsyncHttpResponseHandler() {

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

			if (modelContainer.getStatus().getCode() == 200) {
				
				
				JsonArray  arrayStatus = modelContainer.getData().get("homework_status").getAsJsonArray();
				
				listStatus = parseHomeWorkStatus(arrayStatus.toString());
				
				adapter.notifyDataSetChanged();
				
				if(listStatus.size() <= 0)
				{
					txtMessage.setVisibility(View.VISIBLE);
				}
				else
				{
					txtMessage.setVisibility(View.GONE);
				}
				
			}
			
			else {

			}
			
			

		};
	};
	
	
	private List<HomeWorkStatus> parseHomeWorkStatus(String object) {

		List<HomeWorkStatus> tags = new ArrayList<HomeWorkStatus>();
		Type listType = new TypeToken<List<HomeWorkStatus>>() {
		}.getType();
		tags = (List<HomeWorkStatus>) new Gson().fromJson(object, listType);
		return tags;
	}
	
	
	
	private class TeacherDoneAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listStatus.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listStatus.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if(convertView == null)
			{
				holder = new ViewHolder();
                convertView = LayoutInflater.from(TeacherHomeworkDoneActivity.this).inflate(R.layout.row_teacherhomeworkdont,null,false);
                
                holder.txtName = (TextView)convertView.findViewById(R.id.txtName);
                holder.txtRoll = (TextView)convertView.findViewById(R.id.txtRoll);
                
                
                convertView.setTag(holder);
			}
			else
			{
				holder = (ViewHolder)convertView.getTag();
			}
			
			
			holder.txtName.setText(listStatus.get(position).getStudent_name());
			holder.txtRoll.setText(listStatus.get(position).getStudent_roll());
			
			return convertView;
		}
		
		
		
		class ViewHolder{
			
			TextView txtName;
			TextView txtRoll;
		}
		
		
		
	}
	

}

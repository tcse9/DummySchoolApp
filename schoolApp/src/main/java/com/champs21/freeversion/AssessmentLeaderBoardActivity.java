package com.champs21.freeversion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.model.AssessmentLeaderBoard;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.SchoolApp;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class AssessmentLeaderBoardActivity extends ChildContainerActivity implements View.OnClickListener{
	
	private UIHelper uiHelper;
	private UserHelper userHelper;
	
	private ListView listViewLeaderBoard;
	
	private List<AssessmentLeaderBoard> listLeaderBoard =  new ArrayList<AssessmentLeaderBoard>();
	
	private AssessmentLeaderBoardAdapter adapter;
	
	private String id = "";
	
	private Button btnQuizRules;
	
	private TextView txtMessage;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_assesment_leaderboard);
	
		id = getIntent().getStringExtra("assessment_id");
		
		userHelper = new UserHelper(this);
		uiHelper = new UIHelper(this);
		
		initView();
		
		
		initApiCall();
		
	}
	
	
	private void initView()
	{
		this.listViewLeaderBoard = (ListView)this.findViewById(R.id.listViewLeaderBoard);
		this.adapter = new AssessmentLeaderBoardAdapter();
		this.listViewLeaderBoard.setAdapter(this.adapter);
		
		this.btnQuizRules = (Button)this.findViewById(R.id.btnQuizRules);
		
		this.txtMessage = (TextView)this.findViewById(R.id.txtMessage);
	}
	
	private void initAction()
	{
		this.btnQuizRules.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(AssessmentLeaderBoardActivity.this, AssessmentRules.class);
				startActivity(intent);
			}
		});
	}
	
	
	private void initApiCall() 
	{

		RequestParams params = new RequestParams();
		params.put("id", id);
		

		AppRestClient.post(URLHelper.URL_ASSESSMENT_LEADERBOARD, params,
				assessmentHandlerLeaderBoard);
	}
	
	private AsyncHttpResponseHandler assessmentHandlerLeaderBoard = new AsyncHttpResponseHandler() {

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
			//Log.e("ASSESSMENT", "data: "+responseString);

			uiHelper.dismissLoadingDialog();

			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);

			if (modelContainer.getStatus().getCode() == 200) {
				
				JsonArray assessment = modelContainer.getData().get("assesment").getAsJsonArray();
				
				
				
				listLeaderBoard = parseAssessmentLeaderBoard(assessment.toString());
				
				if(listLeaderBoard.size() <=0 )
				{
					//Toast.makeText(AssessmentLeaderBoardActivity.this, "No leader board found!", Toast.LENGTH_SHORT).show();
					txtMessage.setVisibility(View.VISIBLE);
				}
				else
					txtMessage.setVisibility(View.GONE);
				
				
			}

			else {

			}
			
			initAction();
			
			adapter.notifyDataSetChanged();

		};

	};
	
	
	public List<AssessmentLeaderBoard> parseAssessmentLeaderBoard(String object) {

		List<AssessmentLeaderBoard> tags = new ArrayList<AssessmentLeaderBoard>();
		Type listType = new TypeToken<List<AssessmentLeaderBoard>>() {
		}.getType();
		tags = (List<AssessmentLeaderBoard>) new Gson().fromJson(object, listType);
		return tags;
	}
	
	
	private String convertSecondsToHMS(String seconds)
	{
		String result = "";
		
		int second = Integer.parseInt(seconds);
		
		int hr = (int)(second/3600);
		int rem = (int)(second%3600);
		int mn = rem/60;
		int sec = rem%60;
		String hrStr = (hr<10 ? "0" : "")+hr;
		String mnStr = (mn<10 ? "0" : "")+mn;
		String secStr = (sec<10 ? "0" : "")+sec; 
		
		result = hrStr+":"+mnStr+":"+secStr;
				
		return result;
	}
	
	
	
	public class AssessmentLeaderBoardAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listLeaderBoard.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listLeaderBoard.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public void clearList() {
			//listStatus.clear();
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();

				convertView = LayoutInflater.from(AssessmentLeaderBoardActivity.this).inflate(R.layout.row_assessment_leaderboard, parent, false);

				holder.progressBar = (ProgressBar)convertView.findViewById(R.id.progressBar);
				holder.imgView = (ImageView)convertView.findViewById(R.id.imgView);
				holder.txtPosition = (TextView)convertView.findViewById(R.id.txtPosition);
				holder.txtUserName = (TextView)convertView.findViewById(R.id.txtUserName);
				//holder.txtLeaderBoardTitle = (TextView)convertView.findViewById(R.id.txtLeaderBoardTitle);
				holder.txtMarks = (TextView)convertView.findViewById(R.id.txtMarks);
				holder.txtTime = (TextView)convertView.findViewById(R.id.txtTime);
				
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.txtPosition.setText(String.valueOf(position+1)+". ");
			SchoolApp.getInstance().displayUniversalImage(listLeaderBoard.get(position).getProfileImage(), holder.imgView , holder.progressBar);
			
			holder.txtUserName.setText(listLeaderBoard.get(position).getUserName());
			//holder.txtLeaderBoardTitle.setText("Title: "+listLeaderBoard.get(position).getTitle());
			holder.txtMarks.setText("Marks: "+listLeaderBoard.get(position).getMark());
			holder.txtTime.setText("Time: "+convertSecondsToHMS(listLeaderBoard.get(position).getTimeTaken()));
			
			return convertView;
		}

	}


	class ViewHolder {
		
		ProgressBar progressBar;
		ImageView imgView;
		TextView txtPosition;
		TextView txtUserName;
		//TextView txtLeaderBoardTitle;
		TextView txtMarks;
		TextView txtTime;
		
	}
	
	
}

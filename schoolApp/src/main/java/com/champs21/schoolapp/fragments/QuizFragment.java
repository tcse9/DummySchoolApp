package com.champs21.schoolapp.fragments;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import com.champs21.freeversion.AssesmentHomeworkActivity;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.fragments.HomeworkFragment.ViewHolderAssessment;
import com.champs21.schoolapp.model.AssessmentHomework;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.champs21.schoolapp.viewhelpers.PopupDialogHomeworkAssessmentResult;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class QuizFragment extends Fragment {
	
	private View view;
	private UIHelper uiHelper;
	private UserHelper userHelper;
	
	
	private boolean hasNextAssessment = false;
	private int pageNumber = 1;
	private boolean isRefreshing = false;
	private boolean loading = false;
	private boolean stopLoadingData = false;
	
	private List<AssessmentHomework> listAssessmentHomework;
	private PullToRefreshListView listViewAssessment;
	
	
	private AssessmentAdapter assessmentAdapter;
	
	private TextView txtNoData;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		uiHelper = new UIHelper(getActivity());
		userHelper = new UserHelper(getActivity());
		
		listAssessmentHomework = new ArrayList<AssessmentHomework>();
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.fragment_quiz, container, false);
		
		initView(view);
		initApiCallAssessment(pageNumber);
		
		return view;
		
		
		
	}
	
	
	
	private void initView(View view)
	{
		listViewAssessment = (PullToRefreshListView)view.findViewById(R.id.listViewAssessment);
		txtNoData = (TextView)view.findViewById(R.id.txtNoData);
		
		setUpList();
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
	
	private void initApiCallAssessment(int pageNumber)
	{
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}
		
		
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
		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
		}
		

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
				
				if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
				{
					holder.btnPlay.setText("Not Played");
				}
				
				holder.btnPlay.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Button btn = ((Button)v);
						
						if (userHelper.getUser().getType() != UserTypeEnum.PARENTS) 
						{
							Intent intent = new Intent(getActivity(), AssesmentHomeworkActivity.class);
							intent.putExtra("ASSESSMENT_HOMEWORK_ID", listAssessmentHomework.get((Integer) btn.getTag()).getId());
							startActivity(intent);
						}
						
						
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
		
		class ViewHolderAssessment {
			
			TextView txtPosition;
			TextView txtName;
			TextView txtStartDate;
			TextView txtEndDate;
			TextView txtMaximumTime;
			TextView txtPassPercentage;
			Button btnPlay;
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

}

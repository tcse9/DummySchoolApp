package com.champs21.freeversion;

import java.util.ArrayList;

import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.champs21.schoolapp.BatchSelectionChangedBroadcastReceiver;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.model.ReportCardModel;
import com.champs21.schoolapp.model.TermReportExamSubjectItem;
import com.champs21.schoolapp.model.TermReportItem;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.SchoolApp;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.champs21.schoolapp.viewhelpers.CustomButton;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SingleItemTermReportActivity extends ChildContainerActivity{
	private SchoolApp app;
	private UIHelper uiHelper;
	private LinearLayout layoutReport;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_term_report);
		init();
		fetchDataFromServer();
	}

	
	private void fetchDataFromServer() {
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put(RequestKeyHelper.SCHOOL_ID, getIntent().getExtras().getString("id"));
		if(userHelper.getUser().getType()==UserTypeEnum.PARENTS){
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
			params.put(RequestKeyHelper.STUDENT_ID, userHelper.getUser().getSelectedChild().getProfileId());
		}else if(userHelper.getUser().getType() == UserTypeEnum.TEACHER){
			params.put(RequestKeyHelper.BATCH_ID, getIntent().getExtras().getString("batch_id"));
			params.put(RequestKeyHelper.STUDENT_ID, getIntent().getExtras().getString("student_id"));
		}
		
		AppRestClient.post(URLHelper.URL_GET_SINGLE_TERM_REPORT, params, reportCardHandler);
	}

	AsyncHttpResponseHandler reportCardHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}
			//pbs.setVisibility(View.GONE);
		};

		@Override
		public void onStart() {
			uiHelper.showLoadingDialog("Please wait...");
			//pbs.setVisibility(View.VISIBLE);

		};

		@Override
		public void onSuccess(int arg0, String responseString) {
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}
			//pbs.setVisibility(View.GONE);

			//Toast.makeText(getActivity(), responseString, Toast.LENGTH_LONG).show();
			Log.e("SINGLE_TERM_RESPONSE", responseString);	
			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(responseString);

			if (wrapper.getStatus().getCode() == 200) {
				TermReportItem reportCardData = GsonParser.getInstance().parseTermReport(wrapper.getData().getAsJsonObject("report").toString());
				arrangeAndShowTermReportData(reportCardData);
			}


		};
	};
	private TextView examName;

	private void init() {
		// TODO Auto-generated method stub
		uiHelper = new UIHelper(this);
		app = (SchoolApp) this.getApplicationContext();
		layoutReport = (LinearLayout) findViewById(R.id.layout_report);
		examName = (TextView) findViewById(R.id.tv_report_exam_name);
		examName.setText(getIntent().getExtras().getString("term_name"));
		
	}

	
	/*private void arrangeAndShowTabholders() {
		// TODO Auto-generated method stub

		LinearLayout tabHolder = (LinearLayout) view
				.findViewById(R.id.tab_holder);

		for (int i = 0; i < items.size(); i++) {
			final int j = i;

			CustomButton button = new CustomButton(this);

			button.setLayoutParams(new LinearLayout.LayoutParams(0,
					LayoutParams.MATCH_PARENT, 1f));

			button.setTitleText(items.get(i).getExamName());

			button.setBackgroundResource(R.drawable.gray_btn);

			button.setClickable(true);

			button.setGravity(Gravity.CENTER);

			button.setTitleColor(getResources().getColor(R.color.gray_1));

			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					layoutReport.removeAllViews();

					CustomButton b = (CustomButton) v;

					for (int k = 0; k < tabList.size(); k++) {
						tabList.get(k).setReportButtonSelected(false);
					}
					b.setReportButtonSelected(true);
					arrangeAndShowTermReportData(j);
				}
			});

			button.setButtonSelected(false);

			tabHolder.addView(button);
			tabList.add(button);

			if (i < items.size() - 1) {
				LinearLayout divider = new LinearLayout(getActivity());
				divider.setLayoutParams(new LayoutParams(1,
						LayoutParams.MATCH_PARENT));
				divider.setBackgroundResource(R.color.gray_dark);

				tabHolder.addView(divider);
			}
		}
	}*/

	private void setValuesAndViews(TermReportItem term) {
		// TODO Auto-generated method stub
		Button gradeBtn = (Button) findViewById(R.id.btn_grade);
		gradeBtn.setText(term.getGrade());

		Button positionBtn = (Button) findViewById(R.id.btn_position);
		positionBtn.setText("Position: " + term.getPosition());

		Button totalBtn = (Button) findViewById(R.id.btn_total);
		totalBtn.setText("Total Students: "
				+ term.getTotalStudent());

		Button gpaBtn = (Button) findViewById(R.id.btn_gpa);
		gpaBtn.setText(term.getGpa());

		
	}

	class ViewHolder {
		LinearLayout layoutRow;

		TextView tvSubject;
		TextView tvGrade;
		TextView tvScore;
		TextView tvHighest;
		TextView tvTotal;
		TextView tvPercent;
	}

	private void arrangeAndShowTermReportData(TermReportItem term) {
		// TODO Auto-generated method stub

		setValuesAndViews(term);

		ArrayList<TermReportExamSubjectItem> subjectReportList = term.getExamSubjectList();

		LayoutInflater mInflater = LayoutInflater.from(this);

		int size = term.getExamSubjectList().size();

		for (int i = 0; i < size; i++) {
			View childView = mInflater.inflate(R.layout.row_term_report, null);

			ViewHolder holder = new ViewHolder();

			if (i == 0) {
				layoutReport.addView(childView);
				continue;
			}

			// *********************** Initialization ***********************
			holder.layoutRow = (LinearLayout) childView
					.findViewById(R.id.layout_row);
			holder.layoutRow.setBackgroundColor(getResources().getColor(
					R.color.white));

			holder.tvSubject = (TextView) childView
					.findViewById(R.id.tv_subject);

			holder.tvGrade = (TextView) childView.findViewById(R.id.tv_grade);
			holder.tvGrade
					.setTextColor(getResources().getColor(R.color.maroon));

			holder.tvScore = (TextView) childView.findViewById(R.id.tv_score);

			holder.tvHighest = (TextView) childView
					.findViewById(R.id.tv_highest);
			holder.tvHighest.setTextColor(getResources().getColor(
					R.color.maroon));

			holder.tvTotal = (TextView) childView.findViewById(R.id.tv_total);
			holder.tvTotal
					.setTextColor(getResources().getColor(R.color.gray_1));

			holder.tvPercent = (TextView) childView
					.findViewById(R.id.tv_percent);
			holder.tvPercent.setTextColor(getResources().getColor(
					R.color.gray_1));

			// *********************** Set values ***********************
			holder.tvSubject.setText(subjectReportList.get(i).getSubjctName());
			holder.tvGrade.setText(subjectReportList.get(i).getGrade());

			float m;

			if (subjectReportList.get(i).getMark().equalsIgnoreCase("-")) {
				holder.tvScore.setText(subjectReportList.get(i).getMark());
			} else {
				m = Float.parseFloat(subjectReportList.get(i).getMark());
				holder.tvScore.setText((int) m + "");
			}

			m = Float.parseFloat(subjectReportList.get(i).getMaxMark());
			holder.tvHighest.setText((int) m + "");

			m = Float.parseFloat(subjectReportList.get(i).getTotalMark());
			holder.tvTotal.setText((int) m + "");

			holder.tvPercent.setText(subjectReportList.get(i).getPercent()
					+ "%");

			layoutReport.addView(childView);
		}
	}


}

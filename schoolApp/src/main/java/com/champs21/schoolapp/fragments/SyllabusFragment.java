package com.champs21.schoolapp.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.champs21.freeversion.PaidVersionHomeFragment;
import com.champs21.freeversion.SyllabusMidLayerActivity;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.model.BaseType;
import com.champs21.schoolapp.model.Batch;
import com.champs21.schoolapp.model.Picker;
import com.champs21.schoolapp.model.Picker.PickerItemSelectedListener;
import com.champs21.schoolapp.model.PickerType;
import com.champs21.schoolapp.model.Term;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.champs21.schoolapp.viewhelpers.CustomTabButtonEllipsizeText;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SyllabusFragment extends Fragment {
	
	private View view;
	private UIHelper uiHelper;
	private UserHelper userHelper;
	
	private CustomTabButtonEllipsizeText btnTermExam, btnClassTest, btnProject, btnYearly;
	
	private CustomTabButtonEllipsizeText currentButton;
	
	private List<CustomTabButtonEllipsizeText> listBtn;
	
	private HorizontalScrollView horizontalScrollView;
	
	private List<Term> listTerm;
	
	
	private ListView listViewTerms;
	private TermsAdapter adapter;
	
	private String selectedBatchId = "";
	private TextView selectedBatchTextView;
	private ImageButton tapLayout;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		listTerm = new ArrayList<Term>();
		
		uiHelper = new UIHelper(getActivity());
		userHelper = new UserHelper(getActivity());
		
		listBtn = new ArrayList<CustomTabButtonEllipsizeText>();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		if(userHelper.getUser().getType() == UserTypeEnum.TEACHER)
		{
			if(!PaidVersionHomeFragment.isBatchLoaded){
				RequestParams params=new RequestParams();
				params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
				AppRestClient.post(URLHelper.URL_GET_TEACHER_BATCH, params, getBatchEventsHandler);
			}else {
				if(PaidVersionHomeFragment.selectedBatch!=null){
					selectedBatchId = PaidVersionHomeFragment.selectedBatch.getId();
					initApiCall("3");
				}else {
					showBatchPicker(PickerType.TEACHER_BATCH);
				}
			}
			
		
		}else initApiCall("3");
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.fragment_syllabus_new,
				container, false);
		
		initView(view);
		
		return view;
	}
	
	@SuppressLint("NewApi")
	private int getWindowWidth()
	{
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		final int width = size.x;
		
		return width;
	}
	
	private void initView(View view)
	{
		
		listViewTerms = (ListView)view.findViewById(R.id.listViewTerms);
		adapter = new TermsAdapter();
		listViewTerms.setAdapter(adapter);
		
		
		horizontalScrollView = (HorizontalScrollView)view.findViewById(R.id.horizontalScrollView);
		tapLayout = (ImageButton)view.findViewById(R.id.select_batch_btn);
		tapLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!PaidVersionHomeFragment.isBatchLoaded){
					RequestParams params=new RequestParams();
					params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
					AppRestClient.post(URLHelper.URL_GET_TEACHER_BATCH, params, getBatchEventsHandler);
				}else {
						showBatchPicker(PickerType.TEACHER_BATCH);
				}
			}
		});
		
		btnTermExam = (CustomTabButtonEllipsizeText)view.findViewById(R.id.btnTermExam);
		btnClassTest = (CustomTabButtonEllipsizeText)view.findViewById(R.id.btnClassTest);
		btnProject = (CustomTabButtonEllipsizeText)view.findViewById(R.id.btnProject);
		btnYearly = (CustomTabButtonEllipsizeText)view.findViewById(R.id.btnYearly);
		selectedBatchTextView = (TextView)view.findViewById(R.id.txtBatchName);
		if(PaidVersionHomeFragment.selectedBatch!=null){
			selectedBatchTextView.setText(PaidVersionHomeFragment.selectedBatch.getName());
		}
		if(userHelper.getUser().getType()!=UserTypeEnum.TEACHER){
			tapLayout.setVisibility(View.GONE);
			selectedBatchTextView.setVisibility(View.GONE);
		}
		listBtn.add(btnTermExam);
		listBtn.add(btnClassTest);
		listBtn.add(btnProject);
		listBtn.add(btnYearly);
		
		currentButton = btnTermExam;
		btnTermExam.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.exam_tap);
		
		//initApiCall("3");
		
		btnTermExam.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//int scrollX = (v.getLeft() - (getWindowWidth() / 2)) + (v.getWidth() / 2);
			    //horizontalScrollView.smoothScrollTo(scrollX, 0);
			    
				currentButton = btnTermExam;
				btnTermExam.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.exam_tap);
				
				updateButtonUi();
				
				initApiCall("3");
				
				
			}
		});
		
		btnClassTest.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//int scrollX = (v.getLeft() - (getWindowWidth() / 2)) + (v.getWidth() / 2);
			    //horizontalScrollView.smoothScrollTo(scrollX, 0);
			    
				currentButton = btnClassTest;
				btnClassTest.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.exam_tap);
				
				updateButtonUi();
				
				initApiCall("1");
			}
		});
		
		btnProject.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//int scrollX = (v.getLeft() - (getWindowWidth() / 2)) + (v.getWidth() / 2);
			    //horizontalScrollView.smoothScrollTo(scrollX, 0);
			    
				currentButton = btnProject;
				btnProject.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.exam_tap);

				updateButtonUi();
				
				initApiCall("2");
			}
		});
		
		btnYearly.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//int scrollX = (v.getLeft() - (getWindowWidth() / 2)) + (v.getWidth() / 2);
			    //horizontalScrollView.smoothScrollTo(scrollX, 0);
			    
				currentButton = btnYearly;
				btnYearly.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.yearly_tap);
				
				updateButtonUi();
				
				Intent intent = new Intent(getActivity(), SyllabusMidLayerActivity.class);
				intent.putExtra(AppConstant.ID_TERM, "");
				if(userHelper.getUser().getType() == UserTypeEnum.TEACHER)
				{
					intent.putExtra(AppConstant.ID_BATCH, selectedBatchId);
				}
				startActivity(intent);
			}
		});
		
		
		
		//btnTermExam.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.term_icon_tap);
	}
	
	AsyncHttpResponseHandler getBatchEventsHandler=new AsyncHttpResponseHandler()
	{

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			//uiHelper.showMessage(arg1);
			uiHelper.dismissLoadingDialog();
		}

		@Override
		public void onStart() {
			super.onStart();
			
			if(!uiHelper.isDialogActive())
				uiHelper.showLoadingDialog(getString(R.string.loading_text));
			else
				uiHelper.updateLoadingDialog(getString(R.string.loading_text));
			
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);
			uiHelper.dismissLoadingDialog();
			Log.e("Response", responseString);
			Wrapper wrapper=GsonParser.getInstance().parseServerResponse(responseString);
			if(wrapper.getStatus().getCode()==AppConstant.RESPONSE_CODE_SUCCESS)
			{
				PaidVersionHomeFragment.isBatchLoaded=true;
				PaidVersionHomeFragment.batches.clear();
				String data=wrapper.getData().get("batches").toString();
				PaidVersionHomeFragment.batches.addAll(GsonParser.getInstance().parseBatchList(data));
				//showPicker(PickerType.TEACHER_BATCH);
				showBatchPicker(PickerType.TEACHER_BATCH);
			}
			
		}
		
	};
	
	
	private void updateButtonUi()
	{
		for(CustomTabButtonEllipsizeText btn : listBtn)
		{
			if(!btn.equals(currentButton))
			{
				
				if(btn == btnYearly)
				{
					btn.setButtonSelected(false, getResources().getColor(R.color.black), R.drawable.yearly_normal);
				}
				else
				{
					btn.setButtonSelected(false, getResources().getColor(R.color.black), R.drawable.exam_normal);
				}
				
			}
		}
	}
	
	
	private void initApiCall(String catId)
	{
		listTerm.clear();
		
		RequestParams params = new RequestParams();

		
		if (userHelper.getUser().getType() == UserTypeEnum.PARENTS) 
		{
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getSelectedChild().getBatchId());
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser().getPaidInfo().getSchoolId());
			params.put(RequestKeyHelper.CATEGORY_ID, catId);
		}
		
		if(userHelper.getUser().getType() == UserTypeEnum.STUDENT)
		{
			
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			params.put(RequestKeyHelper.BATCH_ID, userHelper.getUser().getPaidInfo().getBatchId());
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser().getPaidInfo().getSchoolId());
			params.put(RequestKeyHelper.CATEGORY_ID, catId);
			
		}
		
		if(userHelper.getUser().getType() == UserTypeEnum.TEACHER)
		{
			params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
			params.put(RequestKeyHelper.BATCH_ID, selectedBatchId);
			params.put(RequestKeyHelper.SCHOOL, userHelper.getUser().getPaidInfo().getSchoolId());
			params.put(RequestKeyHelper.CATEGORY_ID, catId);
		}
		
		AppRestClient.post(URLHelper.URL_SYLLABUS_TERM, params, termHandler);
	}
	
	
	
	private void showBatchPicker(PickerType type) {

		Picker picker = Picker.newInstance(0);
		picker.setData(type, PaidVersionHomeFragment.batches, PickerCallback , "Select Batch");
		picker.show(getChildFragmentManager(), null);
	}

	PickerItemSelectedListener PickerCallback = new PickerItemSelectedListener() {

		@Override
		public void onPickerItemSelected(BaseType item) {

			switch (item.getType()) {
			case TEACHER_BATCH:
				Batch selectedBatch=(Batch)item;
				PaidVersionHomeFragment.selectedBatch=selectedBatch;
				Intent i = new Intent("com.champs21.schoolapp.batch");
                i.putExtra("batch_id", selectedBatch.getId());
                getActivity().sendBroadcast(i);
				
				
				selectedBatchId = selectedBatch.getId();
				selectedBatchTextView.setText(selectedBatch.getName());
				initApiCall("3");
				currentButton = btnTermExam;
				btnTermExam.setButtonSelected(true, getResources().getColor(R.color.black), R.drawable.exam_tap);
				updateButtonUi();
				break;
			default:
				break;
			}

		}
	};
	
	
	
	AsyncHttpResponseHandler termHandler = new AsyncHttpResponseHandler() {

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
				
				listTerm = GsonParser.getInstance().parseTerm(modelContainer.getData().getAsJsonArray("terms").toString());
				
				adapter.notifyDataSetChanged();
				
				
				if(listTerm.size() <= 0)
				{
					Toast.makeText(getActivity(), "No data found!", Toast.LENGTH_SHORT).show();
				}
			}
			
			else {

			}
			
			

		};
	};
	
	
	private class TermsAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listTerm.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listTerm.get(position);
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
            if (convertView == null)
            { 
            	holder = new ViewHolder();
            	convertView = LayoutInflater.from(getActivity()).inflate(R.layout.row_term_syllabus,null,false);
            	
            	holder.txtDate = (TextView)convertView.findViewById(R.id.txtDate);
            	holder.txtExamName = (TextView)convertView.findViewById(R.id.txtExamName);
            	holder.layoutAction = (LinearLayout)convertView.findViewById(R.id.layoutAction);
            	holder.txtAction = (TextView)convertView.findViewById(R.id.txtAction);
            	
            	
            	convertView.setTag(holder);
            }
            
            else
            {
            	holder = (ViewHolder)convertView.getTag();
            }
			
            holder.layoutAction.setTag(listTerm.get(position).getTermId());
            
            holder.txtExamName.setText(listTerm.get(position).getTermTitle());
            holder.txtAction.setText("View");
            holder.txtDate.setText(listTerm.get(position).getExam_date());
            
            holder.layoutAction.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					String termId  = (String) ((LinearLayout)v).getTag();
					Intent intent = new Intent(getActivity(), SyllabusMidLayerActivity.class);
					intent.putExtra(AppConstant.ID_TERM, termId);
					if(userHelper.getUser().getType() == UserTypeEnum.TEACHER)
					{
						intent.putExtra(AppConstant.ID_BATCH, selectedBatchId);
					}
					startActivity(intent);
					
				}
			});
            
			
			return convertView;
		}
		
	}
	
	class ViewHolder{
		
		TextView txtDate;
		TextView txtExamName;
		LinearLayout layoutAction;
		TextView txtAction;
        
    }

	
}

package com.champs21.schoolapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.champs21.freeversion.ChildContainerActivity;
import com.champs21.freeversion.HomeBaseActivity;
import com.champs21.schoolapp.model.Student;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppUtility;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class StudentInfoActivity extends ChildContainerActivity {

	private String studentId;
	private LinearLayout pbLayout;
	private Context con;
	private TextView studentName,studentClass, roll, guardianName, gender, batchName, admissionNumber, txtContactNumber, txtAddress;
	private ImageView profileImage;
	private ProgressBar progress;
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
        homeBtn.setVisibility(View.VISIBLE);
        logo.setVisibility(View.GONE);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_info);
		
		if(getIntent().getExtras() != null)
			studentId = getIntent().getExtras().getString("student_id");
		
		fetchStudentInfo();
		
		init();
	}
	
	private void init() {
		con=StudentInfoActivity.this;
		studentId=getIntent().getStringExtra("student_id");
		studentName = (TextView)findViewById(R.id.tv_profile_student_name);
		studentClass = (TextView)findViewById(R.id.tv_profile_student_class);
		roll = (TextView)findViewById(R.id.tv_profile_roll);
		guardianName = (TextView)findViewById(R.id.tv_profile_guardian_name);
		gender = (TextView )findViewById(R.id.tv_profile_gender);
		batchName = (TextView )findViewById(R.id.tv_profile_batch_name);
		admissionNumber = (TextView)findViewById(R.id.tv_profile_admission_no);
		profileImage = (ImageView)findViewById(R.id.iv_profile_picture);
		progress = (ProgressBar)findViewById(R.id.progress);
		pbLayout = (LinearLayout)findViewById(R.id.pb);
		
		
		txtContactNumber = (TextView)findViewById(R.id.txtContactNumber);
		txtAddress = (TextView)findViewById(R.id.txtAddress);
	}

	/*@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		fetchStudentInfo();
	}*/
	
	private void fetchStudentInfo()
	{
		RequestParams params=new RequestParams();
		params.put(RequestKeyHelper.STUDENT_ID, studentId);
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		AppRestClient.post(URLHelper.URL_GET_STUDENT_INFO, params, getStudentInfoHandler);
	}
	
	private void updateUI(Student student)
	{
		studentName.setText(student.getStudentName());
		studentClass.setText(student.getStudentClass());
		guardianName.setText(student.getGaurdian());
		roll.setText(student.getRollNo());
		//gender.setText(student.getGender());
		batchName.setText(student.getBatchName());
		admissionNumber.setText(student.getAdmissionNo());
		ImageLoader.getInstance().displayImage(student.getImageUrl(),
			     profileImage, AppUtility.getOptionForUserImage(),
			     new ImageLoadingListener() {

			      @Override
			      public void onLoadingStarted(String imageUri, View view) {
			        progress.setVisibility(View.VISIBLE);

			      }

			      @Override
			      public void onLoadingFailed(String imageUri, View view,
			        FailReason failReason) {
			       // spinner.setVisibility(View.GONE);
			    	  progress.setVisibility(View.GONE);
			      }

			      @Override
			      public void onLoadingComplete(String imageUri,
			        View view, Bitmap loadedImage) {
			    	  progress.setVisibility(View.GONE);
			      }

			      @Override
			      public void onLoadingCancelled(String imageUri,
			        View view) {

			      }
			     });
		
		
		txtContactNumber.setText(student.getPhone());
		txtAddress.setText(student.getContact());
		
	}
	
	AsyncHttpResponseHandler getStudentInfoHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			pbLayout.setVisibility(View.GONE);
		}

		@Override
		public void onStart() {
			super.onStart();
			pbLayout.setVisibility(View.VISIBLE);
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);
			pbLayout.setVisibility(View.GONE);
			Log.e("Response****", responseString);

			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					responseString);
			if (wrapper.getStatus().getCode() == 200) {
				Student student=GsonParser.getInstance().parseStudentInfo(wrapper.getData().get("student").toString());
				updateUI(student);
				
			} else {
				
			}

		}

	};

}

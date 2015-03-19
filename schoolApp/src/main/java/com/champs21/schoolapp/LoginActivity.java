package com.champs21.schoolapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.champs21.freeversion.AssesmentActivity;
import com.champs21.freeversion.CompleteProfileActivityContainer;
import com.champs21.freeversion.HomePageFreeVersion;
import com.champs21.freeversion.RegistrationActivity;
import com.champs21.schoolapp.fragments.UserTypeSelectionDialog;
import com.champs21.schoolapp.fragments.UserTypeSelectionDialog.UserTypeListener;
import com.champs21.schoolapp.model.AssessmentQuestion;
import com.champs21.schoolapp.utils.SPKeyHelper;
import com.champs21.schoolapp.utils.SchoolApp;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;
import com.champs21.schoolapp.viewhelpers.UIHelper;

public class LoginActivity extends SocialBaseActivity implements
		OnClickListener, UserTypeListener {

	EditText etUserName;
	EditText etPassword;
	Button btnLogin;
	Button btnFbLogin;
	Button btnGoogleLogin;
	TextView tvSignup;
	private final int DIALOG_FRAGMENT = 101;
	SchoolApp app;
	boolean isFirstTime;
	UIHelper uiHelper;
	UserHelper userHelper;
	String username = "", password = "";
	
	private String fromAssessment = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		userHelper = new UserHelper(this, LoginActivity.this);
		uiHelper = new UIHelper(LoginActivity.this);
		etUserName=(EditText)findViewById(R.id.et_username);
		//etUserName.setText("ovi@gmail.com");
		etPassword=(EditText)findViewById(R.id.et_password);
		//etPassword.setText("123456");
		btnLogin=(Button)findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(this);
		btnFbLogin=(Button)findViewById(R.id.fb_login_btn);
		btnFbLogin.setOnClickListener(this);
		btnGoogleLogin=(Button)findViewById(R.id.google_login_btn);
		btnGoogleLogin.setOnClickListener(this);
		tvSignup=(TextView)findViewById(R.id.tv_signup);
		tvSignup.setOnClickListener(this);
		app = (SchoolApp) getApplicationContext();
		app.setupUI(findViewById(R.id.layout_parent), this);
		
		if(getIntent() != null && getIntent().getExtras() != null)
			fromAssessment = getIntent().getExtras().getString("assessment_score");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			validateFieldAndCallLogIn();
			break;
		case R.id.fb_login_btn:
			doFaceBookLogin(false);
			break;
		case R.id.google_login_btn:
			doGooglePlusLogin();
			break;
		case R.id.tv_signup:
			startActivity(new Intent(this, RegistrationActivity.class));
			finish();
			break;
		default:
			break;
		}

	}

	private void validateFieldAndCallLogIn() {
		String userName = etUserName.getText().toString().trim();
		String password = etPassword.getText().toString().trim();
		if (TextUtils.isEmpty(userName)) {
			etUserName.setError("Please enter UserName!!");
		} else if (TextUtils.isEmpty(password)) {
			etPassword.setError("Please enter Password!!");
		} else
			userHelper.doLogIn(userName, password);
	}

	@Override
	public void onAuthenticationStart() {
		uiHelper.showLoadingDialog(getString(R.string.loading_text));
	}

	@Override
	public void onAuthenticationSuccessful() {
		
		
		if (uiHelper.isDialogActive()) {
			uiHelper.dismissLoadingDialog();
			
		}
		if (UserHelper.isRegistered()) {
			if (UserHelper.isLoggedIn()) {
				
			
				if(fromAssessment!=null && fromAssessment.length() > 0)
				{
					//Intent intent = new Intent(LoginActivity.this, AssesmentActivity.class);
					finish();
					
					return;
				}
				
				
				switch (UserHelper.getUserAccessType()) {
				case FREE:
					Intent intent = new Intent(LoginActivity.this,
							HomePageFreeVersion.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(intent);
					finish();
					break;
				case PAID:
					
					switch (userHelper.getUser().getType()) {
					
					case PARENTS:
						if (userHelper.getUser().getChildren() == null) {
							Log.e("Userhelper", "null");
						}
						if (userHelper.getUser().getChildren().size() > 0) {
							Intent paidIntent = new Intent(LoginActivity.this,
									HomePageFreeVersion.class);
							paidIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(paidIntent);
							finish();
						}
						break;
					default:
						Intent paidIntent = new Intent(LoginActivity.this,
								HomePageFreeVersion.class);
						paidIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
								| Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(paidIntent);
						finish();
						break;
					}

					break;

				default:
					break;
				}
			
				
				
				
				
			} else {
				finish();
				Intent intent = new Intent(LoginActivity.this,
						CompleteProfileActivityContainer.class);
				intent.putExtra(SPKeyHelper.USER_TYPE, userHelper.getUser()
						.getType().ordinal());
				intent.putExtra("FIRST_TIME", true);
				startActivity(intent);

			}
		} else {
			Log.e("TypeSelection!", "GOOOOOOOOOOOOOOOO");
			UserTypeSelectionDialog dialogFrag = UserTypeSelectionDialog
					.newInstance();
			dialogFrag.show(getSupportFragmentManager().beginTransaction(),
					"dialog");

		}

	}

	@Override
	public void onAuthenticationFailed(String msg) {
		if (uiHelper.isDialogActive()) {
			uiHelper.dismissLoadingDialog();
			uiHelper.showMessage(msg);
		}
	}

	@Override
	public void onTypeSelected(int ordinal) {

		Log.e("Type", UserTypeEnum.values()[ordinal].toString());
		userHelper.updateUserWithType(ordinal);
	}

	@Override
	public void onPaswordChanged() {
		// TODO Auto-generated method stub
		
	}
}

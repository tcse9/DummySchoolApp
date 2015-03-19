/**
 * 
 */
package com.champs21.freeversion;

import roboguice.inject.InjectView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.RelativeLayout;

import com.champs21.schoolapp.R;
import com.champs21.schoolapp.fragments.CompleteProfileFragmentParents;
import com.champs21.schoolapp.fragments.CompleteProfileFragmentStudent;
import com.champs21.schoolapp.fragments.CompleteProfileFragmentTeacher;
import com.champs21.schoolapp.utils.SPKeyHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.utils.UserHelper.UserTypeEnum;


public class CompleteProfileActivityContainer extends FragmentActivity{

	private RelativeLayout container;
	private UserTypeEnum type;
	private boolean isFirstTime=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_complete_profile);
		container=(RelativeLayout)findViewById(R.id.container);
		getIntentData();
		updateUI();
		
		
	}

	public boolean isFirstTimeUpdate()
	{
		return isFirstTime;
	}
	
	
	private void updateUI() {
		 FragmentManager fragmentManager = getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         switch (type) {
         case STUDENT:
        	 CompleteProfileFragmentStudent student = new CompleteProfileFragmentStudent();
             fragmentTransaction.add(R.id.container, student, "Student");
             fragmentTransaction.commit();
             break;
         case PARENTS:
        	 CompleteProfileFragmentParents parents=new CompleteProfileFragmentParents();
        	 fragmentTransaction.add(R.id.container, parents, "Parents");
             fragmentTransaction.commit();
        	 break;
         case TEACHER:
        	 CompleteProfileFragmentTeacher teacher=new CompleteProfileFragmentTeacher();
        	 fragmentTransaction.add(R.id.container, teacher, "Teacher");
             fragmentTransaction.commit();
        	 break;
         case OTHER:
        	 CompleteProfileFragmentParents others=new CompleteProfileFragmentParents();
        	 fragmentTransaction.add(R.id.container, others, "Parents");
             fragmentTransaction.commit();
        	 break;
		default:
			break;
			
		}
	}

	private void getIntentData() {
		if(getIntent().hasExtra(SPKeyHelper.USER_TYPE))
		{
			int ordinal=getIntent().getIntExtra(SPKeyHelper.USER_TYPE, 0);
			type=UserTypeEnum.values()[ordinal];
		}
		if (getIntent().hasExtra("FIRST_TIME")) {
			isFirstTime=true;
		}
	}
	
	public void goToAfterLogIn()
	{
		Intent intent = new Intent(CompleteProfileActivityContainer.this, HomePageFreeVersion.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();   
        Log.e("Back", "Chapse");
        if(UserHelper.isLoggedIn())
        {
        	finish();
        }
        else
        {
        	UserHelper.setLoggedIn(true);
        	goToAfterLogIn();
        }

    }

	


}

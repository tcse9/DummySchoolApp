package com.champs21.freeversion;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpResponseException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportV4App;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.champs21.schoolapp.NotificationActivity;
import com.champs21.schoolapp.R;
import com.champs21.schoolapp.fragments.AllSchoolListFragment;
import com.champs21.schoolapp.fragments.CommonChildFragment;
import com.champs21.schoolapp.fragments.VideoFragment;
import com.champs21.schoolapp.model.CHILD_TYPE;
import com.champs21.schoolapp.model.DrawerChildBase;
import com.champs21.schoolapp.model.DrawerChildMenu;
import com.champs21.schoolapp.model.DrawerChildMenuDiary;
import com.champs21.schoolapp.model.DrawerChildMySchool;
import com.champs21.schoolapp.model.DrawerChildSettings;
import com.champs21.schoolapp.model.DrawerGroup;
import com.champs21.schoolapp.model.Student;
import com.champs21.schoolapp.model.Syllabus;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.champs21.schoolapp.utils.AppConstant;
import com.champs21.schoolapp.utils.AppUtility;
import com.champs21.schoolapp.utils.GsonParser;
import com.champs21.schoolapp.utils.RequestKeyHelper;
import com.champs21.schoolapp.utils.SPKeyHelper;
import com.champs21.schoolapp.utils.SharedPreferencesHelper;
import com.champs21.schoolapp.utils.URLHelper;
import com.champs21.schoolapp.utils.UserHelper;
import com.champs21.schoolapp.viewhelpers.UIHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

@SuppressLint("NewApi")
public class HomePageFreeVersion extends HomeContainerActivity {
	
	private UIHelper uiHelper;

	private DrawerChildBase selectedMenu;
	private Handler handler = new Handler();

	private static final String LOG_TAG = HomePageFreeVersion.class
			.getSimpleName();

	private static final int[] POW_2 = { 1, 2, 4, 8, 16, 32, 64, 128, 256, 512,
			1024, 2048, 4096 };
	// 16 bits available at all
	private static final int CHAIN_BITS_FOR_INDEX = 3; // adjustable constant,
														// use value 3 or 4
	private static final int CHAIN_BITS_COUNT = 9; // adjustable constant, use
													// value 9 or 12
	private static final int CHAIN_INDEX_MASK = ~(0x80000000 >> (31 - CHAIN_BITS_FOR_INDEX));
	// max allowed depth of fragments
	private static final int CHAIN_MAX_DEPTH = CHAIN_BITS_COUNT
			/ CHAIN_BITS_FOR_INDEX;
	// bits for external usage
	private static final int REQUEST_CODE_EXT_BITS = 16 - CHAIN_BITS_COUNT;
	private static final int REQUEST_CODE_MASK = ~(0x80000000 >> (31 - REQUEST_CODE_EXT_BITS));
	// we have to add +1 for every index
	// because we could not determine 0 index at all
	private static final int FRAGMENT_MAX_COUNT = POW_2[CHAIN_BITS_FOR_INDEX] - 1;

	public void startActivityFromFragment(Fragment fragment, Intent intent,
			int requestCode) {
		if ((requestCode & (~REQUEST_CODE_MASK)) != 0) {
			Log.w(LOG_TAG, "Can only use lower " + REQUEST_CODE_EXT_BITS
					+ " bits for requestCode, int value in range 1.."
					+ (POW_2[REQUEST_CODE_EXT_BITS] - 1));
			super.startActivityFromFragment(fragment, intent, requestCode);
			return;
		}

		int chain = 0;
		int depth = 0;

		Fragment node = fragment;

		do {
			if (depth > CHAIN_MAX_DEPTH) {
				throw new IllegalStateException(
						"Too deep structure of fragments, max "
								+ CHAIN_MAX_DEPTH);
			}

			int index = SupportV4App.fragmentIndex(node);
			if (index < 0) {
				throw new IllegalStateException(
						"Fragment is out of FragmentManager: " + node);
			}

			if (index >= FRAGMENT_MAX_COUNT) {
				throw new IllegalStateException(
						"Too many fragments inside (max " + FRAGMENT_MAX_COUNT
								+ "): " + node.getParentFragment());
			}

			chain = (chain << CHAIN_BITS_FOR_INDEX) + (index + 1);
			node = node.getParentFragment();
			depth += 1;
		} while (node != null);

		int newCode = (chain << REQUEST_CODE_EXT_BITS)
				+ (requestCode & REQUEST_CODE_MASK);

		super.startActivityForResult(intent, newCode);
	}

	Runnable loadRun = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			closeDrawer();
		}
	};

	private void closeDrawer() {
		mDrawerLayout.closeDrawer(Gravity.RIGHT);
	}
	
	@Override
	public void onBackPressed() {
		Fragment myFragment = getSupportFragmentManager().findFragmentByTag("PAID");
		if (myFragment!=null) {
			if(myFragment.isVisible()){
				loadHome();
			}else super.onBackPressed();
				
		}else super.onBackPressed();
	}
	
	public void loadFragment(Fragment frag)
	{
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.pager_frame, frag, TAG).commit();
	}
	
	public void loadPaidFragment(Fragment frag){
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.pager_frame, frag, "PAID").commit();
	}
	
	
	

	private void loadDiaryItem(DrawerChildMenuDiary item) {
		String className = item.getClazzName();
		try {
			// Object xyz = Class.forName(className).newInstance();

			Constructor<?> ctor = Class.forName(className).getConstructor();
			Fragment object = (Fragment) ctor.newInstance(new Object[] {});
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.pager_frame, object, TAG).commit();

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*Log.e(LOG_TAG,
				"Activity result requestCode does not correspond restrictions: 0x"
						+ Integer.toHexString(requestCode));*/
		if ((requestCode & 0xffff0000) != 0 ) {
			//&&(requestCode & 0x2009b) != 0
			Log.e(LOG_TAG,
					"Activity result requestCode does not correspond restrictions: 0x"
							+ Integer.toHexString(requestCode));
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}

		SupportV4App.activityFragmentsNoteStateNotSaved(this);

		int chain = requestCode >>> REQUEST_CODE_EXT_BITS;
		if (chain != 0) {
			ArrayList<Fragment> active = SupportV4App
					.activityFragmentsActive(this);
			Fragment fragment;

			do {
				int index = (chain & CHAIN_INDEX_MASK) - 1;
				if (active == null || index < 0 || index >= active.size()) {
					Log.e(LOG_TAG,
							"Activity result fragment chain out of range: 0x"
									+ Integer.toHexString(requestCode));
					super.onActivityResult(requestCode, resultCode, data);
					return;
				}

				fragment = active.get(index);
				if (fragment == null) {
					break;
				}

				active = SupportV4App
						.fragmentChildFragmentManagerActive(fragment);
				chain = chain >>> CHAIN_BITS_FOR_INDEX;
			} while (chain != 0);

			if (fragment != null) {
				fragment.onActivityResult(requestCode & REQUEST_CODE_MASK,
						resultCode, data);
			} else {
				Log.e(LOG_TAG,
						"Activity result no fragment exists for chain: 0x"
								+ Integer.toHexString(requestCode));
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void navigateTo(int position) {
		// Log.v(TAG, "List View Item: " + position);

		switch (position) {
		case 0:
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.pager_frame,
							CommonChildFragment.newInstance(-1, ""), "COMMON")
					.commit();
			break;
		case 1:
			break;
		}
	}

	// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
	// public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "441812119192";
	/**
	 * Tag used on log messages.
	 */
	public static final String TAG = "GCM Demo";

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;

	String regid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_homepage_freeversion);
		navigateTo(0);

		context = getApplicationContext();

		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regid = getRegistrationId(context);

			if (TextUtils.isEmpty(regid)) {
				registerInBackground();
			} else if (!getRegisteredToServer()) {
				sendRegistrationIdToBackend();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

		expListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {

				Log.e("testing", "GroupPosition-" + groupPosition
						+ " child position-" + childPosition + " " + id);
				DrawerChildBase item = childList.get(
						groupItem.get(groupPosition).getText()).get(
						childPosition);

				ImageView image = (ImageView) v.findViewById(R.id.image);

				if (item.getType() == CHILD_TYPE.MENU) {
					DrawerChildMenu menu = (DrawerChildMenu) item;

					boolean childState = childSelectionStates.get(
							groupItem.get(groupPosition).getText()).get(
							childPosition);

					if (!childState) {
						listAdapter.initializeStates();
						childSelectionStates.get(
								groupItem.get(groupPosition).getText()).remove(
								childPosition);
						childSelectionStates.get(
								groupItem.get(groupPosition).getText()).add(
								childPosition, new Boolean(!childState));
						listAdapter.notifyDataSetChanged();

						selectedMenu = menu;
						final int menuId = Integer
								.parseInt(((DrawerChildMenu) selectedMenu)
										.getId());
						switch (menuId) {
						case 49:// for video fragment
							getSupportFragmentManager()
									.beginTransaction()
									.replace(R.id.pager_frame,
											VideoFragment.newInstance(menuId),
											TAG).commit();
							break;
						default:
							getSupportFragmentManager()
									.beginTransaction()
									.replace(
											R.id.pager_frame,
											CommonChildFragment.newInstance(
													menuId, ""), TAG).commit();
							break;
						}
						handler.postDelayed(loadRun, 500);
						// loadContent(menu);

					}

					/*
					 * image.setImageResource(context.getResources().getIdentifier
					 * ( menu.getImageName() + "_tap", "drawable",
					 * context.getPackageName()));
					 */

				}else if(item.getType()== CHILD_TYPE.MYSCHOOL){
					mDrawerLayout.closeDrawer(Gravity.RIGHT);
					DrawerChildMySchool settings = (DrawerChildMySchool) item;
					final int menuIdSchool = Integer.parseInt(settings.getId());
					switch (menuIdSchool) {

					case 0:// for edit profile
						Intent intent = new Intent(
								HomePageFreeVersion.this,
								SchoolScrollableDetailsActivity.class);
						intent.putExtra(AppConstant.SCHOOL_ID, userHelper.getJoinedSchool().getSchool_id());
						startActivity(intent);
						break;
					default:
						break;
					}
				}else if (item.getType() == CHILD_TYPE.SETTINGS) {
					mDrawerLayout.closeDrawer(Gravity.RIGHT);
					DrawerChildSettings settings = (DrawerChildSettings) item;
					final int menuId = Integer.parseInt(settings.getId());

					if (UserHelper.isLoggedIn()) {
						switch (menuId) {
						case 0:// for account settings
							Intent accountSettingsIntent = new Intent(
									HomePageFreeVersion.this,
									AccountSettingsActivity.class);
							startActivity(accountSettingsIntent);
							break;
						case 1:// for preference settings
							Intent prefIntent = new Intent(
									HomePageFreeVersion.this,
									PreferenceSettingsActivity.class);
							startActivity(prefIntent);
							break;
						case 2:// for edit profile
							Intent i = new Intent(HomePageFreeVersion.this,
									CompleteProfileActivityContainer.class);
							i.putExtra(SPKeyHelper.USER_TYPE, userHelper
									.getUser().getType().ordinal());
							startActivity(i);
							break;
						case 3:// for edit profile
							Intent aboutIntent = new Intent(
									HomePageFreeVersion.this,
									InfoActivity.class);
							aboutIntent.putExtra("title", "About Us");
							aboutIntent.putExtra("description", getResources()
									.getString(R.string.about_use_text));
							startActivity(aboutIntent);
							break;
						case 4:// for edit profile
							Intent tpIntent = new Intent(
									HomePageFreeVersion.this,
									InfoActivity.class);
							tpIntent.putExtra("title", "Terms & Policy");
							tpIntent.putExtra("description", getResources()
									.getString(R.string.termsandpolicy_text));
							startActivity(tpIntent);
							break;
						default:
							break;
						}
					} else {
						switch (menuId) {

						case 0:// for edit profile
							Intent aboutIntent = new Intent(
									HomePageFreeVersion.this,
									InfoActivity.class);
							aboutIntent.putExtra("title", "About Us");
							aboutIntent.putExtra("description", getResources()
									.getString(R.string.about_use_text));
							startActivity(aboutIntent);
							break;
						case 1:// for edit profile
							Intent tpIntent = new Intent(
									HomePageFreeVersion.this,
									InfoActivity.class);
							tpIntent.putExtra("title", "Terms & Policy");
							tpIntent.putExtra("description", getResources()
									.getString(R.string.termsandpolicy_text));
							startActivity(tpIntent);
							break;
						default:
							break;
						}
					}
				} else if (item.getType() == CHILD_TYPE.DIARY) {
					DrawerChildMenuDiary menu = (DrawerChildMenuDiary) item;

					boolean childState = childSelectionStates.get(
							groupItem.get(groupPosition).getText()).get(
							childPosition);

					if (!childState) {
						listAdapter.initializeStates();
						childSelectionStates.get(
								groupItem.get(groupPosition).getText()).remove(
								childPosition);
						childSelectionStates.get(
								groupItem.get(groupPosition).getText()).add(
								childPosition, new Boolean(!childState));
						listAdapter.notifyDataSetChanged();

						selectedMenu = menu;
						loadDiaryItem(menu);
						handler.postDelayed(loadRun, 500);
						// loadContent(menu);

					}
				}
				return false;
			}
		});
		expListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {

				DrawerGroup group = (DrawerGroup) groupItem.get(groupPosition);
				int groupId = Integer.parseInt(group.getId());
				switch (groupId) {
				case 3:
					mDrawerLayout.closeDrawer(Gravity.RIGHT);
					Intent assessmentScoreIntent = new Intent(
							HomePageFreeVersion.this,
							AssesmentHistoryActivity.class);
					startActivity(assessmentScoreIntent);
					break;
				case 4:
					/*mDrawerLayout.closeDrawer(Gravity.RIGHT);
					UserHelper.setLoggedIn(false);
					UserHelper.saveIsJoinedSchool(false);
					Intent intent = new Intent(HomePageFreeVersion.this,
							HomePageFreeVersion.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
							| Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TASK);

					finish();
					overridePendingTransition(0, 0);

					startActivity(intent);
					overridePendingTransition(0, 0);*/
					
					
					if(AppUtility.isInternetConnected())
					{
						initApiCallLogout();
					}
					else
					{
						Toast.makeText(context, "Sorry no internet connection found!", Toast.LENGTH_SHORT).show();
					}
					
					break;

				default:
					break;
				}
				return false;
			}
		});
		
		
		
		uiHelper = new UIHelper(this);
	}
	
	
	
	private void initApiCallLogout()
	{
		RequestParams params=new RequestParams();
		params.put(RequestKeyHelper.USER_SECRET, UserHelper.getUserSecret());
		params.put("gcm_id", getRegistrationId(this));
		
		AppRestClient.post(URLHelper.URL_LOGOUT, params, logoutHandler);
	}
	
	AsyncHttpResponseHandler logoutHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			uiHelper.showMessage(arg1);
			if (uiHelper.isDialogActive()) {
				uiHelper.dismissLoadingDialog();
			}
		}

		@Override
		public void onStart() {
			uiHelper.showLoadingDialog("Please wait...");
			
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			
			uiHelper.dismissLoadingDialog();
			
			
			Wrapper modelContainer = GsonParser.getInstance()
					.parseServerResponse(responseString);

			if (modelContainer.getStatus().getCode() == 200) {
				
				mDrawerLayout.closeDrawer(Gravity.RIGHT);
				UserHelper.setLoggedIn(false);
				UserHelper.saveIsJoinedSchool(false);
				Intent intent = new Intent(HomePageFreeVersion.this,
						HomePageFreeVersion.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
						| Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);

				finish();
				overridePendingTransition(0, 0);

				startActivity(intent);
				overridePendingTransition(0, 0);
			}
			
			else {

				Toast.makeText(context, "Something went wrong with your internet connectivity, pleaswe try again later.", Toast.LENGTH_SHORT).show();
				
			}
			
			
			
			
		}

	};
	
	
	
	

	@Override
	protected void onResume() {
		super.onResume();
		// Check device for Play Services APK.
		checkPlayServices();

	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (TextUtils.isEmpty(registrationId)) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device will send
					// upstream messages to a server that echo back the message
					// using the
					// 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Log.e("GCM SERVER RESPONSE", msg);
			}
		}.execute(null, null, null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(HomePageFreeVersion.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		String deviceId = Secure.getString(this.getContentResolver(),
				Secure.ANDROID_ID);
		RequestParams params = new RequestParams();
		params.put("gcm_id", regid);
		params.put("device_id", deviceId);
		AppRestClient.post(URLHelper.URL_GCM_REGISTER, params,
				serverResponseHandler);

	}

	private void setRegisteredToServer(boolean b) {

		SharedPreferencesHelper.getInstance().setBoolean(
				AppConstant.GCM_REGISTRATION_SERVER, b);
	}

	private boolean getRegisteredToServer() {

		return SharedPreferencesHelper.getInstance().getBoolean(
				AppConstant.GCM_REGISTRATION_SERVER, false);
	}

	AsyncHttpResponseHandler serverResponseHandler = new AsyncHttpResponseHandler() {
		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			HttpResponseException hre = (HttpResponseException) arg0;
			int statusCode = hre.getStatusCode();
			if (statusCode == 401) {

			} else {

			}

		}

		@Override
		public void onStart() {
			super.onStart();
			// uiHelper.showLoadingDialog(getString(R.string.loading_dialog_text)+" "+TAG+"...");
		}

		@Override
		public void onSuccess(int statusCode, String content) {
			super.onSuccess(statusCode, content);
			// uiHelper.dismissLoadingDialog();
			Log.e("RESPONSE", content);

			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					content);

			if (wrapper.getStatus().getCode() == AppConstant.RESPONSE_CODE_SUCCESS) {
				setRegisteredToServer(true);
			} else {
				setRegisteredToServer(false);
			}
		}

	};

	public void loadHome() {
		setActionBarNormal();
		getSupportFragmentManager()

				.beginTransaction()
				.replace(R.id.pager_frame,
						CommonChildFragment.newInstance(-1, ""), TAG).commit();

		listAdapter.initializeStates();
		listAdapter.notifyDataSetChanged();
	}

	public void loadCategory(int cat, String subcat) {
		setActionBarNormal();
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.pager_frame,
						CommonChildFragment.newInstance(cat, subcat), TAG)
				.commit();
	}


}

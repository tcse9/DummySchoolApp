package com.champs21.schoolapp.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import android.util.Log;

import com.champs21.freeversion.HomePageFreeVersion;
import com.champs21.schoolapp.model.School;
import com.champs21.schoolapp.model.User;
import com.champs21.schoolapp.model.UserAuthListener;
import com.champs21.schoolapp.model.UserPaidInfo;
import com.champs21.schoolapp.model.UserWrapper;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.networking.AppRestClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class UserHelper {

	public enum UserTypeEnum {
		ADMIN, OTHER, STUDENT, TEACHER, PARENTS, ALUMNI
	}

	public enum UserAccessType {
		FREE, PAID
	}

	public UserHelper(Context context) {
		super();
		this.context = context;
	}

	public UserHelper(UserAuthListener uListener, Context con) {
		super();
		this.uListener = uListener;
		this.context = con;
	}

	private UserAuthListener uListener;
	private Context context;
	public User logedInUser;

	public void doLogIn(String userName, String password) {
		User u = new User();
		u.setUsername(userName);
		u.setPassword(password);
		doLogIn(u);
	}

	public void doLogIn() {
		doLogIn(getUser());
	}

	public void doLogIn(User user) {

		RequestParams params = new RequestParams();
		params.put("username", user.getUsername());
		params.put("password", user.getPassword());
		params.put("udid", SchoolApp.getInstance().getUDID());
		Log.e("GCM_ID", getRegistrationId(context));
		params.put("gcm_id", getRegistrationId(context));
		this.logedInUser = user;
		AppRestClient.post(URLHelper.URL_LOGIN, params, logInHandler);
	}

	private SharedPreferences getGcmPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return this.context
				.getSharedPreferences(
						HomePageFreeVersion.class.getSimpleName(),
						Context.MODE_PRIVATE);

	}

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

	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(
				HomePageFreeVersion.PROPERTY_REG_ID, "");
		if (TextUtils.isEmpty(registrationId)) {
			Log.i(HomePageFreeVersion.TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(
				HomePageFreeVersion.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(HomePageFreeVersion.TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	public void createUser(User user) {
		RequestParams params = new RequestParams();
		params.put("password", user.getPassword());
		params.put("email", user.getEmail());
		params.put("user_type", user.getType().ordinal() + "");
		this.logedInUser = user;
		AppRestClient.post(URLHelper.FREE_USER_CREATE, params, registerHandler);

	}

	public void updateUser(User user) {
		RequestParams params = new RequestParams();
		if (user.getType() != null)
			params.put("user_type", user.getType().ordinal() + "");
		params.put(RequestKeyHelper.USER_ID, user.getUserId());
		params.put(RequestKeyHelper.FIRST_NAME, user.getFirstName());
		params.put(RequestKeyHelper.LAST_NAME, user.getLastName());
		params.put(RequestKeyHelper.GENDER, user.getGender() + "");
		params.put(RequestKeyHelper.MID_NAME, user.getMiddleName());
		params.put(RequestKeyHelper.NICK_NAME, user.getNickName());
		params.put(RequestKeyHelper.MOBILE, user.getMobileNum());
		params.put(RequestKeyHelper.DOB, user.getDob());
		params.put(RequestKeyHelper.OCCUPATION, user.getOccupation());
		params.put(RequestKeyHelper.GRADE_IDS, user.getGradeIds());
		params.put(RequestKeyHelper.SCHOOL_NAME, user.getSchoolName());
		params.put(RequestKeyHelper.MEDIUM, user.getMedium());
		params.put(RequestKeyHelper.TEACHING_FOR, user.getTeachinFor());
		params.put(RequestKeyHelper.CITY, user.getCity());
		this.logedInUser = user;

		AppRestClient.post(URLHelper.FREE_USER_CREATE, params, registerHandler);
	}

	public void updateProfilePicture(File file) {
		User user = getUser();
		this.logedInUser = user;
		RequestParams params = new RequestParams();
		params.put(RequestKeyHelper.USER_ID, user.getUserId());
		try {
			params.put(RequestKeyHelper.PROFILE_IMAGE, file);
		} catch (FileNotFoundException e) {
		}
		AppRestClient.post(URLHelper.FREE_USER_CREATE, params, registerHandler);
	}

	public void updatePassword(User user, String newPassword,
			String previousPassword) {
		RequestParams params = new RequestParams();
		if (user.getType() != null)
			params.put(RequestKeyHelper.USER_ID, user.getUserId());
		params.put("password", newPassword);
		params.put("previous_password", previousPassword);
		this.logedInUser = user;
		this.logedInUser.setPassword(newPassword);
		AppRestClient.post(URLHelper.FREE_USER_CREATE, params,
				passwordChangeHandler);
	}

	public void fbLogIn(User user) {
		RequestParams params = new RequestParams();
		if (user.getType() != null)
			params.put("user_type", user.getType().ordinal() + "");
		params.put("fb_profile_id", user.getFbId());
		params.put("first_name", user.getFirstName());
		params.put("last_name", user.getLastName());
		params.put("gender", user.getGender() + "");
		params.put("middle_name", user.getMiddleName());
		params.put("nick_name", user.getNickName());
		params.put("mobile_no", user.getMobileNum());
		params.put("email", user.getEmail());
		params.put("profile_image", user.getProfilePicsUrl());
		if (!user.getDob().equals(""))
			params.put("dob", AppUtility.getDateString(user.getDob(),
					AppUtility.DATE_FORMAT_SERVER,
					AppUtility.DATE_FORMAT_FACEBOOK));
		this.logedInUser = user;
		// uListener.onAuthenticationSuccessful();
		AppRestClient.post(URLHelper.FREE_USER_CREATE, params, registerHandler);
	}

	public void gPlusLogIn(User user) {
		RequestParams params = new RequestParams();
		if (user.getType() != null)
			params.put("user_type", user.getType().ordinal() + "");

		params.put("gl_profile_id", user.getgPlusId());
		params.put("first_name", user.getFirstName());
		params.put("last_name", user.getLastName());
		params.put("gender", user.getGender() + "");
		params.put("middle_name", user.getMiddleName());
		params.put("nick_name", user.getNickName());
		params.put("mobile_no", user.getMobileNum());
		params.put("email", user.getEmail());
		String dob = user.getDob();
		if (!TextUtils.isEmpty(dob))
			params.put("dob", user.getDob());
		params.put("profile_image", user.getProfilePicsUrl());
		this.logedInUser = user;
		AppRestClient.post(URLHelper.FREE_USER_CREATE, params, registerHandler);
	}

	public void updateUserWithType(int ordinal) {
		User user = getUser();
		RequestParams params = new RequestParams();
		params.put("user_type", ordinal + "");
		params.put("fb_profile_id", user.getFbId());
		params.put("gl_profile_id", user.getgPlusId());
		params.put("first_name", user.getFirstName());
		params.put("last_name", user.getLastName());
		params.put("gender", user.getGender() + "");
		params.put("middle_name", user.getMiddleName());
		params.put("nick_name", user.getNickName());
		params.put("mobile_no", user.getMobileNum());
		params.put("email", user.getEmail());
		if (!user.getDob().equals(""))
			if (user.getFbId().equals("")) {
				params.put("dob", user.getDob());
			} else
				params.put("dob", AppUtility.getDateString(user.getDob(),
						AppUtility.DATE_FORMAT_SERVER,
						AppUtility.DATE_FORMAT_FACEBOOK));
		params.put("user_id", user.getUserId());
		params.put("profile_image", user.getProfilePicsUrl());
		this.logedInUser = user;
		AppRestClient.post(URLHelper.FREE_USER_CREATE, params, registerHandler);
	}

	private AsyncHttpResponseHandler passwordChangeHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			uListener.onAuthenticationFailed(arg1);
			Log.e("Response****", arg1);

		}

		@Override
		public void onStart() {
			super.onStart();
			uListener.onAuthenticationStart();

		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);

			Log.e("Response****", responseString);

			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					responseString);
			if (wrapper.getStatus().getCode() == 200) {
				UserWrapper userData = GsonParser.getInstance()
						.parseUserWrapper(wrapper.getData().toString());
				if (userData.getUserType() == 0) {
					User u = new User();
					if (userData.isRegistered()) {
						u = userData.getUser();
						u.setUsername(logedInUser.getUsername());
						u.setPassword(logedInUser.getPassword());
						// u.setType(logedInUser.getType());;
						u.setAccessType(UserAccessType.values()[userData
								.getUserType()]);
					} else {
						u = logedInUser;
					}
					storeLoggedInUser(u);
					setLoggedIn(userData.isLoggedIn());
					setRegistered(userData.isRegistered());
					uListener.onPaswordChanged();
				}
				// uListener.onAuthenticationSuccessful();

			} else {
				uListener.onAuthenticationFailed(wrapper.getStatus().getMsg());
			}

		}

	};

	private AsyncHttpResponseHandler registerHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			uListener.onAuthenticationFailed(arg1);
			Log.e("Response****", arg1);

		}

		@Override
		public void onStart() {
			super.onStart();
			uListener.onAuthenticationStart();

		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);

			Log.e("Response****", responseString);

			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					responseString);
			if (wrapper.getStatus().getCode() == 200) {
				UserWrapper userData = GsonParser.getInstance()
						.parseUserWrapper(wrapper.getData().toString());
				if (userData.getUserType() == 0) {
					User u = new User();
					if (userData.isRegistered()) {
						u = userData.getUser();
						u.setUsername(logedInUser.getUsername());
						u.setPassword(logedInUser.getPassword());
						// u.setType(logedInUser.getType());;
						u.setAccessType(UserAccessType.values()[userData
								.getUserType()]);
					} else {
						u = logedInUser;
					}
					storeLoggedInUser(u);
					setLoggedIn(userData.isLoggedIn());
					setRegistered(userData.isRegistered());
					uListener.onAuthenticationSuccessful();
				}
				// uListener.onAuthenticationSuccessful();

			} else {
				uListener.onAuthenticationFailed(wrapper.getStatus().getMsg());
			}
		}
	};

	AsyncHttpResponseHandler logInHandler = new AsyncHttpResponseHandler() {

		@Override
		public void onFailure(Throwable arg0, String arg1) {
			super.onFailure(arg0, arg1);
			uListener.onAuthenticationFailed(arg1);
		}

		@Override
		public void onStart() {
			super.onStart();
			uListener.onAuthenticationStart();
		}

		@Override
		public void onSuccess(int arg0, String responseString) {
			super.onSuccess(arg0, responseString);

			Log.e("Response****", responseString);

			Wrapper wrapper = GsonParser.getInstance().parseServerResponse(
					responseString);
			if (wrapper.getStatus().getCode() == 200) {
				UserWrapper userData = GsonParser.getInstance()
						.parseUserWrapper(wrapper.getData().toString());
				User u = new User();
				u = userData.getUser();
				u.setUsername(logedInUser.getUsername());
				u.setPassword(logedInUser.getPassword());
				u.setSessionID(userData.getSession());
				u.setChildren(userData.getChildren());
				if (userData.getUserType() == 0)
					u.setAccessType(UserAccessType.FREE);
				else {
					u.setAccessType(UserAccessType.PAID);
					u.setPaidInfo(userData.getInfo());
				}
				storeLoggedInUser(u);
				setLoggedIn(true);
				setRegistered(true);
				uListener.onAuthenticationSuccessful();
			} else {
				uListener.onAuthenticationFailed(wrapper.getStatus().getMsg());
			}
		}

	};

	public void saveUserSecret(String secret) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.TOKEN,
				secret);
	}

	public void saveBatchID(String batchId) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.BATCH_ID,
				batchId);
	}

	public void saveUserName(String name) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.USERNAME,
				name);
	}

	public void saveUserPassword(String password) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.PASSWORD,
				password);
	}

	public void saveSchoolId(String id) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.SCHOOL_ID,
				id);
	}

	public void saveSchoolName(String name) {
		SharedPreferencesHelper.getInstance().setString(
				SPKeyHelper.SCHOOL_NAME, name);
	}

	public void saveProfileId(String id) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.PROFILE_ID,
				id);
	}

	public void saveUserType(int ordinal) {
		SharedPreferencesHelper.getInstance().setInt(SPKeyHelper.USER_TYPE,
				ordinal);
	}

	public void saveUserAccessType(int ordinal) {
		SharedPreferencesHelper.getInstance().setInt(SPKeyHelper.ACCESS_TYPE,
				ordinal);
	}

	public void saveUserSession(String session) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.SESSION,
				session);
	}

	public void saveUserChildrenInfoJson(String childrenJsonString) {
		SharedPreferencesHelper.getInstance().setString(
				SPKeyHelper.CHILDREN_INFO_JSON, childrenJsonString);
	}

	public void saveSelectedChildInfoJson(String childJsonString) {
		SharedPreferencesHelper.getInstance().setString(
				SPKeyHelper.SELECTED_CHILD_INFO, childJsonString);
	}

	public void saveUserGplusId(String id) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.GPLUS_ID,
				id);
	}

	public void saveFBId(String id) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.FB_ID, id);
	}

	public void saveLastName(String name) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.LAST_NAME,
				name);
	}

	public void saveMiddleName(String name) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.MID_NAME,
				name);
	}

	public void saveFirstName(String name) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.FIRST_NAME,
				name);
	}

	public void saveDob(String dob) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.DOB, dob);
	}

	public void saveGender(int gender) {
		SharedPreferencesHelper.getInstance()
				.setInt(SPKeyHelper.GENDER, gender);
	}

	public void saveEmail(String email) {
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.EMAIL,
				email);
	}

	public void saveId(String id) {
		SharedPreferencesHelper.getInstance()
				.setString(SPKeyHelper.FREE_ID, id);
	}

	/*
	 * public void saveJoinedSchools(List<School> schools) { School
	 * school=schools.get(0); SharedPreferencesHelper.getInstance()
	 * .setString(SPKeyHelper.FREE_ID, ); }
	 */

	@SuppressWarnings("unchecked")
	public User getUser() {
		User u = new User();

		UserPaidInfo info = new UserPaidInfo();
		info.setBatchId(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.BATCH_ID, ""));
		info.setProfileId(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.PROFILE_ID, ""));
		info.setSchoolId(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.SCHOOL_ID, ""));
		info.setSchool_name(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.SCHOOL_NAME, ""));
		info.setSecret(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.TOKEN, ""));
		u.setPaidInfo(info);

		u.setUsername(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.USERNAME, ""));
		u.setPassword(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.PASSWORD, ""));

		u.setSessionID(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.SESSION, ""));

		u.setFirstName(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.FIRST_NAME, ""));
		u.setLastName(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.LAST_NAME, ""));
		u.setMiddleName(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.MID_NAME, ""));
		u.setNickName(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.NICK_NAME, ""));
		u.setCity(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.CITY, ""));
		u.setDob(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.DOB, ""));
		u.setGender(SharedPreferencesHelper.getInstance().getInt(
				SPKeyHelper.GENDER, 1));
		u.setEmail(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.EMAIL, ""));
		u.setgPlusId(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.GPLUS_ID, ""));
		u.setFbId(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.FB_ID, ""));
		u.setUserId(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.FREE_ID, ""));
		u.setAccessType(getUserAccessType());
		u.setProfilePicsUrl(SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.PROFILE_PICS, ""));

		int type = SharedPreferencesHelper.getInstance().getInt(
				SPKeyHelper.USER_TYPE, 2);
		UserTypeEnum typeEum = UserTypeEnum.values()[type];
		u.setType(typeEum);

		if (u.getType() == UserTypeEnum.PARENTS) {

			Gson gson = new Gson();
			List<UserPaidInfo> childrenList = new ArrayList<UserPaidInfo>();
			Type listType = new TypeToken<List<UserPaidInfo>>() {
			}.getType();
			childrenList = (List<UserPaidInfo>) gson.fromJson(
					SharedPreferencesHelper.getInstance().getString(
							SPKeyHelper.CHILDREN_INFO_JSON, ""), listType);

			u.setChildren((ArrayList<UserPaidInfo>) childrenList);

			UserPaidInfo child = new UserPaidInfo();
			child = gson.fromJson(SharedPreferencesHelper.getInstance()
					.getString(SPKeyHelper.SELECTED_CHILD_INFO, ""),
					UserPaidInfo.class);

			u.setSelectedChild(child);
		}
		u.setJoinedToSchool(SharedPreferencesHelper.getInstance().getBoolean(
				SPKeyHelper.IS_JOINED_SCHOOL, false));

		return u;
	}

	public static School getJoinedSchool() {

		Gson gson = new Gson();
		School school = gson.fromJson(SharedPreferencesHelper.getInstance()
				.getString(SPKeyHelper.JOINED_SCHOOL, ""), School.class);
		return school;

	}

	public static String getUserSecret() {
		return SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.TOKEN, "");
	}

	public static String getUserSession() {
		return SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.SESSION, "");
	}

	public static UserAccessType getUserAccessType() {
		int type = SharedPreferencesHelper.getInstance().getInt(
				SPKeyHelper.ACCESS_TYPE, 0);
		return UserAccessType.values()[type];
	}

	public static String getUserFreeId() {
		return SharedPreferencesHelper.getInstance().getString(
				SPKeyHelper.FREE_ID, "");
	}

	public static boolean isLoggedIn() {
		return SharedPreferencesHelper.getInstance().getBoolean(
				SPKeyHelper.IS_LOGGED_IN, false);

	}

	public static boolean isRegistered() {
		return SharedPreferencesHelper.getInstance().getBoolean(
				SPKeyHelper.IS_REGISTERED, false);

	}

	public static void storeJoinedSchool(School school) {
		Gson gson = new Gson();
		String json = gson.toJson(school);
		SharedPreferencesHelper.getInstance().setString(
				SPKeyHelper.JOINED_SCHOOL, json);
		SharedPreferencesHelper.getInstance().setBoolean(
				SPKeyHelper.IS_JOINED_SCHOOL, true);
	}

	public void storeLoggedInUser(User user) {
		saveUserName(user.getUsername());
		saveUserPassword(user.getPassword());
		saveUserSession(user.getSessionID());
		saveUserGplusId(user.getgPlusId());
		saveFBId(user.getFbId());
		saveFirstName(user.getFirstName());
		saveLastName(user.getLastName());
		saveMiddleName(user.getMiddleName());
		saveNickName(user.getNickName());
		saveCity(user.getCity());
		saveDob(user.getDob());
		saveEmail(user.getEmail());
		saveGender(user.getGender());
		saveId(user.getUserId());
		saveProfilePicUrl(user.getProfilePicsUrl());
		if (user.getAccessType() != null) {
			saveUserAccessType(user.getAccessType().ordinal());
			switch (user.getAccessType()) {
			case FREE:
				saveUserType(user.getType().ordinal());
				if (user.getJoinedSchool().size() > 0) {
					Gson gson = new Gson();
					String json = gson.toJson(user.getJoinedSchool().get(0));
					saveJoinedSchool(json);
					saveIsJoinedSchool(true);
				} else {
					saveIsJoinedSchool(false);
				}
				break;
			case PAID:
				if (user.getJoinedSchool().size() > 0) {
					Gson gson = new Gson();
					String json = gson.toJson(user.getJoinedSchool().get(0));
					saveJoinedSchool(json);
					saveIsJoinedSchool(true);
				} else {
					saveIsJoinedSchool(false);
				}
				saveUserSecret(user.getPaidInfo().getSecret());
				saveSchoolId(user.getPaidInfo().getSchoolId());
				saveSchoolName(user.getPaidInfo().getSchool_name());
				saveProfileId(user.getPaidInfo().getProfileId());
				saveBatchID(user.getPaidInfo().getBatchId());

				if (user.getPaidInfo().isAdmin())
					saveUserType(UserTypeEnum.ADMIN.ordinal());
				if (user.getPaidInfo().isParent())
					saveUserType(UserTypeEnum.PARENTS.ordinal());
				if (user.getPaidInfo().isTeacher())
					saveUserType(UserTypeEnum.TEACHER.ordinal());
				if (user.getPaidInfo().isStudent())
					saveUserType(UserTypeEnum.STUDENT.ordinal());

				if (user.getPaidInfo().isParent()) {
					ArrayList<UserPaidInfo> children = user.getChildren();
					Gson gson = new Gson();
					String json = gson.toJson(children);
					Log.e("json", json);
					saveUserChildrenInfoJson(json);
				}
				break;
			default:
				break;
			}
		}

	}

	public static void saveIsJoinedSchool(boolean b) {
		SharedPreferencesHelper.getInstance().setBoolean(
				SPKeyHelper.IS_JOINED_SCHOOL, b);
	}

	private void saveJoinedSchool(String jsonStr) {
		SharedPreferencesHelper.getInstance().setString(
				SPKeyHelper.JOINED_SCHOOL, jsonStr);
	}

	private void saveCity(String city) {
		// TODO Auto-generated method stub
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.CITY, city);
	}

	private void saveNickName(String nickName) {
		// TODO Auto-generated method stub
		SharedPreferencesHelper.getInstance().setString(SPKeyHelper.NICK_NAME,
				nickName);
	}

	private void saveProfilePicUrl(String profilePicsUrl) {
		// TODO Auto-generated method stub
		SharedPreferencesHelper.getInstance().setString(
				SPKeyHelper.PROFILE_PICS, profilePicsUrl);
	}

	public void storeCurrentChildInfo(UserPaidInfo child) {
		Gson gson = new Gson();
		String json = gson.toJson(child);
		saveSelectedChildInfoJson(json);
	}

	public static void setLoggedIn(boolean flag) {
		SharedPreferencesHelper.getInstance().setBoolean(
				SPKeyHelper.IS_LOGGED_IN, flag);
	}

	public void setRegistered(boolean flag) {
		SharedPreferencesHelper.getInstance().setBoolean(
				SPKeyHelper.IS_REGISTERED, flag);
	}
}

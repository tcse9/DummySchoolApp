/**
 * 
 */
package com.champs21.schoolapp.utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.champs21.schoolapp.model.AcademicCalendarDataItem;
import com.champs21.schoolapp.model.AttendenceEvents;
import com.champs21.schoolapp.model.Batch;
import com.champs21.schoolapp.model.ClassReport;
import com.champs21.schoolapp.model.Comment;
import com.champs21.schoolapp.model.ExamRoutine;
import com.champs21.schoolapp.model.FolderList;
import com.champs21.schoolapp.model.FreeVersionPost;
import com.champs21.schoolapp.model.GoodReadPostAll;
import com.champs21.schoolapp.model.LeaveType;
import com.champs21.schoolapp.model.MainCategory;
import com.champs21.schoolapp.model.MenuData;
import com.champs21.schoolapp.model.ModelContainer;
import com.champs21.schoolapp.model.NotificationReminder;
import com.champs21.schoolapp.model.Period;
import com.champs21.schoolapp.model.ReportCardModel;
import com.champs21.schoolapp.model.RoutineTimeTable;
import com.champs21.schoolapp.model.SchoolEvent;
import com.champs21.schoolapp.model.SchoolEventWrapper;
import com.champs21.schoolapp.model.Student;
import com.champs21.schoolapp.model.StudentAttendance;
import com.champs21.schoolapp.model.SubCategory;
import com.champs21.schoolapp.model.Subject;
import com.champs21.schoolapp.model.Syllabus;
import com.champs21.schoolapp.model.TeacherHomework;
import com.champs21.schoolapp.model.Term;
import com.champs21.schoolapp.model.TermReportItem;
import com.champs21.schoolapp.model.Transport;
import com.champs21.schoolapp.model.User;
import com.champs21.schoolapp.model.UserWrapper;
import com.champs21.schoolapp.model.WeekDay;
import com.champs21.schoolapp.model.Wrapper;
import com.champs21.schoolapp.model.WrapperClubNews;
import com.champs21.schoolapp.model.YearlyAttendanceData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;


/**
 * @author Amit
 *
 */
public class GsonParser {

	private Gson gson;

	public String parseGsonToString(JsonElement jsonElement) {
		return gson.fromJson(jsonElement, String.class);
	}
	
	public ModelContainer parseGson(String object) {
		ModelContainer model = gson.fromJson(object, ModelContainer.class);

		return model;
	}

	public Wrapper parseServerResponse(String object) {
		Wrapper wrapper = gson.fromJson(object, Wrapper.class);
		return wrapper;
	}
	
	public FolderList parseFolderList(String object) {
		FolderList data = gson.fromJson(object, FolderList.class);

		return data;
	}
	
	public RoutineTimeTable parseRoutineTimeTable(String object) {
		RoutineTimeTable data = gson.fromJson(object, RoutineTimeTable.class);

		return data;
	}
	
	public GoodReadPostAll parseGoodreadPostAll(String object) {
		GoodReadPostAll data = gson.fromJson(object, GoodReadPostAll.class);

		return data;
	}

	public AttendenceEvents parseAttendence(String object) {
		AttendenceEvents data = gson.fromJson(object, AttendenceEvents.class);

		return data;
	}

	public YearlyAttendanceData parseYearlyAttendanceData(String object)
	{
		YearlyAttendanceData data = gson.fromJson(object, YearlyAttendanceData.class);
		return data;
	}
	
	public Student parseStudentInfo(String object) {
		Student data = gson.fromJson(object, Student.class);

		return data;
	}
	
	public SchoolEventWrapper parseEventWrapper(String object) {
		SchoolEventWrapper data = gson.fromJson(object, SchoolEventWrapper.class);

		return data;
	}
	
	public WrapperClubNews parseClubWrapper(String object) {
		WrapperClubNews data = gson.fromJson(object, WrapperClubNews.class);
		return data;
	}
	
	public User parseUser(String object) {
		User data = gson.fromJson(object, User.class);
		
		return data;
	}
	
	
    public Map<String, String> parseMsg(String object){
    	Type mapType = new TypeToken<Map<String,String>>() {}.getType();
        Map<String,String> map = gson.fromJson(object, mapType);
        return map;
    }
    
	public UserWrapper parseUserWrapper(String object) {
		UserWrapper data = gson.fromJson(object, UserWrapper.class);
		if(data.getUser()!=null)
			data.getUser().setType();
		return data;
	}
	
	public ClassReport parseClassReport(String object) {
		ClassReport data = gson.fromJson(object, ClassReport.class);
		return data;
	}
	
	public List<MenuData> parseMenu(String object) {

		List<MenuData> datas=new ArrayList<MenuData>();
		Type listType = new TypeToken<List<MenuData>>(){}.getType();
		datas = (List<MenuData>) gson.fromJson(object, listType);
		return datas;
	}
	
	public List<Batch> parseBatchList(String object) {

		List<Batch> dataList=new ArrayList<Batch>();
		Type listType = new TypeToken<List<Batch>>(){}.getType();
		dataList = (List<Batch>) gson.fromJson(object, listType);
		return dataList;
	}
	
	public List<StudentAttendance> parseStudentList(String object) {

		List<StudentAttendance> dataList=new ArrayList<StudentAttendance>();
		Type listType = new TypeToken<List<StudentAttendance>>(){}.getType();
		dataList = (List<StudentAttendance>) gson.fromJson(object, listType);
		return dataList;
	}
	
	
	public List<LeaveType> parseLeaveTypeList(String object) {

		List<LeaveType> dataList=new ArrayList<LeaveType>();
		Type listType = new TypeToken<List<LeaveType>>(){}.getType();
		dataList = (List<LeaveType>) gson.fromJson(object, listType);
		return dataList;
	}
	
	public List<Subject> parseSubject(String object) {

		List<Subject> datas=new ArrayList<Subject>();
		Type listType = new TypeToken<List<Subject>>(){}.getType();
		datas = (List<Subject>) gson.fromJson(object, listType);
		return datas;
	}
	public List<SchoolEvent> parseEvents(String object) {

		List<SchoolEvent> events=new ArrayList<SchoolEvent>();
		Type listType = new TypeToken<List<SchoolEvent>>(){}.getType();
		events = (List<SchoolEvent>) gson.fromJson(object, listType);
		return events;
	}
	
	public List<AcademicCalendarDataItem> parseAcademicCalendarData(String object) {

		List<AcademicCalendarDataItem> events=new ArrayList<AcademicCalendarDataItem>();
		Type listType = new TypeToken<List<AcademicCalendarDataItem>>(){}.getType();
		events = (List<AcademicCalendarDataItem>) gson.fromJson(object, listType);
		return events;
	}
		
	public List<ExamRoutine> parseExamRoutine(String object) {

		List<ExamRoutine> events=new ArrayList<ExamRoutine>();
		Type listType = new TypeToken<List<ExamRoutine>>(){}.getType();
		events = (List<ExamRoutine>) gson.fromJson(object, listType);
		return events;
	}
	public ReportCardModel parseReports(String object) {

		ReportCardModel data = gson.fromJson(object, ReportCardModel.class);

		return data;
	}

	public TermReportItem parseTermReport(String object){
		TermReportItem data = gson.fromJson(object, TermReportItem.class);

		return data;
	}



	public ArrayList<Term> parseTerm(String object) {
		ArrayList<Term> allTerm = new ArrayList<Term>();
		allTerm  = new Gson().fromJson(object, new TypeToken<ArrayList<Term>>() {
		}.getType());
		
		return allTerm;
	}
	public ArrayList<Period> parsePeriod(String object) {
		ArrayList<Period> allPeriod = new ArrayList<Period>();
		allPeriod = new Gson().fromJson(object, new TypeToken<ArrayList<Period>>(){
		}.getType());
		return allPeriod;
	}
	
	public ArrayList<FreeVersionPost> parseFreeVersionPost(String object) {
		ArrayList<FreeVersionPost> allPost = new ArrayList<FreeVersionPost>();
		allPost = new Gson().fromJson(object, new TypeToken<ArrayList<FreeVersionPost>>(){
		}.getType());
		return allPost;
	}
	public ArrayList<NotificationReminder> parseNotification(String object) {
		ArrayList<NotificationReminder> allPost = new ArrayList<NotificationReminder>();
		allPost = new Gson().fromJson(object, new TypeToken<ArrayList<NotificationReminder>>(){
		}.getType());
		return allPost;
	}
	
	public ArrayList<TeacherHomework> parseTeacherHomework(String object) {
		ArrayList<TeacherHomework> allPost = new ArrayList<TeacherHomework>();
		allPost = new Gson().fromJson(object, new TypeToken<ArrayList<TeacherHomework>>(){
		}.getType());
		return allPost;
	}
	public ArrayList<Comment> parseFreeVersionComment(String object) {
		ArrayList<Comment> allComment = new ArrayList<Comment>();
		allComment = new Gson().fromJson(object, new TypeToken<ArrayList<Comment>>(){
		}.getType());
		return allComment;
	}
	
	public ArrayList<SubCategory> parseSubCategory(String object) {
		ArrayList<SubCategory> allSubCategory = new ArrayList<SubCategory>();
		allSubCategory = new Gson().fromJson(object, new TypeToken<ArrayList<SubCategory>>(){
		}.getType());
		return allSubCategory;
	}
	public ArrayList<Period> parseWeekPeriod(String object) {

		ArrayList<Period> allWeekPeriod = new  ArrayList<Period>();
		allWeekPeriod = new Gson().fromJson(object,new TypeToken<ArrayList<Period>>(){
		}.getType());
		return allWeekPeriod;
	}
	
	public ArrayList<ExamRoutine> parseExam(String object) {
		ArrayList<ExamRoutine> allExam = new ArrayList<ExamRoutine>();
		allExam = new Gson().fromJson(object, new TypeToken<ArrayList<ExamRoutine>>(){
		}.getType());
		return allExam;
	}
	
	
	
	public ArrayList<Transport> parseTransportSchedule(String object) {
		ArrayList<Transport> data = new ArrayList<Transport>();
		data = new Gson().fromJson(object, new TypeToken<ArrayList<Transport>>(){
		}.getType());
		return data;
	}
	
	
	public ArrayList<FreeVersionPost> parseFreeVersionHome(String object) {
		ArrayList<FreeVersionPost> data = new ArrayList<FreeVersionPost>();
		data = new Gson().fromJson(object, new TypeToken<ArrayList<FreeVersionPost>>(){
		}.getType());
		return data;
	}
	
	public ArrayList<MainCategory> parseMainCategories(String object) {
		ArrayList<MainCategory> data = new ArrayList<MainCategory>();
		data = new Gson().fromJson(object, new TypeToken<ArrayList<MainCategory>>(){
		}.getType());
		return data;
	}
	
	public List<RoutineTimeTable> parseClassList(String object) {
		List<RoutineTimeTable> allClasses = new ArrayList<RoutineTimeTable>();
		allClasses = new Gson().fromJson(object, new TypeToken<List<RoutineTimeTable>>() {}.getType());
		
		return allClasses;
	}
	
	public List<WeekDay> parseWeekDays(String object) {
		List<String> daysString = new ArrayList<String>();
		daysString = gson.fromJson(object, new TypeToken<List<String>>() {}.getType());
		
		List<WeekDay> days=new ArrayList<WeekDay>();
		for(int i=0;i<daysString.size();i++)
		{
			WeekDay day=new WeekDay();
			day.setName(daysString.get(i));
			day.setId(i+"");
			days.add(day);
		}
		
		return days;
	}
	
	
	public ArrayList<Syllabus> parseTermSyllabus(String object) {
		ArrayList<Syllabus> allSyllabus = new ArrayList<Syllabus>();
		allSyllabus = new Gson().fromJson(object, new TypeToken<ArrayList<Syllabus>>() {}.getType());
		
		return allSyllabus;
	}
	//############################################# Singleton ######################################################

	// Singleton Stuffs
	private static GsonParser instance = null;

	private GsonParser() {
		// Exists only to defeat instantiation.
		gson = new Gson();
	}

	public static synchronized GsonParser getInstance() {
		if (instance == null) {
			instance = new GsonParser();
		}
		return instance;
	}

	public static void destroyInstance() {
		instance = null;
	}
}

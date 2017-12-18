package test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.quartz.impl.calendar.HolidayCalendar;
import org.quartz.impl.calendar.WeeklyCalendar;

import com.mcg.scheduler.CalendarHelper;
import com.mcg.scheduler.ScheduleController;
import com.mcg.scheduler.Scheduler;

public class testCalendar {

	public static void main(String args[]) {
		// TODO Auto-generated constructor stub
		//CalendarHelper ch = new CalendarHelper();
		//ScheduleController scontroller = new ScheduleController();
		try {
		    System.out.println("testCalendar.main()" + "test|test2".substring(0, "test|test2".lastIndexOf("|")));
		   // scontroller.startScheduler("../../../config/quartz.properties");
		    /*HashMap<String, Object> scheduleParameters = new HashMap<String, Object>();
            scheduleParameters.put("jobName", "TestJob");
            scheduleParameters.put("jobGroupName","TestJobGroup");
            scheduleParameters.put("triggerName", "TestJobTrigger");
            scheduleParameters.put("triggerGroupName", "TestJobTriggerGroup");
            scheduleParameters.put("cron", "0 0/25 * 1/1 * ? *");
            scheduleParameters.put("timeZone", "IST");
            scheduleParameters.put("startAtDateTime", "2014-01-08 00:00:00");
            scheduleParameters.put("endDateTime", "");
            scheduleParameters.put("calendarName", "");
            scheduleParameters.put("jobData", new HashMap<String,String>());
		    scontroller.addSchedule(scheduleParameters);
            scheduleParameters.put("jobName", "TestJob");
            scheduleParameters.put("jobGroupName","TestJobGroup");
            scheduleParameters.put("triggerName", "TestJobTrigger2");
            scheduleParameters.put("triggerGroupName", "TestJobTriggerGroup");
            scheduleParameters.put("cron", "0 0/25 * 1/1 * ? *");
            scheduleParameters.put("timeZone", "IST");
            scheduleParameters.put("startAtDateTime", "2014-01-08 00:00:00");
            scheduleParameters.put("endDateTime", "");
            scheduleParameters.put("calendarName", "");
            scheduleParameters.put("jobData", new HashMap<String,String>());
            scontroller.addSchedule(scheduleParameters);*/
		        //"04/10/2014;05/10/2014;11/10/2014;12/10/2014;18/10/2014;19/10/2014;25/10/2014;26/10/2014;01/11/2014;02/11/2014;08/11/2014;09/11/2014"
		    /*h.addCalendar("TestCalendar", "04/10/2014;05/10/2014;11/10/2014;12/10/2014;18/10/2014;19/10/2014;25/10/2014;26/10/2014;01/11/2014;02/11/2014;08/11/2014;09/11/2014" , true, "GMT+1000", new String[] {"1", "5"});
            System.out.println("testCalendar.main()" + scontroller.getSchedulerStatus());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ssZ");
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1000"));
            //cal.setTimeZone(TimeZone.getTimeZone("GMT"));
            sdf.setCalendar(cal);
            //System.out.println("testCalendar.main()" + sdf.parse("04/01/2014"));
            System.out.println("testCalendar.main()" + sdf.format(cal.getTime()));
            cal.setTimeInMillis(scontroller.getCalendarByName("TestCalendar").getNextIncludedTime(System.currentTimeMillis()));
            HolidayCalendar hcal = (HolidayCalendar) scontroller.getCalendarByName("TestCalendar");
            WeeklyCalendar wcal = (WeeklyCalendar) hcal.getBaseCalendar();
            boolean[] temp = wcal.getDaysExcluded();
            for (int i=0; i<temp.length;i++) {
            System.out.println("testCalendar.main()" + temp[i]);
            }
            System.out.println("testCalendar.main()" + sdf.format(cal.getTime()));
            System.out.println(cal.getTime() + "" + ch.listAllHolidays("TestCalendar") + hcal.getTimeZone() + hcal.getNextIncludedTime(System.currentTimeMillis()));
			//System.out.print(CalendarHelper.getAllDates(2013, 1));
*/		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

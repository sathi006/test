package com.mcg.scheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.quartz.impl.calendar.HolidayCalendar;
import org.quartz.impl.calendar.WeeklyCalendar;

/**
 * <p>
 * Class that provides Static Methods to create and manage the Business
 * Calendar.
 *
 */
public class CalendarHelper {

    /**
     * Number of Days.
     */
    private static final int DAYS = 7;

    /**
     * SUNDAY.
     */
    private static final int SUNDAY = 1;

    /**
     * SATURDAY.
     */
    private static final int SATURDAY = 7;

    /**
     * Method used to create a new instance of Calendar with provided
     * Holidays & Weekends.
     *
     * @param calendarName String
     * @param holidays String
     * @param timeZone String
     * @param weekends String List
     * @return HolidayCalendar
     * @throws Exception Exception
     */

    private static HolidayCalendar createCalendar(final String calendarName,
        final String holidays, final String timeZone,
        final String[] weekends)
            throws Exception {
            HolidayCalendar calendar = new HolidayCalendar();
          //  Calendar today = new GregorianCalendar();
            WeeklyCalendar weeklyCal = new WeeklyCalendar();
            Date holiday = null;
            String tempHolidays = holidays;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ssZ");
            TimeZone caltimeZone = TimeZone.getTimeZone(timeZone);
            calendar.setTimeZone(caltimeZone);
           // sdf.setTimeZone(caltimeZone);
           // today.setTimeZone(caltimeZone);
           // TimeZone.setDefault(caltimeZone);
            calendar.setDescription(calendarName);
            weeklyCal.setDayExcluded(SATURDAY, false);
            weeklyCal.setDayExcluded(SUNDAY, false);
            weeklyCal.setTimeZone(caltimeZone);
            if (weekends != null && weekends.length != 0) {
            for (String i : weekends) {
                    weeklyCal.setDayExcluded(Integer.parseInt(i), true);
            }
            }
            calendar.setBaseCalendar(weeklyCal);
/*            int currentYear = today.get(Calendar.YEAR);
            for (String i : weekends) {
                if (tempHolidays == null || tempHolidays.isEmpty()) {
                  tempHolidays = getAllDates(currentYear, Integer.parseInt(i));
                } else {
                    tempHolidays = tempHolidays + ";" + getAllDates(currentYear,
                            Integer.parseInt(i));
                }
            }*/
            if (tempHolidays != null && !tempHolidays.isEmpty()) {
            StringTokenizer st = new StringTokenizer(tempHolidays, ";");
            /*String thisYear = Integer.toString(currentYear);*/
            String tempDate = "";
            while (st.hasMoreTokens()) {
                tempDate = st.nextToken() + " 00:00:00"
                        + caltimeZone.getDisplayName();
                holiday = sdf.parse(tempDate);
                calendar.addExcludedDate(holiday);
            }
            }
          //  TimeZone.setDefault(SERVERTIMEZONE);
            return calendar;
    }

    /**
     * Method used to generate any set of week days in a particular year.
     *
     * @param year Integer
     * @param dayOfWeek Integer
     * @return DatesList
     * @throws Exception Exception
     */
    @SuppressWarnings("unused")
    private static String getAllDates(final int year, final int dayOfWeek)
          throws Exception {
        String dayList = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM",
                Locale.getDefault());
        Calendar sysCal = new GregorianCalendar(year, Calendar.JANUARY, 1);
        if (!(dayOfWeek >= 1 && dayOfWeek <= DAYS)) {
              throw new Exception("Invalid Weekend Day Specified");
        }
        /* Loop until the first day of week */
        for (;;) {
            if (sysCal.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                  break;
            }
            sysCal.add(Calendar.DAY_OF_WEEK, 1);
        }
        dayList = sdf.format(sysCal.getTime());
        // Loop entire year to get all list of dates for
        // particular day of week.
        while (true) {
            sysCal.add(Calendar.DAY_OF_WEEK, DAYS);
            if (sysCal.get(Calendar.YEAR) != year) {
                break;
            }
            dayList = dayList + ";" + sdf.format(sysCal.getTime());
        }
        return dayList;
    }

    /**
     * Method to create and add a calendar with given parameters to the
     * scheduler.
     *
     * @param calendarName String
     * @param holidays String
     * @param replace boolean
     * @param timeZone String
     * @param weekends String List
     * @return Status
     * @throws Exception Exception
     */
    public final boolean addCalendar(final String calendarName,
            final String holidays, final boolean replace,
            final String timeZone, final String[] weekends) throws Exception {
        org.quartz.Scheduler scheduler = Scheduler.getScheduler();
        HolidayCalendar calendar = createCalendar(calendarName, holidays,
                timeZone, weekends);
        scheduler.addCalendar(calendarName, calendar, replace, true);
        return true;
    }

    /**
     * Method to check whether a particular day is excluded for execution for a
     * particular year.
     *
     * @param calendarName String
     * @param dayToCheck String
     * @return isDayExcluded
     * @throws Exception Exception
     */
    public final boolean isDayExcluded(final String calendarName,
            final String dayToCheck)
            throws Exception {
        org.quartz.Scheduler sche = Scheduler.getScheduler();
        HolidayCalendar calendar = (HolidayCalendar) sche
                .getCalendar(calendarName);
        Calendar today = new GregorianCalendar();
        Date dateToCheck = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault());
       // sdf.setCalendar(today);
        String thisYear = Integer.toString(today.get(Calendar.YEAR));
        dateToCheck = sdf.parse(dayToCheck + "/" + thisYear);
        return !(calendar.isTimeIncluded(dateToCheck.getTime()));
    }

    /**
     * Method to list all Holidays for a given Calendar.
     *
     * @param calendarName String
     * @return HolidaysList
     * @throws Exception Exception
     */
    public final List<String> listAllHolidays(final String calendarName)
            throws Exception {
        org.quartz.Scheduler sche = Scheduler.getScheduler();
        HolidayCalendar calendar = (HolidayCalendar) sche
                .getCalendar(calendarName);
        List<String> holidays = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",
                Locale.getDefault());
        sdf.setTimeZone(calendar.getTimeZone());
        SortedSet<Date> excludedDates = calendar.getExcludedDates();
        for (Date i : excludedDates) {
            holidays.add(sdf.format(i));
        }
        return holidays;
    }

    /**
     *
     * @param calendarName String
     * @return boolean
     * @throws Exception Exception
     */
    public final boolean deleteCalendar(
            final String calendarName) throws Exception {
        org.quartz.Scheduler sche = Scheduler.getScheduler();
        return sche.deleteCalendar(calendarName);
    }

    /**
     *
     * @param calendarName String
     * @return boolean boolean
     * @throws Exception Exception
     */
    @SuppressWarnings("unused")
    private boolean removeAllExcludedDates(
            final String calendarName) throws Exception {
        org.quartz.Scheduler sche = Scheduler.getScheduler();
        HolidayCalendar cal = (HolidayCalendar) sche.getCalendar(calendarName);
        Set<Date> excludedDates = cal.getExcludedDates();
        for (Date temp : excludedDates) {
           cal.removeExcludedDate(temp);
        }
        return true;
    }
}

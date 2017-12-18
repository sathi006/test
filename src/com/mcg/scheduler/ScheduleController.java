package com.mcg.scheduler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.quartz.Calendar;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that provides methods to configure and manage schedules. This class
 * also provides methods to manage scheduler.
 *
 */
public class ScheduleController {

    /**
     * THREE.
     */
    public static final int THREE = 3;

    /**
     * NINTY.
     */
    public static final int NINTY = 90;

    /**
     * PIPE_SEPERATOR.
     */
    public static final String PIPE_SEPERATOR = "|";

    /**
     * Log.
     */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(ScheduleController.class);

    /**
     * Method used to add a schedule to SmartBatch Scheduler using the
     * Parameters Job Name, Job Group Name, Trigger Name, Trigger Group Name,
     * Start TimeStamp, End TimeStamp, Time Zone, Cron Expression and Business
     * Calendar.
     *
     * @param scheduleParameters
     *            HashMap
     * @return Status
     * @throws Exception
     *             Exception
     */
    @SuppressWarnings("rawtypes")
    public final boolean addSchedule(
        final HashMap<String, Object> scheduleParameters) throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
        Locale.getDefault());
    String endDateTime = (String) scheduleParameters.get("endDateTime");
    String triggerName = (String) scheduleParameters.get("triggerName");
    String triggerGroupName = (String) scheduleParameters
        .get("triggerGroupName");
    String cron = (String) scheduleParameters.get("cron");
    String timeZone = (String) scheduleParameters.get("timeZone");
    String jobName = (String) scheduleParameters.get("jobName");
    String jobGroupName = (String) scheduleParameters.get("jobGroupName");
    String calendarName = (String) scheduleParameters.get("calendarName");
    @SuppressWarnings("unchecked")
    Map<String, String> jobData = (Map<String, String>) scheduleParameters
        .get("jobData");
    JobDataMap jobDataMap = new JobDataMap(jobData);
    JobDetail job = JobBuilder.newJob(SchedulerJob.class)
        .withIdentity(jobName.concat(PIPE_SEPERATOR).
               concat(triggerName), jobGroupName).
               usingJobData(jobDataMap)
        .build();
    CronTrigger trigger;
    Date startAtDateTime = df.parse((String) scheduleParameters
        .get("startAtDateTime"));
    startAtDateTime = convertTimeZone(startAtDateTime,
        TimeZone.getTimeZone(timeZone), TimeZone.getDefault());
    if (endDateTime != null && !(endDateTime.isEmpty())) {
        Date endAtDateTime = df.parse(endDateTime);
        endAtDateTime = convertTimeZone(endAtDateTime,
            TimeZone.getTimeZone(timeZone), TimeZone.getDefault());
        trigger = TriggerBuilder
            .newTrigger()
            .withIdentity(
                TriggerKey
                    .triggerKey(triggerName, triggerGroupName))
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionFireAndProceed().inTimeZone(
                    TimeZone.getTimeZone(timeZone)))
            .startAt(startAtDateTime).endAt(endAtDateTime).build();
    } else {
        trigger = TriggerBuilder
            .newTrigger()
            .withIdentity(
                TriggerKey
                    .triggerKey(triggerName, triggerGroupName))
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionFireAndProceed().inTimeZone(
                    TimeZone.getTimeZone(timeZone)))
            .startAt(startAtDateTime).build();

    }
    if (calendarName != null && !calendarName.isEmpty()) {
        TriggerBuilder tb = trigger.getTriggerBuilder();
        List<String> availableCalendars = listCalendars();
        if (availableCalendars.contains(calendarName)) {
        tb.modifiedByCalendar(calendarName);
        trigger = (CronTrigger) tb.build();
        } else {
        throw new Exception("Calendar Name is not configured"
            + " with the scheduler");
        }
    }
    /* Capturing Logging information */
    LOGGER.info("-------Adding Schedule Started"
        + " with below details at-------"
        + new Date());
    LOGGER.info("----Scheduler Name----  " + scheduler.getSchedulerName());
    LOGGER.info("----Job Name----  " + jobName);
    LOGGER.info("----Job Group----  " + jobGroupName);
    LOGGER.info("----Trigger Name----  " + triggerName);
    LOGGER.info("----Trigger Group----  " + triggerGroupName);
    LOGGER.info("----CRON----  " + cron);
    LOGGER.info("----Calendar Name----" + calendarName);
    LOGGER.info("----Start Time---" + startAtDateTime);
    LOGGER.info("----End Time---" + endDateTime);
    LOGGER.info("----TimeZone---" + timeZone);
    /* Adding a schedule */
    if (trigger != null) {
        scheduler.scheduleJob(job, trigger);
    } else {
        LOGGER.info("Trigger object was NULL");
        throw new Exception("Something went wrong while creating Trigger");
    }
    return true;
    }

    /**
     * Convert time zone.
     *
     * @param date the date
     * @param fromTZ the from tz
     * @param toTZ the to tz
     * @return the java.util. date
     */
    private java.util.Date convertTimeZone(final java.util.Date date,
        final TimeZone fromTZ, final TimeZone toTZ) {
    long fromTZDst = 0;
    if (fromTZ.inDaylightTime(date)) {
        fromTZDst = fromTZ.getDSTSavings();
    }

    long fromTZOffset = fromTZ.getRawOffset() + fromTZDst;

    long toTZDst = 0;
    if (toTZ.inDaylightTime(date)) {
        toTZDst = toTZ.getDSTSavings();
    }
    long toTZOffset = toTZ.getRawOffset() + toTZDst;

    return new java.util.Date(date.getTime() + (toTZOffset - fromTZOffset));
    }

    /**
     * Method to unschedule a schedule from the SmartBatch scheduler.
     *
     * @param triggerName
     *            String
     * @param triggerGroupName
     *            String
     * @return Status
     * @throws Exception
     *             Exception
     */
    public final boolean destroySchedule(final String triggerName,
        final String triggerGroupName) throws Exception {
    boolean status = false;
    try {
        org.quartz.Scheduler scheduler = Scheduler.getScheduler();
        /* logging */
        LOGGER.info("-------Destroying a Schedule started with"
            + " below details at-------" + new Date());
        LOGGER.info("----Trigger Name----  " + triggerName);
        LOGGER.info("----Trigger Group----  " + triggerGroupName);
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName,
            triggerGroupName);
        LOGGER.info("Trigger Key : " + triggerKey);
        Trigger triggerToDelete = scheduler.getTrigger(triggerKey);
        LOGGER.info("Trigger to delete: " + triggerToDelete);
        JobKey jobToDelete = triggerToDelete.getJobKey();
        LOGGER.info("Job to delete: " + jobToDelete);
        scheduler.deleteJob(jobToDelete);
        LOGGER.info("-------Destroying a Schedule is Done-------");
        status = true;
    } catch (Exception e) {
        LOGGER.debug(e.getMessage());
        throw new Exception("Exception while destroying"
            + " the schedule : " + e.getMessage());
    }
    return status;
    }

    /**
     * Method to disable a schedule in SmartBatch Scheduler.
     *
     * @param triggerName
     *            String
     * @param triggerGroupName
     *            String
     * @return Status
     * @throws Exception
     *             Exception
     */
    public final boolean disableSchedule(final String triggerName,
        final String triggerGroupName) throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    /* logging */
    LOGGER.info("-------Disabling a Schedule started"
        + " with below details at-------" + new Date());
    LOGGER.info("----Trigger Name----  " + triggerName);
    LOGGER.info("----Trigger Group----  " + triggerGroupName);
    /* Disabling a trigger */
    scheduler.pauseTrigger(TriggerKey.triggerKey(triggerName,
        triggerGroupName));
    LOGGER.info("-------Disabling a Schedule is Done--------");
    return true;
    }

    /**
     * Method to enable a scheduler in the SmartBatch Scheduler.
     *
     * @param triggerName
     *            String
     * @param triggerGroupName
     *            String
     * @return Boolean
     * @throws Exception
     *             Exception
     */
    public final boolean enableSchedule(final String triggerName,
        final String triggerGroupName) throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    /* logging */
    LOGGER.info("-------Enabling a Schedule started with"
        + " below details at-------" + new Date());
    LOGGER.info("----Trigger Name----  " + triggerName);
    LOGGER.info("----Trigger Group----  " + triggerGroupName);
    /* Disabling a trigger */
    scheduler.resumeTrigger(TriggerKey.triggerKey(triggerName,
        triggerGroupName));
    LOGGER.info("-------Enabling a Schedule is Done--------");
    return true;
    }

    /**
     * Method to retrieve the Scheduler Name.
     *
     * @return SchedulerName
     * @throws Exception
     *             Exception
     */
    public final String getSchedulerName() throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    return scheduler.getSchedulerName();
    }

    /**
     * Method to retrieve the SmartBatch Scheduler Status.
     *
     * @return SchedulerStatus
     * @throws Exception
     *             Exception
     */
    public final String getSchedulerStatus() throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    /* Get the Scheduler Status */
    String schedulerStatus = Boolean.toString(scheduler.isShutdown());

    if (schedulerStatus.equalsIgnoreCase("false")) {
        schedulerStatus = "RUNNING";
    } else {
        schedulerStatus = "DOWN";
    }
    return schedulerStatus;
    }

    /**
     * Method to reschedule a schedule in SmartBatch Scheduler.
     *
     * @param scheduleParameters
     *            HashMap
     * @return Status
     * @throws Exception
     *             Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final boolean putSchedule(
        final HashMap<String, Object> scheduleParameters) throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
        Locale.getDefault());
    CronTrigger oldTrigger = null;
    JobDetail newJob = null;
    String endDateTime = (String) scheduleParameters.get("endDateTime");
    String triggerName = (String) scheduleParameters.get("triggerName");
    String triggerGroupName = (String) scheduleParameters
        .get("triggerGroupName");
    String cron = (String) scheduleParameters.get("cron");
    String timeZone = (String) scheduleParameters.get("timeZone");
    String calendarName = (String) scheduleParameters.get("calendarName");
    Map<String, String> jobData = (Map<String, String>) scheduleParameters
        .get("jobData");
    LOGGER.info("Job Data Size : " + jobData.size());
    JobDataMap jobDataMap = new JobDataMap(jobData);
    oldTrigger = (CronTrigger) scheduler.getTrigger(TriggerKey
        .triggerKey(triggerName, triggerGroupName));
    if (jobDataMap != null && jobDataMap.size() != 0) {
    newJob = JobBuilder.newJob(SchedulerJob.class)
        .withIdentity(oldTrigger.getJobKey()).usingJobData(jobDataMap)
        .build();
    } else {
        newJob = scheduler.getJobDetail(oldTrigger.getJobKey());
    }
    /* obtain a builder that would produce the trigger */
    TriggerBuilder tb = oldTrigger.getTriggerBuilder();
    /*
     * update the schedule associated with the builder, and build the new
     * trigger
     */
    /*
     * (other builder methods could be called, to change the trigger in any
     * desired way)
     */
    Trigger newTrigger;
    Date startAtDateTime = df.parse((String) scheduleParameters
        .get("startAtDateTime"));
    startAtDateTime = convertTimeZone(startAtDateTime,
        TimeZone.getTimeZone(timeZone), TimeZone.getDefault());
    if (endDateTime != null && !endDateTime.isEmpty()) {

        Date endAtDateTime = df.parse(endDateTime);
        endAtDateTime = convertTimeZone(endAtDateTime,
            TimeZone.getTimeZone(timeZone), TimeZone.getDefault());
        newTrigger = tb
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionFireAndProceed().inTimeZone(
                    TimeZone.getTimeZone(timeZone)))
            .startAt(startAtDateTime).endAt(endAtDateTime).build();
    } else {
        Date endAtDateTime = null;
        newTrigger = tb
            .withSchedule(
                CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionFireAndProceed().inTimeZone(
                    TimeZone.getTimeZone(timeZone)))
            .startAt(startAtDateTime).endAt(endAtDateTime).build();
    }
    if (calendarName != null && !calendarName.isEmpty()) {
        List<String> availableCalendars = listCalendars();
        if (availableCalendars.contains(calendarName)) {
        tb.modifiedByCalendar(calendarName);
        newTrigger = tb.build();
        } else {
        throw new Exception("Calendar Name is not configured"
            + " with the scheduler");
        }
    } else {
        tb.modifiedByCalendar(null);
        newTrigger = tb.build();
    }
    /* Capturing Logging information */
    LOGGER.info("-------Editing a Schedule started with below"
        + " details at-------" + new Date());
    LOGGER.info("----Trigger Name----  " + triggerName);
    LOGGER.info("----Trigger Group----  " + triggerGroupName);
    /* Adding a schedule */
    if (newTrigger != null) {
        scheduler.deleteJob(oldTrigger.getJobKey());
        scheduler.scheduleJob(newJob, newTrigger);
    } else {
        LOGGER.info("Trigger object was NULL");
        throw new Exception(
            "Something went wrong while creating new Trigger");
    }
    return true;
    }

    /**
     * Method to Start the SmartBatch Scheduler.
     *
     * @param propertyFilePath the property file path
     * @throws Exception             Exception
     */
    public final void startScheduler(
        final String propertyFilePath) throws Exception {
    try {
        org.quartz.Scheduler scheduler = Scheduler
            .getScheduler(propertyFilePath);
        scheduler.start();
    } catch (Exception e) {
        throw new Exception("Exception while starting the"
            + " scheduler : " + e.getMessage());
    }
    }

    /**
     * Method to stop the SmartBatch Scheduler.
     *
     * @throws Exception
     *             Exception
     */
    public final void stopScheduler() throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    scheduler.shutdown();
    }

    /**
     * Method to List all Calendars configured with SmartBatch Scheduler.
     *
     * @return List
     * @throws Exception
     *             Exception
     */
    public final List<String> listCalendars() throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    return scheduler.getCalendarNames();
    }

    /**
     * Gets the all calendars.
     *
     * @return List
     * @throws Exception             Exception
     */
    public final Map<String, Calendar> getAllCalendars() throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    List<String> calendarNames = scheduler.getCalendarNames();
    Map<String, Calendar> calendars = new HashMap<String, Calendar>();
    for (String calendarName : calendarNames) {
        calendars.put(calendarName, scheduler.getCalendar(calendarName));
    }
    return calendars;
    }

    /**
     * Gets the calendar by name.
     *
     * @param calendarName            String
     * @return Calendar Calendar
     * @throws Exception             Exception
     */
    public final Calendar getCalendarByName(final String calendarName)
        throws Exception {
    org.quartz.Scheduler scheduler = Scheduler.getScheduler();
    return scheduler.getCalendar(calendarName);
    }

    /**
     * Gets the job data.
     *
     * @param triggerName the trigger name
     * @param triggerGroupName the trigger group name
     * @return the job data
     * @throws Exception the exception
     */
    public final Map<String, String> getJobData(
        final String triggerName,
        final String triggerGroupName) throws Exception {
    Map<String, String> jobData = new HashMap<String, String>();
    try {
        org.quartz.Scheduler scheduler = Scheduler.getScheduler();
        CronTrigger oldTrigger = (CronTrigger)
            scheduler.getTrigger(TriggerKey
                    .triggerKey(triggerName, triggerGroupName));
        JobDataMap dataMap = scheduler.getJobDetail(
            oldTrigger.getJobKey()).getJobDataMap();
        if (dataMap != null) {
        for (String key : dataMap.getKeys()) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Job Data Key for Trigger "
        + triggerName + ": " + key);
            LOGGER.info("Job Data Value for Key "
        + key + ": " + dataMap.get(key));
        }
        jobData.put(key, String.valueOf(dataMap.get(key)));
        }
        }
        return jobData;
    } catch (Exception exception) {
        LOGGER.error("Exception while retrieving"
            + " Job Data for Trigger" + triggerName, exception);
        throw exception;
    }
    }
}

package com.mcg.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wm.app.b2b.client.Context;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceThread;
import com.wm.app.b2b.server.ThreadManager;
import com.wm.data.IData;
import com.wm.data.IDataCursor;
import com.wm.data.IDataFactory;
import com.wm.data.IDataUtil;
import com.wm.lang.ns.NSName;
import com.wm.util.JournalLogger;

/**
 * This class provides simple Implementation of Job Interface for use with
 * Configuring Schedule.
 *
 */
@PersistJobDataAfterExecution
public class SchedulerJob implements Job {

    /**
     * log.
     */
    private static final Logger LOGGER = LoggerFactory
          .getLogger(SchedulerJob.class);

    /**
     * THREE.
     */
    private static final int THREE = 3;

    /**
     * NINTY.
     */
    private static final int NINTY = 90;

    /** Constant TEN. */
    private static final int TEN = 10;

    /** Constant THIRTY_THOUSAND. */
    private static final int THIRTY_THOUSAND = 30000;

    /**
     * Constant SIXTY_THOUSAND.
     */
    private static final int TWELVEHUNDRED_THOUSAND = 120000;

    /**
     * serverPort.
     */
    private static String serverPort;

    /** refireCount. */
    private static int refireCount;

    /** refireImmediately. */
    private static boolean refireImmediately;

    /** refireDelay. */
    private static int refireDelay;

    /**
     * fireDelay.
     */
    private static int fireDelay;

    /**
     * Implementation of the execute method in Job Interface that executes when
     * ever a schedule is fired.
     *
     * @param context
     *            JobExecutionContext
     * @throws JobExecutionException
     *             Exception
     */
    public final void execute(final JobExecutionContext context)
          throws JobExecutionException {
      JobKey jobKey = context.getJobDetail().getKey();
      LOGGER.info("SchedulerJob says: " + jobKey + " executing at "
            + new Date());
      if (context.getRefireCount() > 0) {
      JournalLogger.log(THREE, NINTY, THREE, "Refiring Trigger "
           + context.getTrigger().getKey().getName());
      } else {
      JournalLogger.log(THREE, NINTY, THREE, "Firing Trigger "
                + context.getTrigger().getKey().getName());
      }
        callService(context);
    }

    static {
      try {
          serverPort = (String) System.getProperty("watt.server.port", "5555");
          refireImmediately = Boolean.parseBoolean(System.getProperty(
                  "watt.smartbatch.scheduler.refireImmediate",
                  Boolean.TRUE.toString()));
          refireCount = Integer.parseInt(System.getProperty(
                  "watt.smartbatch.scheduler.refireCount", "10"));
          refireDelay = Integer.parseInt(System.getProperty(
                  "watt.smartbatch.scheduler.refireDelay", "30000"));
          fireDelay = Integer.parseInt(System.getProperty(
               "watt.smartbatch.scheduler.fireDelay", "120000"));
      } catch (Exception e) {
          LOGGER.error("Unable to retrieve default properties.", e);
          serverPort = "5555";
          refireImmediately = true;
          refireCount = TEN;
          refireDelay = THIRTY_THOUSAND;
          fireDelay = TWELVEHUNDRED_THOUSAND;
      }
    }

    /**
     * Retrieve the server Context.
     *
     * @return Context Context
     */
    @SuppressWarnings("unused")
    private Context getContext() {
      Context context = new Context();
      try {
          String server = com.wm.app.b2b.server.ServerAPI.getServerName();
          context.connect(server + ":" + serverPort);
      } catch (Exception e) {
          JournalLogger.log(THREE, NINTY, THREE,
               "Could not load the Context of Scheduler"
               + " Integration Server : " + e.getMessage());
          e.printStackTrace();
      }
      return context;
    }

    /**
     * Invokes the Integration Server Service to fire schedule.
     *
     * @param jobExecutionContext
     *            JobExecutionContext
     * @throws JobExecutionException JobExecutionException
     */
    private void callService(
          final JobExecutionContext jobExecutionContext)
                throws JobExecutionException {
      String triggerName = null, tempJobName = null;
      IDataCursor onScheduleFireInputCursor = null;
      IDataCursor inputCursor = null;
      int currentRefireCount = 0;
          try {
            String environment = null, domain = null,
                 area = null, plot = null, role = null;
            triggerName = jobExecutionContext.getTrigger().getKey().getName();
            JobDataMap jobDataMap = jobExecutionContext
                  .getMergedJobDataMap();
            String[] jobDataKeys = jobDataMap.getKeys();
            List<IData> jobData = new ArrayList<IData>();
            IData input = IDataFactory.create();
            inputCursor = input.getCursor();
            IData onScheduleFireInput = IDataFactory.create();
            IData jobDataTemp = null;
            for (int index = 0; index < jobDataKeys.length; index++) {
                jobDataTemp = IDataFactory.create();
                IDataCursor jobDataCursor = jobDataTemp.getCursor();
                if ("environment".equalsIgnoreCase(jobDataKeys[index])) {
                  environment = jobDataMap.getString(jobDataKeys[index]);
                } else if ("domain".equalsIgnoreCase(jobDataKeys[index])) {
                  domain = jobDataMap.getString(jobDataKeys[index]);
                } else if ("area".equalsIgnoreCase(jobDataKeys[index])) {
                  area = jobDataMap.getString(jobDataKeys[index]);
                } else if ("plot".equalsIgnoreCase(jobDataKeys[index])) {
                  plot = jobDataMap.getString(jobDataKeys[index]);
                } else if ("role".equalsIgnoreCase(jobDataKeys[index])) {
                  role = jobDataMap.getString(jobDataKeys[index]);
                } else if ("refireCount".equalsIgnoreCase(jobDataKeys[index])) {
                    currentRefireCount = jobDataMap.getInt(jobDataKeys[index]);
                }  else {
                  IDataUtil
                        .put(jobDataCursor, "name", jobDataKeys[index]);
                  IDataUtil.put(jobDataCursor, "value",
                        jobDataMap.getString(jobDataKeys[index]));
                  jobData.add(jobDataTemp);
                  jobDataCursor.destroy();
                  jobDataTemp = null;
                }
            }
            onScheduleFireInputCursor = onScheduleFireInput
                  .getCursor();
            IDataUtil.put(onScheduleFireInputCursor, "triggerName",
                  triggerName);
            tempJobName = jobExecutionContext.getTrigger().
                  getJobKey().getName();
            if (tempJobName != null && tempJobName.contains(
                  ScheduleController.PIPE_SEPERATOR)) {
              tempJobName = tempJobName.
                        substring(0, tempJobName.lastIndexOf(
                                    ScheduleController.PIPE_SEPERATOR));
            }
            IDataUtil.put(onScheduleFireInputCursor, "jobName", tempJobName);
            IDataUtil.put(onScheduleFireInputCursor, "environment",
                  environment);
            IDataUtil.put(onScheduleFireInputCursor, "domain", domain);
            IDataUtil.put(onScheduleFireInputCursor, "area", area);
            IDataUtil.put(onScheduleFireInputCursor, "plot", plot);
            IDataUtil.put(onScheduleFireInputCursor, "role", role);
            if (jobData != null) {
                IDataUtil.put(onScheduleFireInputCursor, "jobData",
                      jobData.toArray(new IData[jobData.size()]));
            }
            IDataUtil.put(inputCursor, "onScheduleFireRequest",
                  onScheduleFireInput);
            ServiceExecutionHook hook = new ServiceExecutionHook();
            ServiceExecutionThread thread = new ServiceExecutionThread(
                 input, hook);
            ThreadManager.runTarget(thread);
            hook.getLatch().await(fireDelay,
                    TimeUnit.MILLISECONDS);
            if (!hook.isSuccess()) {
              throw hook.getException();
            }
          } catch (Exception e) {
            JournalLogger.log(THREE, NINTY, THREE,
                  "Exception while Firing the Trigger "
            + jobExecutionContext.getTrigger().getKey().getName()
            + " - " + jobExecutionContext.getFireInstanceId()
            + " : " + e.getMessage());
            e.printStackTrace();
            JobExecutionException exception =  new JobExecutionException(e);
            if (refireCount > currentRefireCount) {
              currentRefireCount++;
              try {
              JournalLogger.log(THREE, NINTY, THREE, "Waiting for "
                              + jobExecutionContext.
                              getTrigger().getKey().getName()
                              + " - " + jobExecutionContext.getFireInstanceId()
                              + " trigger to be refired. Total Attempts : "
                              + currentRefireCount);
              Thread.sleep(refireDelay);
              } catch (Exception e2) {
                  JournalLogger.log(THREE, NINTY, THREE,
                        "Refire Delay Interrupted"
                        + " due to exception : ", e2);
              }
            if (refireImmediately) {
            exception.setRefireImmediately(true);
            }
            jobExecutionContext.getJobDetail().getJobDataMap().
            put("refireCount",
                  currentRefireCount);
            } else {
              JournalLogger.log(THREE, NINTY, THREE,
                    "Refire Count Expired for Trigger "
                        + jobExecutionContext.getTrigger().getKey().getName()
                        + " - "
                        + jobExecutionContext.getFireInstanceId());
              jobExecutionContext.getJobDetail().getJobDataMap().
              put("refireCount",
                    0);
            }
            throw exception;
          } finally {
            if (onScheduleFireInputCursor != null) {
            onScheduleFireInputCursor.destroy();
            }
            if (inputCursor != null) {
             inputCursor.destroy();
            }
          }
      }

    /**
     *
     * @param key String
     * @return password String
     * @throws Exception Exception
     */
    @SuppressWarnings("unused")
    private String retrievePassword(final String key) throws Exception {
       IData input = IDataFactory.create();
       IData output = IDataFactory.create();
       IDataCursor inputCursor = input.getCursor();
       IDataUtil.put(inputCursor, "key", key);
       NSName nsName = NSName
                  .create("pub.security.outboundPasswords:getPassword");
       ServiceThread thread = Service.doThreadInvoke(nsName, input);
       output = thread.getIData();
       Object secureString = IDataUtil.get(output.getCursor(), "password");
       input = IDataFactory.create();
       IDataUtil.put(input.getCursor(), "secureString", secureString);
       IDataUtil.put(input.getCursor(), "returnAs", "string");
       nsName = NSName.create("pub.security.util:convertSecureString");
       thread = Service.doThreadInvoke(nsName, input);
       output = thread.getIData();
       return IDataUtil.getString(output.getCursor(), "string");
    }
}

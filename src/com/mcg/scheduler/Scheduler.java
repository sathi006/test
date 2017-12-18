package com.mcg.scheduler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;


/**
 * Class that holds the singleton Instance of SmartBatch Scheduler retrieved
 * from Quartz Scheduler Factory.
 *
 */
public final class Scheduler {

    /**
     * DATASOURCE_PROP_NAME.
     */
    private static final String DATASOURCE_PROP_NAME =
          "org.quartz.jobStore.dataSource";

    /**
     * PASSWORD_PROP_NAME.
     */
    private static final String PASSWORD_PROP_NAME = "password";

    /**
     * PASSWORD_ENCRYPTED.
     */
    private static final String PASSWORD_ENCRYPTED =
          "org.quartz.database.password.encrypted";

    /**
     * Scheduler.
     */
    private static org.quartz.Scheduler sched = null;

    /**
     * Synchronized Object.
     */
    private static Object synchronizedObject = new Object();

    /**
     * Unused Constructor.
     */
    private Scheduler() {

    }

    /**
     * Static method that returns the Singleton instance of Scheduler.
     *
     * @param propertyFilePath String
     * @return Scheduler
     * @throws Exception the exception
     */
    public static org.quartz.Scheduler getScheduler(
        final String propertyFilePath)
        throws Exception {
          String encryptedPwd = null;
          String dsName = null;
          InputStream is = null;
    if (sched == null || (sched != null && sched.isShutdown())) {
        synchronized (synchronizedObject) {
            try {
        /*URL url = new URL("file://"
            + ServerAPI.getPackageConfigDir(Service
                .getPackageName())
            + "/schedulerlog4j.properties");
        PropertyConfigurator.configure(url);*/

          Properties props = new Properties();
          is = new BufferedInputStream(new FileInputStream(propertyFilePath));
          props.load(is);
          dsName = props.getProperty(DATASOURCE_PROP_NAME);
          boolean isEncrypted = Boolean.parseBoolean(
                props.getProperty(PASSWORD_ENCRYPTED));
          if (isEncrypted) {
          if (dsName != null && dsName.trim() != "") {
              encryptedPwd = props.getProperty(
                    StdSchedulerFactory.PROP_DATASOURCE_PREFIX
                    + "." + dsName + "." + PASSWORD_PROP_NAME);
              props.setProperty(StdSchedulerFactory.PROP_DATASOURCE_PREFIX
                    + "." + dsName + "." + PASSWORD_PROP_NAME,
                   EncryptorUtils.decryptPassword(encryptedPwd));
          }
          }
        SchedulerFactory sf1 =
            new StdSchedulerFactory(props);
        sched = sf1.getScheduler();
            } finally {
              if (is != null) {
                  is.close();
              }
            }
        }
    }
    return sched;
    }

    /**
     * Gets the scheduler.
     *
     * @return the scheduler
     * @throws SchedulerException the scheduler exception
     */
    public static org.quartz.Scheduler getScheduler()
        throws SchedulerException {
    return sched;
    }
}

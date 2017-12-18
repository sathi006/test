package com.mcg.scheduler;

import com.wm.app.b2b.server.InvokeState;
import com.wm.app.b2b.server.Session;
import com.wm.app.b2b.server.StateManager;
import com.wm.app.b2b.server.User;
import com.wm.app.b2b.server.UserManager;
import com.wm.app.b2b.server.invoke.InvokeManager;
import com.wm.data.IData;
import com.wm.lang.ns.NSName;
import com.wm.util.JournalLogger;
import com.wm.util.lifecycle.LifecycleEvent;
import com.wm.util.lifecycle.LifecycleListener;
import com.wm.util.lifecycle.LifecycleManager;

/**
 * The Class ServiceExecutionThread.
 */
public final class ServiceExecutionThread implements Runnable,
      LifecycleListener {

    /**
     * THREE.
     */
    private static final int THREE = 3;

    /**
     * NINTY.
     */
    private static final int NINTY = 90;

    /**
     * Session.
     */
    private Session session = null;

    /**
     * input.
     */
    private IData input = null;

    /**
     * executionHook.
     */
    private ServiceExecutionHook executionHook;

    /**
     * Instantiates a new service execution thread.
     *
     * @param input1 the input1
     * @param hook ServiceExecutionHook
     */
    public ServiceExecutionThread(final IData input1,
         final ServiceExecutionHook hook) {
      super();
      input = input1;
      this.executionHook = hook;
    }

    /* (non-Javadoc)
     * @see com.wm.util.lifecycle.LifecycleListener#update(
     * com.wm.util.lifecycle.LifecycleEvent)
     */
    @Override
    public void update(final LifecycleEvent arg0) {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
      LifecycleManager.registerLifecycleListener(this);
      User user = UserManager.getUser("Administrator");
      session = StateManager.createContext(Integer.MAX_VALUE, "system", user);
      NSName svcName = NSName
              .create("scbSmartBatchScheduler.services."
                    + "flow.schedule:onScheduleFire");
      InvokeState.setCurrentUser(user);
      InvokeState.setCurrentSession(session);
      if (session != null) {
          User sessionUser = session.getUser();
          /*
           * If there is no user in session, set the InvokeState user as
           * session user.
           */
          if (sessionUser == null) {
            session.setUser(user);
          }
      }
      try {
          // Use InvokeManager to invoke the service
          InvokeManager invokeManager = InvokeManager.getDefault();
          invokeManager.invoke(svcName, input, false);
      } catch (Exception e) {
          JournalLogger.log(THREE, NINTY, THREE, "Exception while executing"
               + " onScheduleFire Service:"
               + e.getMessage());
          this.executionHook.setException(e);
      } finally {
       this.executionHook.getLatch().countDown();
          boolean runCompletedTask = !LifecycleManager.getLifecycleHandler()
                .isKilled();
          if (runCompletedTask) {
            StateManager.deleteContext(session.getSessionID());
            LifecycleManager.unregisterLifecycleListener(this);
          }

      }

    }

    /**
     * Getter for executionHook.
     *
     * @return executionHook ServiceExecutionHook
     */
    public ServiceExecutionHook getExecutionHook() {
        return executionHook;
    }
}

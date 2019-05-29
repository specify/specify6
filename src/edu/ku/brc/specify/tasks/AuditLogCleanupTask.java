/**
 *
 */
package edu.ku.brc.specify.tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.util.Pair;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timo
 *
 */
public class AuditLogCleanupTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(AuditLogCleanupTask.class);

    public static final String AUDIT_LIFESPAN_MONTHS_PREF = "AUDIT_LIFESPAN_MONTHS";
    public static final String     AuditLogCLEANUP             = "AuditLogCleanup";
    public static int			   AUDIT_LIFESPAN_MONTHS = 0;

    protected final AtomicBoolean appIsShuttingDown = new AtomicBoolean(false);
    protected final AtomicBoolean taskIsShutDown = new AtomicBoolean(false);

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public AuditLogCleanupTask()
    {
        super(AuditLogCLEANUP, AuditLogCLEANUP);
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#isPermissionsSettable()
     */
    @Override
    public boolean isPermissionsSettable() {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#initialize()
     */
    @Override
    public void initialize() {
        super.initialize();
        AUDIT_LIFESPAN_MONTHS = AppPreferences.getRemote().getInt(AUDIT_LIFESPAN_MONTHS_PREF, AUDIT_LIFESPAN_MONTHS);
        if (AUDIT_LIFESPAN_MONTHS > 0) {
            //UIRegistry.displayStatusBarText(UIRegistry.getResourceString("AuditLogCleanupTask.RemovingExpiredAuditItems"));
            Thread t = new Thread(new Runnable() {

                /* (non-Javadoc)
                 * @see java.lang.Runnable#run()
                 */
                @Override
                public void run() {
                    try {
                        if (appIsShuttingDown.get()) {
                            return;
                        }
                        int count = BasicSQLUtils.update("delete from spauditlogfield where date_sub(curdate(), Interval " + AUDIT_LIFESPAN_MONTHS + " month) > timestampcreated");
                        log.info("deleted " + count + " expired spauditlogfield entries");
                        count = BasicSQLUtils.update("delete from spauditlog where date_sub(curdate(), Interval " + AUDIT_LIFESPAN_MONTHS + " month) > timestampcreated");
                        log.info("deleted " + count + " expired spauditlog entries");
                    } finally {
                        taskIsShutDown.set(true);
                        //if (UIRegistry.getResourceString("AuditLogCleanupTask.RemovingExpiredAuditItems").equals(UIRegistry.getStatusBar().getText())) {
                        //    UIRegistry.displayStatusBarText("");
                        //}
                    }
                }

            });
            //Lower the priority. WARNING: setting to MIN_PRIORITY was crashing the jvm.
            if (t.getPriority() - 1 > Thread.MIN_PRIORITY) {
                t.setPriority(t.getPriority() - 1);
            }
            t.start();
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#shutdown()
     */
    @Override
    public void shutdown() {
        super.shutdown();
        appIsShuttingDown.set(true);
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(
                    AuditLogCleanupTask.class, ex);
        }
    }


}

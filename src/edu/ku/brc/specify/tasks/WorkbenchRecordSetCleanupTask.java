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
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class WorkbenchRecordSetCleanupTask extends BaseTask 
{
	private static final Logger log = Logger.getLogger(WorkbenchRecordSetCleanupTask.class);

    public static final String     WORKBENCHRECORDSETCLEANUP             = "WorkbenchRecordSetCleanup";
    public static int			   HIDDEN_UPLOAD_RECORDSET_LIFESPAN_DAYS = 30;

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

	public WorkbenchRecordSetCleanupTask()
	{
		super(WORKBENCHRECORDSETCLEANUP, WORKBENCHRECORDSETCLEANUP);
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
	public void initialize() 
	{
		super.initialize();
        HIDDEN_UPLOAD_RECORDSET_LIFESPAN_DAYS = AppPreferences.getRemote().getInt("HIDDEN_UPLOAD_RECORDSET_LIFESPAN_DAYS", HIDDEN_UPLOAD_RECORDSET_LIFESPAN_DAYS);
        Thread t = new Thread(new Runnable() {

			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
		    	try 
		    	{
		    		//Using hibernate to avoid using dbms-specific sql date functions
		    		DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		    		List<Pair<Integer, String>> toRemove = new ArrayList<Pair<Integer, String>>();
		    		long maxAgeInMillis = (long)HIDDEN_UPLOAD_RECORDSET_LIFESPAN_DAYS * 24L * 60L * 60L * 1000L;
		    		long todayInMillis = Calendar.getInstance().getTimeInMillis();
		    		try
		    		{
		    			//List<RecordSet> recSets = session.getDataList(RecordSet.class, "type", RecordSet.WB_UPLOAD);
		    			List<?> recSets = session.getDataList("select distinct rs from RecordSet rs where type = " + RecordSet.WB_UPLOAD);
		    			for (Object rsObj : recSets)
		    			{
		    				RecordSet rs = (RecordSet) rsObj;
		    				if (todayInMillis - rs.getTimestampCreated().getTime() > maxAgeInMillis)
		    				{
		    					toRemove.add(new Pair<Integer,String>(rs.getId(), rs.getName()));
		    				}
		    			}
		    		} finally
		    		{
		    			session.close();
		    		}
				
		    		//System.out.println("Removing " + toRemove.size() + " expired recordsets.");
		    		
		    		for (Pair<Integer, String> rs : toRemove)
		    		{
		    			if (appIsShuttingDown.get())
		    			{
		    				break;
		    			}
		    			int count = BasicSQLUtils.update("delete from recordsetitem where recordsetid = " + rs.getFirst());
		    			log.info("deleted " + count + " recordsetitems from expired recordset " + rs.getSecond());
		    			//System.out.println("deleted " + count + " recordsetitems from expired recordset " + rs.getSecond());
		    			count = BasicSQLUtils.update("delete from recordset where recordsetid = " + rs.getFirst());
		    			if (count == 1) 
		    			{
		    				log.info("deleted expired recordset " + rs.getSecond());
		    				//System.out.println("deleted expired recordset " + rs.getSecond());
		    			} else
		    			{
		    				log.warn("unable to delete expired recordset " + rs.getSecond());
		    				//System.out.println("unable to delete expired recordset " + rs.getSecond());
		    			}
		    		}
		    	} finally
		    	{
		    		taskIsShutDown.set(true);
		    	}
			}
        	
        });
        //t.setPriority(Thread.MIN_PRIORITY);
        //System.out.println("max priority: " + Thread.MAX_PRIORITY + ". min priority: " + Thread.MIN_PRIORITY + " . my priority: " + t.getPriority());
        
        //Lower the priority. WARNING: setting to MIN_PRIORITY was crashing the jvm.
        if (t.getPriority() - 1 > Thread.MIN_PRIORITY)
        {
        	t.setPriority(t.getPriority() - 1);
        }
        t.start();
	}

	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#shutdown()
	 */
	@Override
	public void shutdown() 
	{
		super.shutdown();
      	appIsShuttingDown.set(true);
      	try 
      	{
      		Thread.sleep(250);
      	} catch (InterruptedException ex)
      	{
        	UsageTracker.incrHandledUsageCount();
        	edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(
        			WorkbenchRecordSetCleanupTask.class, ex);
      	}
      	//while (!taskIsShutDown.get()); //too risky??
	}

	
}

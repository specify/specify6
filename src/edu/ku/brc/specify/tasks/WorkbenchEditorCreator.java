/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.google.common.base.Function;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.ui.UIRegistry;

abstract class WorkbenchEditorCreator
{
    private static final Logger log = Logger.getLogger(WorkbenchEditorCreator.class);

    final Workbench                                     workbench;
    final DataProviderSessionIFace                      session;
    final boolean                                       showImageView;
    final BaseTask                                      thisTask;
    private final boolean                               isReadOnly;
    private final SwingWorker<WorkbenchPaneSS, Integer> worker;    
    
	public WorkbenchEditorCreator(Workbench workbench,
			DataProviderSessionIFace session, boolean showImageView,
			BaseTask thisTask, boolean isReadOnly)
	{
		this.workbench = workbench;

 		this.session = session; 
		
		this.showImageView = showImageView;
		this.thisTask = thisTask;
		this.isReadOnly = isReadOnly;
		
		worker = new SwingWorker<WorkbenchPaneSS, Integer>()
		{
		    @Override
		    public WorkbenchPaneSS doInBackground()
		    {
		        return doIt(new Function<Integer, Void>()
                {
                    @Override
                    public Void apply(Integer progress)
                    {
                        publish(progress);
                        return null;
                    }
                });
		    }
		    
		    @Override
		    protected void process(java.util.List<Integer> chunks) 
		    {
		        progressUpdated(chunks);
		    }
		    
		    @Override
		    public void done()
		    {
		        WorkbenchPaneSS workbenchPane;
		        try
		        {
		            workbenchPane = get();
		        } catch (Exception e)
		        {
		            // TODO Auto-generated catch block
		            e.printStackTrace();
		            return;
		        }
		        completed(workbenchPane);
		    }
		};
	}

	
    public void runInForeground()
    {
        completed(doIt(new Function<Integer, Void>()
        {
            @Override
            public Void apply(Integer arg0)
            {
                 return null;
            }
        }));
    }
    
    public void runInBackground()
    {
        worker.execute();
    }
	
    abstract public void completed(WorkbenchPaneSS pane);
    abstract public void progressUpdated(List<Integer> chunks);
    
	private WorkbenchPaneSS doIt(Function<Integer, Void> updateProgress)
	{
        // Make sure we have a session but use an existing one if it is passed in	    
        boolean usingExistingSession = session != null;
        DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
        
        WorkbenchPaneSS workbenchPane = null;
        try
         {
             if (!usingExistingSession)
             {
                 tmpSession.attach(workbench);
             }
             
             final int rowCount = workbench.getWorkbenchRows().size() + 1;
             /*SwingUtilities.invokeLater(new Runnable() {
                 public void run()
                 {
                     UIRegistry.getStatusBar().setProgressRange(workbench.getName(), 0, rowCount);
                     UIRegistry.getStatusBar().setIndeterminate(workbench.getName(), false);
                 }
             });*/
             
             //force load the workbench here instead of calling workbench.forceLoad() because
             //is so time-consuming and needs progress bar.
             //workbench.getWorkbenchTemplate().forceLoad();
             workbench.getWorkbenchTemplate().checkMappings(WorkbenchTask.getDatabaseSchema());
             //UIRegistry.getStatusBar().incrementValue(workbench.getName());
             int count = 0;
             // Adjust paint increment for number of rows in DataSet
//             int mod;
//             if (rowCount < 50) mod = 1;
//             else if (rowCount < 100) mod = 10;
//             else if (rowCount < 500) mod = 20;
//             else  if (rowCount < 1000) mod = 40;
//             else mod = 50;
             for (WorkbenchRow row : workbench.getWorkbenchRows())
             {
                 row.forceLoad();
                 //UIRegistry.getStatusBar().incrementValue(workbench.getName());
                 
//                 if (count % mod == 0)
//                 {
                     count++;
                     updateProgress.apply((int)( (100.0 * count) / rowCount));
//                 }
             }
             updateProgress.apply(100);
             
             // do the conversion code right here!
             boolean convertedAnImage = false;
             Set<WorkbenchRow> rows = workbench.getWorkbenchRows();
             if (rows != null)
             {
                 for (WorkbenchRow row: rows)
                 {
                     // move any single images over to the wb row image table
                     Set<WorkbenchRowImage> rowImages = row.getWorkbenchRowImages();
                     if (rowImages == null)
                     {
                         rowImages = new HashSet<WorkbenchRowImage>();
                         row.setWorkbenchRowImages(rowImages);
                     }
                     if (row.getCardImageFullPath() != null/* && 
                             row.getCardImageData() != null && 
                             row.getCardImageData().length > 0*/)
                     {
                         // create the WorkbenchRowImage record
                         WorkbenchRowImage rowImage = new WorkbenchRowImage();
                         rowImage.initialize();
                         //rowImage.setCardImageData(row.getCardImageData());
                         rowImage.setCardImageFullPath(row.getCardImageFullPath());
                         rowImage.setImageOrder(0);
                         
                         // clear the fields holding the single-image data
                         row.setCardImageData(null);
                         row.setCardImageFullPath(null);

                         // connect the image and the row
                         rowImage.setWorkbenchRow(row);
                         rowImages.add(rowImage);
                         
                         convertedAnImage = true;
                     }
                 }
             }
             
             workbenchPane = new WorkbenchPaneSS(workbench.getName(), thisTask, workbench, 
                     showImageView, isReadOnly);
             
             if (convertedAnImage)
             {
                 Component topFrame = UIRegistry.getTopWindow();
                 String message     = getResourceString("WB_DATASET_IMAGE_CONVERSION_NOTIFICATION");
                 String msgTitle    = getResourceString("WB_DATASET_IMAGE_CONVERSION_NOTIFICATION_TITLE");
                 JOptionPane.showMessageDialog(topFrame, message, msgTitle, JOptionPane.INFORMATION_MESSAGE);
                 workbenchPane.setChanged(true);
             }
         } catch (Exception ex)
         {
             edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
             edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
             log.error(ex);
             ex.printStackTrace();
         } 
         finally
         {
             if (!usingExistingSession && tmpSession != null)
             {
                 try
                 {
                     tmpSession.close();
                     
                 } catch (Exception ex)
                 {
                     edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                     edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                     log.error(ex);
                 }
             }
         }
         
         return workbenchPane;
    }
}
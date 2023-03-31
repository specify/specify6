/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.Frame;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.FeedBackSender;
import edu.ku.brc.ui.FeedBackSenderItem;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2009
 *
 */
public class FeedBackDlg extends FeedBackSender
{
    /**
     * 
     */
    public FeedBackDlg()
    {
        super();
    }
    
    /**
     * @return the url that the info should be sent to
     */
    protected String getSenderURL()
    {
        return UIRegistry.getResourceString("CGI_BASE_URL") + "/feedback.php";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.exceptions.ExceptionTracker#getFeedBackSenderItem(java.lang.Class, java.lang.Exception)
     */
    @Override
    protected FeedBackSenderItem getFeedBackSenderItem(final Class<?> cls, final Exception exception)
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,4px,p,4px,p,4px,p,4px,f:p:g"));
        
        Vector<String>  taskItems  = new Vector<String>();
        for (Taskable task : TaskMgr.getInstance().getAllTasks())
        {
            taskItems.add(task.getTitle());
        }
        
        String[] OTHERS = {"WEBSITE", "CSTSUP", "INSTL", "DOC", "WHTPR", "HLP"};
        for (String key : OTHERS)
        {
            taskItems.add(UIRegistry.getResourceString("FeedBackDlg." + key));
        }
        Collections.sort(taskItems);
        
        final JComboBox  taskCBX    = createComboBox(taskItems);
        final JTextField subjectTF  = createTextField();
        final JTextField issueTF    = createTextField();
        final JTextArea  commentsTA = createTextArea(15, 60);
        
        int y = 1;
        pb.add(createI18NLabel("FeedBackDlg.INFO"), cc.xyw(1, y, 4)); y += 2;

        pb.add(createI18NFormLabel("FeedBackDlg.SUB"), cc.xy(1, y));
        pb.add(subjectTF,                  cc.xyw(3, y, 2)); y += 2;
        
        pb.add(createI18NFormLabel("FeedBackDlg.COMP"), cc.xy(1, y));
        pb.add(taskCBX,                      cc.xy(3, y)); y += 2;
        
        //pb.add(createFormLabel("Question/Issue"), cc.xy(1, y));
        //pb.add(issueTF,                           cc.xyw(3, y, 2)); y += 2;
        
        pb.add(createI18NFormLabel("FeedBackDlg.COMM"), cc.xy(1, y));     y += 2;
        pb.add(createScrollPane(commentsTA, true), cc.xyw(1, y, 4)); y += 2;
        
        Taskable currTask = SubPaneMgr.getInstance().getCurrentSubPane().getTask();
        taskCBX.setSelectedItem(currTask != null ? currTask : TaskMgr.getDefaultTaskable());
        
        pb.setDefaultDialogBorder();
        CustomDialog dlg = CustomDialog.create(UIRegistry.getResourceString("FeedBackDlg.TITLE"), true, CustomDialog.OKCANCEL, pb.getPanel());
        
        centerAndShow(dlg);
        
        if (!dlg.isCancelled())
        {
            String taskTitle = (String)taskCBX.getSelectedItem();
            if (taskTitle != null)
            {
                for (Taskable task : TaskMgr.getInstance().getAllTasks())
                {
                    if (task.getTitle().equals(taskTitle))
                    {
                        taskTitle = task.getName();
                    }
                }
            } else
            {
                taskTitle = "No Task Name";
            }
            FeedBackSenderItem item = new FeedBackSenderItem(taskTitle, subjectTF.getText(), issueTF.getText(), commentsTA.getText(), "", cls != null ? cls.getName() : "");
            return item;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.exceptions.ExceptionTracker#collectSecondaryStats()
     */
    @Override
    protected Vector<NameValuePair> collectionSecondaryInfo(final FeedBackSenderItem item)
    {
        Vector<NameValuePair> stats = new Vector<NameValuePair>();
        if (AppContextMgr.getInstance().hasContext())
        {
            Collection  collection  = AppContextMgr.getInstance().getClassObject(Collection.class);
            Discipline  discipline  = AppContextMgr.getInstance().getClassObject(Discipline.class);
            Division    division    = AppContextMgr.getInstance().getClassObject(Division.class);
            Institution institution = AppContextMgr.getInstance().getClassObject(Institution.class);
            
            stats.add(new BasicNameValuePair("collection",  collection != null  ? collection.getCollectionName() : "No Collection")); //$NON-NLS-1$
            stats.add(new BasicNameValuePair("discipline",  discipline != null  ? discipline.getName() :           "No Discipline")); //$NON-NLS-1$
            stats.add(new BasicNameValuePair("division",    division != null    ? division.getName() :             "No Division")); //$NON-NLS-1$
            stats.add(new BasicNameValuePair("institution", institution != null ? institution.getName() :          "No Institution")); //$NON-NLS-1$
        }
        return stats;
    }

}

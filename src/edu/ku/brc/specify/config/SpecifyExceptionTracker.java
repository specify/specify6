/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createFormLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.httpclient.NameValuePair;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.exceptions.ExceptionTracker;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.FeedBackSenderItem;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2009
 *
 */
public class SpecifyExceptionTracker extends ExceptionTracker
{
    /**
     * 
     */
    public SpecifyExceptionTracker()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.exceptions.ExceptionTracker#getFeedBackSenderItem(java.lang.Class, java.lang.Exception)
     */
    @Override
    protected FeedBackSenderItem getFeedBackSenderItem(final Class<?> cls, final Exception exception)
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,4px,p,4px,p,4px,p,2px,p,4px,p,2px,f:p:g"));
        
        //TreeSet<Taskable> treeSet    = new TreeSet<Taskable>(TaskMgr.getInstance().getAllTasks());
        Vector<Taskable>  taskItems  = new Vector<Taskable>(TaskMgr.getInstance().getAllTasks());
        Collections.sort(taskItems, new Comparator<Taskable>() {
            @Override
            public int compare(Taskable o1, Taskable o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        final JComboBox         taskCBX    = createComboBox(taskItems);
        final JTextField        titleTF    = createTextField();
        final JTextField        bugTF      = createTextField();
        final JTextArea         commentsTA = createTextArea(6, 60);
        final JTextArea         stackTraceTA = createTextArea(15, 60);
        
        int y = 1;
        pb.add(createFormLabel("Task"), cc.xy(1, y));
        pb.add(taskCBX,                 cc.xy(3, y)); y += 2;
        
        pb.add(createFormLabel("Title"), cc.xy(1, y));
        pb.add(titleTF,                  cc.xyw(3, y, 2)); y += 2;
        
        pb.add(createFormLabel("Bug #"), cc.xy(1, y));
        pb.add(bugTF,                    cc.xy(3, y)); y += 2;
        
        pb.add(createFormLabel("Comments"), cc.xy(1, y));     y += 2;
        pb.add(createScrollPane(commentsTA, true), cc.xyw(1, y, 4)); y += 2;
        
        pb.add(createFormLabel("Stack Trace"), cc.xy(1, y));     y += 2;
        pb.add(createScrollPane(stackTraceTA), cc.xyw(1, y, 4)); y += 2;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exception.printStackTrace(new PrintStream(baos));
        
        stackTraceTA.setText(baos.toString());
        
        
        if (SubPaneMgr.getInstance().getCurrentSubPane() != null &&
            SubPaneMgr.getInstance().getCurrentSubPane().getTask() != null)
        {
            Taskable currTask = SubPaneMgr.getInstance().getCurrentSubPane().getTask();
            taskCBX.setSelectedItem(currTask != null ? currTask : TaskMgr.getDefaultTaskable());
        } else
        {
            taskCBX.setSelectedItem(0);
        }
        
        pb.setDefaultDialogBorder();
        CustomDialog dlg = new CustomDialog((Frame)null, "Handled Exception", true, pb.getPanel())
        {
            
            /* (non-Javadoc)
             * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
             */
            @Override
            protected void cancelButtonPressed()
            {
                String taskName = taskCBX.getSelectedItem() != null ? ((Taskable)taskCBX.getSelectedItem()).getName() : "No Task Name";
                FeedBackSenderItem item = new FeedBackSenderItem(taskName, titleTF.getText(), bugTF.getText(), commentsTA.getText(), stackTraceTA.getText(), cls.getName());
                NameValuePair[] pairs = createPostParameters(item);
                
                StringBuilder sb = new StringBuilder();
                for (NameValuePair pair : pairs)
                {
                    if (!pair.getName().equals("bug"))
                    {
                        sb.append(pair.getName());
                        sb.append(": ");
                        if (pair.getName().equals("comments") || pair.getName().equals("stack_trace"))
                        {
                            sb.append("\n");
                        }
                        sb.append(pair.getValue());
                        sb.append("\n");
                    }
                }
                
                StringSelection stsel  = new StringSelection(sb.toString());
                Clipboard       sysClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                sysClipboard.setContents(stsel, stsel);
            }
        };
        dlg.setCancelLabel("Copy To Clipboard");
        
        centerAndShow(dlg);
        
        String taskName = taskCBX.getSelectedItem() != null ? ((Taskable)taskCBX.getSelectedItem()).getName() : "No Task Name";
        FeedBackSenderItem item = new FeedBackSenderItem(taskName, titleTF.getText(), bugTF.getText(), commentsTA.getText(), stackTraceTA.getText(), cls.getName());
        return item;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.exceptions.ExceptionTracker#collectSecondaryStats()
     */
    @Override
    protected Vector<NameValuePair> collectionSecondaryInfo()
    {
        Vector<NameValuePair> stats = new Vector<NameValuePair>();
        if (AppContextMgr.getInstance().hasContext())
        {
            Collection  collection  = AppContextMgr.getInstance().getClassObject(Collection.class);
            Discipline  discipline  = AppContextMgr.getInstance().getClassObject(Discipline.class);
            Division    division    = AppContextMgr.getInstance().getClassObject(Division.class);
            Institution institution = AppContextMgr.getInstance().getClassObject(Institution.class);
            
            stats.add(new NameValuePair("collection",  collection != null  ? collection.getCollectionName() : "No Collection")); //$NON-NLS-1$
            stats.add(new NameValuePair("discipline",  discipline != null  ? discipline.getName() :           "No Discipline")); //$NON-NLS-1$
            stats.add(new NameValuePair("division",    division != null    ? division.getName() :             "No Division")); //$NON-NLS-1$
            stats.add(new NameValuePair("institution", institution != null ? institution.getName() :          "No Institution")); //$NON-NLS-1$
        }
        return stats;
    }

}

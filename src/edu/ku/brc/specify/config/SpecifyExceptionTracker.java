/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;

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
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

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
    protected ImageIcon    forwardImgIcon;
    protected ImageIcon    downImgIcon;
    protected JPanel       stackTracePanel;
    protected CustomDialog dlg;
    
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
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,8px,p,2px, p,4px,p,2px,f:p:g"));
        
        Vector<Taskable>  taskItems  = new Vector<Taskable>(TaskMgr.getInstance().getAllTasks());
        Collections.sort(taskItems, new Comparator<Taskable>() {
            @Override
            public int compare(Taskable o1, Taskable o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        final JTextArea         commentsTA = createTextArea(3, 60);
        final JTextArea         stackTraceTA = createTextArea(15, 60);
        final JCheckBox         moreBtn;
        
        commentsTA.setWrapStyleWord(true);
        commentsTA.setLineWrap(true);
        
        //JLabel desc = createI18NLabel("UNHDL_EXCP", SwingConstants.LEFT);
        JEditorPane desc = new JEditorPane("text/html", getResourceString("UNHDL_EXCP"));
        desc.setEditable(false);
        desc.setOpaque(false);
        //desc.setFont(new Font(Font.SANS_SERIF, Font.BOLD, (new JLabel("X")).getFont().getSize()));
        
        JScrollPane sp = new JScrollPane(stackTraceTA,  ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        int y = 1;
        pb.add(desc,                                  cc.xyw(1, y, 4)); y += 2;
        pb.add(createI18NFormLabel("UNHDL_EXCP_CMM"), cc.xy(1, y));     y += 2;
        pb.add(createScrollPane(commentsTA, true),    cc.xyw(1, y, 4)); y += 2;
        
        
        forwardImgIcon = IconManager.getIcon("Forward"); //$NON-NLS-1$
        downImgIcon    = IconManager.getIcon("Down"); //$NON-NLS-1$
        moreBtn        = new JCheckBox(getResourceString("LOGIN_DLG_MORE"), forwardImgIcon); //$NON-NLS-1$
        setControlSize(moreBtn);
        JButton copyBtn = createI18NButton("UNHDL_EXCP_COPY");
        
        PanelBuilder innerPB = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,2px,p:g,2px,p"));
        innerPB.add(createI18NLabel("UNHDL_EXCP_STK"),     cc.xy(1, 1));
        innerPB.add(sp,                                    cc.xyw(1, 3, 3));
        innerPB.add(copyBtn,                               cc.xy(1, 5));
        stackTracePanel = innerPB.getPanel();
        stackTracePanel.setVisible(false);
        
        pb.add(moreBtn,         cc.xyw(1, y, 4)); y += 2;
        pb.add(stackTracePanel, cc.xyw(1, y, 4)); y += 2;
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exception.printStackTrace(new PrintStream(baos));
        
        stackTraceTA.setText(baos.toString());
        
        
        moreBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (stackTracePanel.isVisible())
                {
                    stackTracePanel.setVisible(false);
                    moreBtn.setIcon(forwardImgIcon);
                } else
                {
                    stackTracePanel.setVisible(true);
                    moreBtn.setIcon(downImgIcon);
                }
                if (dlg != null)
                {
                    dlg.pack();
                }
            }
        });
        
        copyBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String taskName = getTaskName();
                FeedBackSenderItem item = new FeedBackSenderItem(taskName,"", "", commentsTA.getText(), stackTraceTA.getText(), cls.getName());
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
                
                // Copy to Clipboard
                UIHelper.setTextToClipboard(sb.toString());
            }
        });
        
        pb.setDefaultDialogBorder();
        dlg = new CustomDialog((Frame)null, getResourceString("UnhandledExceptionTitle"), true, CustomDialog.OK_BTN, pb.getPanel());
        dlg.setOkLabel(getResourceString("UNHDL_EXCP_SEND"));
        
        dlg.createUI();
        stackTracePanel.setVisible(false);
        
        centerAndShow(dlg);
        
        String taskName = getTaskName();
        FeedBackSenderItem item = new FeedBackSenderItem(taskName, "", "", commentsTA.getText(), stackTraceTA.getText(), cls.getName());
        return item;
    }
    
    /**
     * @return
     */
    private String getTaskName()
    {
        String taskName = "";
        if (SubPaneMgr.getInstance().getCurrentSubPane() != null &&
                SubPaneMgr.getInstance().getCurrentSubPane().getTask() != null)
        {
            Taskable currTask = SubPaneMgr.getInstance().getCurrentSubPane().getTask();
            Taskable tsk = currTask != null ? currTask : TaskMgr.getDefaultTaskable();
            if (tsk != null)
            {
                taskName = tsk.getName();
            }
        }
        return taskName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.exceptions.ExceptionTracker#collectSecondaryStats()
     */
    @Override
    protected Vector<NameValuePair> collectionSecondaryInfo(final FeedBackSenderItem item)
    {
        AppContextMgr mgr = AppContextMgr.getInstance();
        Vector<NameValuePair> stats = new Vector<NameValuePair>();
        if (mgr.hasContext())
        {
            Collection  collection  = mgr.getClassObject(Collection.class);
            Discipline  discipline  = mgr.getClassObject(Discipline.class);
            Division    division    = mgr.getClassObject(Division.class);
            Institution institution = mgr.getClassObject(Institution.class);
            
            stats.add(new NameValuePair("collection",  collection != null  ? collection.getCollectionName() : "No Collection")); //$NON-NLS-1$ $NON-NLS-2$
            stats.add(new NameValuePair("discipline",  discipline != null  ? discipline.getName() :           "No Discipline")); //$NON-NLS-1$ $NON-NLS-2$
            stats.add(new NameValuePair("division",    division != null    ? division.getName() :             "No Division")); //$NON-NLS-1$ $NON-NLS-2$
            stats.add(new NameValuePair("institution", institution != null ? institution.getName() :          "No Institution")); //$NON-NLS-1$ $NON-NLS-2$
            
            stats.add(new NameValuePair("Collection_number",  collection != null  ? collection.getRegNumber()  : "No Collection Number")); //$NON-NLS-1$ $NON-NLS-2$
            stats.add(new NameValuePair("Discipline_number",  discipline != null  ? discipline.getRegNumber()  : "No Discipline Number")); //$NON-NLS-1$ $NON-NLS-2$
            stats.add(new NameValuePair("Division_number",    division != null    ? division.getRegNumber()    : "No Division Number")); //$NON-NLS-1$ $NON-NLS-2$
            stats.add(new NameValuePair("Institution_number", institution != null ? institution.getRegNumber() : "No Institution Number")); //$NON-NLS-1$ $NON-NLS-2$
            
            if (collection != null && StringUtils.isNotEmpty(collection.getIsaNumber()))
            {
                stats.add(new NameValuePair("ISA_number", collection.getIsaNumber())); //$NON-NLS-1$
            }
            
            if (item.isIncludeEmail())
            {
                String email = ((SpecifyAppContextMgr)mgr).getMailAddr(false);
                if (StringUtils.isNotEmpty(email))
                {
                    stats.add(new NameValuePair("email", email)); //$NON-NLS-1$
                }
            }
        }
        return stats;
    }

}

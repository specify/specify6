/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.specify.tasks.WebSearchTask;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 17, 2009
 *
 */
public class WebServiceSubPane extends BaseSubPane
{
    //private Vector<File>     fileListVec   = new Vector<File>();
    private DefaultListModel fileListModel = new DefaultListModel();
    private JList            fileList       = new JList(fileListModel);
    
    private EditDeleteAddPanel eadPanel;
    private JButton            uploadFilesBtn;
    private JButton            uploadDBBtn;
    private JLabel             lastUpdatedLbl;
    
    
    /**
     * @param name
     * @param task
     * @param buildProgressUI
     * @param includeProgressCancelBtn
     */
    public WebServiceSubPane(String name, Taskable task, boolean buildProgressUI,boolean includeProgressCancelBtn)
    {
        super(name, task, buildProgressUI, includeProgressCancelBtn);
    }

    /**
     * @param name
     * @param task
     * @param buildProgressUI
     */
    public WebServiceSubPane(String name, Taskable task, boolean buildProgressUI)
    {
        super(name, task, buildProgressUI);
        createUI();
    }

    /**
     * @param name
     * @param task
     */
    public WebServiceSubPane(String name, Taskable task)
    {
        super(name, task);
        createUI();
    }
    
    /**
     * @param btn
     * @return
     */
    private JPanel makeCenteredBtn(final JButton btn)
    {
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        pb.add(btn, (new CellConstraints()).xy(2,1));
        pb.getPanel().setOpaque(false);
        return pb.getPanel();
    }
    
    /**
     * 
     */
    private void createUI()
    {
        removeAll();
        
        ActionListener addAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addFileItem();
            }
        };
        ActionListener delAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                delFileItem();
            }
        };
        eadPanel = new EditDeleteAddPanel(null, delAction, addAction);
        eadPanel.setOpaque(false);
        
        uploadFilesBtn = UIHelper.createI18NButton("WSSP_UPLOAD_FILES_BTN");
        uploadDBBtn    = UIHelper.createI18NButton("WSSP_UPLOAD_DB_BTN");
        
        uploadFilesBtn.setEnabled(false);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,f:p:g", "p,4px, p,2px,p,2px,p,2px,p,20px,p,10px,p"), this);
        
        int y = 1;
        pb.addSeparator(UIRegistry.getResourceString("WSSP_FILES_UPLD"), cc.xyw(1, y, 1)); y += 2;
        pb.add(UIHelper.createI18NLabel("", SwingConstants.CENTER), cc.xy(1, y)); y += 2;
        pb.add(UIHelper.createScrollPane(fileList),                 cc.xy(1, y)); y += 2;
        pb.add(eadPanel,                                            cc.xy(1, y)); y += 2;
        pb.add(makeCenteredBtn(uploadFilesBtn),                     cc.xy(1, y)); y += 2;

        pb.addSeparator(UIRegistry.getResourceString("WSSP_UPLOAD_DB"), cc.xyw(1, y, 1)); y += 2;
        
        PanelBuilder innerPanel = new PanelBuilder(new FormLayout("p,4px,p:g", "p,16px,p"));
        
        lastUpdatedLbl = new JLabel();
        fillDate();
        
        innerPanel.add(UIHelper.createI18NFormLabel("WSSP_LAST_UPLD"), cc.xy(1, 1));
        innerPanel.add(lastUpdatedLbl,                              cc.xy(3, 1));
        
        innerPanel.add(makeCenteredBtn(uploadDBBtn),    cc.xyw(1, 3, 3));
        
        innerPanel.getPanel().setOpaque(false);
        pb.add(innerPanel.getPanel(), cc.xy(1, y)); y += 2;
        
        eadPanel.getAddBtn().setEnabled(true);
        
        fileList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    eadPanel.getDelBtn().setEnabled(fileList.getSelectedIndex() > -1); 
                }
            }
        });
        
        uploadDBBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ((WebSearchTask)task).createAndSendBackup();
                AppPreferences.getRemote().putLong("LAST_WS_UPLOAD", Calendar.getInstance().getTimeInMillis());
                
                fillDate();
            }
        });
        
        pb.setDefaultDialogBorder();
    }
    
    /**
     * @return
     */
    private String fillDate()
    {
        long lastUploadTime = AppPreferences.getRemote().getLong("LAST_WS_UPLOAD", 0l);
        String lblTxt;
        if (lastUploadTime == 0)
        {
            lblTxt = UIRegistry.getResourceString("NONE");
        } else
        {
            lblTxt = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(new Date(lastUploadTime));
        }
        lastUpdatedLbl.setText(lblTxt);
        return lblTxt;
    }
        
    /**
     * 
     */
    private void addFileItem()
    {
        FileDialog dlg = new FileDialog((Frame)UIRegistry.getMostRecentWindow(), "WSSP_CHSE_FILE", FileDialog.LOAD);
        dlg.setModal(true);
        dlg.setVisible(true);
        String fileName = dlg.getFile();
        if (fileName != null)
        {
            fileName = dlg.getDirectory() + dlg.getFile();
            fileListModel.addElement(fileName);
            
            uploadFilesBtn.setEnabled(true);
        }
    }

    /**
     * 
     */
    private void delFileItem()
    {
        fileListModel.remove(fileList.getSelectedIndex());
        uploadFilesBtn.setEnabled(fileListModel.size() > 0);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        return super.aboutToShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getHelpTarget()
     */
    @Override
    public String getHelpTarget()
    {
        return super.getHelpTarget();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#getUIComponent()
     */
    @Override
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();
    }

}

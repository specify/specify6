/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config.init;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.BrowseBtnPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Creates a dialog for entering database name and selecting the appropriate driver.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 17, 2008
 *
 */
/**
 * This is the configuration window for create a new user and new database.
 */
class DBLocationPanel extends BaseSetupPanel
{
    protected BrowseBtnPanel             browse;
    
    protected JRadioButton               useCurrentRB;
    protected JRadioButton               useHomeRB;
    protected JRadioButton               useUserDefinedRB;
    protected JTextField                 filePath          = null;
    protected boolean                    localDirOK        = true;
    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     */
    public DBLocationPanel(final JButton nextBtn)
    {
        super("Storage", nextBtn);

        
        localDirOK = true;
        File currentPath = new File(UIRegistry.getAppDataDir() + File.separator + "specify_tmp.tmp");
        try
        {
            FileUtils.touch(currentPath);
            currentPath.delete();
            
        } catch (IOException ex)
        {
            localDirOK = false;
        }
        
        //localDirOK = false ; // XXX TESTING
        
        ButtonGroup  grp       = new ButtonGroup();
        useHomeRB = new JRadioButton("<html>Use your home directory: <b>"+UIRegistry.getUserHomeAppDir()+"</b></html>");
        grp.add(useHomeRB);
        useHomeRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (browse != null)
                {
                    browse.setEnabled(false);
                }
                updateBtnUI();
            }
        });

        int numRows = 3;
        StringBuilder header = new StringBuilder("<html>This step requires you to select a storage for the database.");
        //localDirOK = false; // DEBUG
        if (localDirOK)
        {
            header.append("There are three options:</html>");

            useCurrentRB = new JRadioButton("<html>Use your current directory: <b>"+UIRegistry.getDefaultWorkingPath()+"</b></html>");
            grp.add(useCurrentRB);
            useCurrentRB.setSelected(true);
            numRows++;
            
        } else
        {
            header.append("<br>The database cannot be stored on the media you are currently running Workbench from, ");
            header.append("so you can allow it to default to your '<i>home</i>' directory. Or choose a different storage.</html>");
            useHomeRB.setSelected(true);
        }
        
        useUserDefinedRB  = new JRadioButton("Use other storage:");
        grp.add(useUserDefinedRB);
        useUserDefinedRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                browse.setEnabled(true);
                updateBtnUI();
            }
        });
        
        filePath = new JTextField(30);
        filePath.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {updateBtnUI();}
            public void removeUpdate(DocumentEvent e) {updateBtnUI();}
            public void changedUpdate(DocumentEvent e) {updateBtnUI();}
        });
        browse = new BrowseBtnPanel(filePath, true, true);
        browse.setEnabled(false);

        CellConstraints cc = new CellConstraints();
        
        JLabel       lbl     = new JLabel(header.toString());
        PanelBuilder cmtBldr = new PanelBuilder(new FormLayout("f:min(300px;p):g", "f:p:g"));
        cmtBldr.add(lbl, cc.xy(1,1));

        PanelBuilder builder = new PanelBuilder(new FormLayout("p,2px,p:g", "p:g,2px," + UIHelper.createDuplicateJGoodiesDef("p", "2px", numRows)+",f:p:g"), this);
        int row = 1;

        builder.add(cmtBldr.getPanel(), cc.xywh(1,row,3,1));row += 2;
        builder.add(useHomeRB, cc.xy(1,row));row += 2;
        if (useCurrentRB != null)
        {
            builder.add(useCurrentRB, cc.xy(1,row));row += 2;
        }
        builder.add(useUserDefinedRB, cc.xy(1,row));row += 2;
        builder.add(browse, cc.xy(1,row));row += 2;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#getValues(java.util.Properties)
     */
    @Override
    protected void getValues(Properties props)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.BaseSetupPanel#setValues(java.util.Hashtable)
     */
    @Override
    protected void setValues(Properties values)
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Checks all the textfeilds to see if they have text
     * @return true of all fields have text
     */
    protected void updateBtnUI()
    {
        nextBtn.setEnabled(isUIValid());
    }
    
    /**
     * Checks all the textfeilds to see if they have text
     * @return true of all fields have text
     */
    protected boolean isUIValid()
    {
        if (useUserDefinedRB.isSelected())
        {
            String path = filePath.getText();
            if (StringUtils.isNotEmpty(path))
            {
                return new File(path).exists();
            }
            return false;
        }
        return true;
    }
    
    public boolean isUsingUserDefinedDirectory()
    {
        return useUserDefinedRB.isSelected();
    }
    
    public boolean isUseHomeDirectory()
    {
        return useHomeRB.isSelected();
    }
    
    public boolean isLocalOKForWriting()
    {
        return localDirOK;
    }
    
    public String getUserDefinedPath()
    {
        return filePath.getText();
    }
}



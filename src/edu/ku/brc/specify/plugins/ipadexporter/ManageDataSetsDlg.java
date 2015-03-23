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
package edu.ku.brc.specify.plugins.ipadexporter;

import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 10, 2012
 *
 */
@SuppressWarnings({"rawtypes" })
public class ManageDataSetsDlg extends CustomDialog
{
    private IPadCloudIFace        cloudHelper;
    
    private JList                 dataSetList;
    private DefaultListModel      dataSetModel;
    private ArrayList<String>     datasetGUIDList;
    private EditDeleteAddPanel    dsEDAPanel;
    
    private JList                 usrList;
    private DefaultListModel      usrModel;
    
    private EditDeleteAddPanel    usrEDAPanel;
    
    /**
     * @param cloudHelper
     * @throws HeadlessException
     */
    public ManageDataSetsDlg(final IPadCloudIFace cloudHelper) throws HeadlessException
    {
        super((Frame)getTopWindow(), "MNG_DS", true, OKCANCEL, null);
        
        this.cloudHelper     = cloudHelper;
        this.datasetGUIDList = new ArrayList<String>();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @SuppressWarnings({ "unchecked"})
    @Override
    public void createUI()
    {
        loadAndPushResourceBundle(iPadDBExporterPlugin.RESOURCE_NAME);
        setTitle(getResourceString("MNG_DS"));
        try
        {
            ActionListener addUsrAction = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    addUserToDS();
                }
            };
            ActionListener delUsrAction = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    removeUserFromDS();
                }
            };
            ActionListener delDSAction = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    removeDS();
                }
            };
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,2px,p, 12px, p,2px,p,2px,p"));
            
            JLabel dsLabel = createI18NLabel("YOUR_DATASETS");
            dataSetList  = new JList(dataSetModel= new DefaultListModel());
            JScrollPane dsSp = createScrollPane(dataSetList, true);
            dsEDAPanel  = new EditDeleteAddPanel(null, delDSAction, null);
            
            JLabel usrLabel = createI18NLabel("USRS_PER_DATASETS");
            usrList      = new JList(usrModel = new DefaultListModel());
            JScrollPane usrSp = createScrollPane(usrList, true);
            usrEDAPanel  = new EditDeleteAddPanel(null, delUsrAction, addUsrAction);
            contentPanel = pb.getPanel();
            
            int y = 1;
            pb.add(dsLabel, cc.xy(1, y)); y+= 2;
            pb.add(dsSp, cc.xy(1, y)); y+= 2;
            pb.add(dsEDAPanel, cc.xy(1, y)); y+= 2;
            
            pb.add(usrLabel, cc.xy(1, y)); y+= 2;
            pb.add(usrSp, cc.xy(1, y)); y+= 2;
            pb.add(usrEDAPanel, cc.xy(1, y)); y+= 2;
            
            pb.setDefaultDialogBorder();
            
            loadDataSetsIntoJList();
            
            dataSetList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        loadUsersForDataSetsIntoJList();
                        
                        boolean isSelected = dataSetList.getSelectedIndex() > -1;
                        if (isSelected)
                        {
                            dsEDAPanel.getDelBtn().setEnabled(true);
                            usrEDAPanel.getAddBtn().setEnabled(true);
                        }
                        dsEDAPanel.getDelBtn().setEnabled(isSelected);
                        usrEDAPanel.getAddBtn().setEnabled(isSelected);
                     }
                }
            });
            
            usrList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        boolean isSelected = usrList.getSelectedIndex() > -1;
                        if (isSelected)
                        {
                            usrEDAPanel.getDelBtn().setEnabled(true);
                        }
                        usrEDAPanel.getDelBtn().setEnabled(isSelected);
                    }
                }
            });
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            popResourceBundle();
        }
        super.createUI();
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void loadDataSetsIntoJList()
    {
        dataSetModel.clear();
        for (DataSetInfo dsi : cloudHelper.getOwnerList())
        {
            dataSetModel.addElement(dsi.getName());
            datasetGUIDList.add(dsi.getCollGuid());
        }
        if (dataSetModel.getSize() == 1)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    dataSetList.setSelectedIndex(0);
                }
            });
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void loadUsersForDataSetsIntoJList()
    {
        usrModel.clear();
        String collGuid = datasetGUIDList.get(dataSetList.getSelectedIndex());
        if (collGuid != null)
        {
            for (String usrName : cloudHelper.getAccessList(collGuid))
            {
                usrModel.addElement(usrName);
            }
        }
    }

    /**
     * 
     */
    private void removeUserFromDS()
    {
        String collGuid = datasetGUIDList.get(dataSetList.getSelectedIndex());
        String usrName = (String)usrList.getSelectedValue();
        if (StringUtils.isNotEmpty(collGuid) &&
            StringUtils.isNotEmpty(usrName))
        {
            if (cloudHelper.removeUserAccessFromDataSet(usrName, collGuid))
            {
                loadUsersForDataSetsIntoJList();
            } else
            {
                UIRegistry.showError("Error Removing "+usrName+" from "+collGuid);
            }
        }
    }

    /**
     * 
     */
    private void addUserToDS()
    {
        final Vector<String> wsList    = new Vector<String>();
        final Vector<String> instItems = new Vector<String>();
        
        String addStr = getResourceString("ADD");
        instItems.add(addStr);
        wsList.add(addStr);
        for (String fullName : cloudHelper.getInstList())
        {
            String[] toks = StringUtils.split(fullName, '\t');
            instItems.add(toks[0]);
            wsList.add(toks[1]);
        }
        
        final JTextField userNameTF = createTextField(20);
        final JTextField passwordTF = createTextField(20);
        final JLabel     pwdLbl     = createI18NFormLabel("Password");
        final JLabel     statusLbl  = createLabel("");
        final JCheckBox  isNewUser  = UIHelper.createCheckBox("Is New User");
        final JLabel     instLbl    = createI18NFormLabel("Insitution");
        final JComboBox  instCmbx   = UIHelper.createComboBox(instItems.toArray());
        
        if (instItems.size() == 2)
        {
            instCmbx.setSelectedIndex(1);
        }
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,4px,p,4px,p,4px,p,8px,p"));
        
        pb.add(createI18NLabel("Add New or Existing User to DataSet"), cc.xyw(1,1,3));
        pb.add(createI18NFormLabel("Username"), cc.xy(1,3));
        pb.add(userNameTF, cc.xy(3,3));
        pb.add(pwdLbl,     cc.xy(1,5));
        pb.add(passwordTF, cc.xy(3,5));
        pb.add(instLbl,    cc.xy(1,7));
        pb.add(instCmbx,   cc.xy(3,7));
        
        pb.add(isNewUser,  cc.xy(3,9));
        
        pb.add(statusLbl,  cc.xyw(1,11,3));
        pb.setDefaultDialogBorder();
        
        pwdLbl.setVisible(false);
        passwordTF.setVisible(false);
        instLbl.setVisible(false);
        instCmbx.setVisible(false);
        
        final CustomDialog dlg = new CustomDialog(this, "Add User", true, OKCANCEL, pb.getPanel())
        {
            @Override
            protected void okButtonPressed()
            {
                String usrName = userNameTF.getText();
                if (cloudHelper.isUserNameOK(usrName))
                {
                    String collGuid = datasetGUIDList.get(dataSetList.getSelectedIndex());
                    if (cloudHelper.addUserAccessToDataSet(usrName, collGuid))
                    {
                        super.okButtonPressed();
                    } else
                    {
                        iPadDBExporterPlugin.setErrorMsg(statusLbl, String.format("Unable to add usr: %s to the DataSet guid: %s", usrName, collGuid));
                    }
                } else if (isNewUser.isSelected())
                {
                    String pwdStr = passwordTF.getText();
                    String guid   = null;
                    if (instCmbx.getSelectedIndex() == 0)
                    {
//                        InstDlg instDlg = new InstDlg(cloudHelper);
//                        if (!instDlg.isInstOK())
//                        {
//                            instDlg.createUI();
//                            instDlg.pack();
//                            centerAndShow(instDlg, 600, null);
//                            if (instDlg.isCancelled())
//                            {
//                                return;
//                            }
//                            //guid = instDlg.getGuid()();
//                        }
                    } else
                    {
                        //webSite = wsList.get(instCmbx.getSelectedIndex());

                    }
                    
                    if (guid != null)
                    {
                        String collGuid = datasetGUIDList.get(dataSetList.getSelectedIndex());
                        if (cloudHelper.addNewUser(usrName, pwdStr, guid))
                        {
                            if (cloudHelper.addUserAccessToDataSet(usrName, collGuid))
                            {
                                ManageDataSetsDlg.this.loadUsersForDataSetsIntoJList();
                                super.okButtonPressed();
                            } else
                            {
                                iPadDBExporterPlugin.setErrorMsg(statusLbl, String.format("Unable to add%s to the DataSet %s", usrName, collGuid));
                            }
                        } else
                        {
                            iPadDBExporterPlugin.setErrorMsg(statusLbl, String.format("Unable to add%s to the DataSet %s", usrName, collGuid));
                        }
                    } else
                    {
                        // error
                    }
                } else
                {
                    iPadDBExporterPlugin.setErrorMsg(statusLbl, String.format("'%s' doesn't exist.", usrName));
                }
            }
        };
        
        KeyAdapter ka = new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                statusLbl.setText("");
                String usrNmStr = userNameTF.getText();
                boolean hasData = StringUtils.isNotEmpty(usrNmStr) && 
                                  (!isNewUser.isSelected() || StringUtils.isNotEmpty(passwordTF.getText())) &&
                                  UIHelper.isValidEmailAddress(usrNmStr);
                dlg.getOkBtn().setEnabled(hasData);
            }
            
        };
        userNameTF.addKeyListener(ka);
        passwordTF.addKeyListener(ka);
        
        final Color textColor = userNameTF.getForeground();
        
        FocusAdapter fa = new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                JTextField tf = (JTextField)e.getSource();
                if (!tf.getForeground().equals(textColor))
                {
                    tf.setText("");
                    tf.setForeground(textColor);
                }
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                JTextField tf = (JTextField)e.getSource();
                if (tf.getText().length() == 0)
                {
                    tf.setText("Enter email address");
                    tf.setForeground(Color.LIGHT_GRAY);
                }
            }
        };
        userNameTF.addFocusListener(fa);
        
        userNameTF.setText("Enter email address");
        userNameTF.setForeground(Color.LIGHT_GRAY);
        
        isNewUser.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean isSel = isNewUser.isSelected();
                pwdLbl.setVisible(isSel);
                passwordTF.setVisible(isSel);
                instLbl.setVisible(isSel);
                instCmbx.setVisible(isSel);
                
                Dimension s   = dlg.getSize();
                int       hgt = isNewUser.getSize().height + 4 + instCmbx.getSize().height;
                s.height += isSel ? hgt : -hgt;
                dlg.setSize(s);
            }
        });
        
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        
        centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            loadUsersForDataSetsIntoJList();
        }
    }

    /**
     * 
     */
    private void removeDS()
    {
        
    }
}

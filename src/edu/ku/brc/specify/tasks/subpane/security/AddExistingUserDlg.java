/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author ricardo
 *
 * @code_status Alpha
 *
 * Jan 6, 2009
 *
 */
@SuppressWarnings("serial")
public class AddExistingUserDlg extends CustomDialog
{
    private static final Logger log = Logger.getLogger(AddExistingUserDlg.class);
    
    final private String AED = "AddExistingUserDlg.";
    
    private JList      userList;
    private SpPrincipal group;
    
    /**
     * @param parentDlg
     * @param group
     */
    public AddExistingUserDlg(final CustomDialog parentDlg, 
                              final SpPrincipal  group) 
    {
        super(parentDlg, getResourceString("AddExistingUserDlg.TITLE"), true, OKCANCEL, null);
        //helpContext = "SECURITY_EXIST_USR";
        
        this.group  = group;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder mainPB = new PanelBuilder( new FormLayout("f:p:g", "p,5px,f:min(400px;p):g,2dlu,p"));
        
        // lay out controls on panel
        mainPB.addSeparator(getResourceString(AED+"SEL_ADD"), cc.xy(1, 1));

        userList = createUserList();
        mainPB.add(UIHelper.createScrollPane(userList, true), cc.xy(1, 3));
        
        mainPB.setDefaultDialogBorder();
        
        // adds panel to custom dialog
        contentPanel = mainPB.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
    }
    
    /**
     * Gets a list of SpecifyUser ids that are in the group or out of the group
     * @param groupId the primary key id of the group
     * @param groupType the type of group
     * @param inGroup whether the user are in the group or out
     * @return the list of users (never null)
     */
    @SuppressWarnings("unchecked")
    private static List<SpecifyUser> getUsers(final int     groupId, 
                                              final boolean inGroup)
    {
        String sql = "SELECT DISTINCT u.SpecifyUserID FROM specifyuser u INNER JOIN specifyuser_spprincipal upr ON u.SpecifyUserID = upr.SpecifyUserID " +
                     "INNER JOIN spprincipal p ON upr.SpPrincipalID = p.SpPrincipalID WHERE p.SpPrincipalID " + 
                     (inGroup ? "= " : "<> ") + groupId;
        
        log.debug(sql);
        
        ArrayList<Integer> ids = new ArrayList<Integer>();
        
        Vector<Object> rows = BasicSQLUtils.querySingleCol(sql);
        for (Object objId : rows)
        {
            sql = "SELECT COUNT(u.SpecifyUserID) FROM specifyuser u INNER JOIN specifyuser_spprincipal upr ON u.SpecifyUserID = upr.SpecifyUserID " +
                  "INNER JOIN spprincipal p ON upr.SpPrincipalID = p.SpPrincipalID WHERE p.SpPrincipalID = " + groupId + " AND u.SpecifyUserID = " + objId;
            log.debug(sql);
            Integer count = BasicSQLUtils.getCount(sql);
            if (count != null && count == 0)
            {
                ids.add((Integer)objId);
            }
        }
        
        if (ids.size() > 0)
        {
            StringBuilder sb = new StringBuilder("FROM SpecifyUser WHERE id in (");
            int i = 0;
            for (Integer id : ids)
            {
                if (i != 0) sb.append(',');
                sb.append(id);
                i++;
            }
            sb.append(") ORDER BY name");
            
            DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
            try
            {
                log.debug(sb.toString());
                
                return (List<SpecifyUser>)session.getDataList(sb.toString());
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                session.close();
            }
        }
        return new ArrayList<SpecifyUser>();
    }
    
    /**
     * @return
     */
    private JList createUserList()
    {
        DefaultListModel         listModel = new DefaultListModel();
        DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
        try
        {
            for (SpecifyUser user : getUsers(group.getUserGroupId(), false))
            {
                listModel.addElement(user);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            session.close();
        }
        
        boolean listEmpty = (listModel.size() == 0);
        if (listEmpty)
        {
            listModel.addElement(getResourceString(AED+"GRP_ALL"));
            okBtn.setEnabled(false);
        }
        JList usrList = new JList(listModel);
        usrList.setEnabled(!listEmpty);
        
        usrList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        
        usrList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    okBtn.doClick(); // emulate button click
                }
            }
        });

        return usrList;
    }
    
    /**
     * Returns the selected user, if OK button was clicked. Returns null if no user was selected.
     * @return the selected user, if OK button was clicked. Returns null if no user was selected.
     */
    public SpecifyUser getSelectedUser() 
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            SpecifyUser selectedUser = (SpecifyUser)userList.getSelectedValue();
            if (btnPressed == OK_BTN && selectedUser != null) 
            {
                session.attach(selectedUser);
                selectedUser.getSpPrincipals().size(); // Force load of the principals
                return selectedUser;
            }
        
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            session.close();
        }
        return null;
    }
}

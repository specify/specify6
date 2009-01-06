package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.util.ComparatorByStringRepresentation;

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
    private JList userList;
    private SpPrincipal group;
    
    /**
     * @param parentDlg
     * @param group
     */
    public AddExistingUserDlg(final CustomDialog parentDlg, final SpPrincipal group) 
    {
        super(parentDlg, getResourceString("SecuritySummaryDlg.DLG_TITLE"), true, OKCANCELHELP, null);
        helpContext = "SECURITY_SUMMARY";
        
        this.group = group;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        final CellConstraints cc = new CellConstraints();
        final PanelBuilder mainPB = new PanelBuilder(
                new FormLayout("f:p:g", "p,5px,min(325px;p),2dlu,p"));
        
        // lay out controls on panel
        mainPB.addSeparator("Select Users to Add", cc.xy(1, 1)); // I18N

        userList = createUserList();
        JScrollPane sp = new JScrollPane(userList, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainPB.add(sp, cc.xy(1, 3));
        
        mainPB.setDefaultDialogBorder();
        
        // adds panel to custom dialog
        contentPanel = mainPB.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
    }
    
    /**
     * @return
     */
    private JList createUserList()
    {
        DefaultListModel listModel = new DefaultListModel();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            List<SpecifyUser> users = session.getDataList(SpecifyUser.class);
            Collections.sort(users, new ComparatorByStringRepresentation<SpecifyUser>());
            session.attach(group);
            session.refresh(group);
            Set<Integer> userIdList = getUserIdsFromGroup(group);
            for (SpecifyUser user : users)
            {
                if (!userIdList.contains(user.getId()))
                {
                    listModel.addElement(user);
                }
            }
        } 
        finally
        {
            session.close();
        }
        
        boolean listEmpty = (listModel.size() == 0);
        if (listEmpty)
        {
            // all users are included in group already
            listModel.addElement("Group has all users already.");
            
        }
        JList usrList = new JList(listModel);
        usrList.setEnabled(!listEmpty);
        
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
     * @param groupArg
     * @return
     */
    private final Set<Integer> getUserIdsFromGroup(final SpPrincipal groupArg)
    {
        Set<SpecifyUser> groupUsers = groupArg.getSpecifyUsers();
        if (groupUsers == null)
        {
            groupUsers = new HashSet<SpecifyUser>();
        }
        Set<Integer> userIdList = new HashSet<Integer>();
        for (SpecifyUser user : groupUsers)
        {
            userIdList.add(user.getId());
        }
        return userIdList;
    }
    
    /**
     * Returns the selected user, if OK button was clicked. Returns null if no user was selected.
     * @return the selected user, if OK button was clicked. Returns null if no user was selected.
     */
    public SpecifyUser[] getSelectedUsers() 
    {
        Object[] objs = userList.getSelectedValues();
        final int n = objs.length;
        if (btnPressed == OK_BTN && n > 0) 
        {
            SpecifyUser[] selectedUsers = new SpecifyUser[n];
            for (int i = 0; i <  n ; i++) {
                selectedUsers[i] = (SpecifyUser) objs[i];
            }
            return selectedUsers;
        }
        return new SpecifyUser[0];
    }
}

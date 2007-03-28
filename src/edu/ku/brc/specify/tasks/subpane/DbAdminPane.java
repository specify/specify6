/*
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.UserGroup;
import edu.ku.brc.specify.help.HelpMgr;
import edu.ku.brc.ui.ListSlider;

/**
 * @author megkumin
 * 
 * @code_status Alpha
 * 
 * Created Date: Feb 5, 2007
 * 
 */
@SuppressWarnings("serial")
public class DbAdminPane extends BaseSubPane
{
    private static final Logger log         = Logger.getLogger(DbAdminPane.class);
    protected JLabel            label       = null;
    protected JButton           cancelBtn;
    protected JButton           okBtn;
    protected JButton           helpBtn;
    protected JButton           addBtn;
    protected JButton           removeBtn;
    protected JTextField        username;
    protected JPasswordField    password;
    protected JPasswordField    password2;

    protected JDialog           thisDlg;
    protected boolean           isCancelled = true;
    protected boolean           isLoggingIn = false;
    protected boolean           isAutoClose = false;
    // private static String value = "";
    private JList               list;
    protected java.util.List    listOfSpecifyUsers;
    protected java.util.List    listOfAvailableUserGroups;
    protected java.util.List    listOfSelectedUserGroups;
    protected java.util.List    listOfAvailableCoDs;
    protected java.util.List    listOfSelectedCoDs;
    // protected java.util.List listOfSpecifyUserNames;
    // Object[] arrayOfObjects = null;

    private DefaultListModel    dataListModel;

    // String[] listOfUserschoices = {"A", "long", "array", "of", "strings"};
    /**
     * 
     * 
     */
    public DbAdminPane(final String name, final Taskable task)
    {
        super(name, task);
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        listOfSpecifyUsers = session.getDataList(SpecifyUser.class);
        listOfAvailableUserGroups = session.getDataList(UserGroup.class);
        listOfSelectedUserGroups = session.getDataList(UserGroup.class);
        listOfSelectedUserGroups.clear();
        listOfAvailableCoDs = session.getDataList(CollectionObjDef.class);
        listOfSelectedCoDs = session.getDataList(CollectionObjDef.class);
        listOfSelectedCoDs.clear();
        // arrayOfObjects = (Object[])listOfSpecifyUsers.toArray(new
        // Object[listOfSpecifyUsers.size()]);
        session.close();
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
    }

    public void createUI()
    {
        JPanel p = new FormDebugPanel();
        PanelBuilder mainFormBuilder = new PanelBuilder(
                new FormLayout(
                        "f:p,3dlu, p,3dlu, p,3dlu", // columns
                        "p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu, p,3dlu"),
                p);// rows
        CellConstraints cc = new CellConstraints();
        JPanel existingUsers = createExistingUserPanel();

        int x = 1;
        int y = 1;
        int w = 1;
        int h = 1;

        // COLUMN 1
        mainFormBuilder.addSeparator("Existing User Accouts:", cc.xywh(x, y, 1, 1));
        y = y + 2;
        h = 10;
        mainFormBuilder.add(existingUsers, cc.xywh(x, y, w, h));
        y = y + h + 2;
        mainFormBuilder.add(buildAddRemoveUserButtons(), cc.xywh(x, y, 1, 1));
        y = y + 2;
        mainFormBuilder.addSeparator("", cc.xywh(x, y, 1, 1));

        // COLUMN2
        y = 1;
        x = 3;
        mainFormBuilder.addSeparator("Login Information:", cc.xywh(x, y, 3, 1));
        coinfgureUserAccoutFillInFormComponents();
        y = y + 2;
        y = addLine("username", username, mainFormBuilder, cc, x, y);
        y = addLine("password", password, mainFormBuilder, cc, x, y);
        y = addLine("password", password2, mainFormBuilder, cc, x, y);
        y = y + 2;
        mainFormBuilder.addSeparator("Assign Groups:", cc.xywh(x, y, 3, 1));
        y = y + 2;

        // listOfAvailableUserGroups.
        ListSlider userGroups = new ListSlider(listOfAvailableUserGroups, "Available Groups",
                listOfSelectedUserGroups, "Selected Groups");
        userGroups.setDefaultListCellRenderer(new UserGroupCellRenderer());
        mainFormBuilder.add(userGroups, cc.xywh(x, y, 3, 1));
        y = y + 2;
        mainFormBuilder.addSeparator("Assign Cods:", cc.xywh(x, y, 3, 1));
        y = y + 2;
        ListSlider cods = new ListSlider(listOfAvailableCoDs, "Available CoDs", listOfSelectedCoDs,
                "Selected CoDs");
        cods.setDefaultListCellRenderer(new CodCellRenderer());
        mainFormBuilder.add(cods, cc.xywh(x, y, 3, 1));
        // outerPanel.add(ButtonBarFactory.buildOKCancelHelpBar(okBtn, cancelBtn, helpBtn),
        // cc.xywh(1,3,3,1));
        this.add(p);
    }

    public void makeCoDlistOfGroups()
    {
        // sourceListModel = new DefaultListModel();
    }

    public JPanel buildAddRemoveUserButtons()
    {
        addBtn = new JButton(getResourceString("Add"));
        removeBtn = new JButton(getResourceString("Remove"));

        addBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("add button clicked");
                log.debug("creating Specify User");
                // addDataToListModel(dataListModel, new String[]{"newuser"});
            }
        });

        removeBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("remove button clicked");
                // JList sourceList = new JList(dataListModel);
                Object[] selectedVals = list.getSelectedValues();
                // addDataToListModel(sourceListModel, selectedVals);
                // removeDataFromListModel(dataListModel, selectedVals);
            }
        });
        return ButtonBarFactory.buildAddRemoveRightBar(addBtn, removeBtn);
    }

    public void configureMainButtons()
    {
        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn = new JButton(getResourceString("OK"));
        helpBtn = new JButton(getResourceString("Help"));

        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        });

        okBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        });

        HelpMgr.registerComponent(helpBtn, "login");
    }

    public void coinfgureUserAccoutFillInFormComponents()
    {
        username = new JTextField(20);
        password = new JPasswordField(20);
        password2 = new JPasswordField(20);

        addFocusListenerForTextComp(username);
        addFocusListenerForTextComp(password);
        addFocusListenerForTextComp(password2);
    }

    public JPanel createExistingUserPanel()
    {

        this.dataListModel = new DefaultListModel();
        addDataToListModel(dataListModel, listOfSpecifyUsers);
        // addDataToListModel(destinationListModel, destinationData);
        // setupListComponents(dataListModel, destinationListModel);
        list = new JList(dataListModel);
        // list = new JList(dataListModel) {
        // //Subclass JList to workaround bug 4832765, which can cause the
        // //scroll pane to not let the user easily scroll up to the beginning
        // //of the list. An alternative would be to set the unitIncrement
        // //of the JScrollBar to a fixed value. You wouldn't get the nice
        // //aligned scrolling, but it should work.
        // public int getScrollableUnitIncrement(Rectangle visibleRect,
        // int orientation,
        // int direction) {
        // int row;
        // if (orientation == SwingConstants.VERTICAL &&
        // direction < 0 && (row = getFirstVisibleIndex()) != -1) {
        // Rectangle r = getCellBounds(row, row);
        // if ((r.y == visibleRect.y) && (row != 0)) {
        // Point loc = r.getLocation();
        // loc.y--;
        // int prevIndex = locationToIndex(loc);
        // Rectangle prevR = getCellBounds(prevIndex, prevIndex);
        //
        // if (prevR == null || prevR.y >= r.y) {
        // return 0;
        // }
        // return prevR.height;
        // }
        // }
        // return super.getScrollableUnitIncrement(
        // visibleRect, orientation, direction);
        // }
        // };
        // / list.
        list.setCellRenderer(new SpecifyUserCellRenderer());
        // list.setSelectionModel(new DefaultListSelectionModel());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // SpecifyUser user = (SpecifyUser)listOfUserschoices[0];
        // String longValue = user.getName();

        // SpecifyUser user = (SpecifyUser)dataListModel.toArray()[0];
        // String longValue =user.getName();
        // if (longValue != null) {
        // list.setPrototypeCellValue(longValue); //get extra space
        // }
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        // list.addMouseListener(new MouseAdapter() {
        // public void mouseClicked(MouseEvent e) {
        // if (e.getClickCount() == 2) {
        // //setButton.doClick(); //emulate button click
        // log.debug("list clicked: " + list.getSelectedIndex());
        // username.setText(((SpecifyUser)list.getSelectedValue()).getName());
        // }
        // }
        // });
        list.addListSelectionListener(new MyListSelectionHandler());
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        // Create a container so that we can add a title around
        // the scroll pane. Can't add a title directly to the
        // scroll pane because its background would be white.
        // Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        // JLabel label = new JLabel("Existing Users");
        // label.setLabelFor(list);
        // listPane.add(label);
        // listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        // listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        return listPane;

    }

    class MyListSelectionHandler implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e)
        {
            username.setText(((SpecifyUser) list.getSelectedValue()).getName());
            // setButtons(e);
        }
    }

    /**
     * Creates a line in the form.
     * 
     * @param label
     *            JLabel text
     * @param comp
     *            the component to be added
     * @param pb
     *            the PanelBuilder to use
     * @param cc
     *            the CellConstratins to use
     * @param y
     *            the 'y' coordinate in the layout of the form
     * @return return an incremented by 2 'y' position
     */
    protected int addLine(final String label,
                          final JComponent comp,
                          final PanelBuilder pb,
                          final CellConstraints cc,
                          final int x,
                          final int y)
    {
        int yy = y;
        pb.add(new JLabel(label != null ? getResourceString(label) + ":" : " ",
                SwingConstants.RIGHT), cc.xy(x, yy));
        pb.add(comp, cc.xy(x + 2, yy));
        yy += 2;
        return yy;
    }

    /**
     * Creates a focus listener so the UI is updated when the focus leaves
     * 
     * @param textField
     *            the text field to be changed
     */
    protected void addFocusListenerForTextComp(final JTextComponent textField)
    {
        textField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                // updateUIControls();
            }
        });
    }

    private void addDataToListModel(DefaultListModel model, java.util.List<SpecifyUser> list)
    {
        // model.
        // Object[] vals = list.toArray();
        for (int i = 0; i < list.size(); i++)
        {
            model.addElement((SpecifyUser) list.get(i));
        }
    }

    /**
     * Set the text to the label (create the label if it doesn't exist)
     * 
     * @param msg
     *            the message to be displayed
     */
    public void setLabelText(final String msg)
    {
        if (label == null)
        {
            removeAll();
            label = new JLabel("", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                label.setText(msg);
                invalidate();
                doLayout();
                repaint();
            }
        });

    }
}

class SpecifyUserCellRenderer extends DefaultListCellRenderer
{
    public SpecifyUserCellRenderer()
    {
        // Don't paint behind the component
        this.setOpaque(false);
    }

    public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                  int index, // cell index
                                                  boolean iss, // is the cell selected
                                                  boolean chf) // the list and the cell have the
                                                                // focus
    {
        super.getListCellRendererComponent(list, value, index, iss, chf);
        // list.getSelectionModel().g
        if (iss)
        {
            setOpaque(true);
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);
            // list.getSele
        } else
        {
            this.setOpaque(false);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        SpecifyUser user = (SpecifyUser) value;
        setText(user.getName());
        return this;
    }

}

class UserGroupCellRenderer extends DefaultListCellRenderer
{
    public UserGroupCellRenderer()
    {
        // Don't paint behind the component
        this.setOpaque(false);
    }

    public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                  int index, // cell index
                                                  boolean iss, // is the cell selected
                                                  boolean chf) // the list and the cell have the
                                                                // focus
    {
        super.getListCellRendererComponent(list, value, index, iss, chf);
        // list.getSelectionModel().g
        if (iss)
        {
            setOpaque(true);
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);
            // list.getSele
        } else
        {
            this.setOpaque(false);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        System.out.println("getlistcellrenderer");
        UserGroup group = (UserGroup) value;
        setText(group.getName());
        return this;
    }

}

class CodCellRenderer extends DefaultListCellRenderer
{
    public CodCellRenderer()
    {
        // Don't paint behind the component
        this.setOpaque(false);
    }

    public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                  int index, // cell index
                                                  boolean iss, // is the cell selected
                                                  boolean chf) // the list and the cell have the
                                                                // focus
    {
        super.getListCellRendererComponent(list, value, index, iss, chf);
        // list.getSelectionModel().g
        if (iss)
        {
            setOpaque(true);
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            list.setSelectedIndex(index);
            // list.getSele
        } else
        {
            this.setOpaque(false);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        System.out.println("getlistcellrenderer");
        CollectionObjDef group = (CollectionObjDef) value;
        setText(group.getName());
        return this;
    }

}
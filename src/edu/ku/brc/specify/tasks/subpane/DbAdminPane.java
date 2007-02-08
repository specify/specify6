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
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.UIHelper;

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
{private static final Logger log = Logger.getLogger(DbAdminPane.class);
    protected JLabel                 label            = null;
    protected JButton          cancelBtn;
    protected JButton          okBtn;
    protected JButton          helpBtn;
    protected JButton          addBtn;
    protected JButton          removeBtn;    
    protected JTextField       username;
    protected JPasswordField   password;
    protected JPasswordField   password2;
    
    protected JDialog          thisDlg;
    protected boolean          isCancelled = true;
    protected boolean          isLoggingIn = false;
    protected boolean          isAutoClose = false;
    //private static String value = "";
    private JList list;   
    protected java.util.List listOfSpecifyUsers; 
    protected java.util.List listOfSpecifyUserNames; 
    Object[] arrayOfObjects = null;
    
    private DefaultListModel    dataListModel;
    //String[] listOfUserschoices = {"A", "long", "array", "of", "strings"};
    /**
     * 
     *
     */
    public DbAdminPane(final String name, 
                       final Taskable task)
    {
        super(name, task);
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        listOfSpecifyUsers = session.getDataList(SpecifyUser.class);
        //arrayOfObjects = (Object[])listOfSpecifyUsers.toArray(new Object[listOfSpecifyUsers.size()]);
        arrayOfObjects = listOfSpecifyUsers.toArray();
        session.close();        
    }


    /**
     * @param args
     */
    public static void main(String[] args)
    {
    }
    
    public void createUI(){

        PanelBuilder mainFormBuilder = new PanelBuilder(
                new FormLayout(
                        "p,3dlu, p,3dlu, p,3dlu",
                        "p,3dlu, p,3dlu"));
        CellConstraints cc = new CellConstraints();

        mainFormBuilder.add(createExistingUserPanel(), cc.xy(1,1)); 
        mainFormBuilder.add(createUserAccoutFillInForm(),cc.xywh(3, 1, 1, 3));
        mainFormBuilder.add(buildAddRemoveUserButtons(), cc.xywh(1,3,1,1));               

        PanelBuilder outerPanel = new PanelBuilder(
                new FormLayout(
                        "p,3dlu, p,3dlu, p,3dlu, p,3dlu", 
                        "p,3dlu, p,3dlu, p,3dlu, p,3dlu,p"), this);
        outerPanel.add(mainFormBuilder.getPanel(), cc.xy(1, 1));
        outerPanel.add(ButtonBarFactory.buildOKCancelHelpBar(okBtn, cancelBtn, helpBtn), cc.xywh(1,3,3,1));    
    }
    
    public JPanel buildAddRemoveUserButtons()
    {                  
        addBtn = new JButton(getResourceString("Add"));
        removeBtn  = new JButton(getResourceString("Remove"));     
        
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("add button clicked");
                log.debug("creating Specify User");
                addDataToListModel(dataListModel, new String[]{"newuser"});
             }
         });
        
        removeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                log.debug("remove button clicked");
               // JList sourceList = new JList(dataListModel);
                Object[] selectedVals = list.getSelectedValues();
                //addDataToListModel(sourceListModel, selectedVals);
                removeDataFromListModel(dataListModel, selectedVals);
            }
         });      
        return ButtonBarFactory.buildAddRemoveBar(addBtn, removeBtn);
    }
    
    public JPanel createUserAccoutFillInForm(){
        username  = new JTextField(20);
        password  = new JPasswordField(20);
        password2  = new JPasswordField(20);
                
        addFocusListenerForTextComp(username);
        addFocusListenerForTextComp(password);
        addFocusListenerForTextComp(password2);
        
        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn  = new JButton(getResourceString("OK"));   
        helpBtn  = new JButton(getResourceString("Help")); 
        
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
             }
         });

        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
            }
         });

        helpBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showConfirmDialog(null, "Future Help when help system works.", 
                        "Login Help", JOptionPane.CLOSED_OPTION);
            }
         });  
        
        PanelBuilder userAcctFields = new PanelBuilder(new FormLayout(
                "p,3dlu,max(220px;p)", 
                UIHelper.createDuplicateJGoodiesDef("p", "2dlu", 11)));
        CellConstraints cc = new CellConstraints();
        
        userAcctFields.addSeparator(getResourceString("logintitle"), cc.xywh(1,1,3,1));
        int x = 1;
        int y = 3;
        y = addLine("username",  username, userAcctFields, cc, x, y);
        y = addLine("password",  password, userAcctFields, cc, x, y);
        y = addLine("password", password2, userAcctFields, cc, x, y);
        return userAcctFields.getPanel();
    }
    
    public String[] getListOfUserNames()
    {
//       // String[] arrayOfObjects =
//        //listOfSpecifyUserNames = list
//        for(int i =0; i < listOfUserschoices.length; i++){
//            SpecifyUser user = (SpecifyUser)listOfSpecifyUsers.get(i);
//            String longValue = user.getName();
//        }
        return null;
    }
    
    
    public JPanel createExistingUserPanel()
    {
        
        this.dataListModel = new DefaultListModel();
        //this.destinationListModel = destinationModel;
        addDataToListModel(dataListModel, arrayOfObjects);
        //addDataToListModel(destinationListModel, destinationData);
        //setupListComponents(dataListModel, destinationListModel);
        list = new JList(dataListModel) {
            //Subclass JList to workaround bug 4832765, which can cause the
            //scroll pane to not let the user easily scroll up to the beginning
            //of the list.  An alternative would be to set the unitIncrement
            //of the JScrollBar to a fixed value. You wouldn't get the nice
            //aligned scrolling, but it should work.
            public int getScrollableUnitIncrement(Rectangle visibleRect,
                                                  int orientation,
                                                  int direction) {
                int row;
                if (orientation == SwingConstants.VERTICAL &&
                      direction < 0 && (row = getFirstVisibleIndex()) != -1) {
                    Rectangle r = getCellBounds(row, row);
                    if ((r.y == visibleRect.y) && (row != 0))  {
                        Point loc = r.getLocation();
                        loc.y--;
                        int prevIndex = locationToIndex(loc);
                        Rectangle prevR = getCellBounds(prevIndex, prevIndex);

                        if (prevR == null || prevR.y >= r.y) {
                            return 0;
                        }
                        return prevR.height;
                    }
                }
                return super.getScrollableUnitIncrement(
                                visibleRect, orientation, direction);
            }
        };

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //SpecifyUser user = (SpecifyUser)listOfUserschoices[0];
        //String longValue = user.getName();
        
        SpecifyUser user = (SpecifyUser)dataListModel.toArray()[0];
        String longValue =user.getName();
        if (longValue != null) {
            list.setPrototypeCellValue(longValue); //get extra space
        }
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    //setButton.doClick(); //emulate button click
                    log.debug("list clicked: " + list.getSelectedIndex());
                    //username.setText(list.g)
                }
            }
        });
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Existing Users");
        label.setLabelFor(list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        //listPane.setBackground(Color.blue);
        
        return listPane;

    }
    /**
     * Creates a line in the form.
     * @param label JLabel text
     * @param comp the component to be added
     * @param pb the PanelBuilder to use
     * @param cc the CellConstratins to use
     * @param y the 'y' coordinate in the layout of the form
     * @return return an incremented by 2 'y' position
     */
    protected int addLine(final String label, final JComponent comp, final PanelBuilder pb, final CellConstraints cc, final int x, final int y)
    {
        int yy = y;
        pb.add(new JLabel(label != null ? getResourceString(label)+":" : " ", SwingConstants.RIGHT), cc.xy(x, yy));
        pb.add(comp, cc.xy(x+2, yy));
        yy += 2;
        return yy;
    }   
    /**
     * Creates a focus listener so the UI is updated when the focus leaves
     * @param textField the text field to be changed
     */
    protected void addFocusListenerForTextComp(final JTextComponent textField)
    {
        textField.addFocusListener(new FocusAdapter(){
            @Override
            public void focusLost(FocusEvent e)
            {
                //updateUIControls();
            }
        });
    } 
    
    private void addDataToListModel(DefaultListModel model, Object newValues[])
    {
        for (int i = 0; i < newValues.length; i++)
        {
            model.addElement(newValues[i]);
        }
        //getDestinationData();
    }

    private void removeDataFromListModel(DefaultListModel model, Object newValues[])
    {
        for (int i = 0; i < newValues.length; i++)
        {
            model.removeElement(newValues[i]);
        }
        //getDestinationData();
    }
    /**
     * Set the text to the label (create the label if it doesn't exist)
     * @param msg the message to be displayed
     */
    public void setLabelText(final String msg)
    {
        if (label == null)
        {
            removeAll();
            label = new JLabel("", JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
        
        SwingUtilities.invokeLater(new Runnable() {
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

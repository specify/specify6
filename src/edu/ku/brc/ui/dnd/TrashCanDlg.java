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
package edu.ku.brc.ui.dnd;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.ui.DndDeletableListCellRenderer;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This dialog enables the user to view the contents of the trash can. It can be used to send commands to have the items recovered.
 *
 *(This needs to be converted to CustomDialog).
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class TrashCanDlg extends JDialog implements ActionListener, ListSelectionListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(TrashCanDlg.class);

    // Data Members
    protected JButton        restoreBtn;
    protected JButton        okBtn;
    protected JList          list;
    protected java.util.List<DndDeletable> items;
    protected Trash          trash;
    
    /**
     * Constructor
     * @param frame parent frame
     * @throws HeadlessException
     */
    public TrashCanDlg(final Frame frame) throws HeadlessException
    {
        super(frame, true);
        
        createUI();
        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }

    }

    /**
     * Create default UI
     *
     */
    protected void createUI()
    {
        trash = Trash.getInstance();
        items = trash.getItems();
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        
        JLabel label = createLabel(getResourceString("DeletedItems"), SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);

        try
        {
            
            ListModel listModel = new AbstractListModel() 
            {
                public int getSize() { return items.size(); }
                public Object getElementAt(int index) { return items.get(index).getName(); }
            };
            
            list = new JList(listModel);
            list.setCellRenderer(new DndDeletableListCellRenderer()); // icon comes from the base class (it's probably size 16)
            list.setVisibleRowCount(10);
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        okBtn.doClick(); //emulate button click
                    }
                }
            });
            list.addListSelectionListener(this);
            
            panel.add(UIHelper.createScrollPane(list), BorderLayout.CENTER);
            
            // Bottom Button UI
            restoreBtn         = createButton(getResourceString("Restore"));
            okBtn             = createButton(getResourceString("CLOSE"));

            restoreBtn.setEnabled(false);
            
            okBtn.addActionListener(this);
            getRootPane().setDefaultButton(okBtn);
            
            ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
            //btnBuilder.addGlue();
             btnBuilder.addGriddedButtons(new JButton[] {restoreBtn, okBtn}); 
 
            restoreBtn.addActionListener(new ActionListener()
                    {  public void actionPerformed(ActionEvent ae) { restoreItem(); }});
            
            panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TrashCanDlg.class, ex);
            log.error(ex);
        }
        
        setContentPane(panel);
        pack();
        //setLocationRelativeTo(locationComp);
        
    }
        
    /**
     * Restores an item
     */
    protected void restoreItem()
    {
        JOptionPane.showMessageDialog(UIRegistry.get(UIRegistry.FRAME), "Sorry, not implemented yet.");
    }
    
    //Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e) 
    {
        setVisible(false);
    }
    
    /**
     * Returns the deletable item
     * @return the deletable item
     */
    public DndDeletable getDeletable()
    {
        int inx = list.getSelectedIndex();
        if (inx != -1)
        {
            return items.get(inx);
        }
        return null;
    }
    
    //------------------------------------------------------
    // ListSelectionListener
    //------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     */
    public void valueChanged(ListSelectionEvent e)
    {
       restoreBtn.setEnabled(list.getSelectedIndex() != -1); 
    }
    
}

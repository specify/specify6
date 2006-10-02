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

package edu.ku.brc.ui;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.ui.dnd.DndDeletable;

/**
 * This dialoig enables the user to view the contents of the trash can. It can be used to send commands to have the items recovered.
 *
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
     * @throws HeadlessException
     */
    public TrashCanDlg() throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), true);
        createUI();
        setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setAlwaysOnTop(true);
        
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
        
        panel.add(new JLabel(getResourceString("DeletedItems"), SwingConstants.CENTER), BorderLayout.NORTH);

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
            
            JScrollPane listScroller = new JScrollPane(list);
            panel.add(listScroller, BorderLayout.CENTER);
            
            // Bottom Button UI
            restoreBtn         = new JButton(getResourceString("Restore"));
            okBtn             = new JButton(getResourceString("Close"));

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
        JOptionPane.showMessageDialog(UICacheManager.get(UICacheManager.FRAME), "Sorry, not implemented yet.");
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

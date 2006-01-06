/* Filename:    $RCSfile: ChooseFromListDlg.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Choose an object from a list of Objects using their "toString"
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ChooseFromListDlg extends JDialog implements ActionListener 
{
    // Static Data Members
    private static Log log = LogFactory.getLog(ChooseFromListDlg.class);
    
    // Data Members
    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JList          list;
    protected List           items;
    protected ImageIcon      icon   = null;
    
    /**
     * Constructor 
     * @param title the title of the dialog
     * @param items the list to be selected from
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final String title, final List items) throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), true);
        this.items = items;
        createUI(title);
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setAlwaysOnTop(true);
    }

    /**
     * Constructor 
     * @param title the title of the dialog
     * @param items the list to be selected from
     * @param icon the icon to be displayed in front of each entry in the list
     * @throws HeadlessException
     */
    public ChooseFromListDlg(final String title, final List items, final ImageIcon icon) throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), true);
        this.items = items;
        this.icon = icon;
        
        createUI(title);
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setAlwaysOnTop(true);
    }

    /**
     * 
     *
     */
    protected void createUI(final String title)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        
        panel.add(new JLabel(title, JLabel.CENTER), BorderLayout.NORTH);

        try
        {
            
            ListModel listModel = new AbstractListModel() 
            {
                public int getSize() { return items.size(); }
                public Object getElementAt(int index) { return items.get(index).toString(); }
            };
            
            list = new JList(listModel);
            if (icon != null)
            {
                list.setCellRenderer(new IconListCellRenderer(icon)); // icon comes from the base class (it's probably size 16)
            }
            list.setVisibleRowCount(10);
            list.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        okBtn.doClick(); //emulate button click
                    }
                }
            });
            JScrollPane listScroller = new JScrollPane(list);
            panel.add(listScroller, BorderLayout.CENTER);
            
            // Bottom Button UI
            cancelBtn         = new JButton(getResourceString("Cancel"));
            okBtn             = new JButton(getResourceString("OK"));

            okBtn.addActionListener(this);
            getRootPane().setDefaultButton(okBtn);
            
            ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
            //btnBuilder.addGlue();
             btnBuilder.addGriddedButtons(new JButton[] {cancelBtn, okBtn}); 
 
            cancelBtn.addActionListener(new ActionListener()
                    {  public void actionPerformed(ActionEvent ae) { setVisible(false);} });
            
            panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

        } catch (Exception ex)
        {
            log.error(ex);
        }
        
        setContentPane(panel);
        pack();
        //setLocationRelativeTo(locationComp);
        
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) 
    {
        setVisible(false);
    }
    
    /**
     * Returns the selected Object or null if nothing was selected
     * @return the selected Object or null if nothing was selected
     */
    public Object getSelectedObject()
    {
        int inx = list.getSelectedIndex();
        if (inx != -1)
        {
            return items.get(inx);
        }
        return null;
    }
    
}

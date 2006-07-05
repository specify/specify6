/* Filename:    $RCSfile: ChooseColObjIdsDlg.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/10 16:52:27 $
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
package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * XXX (NOT USED RIGHT NOW)
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial") 
public class ChooseColObjIdsDlg extends JDialog implements ActionListener 
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ChooseRecordSetDlg.class);
    
    private final static ImageIcon icon = IconManager.getImage(RecordSetTask.RECORD_SET, IconManager.IconSize.Std16);

    // Data Members
    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JList          list;
    protected java.util.List recordSets;
    
    
    public ChooseColObjIdsDlg() throws HeadlessException
    {
        super((Frame)UICacheManager.get(UICacheManager.FRAME), true);
        createUI();
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setAlwaysOnTop(true);
    }

    /**
     * 
     *
     */
    protected JPanel createUIRecordSets()
    {
        JPanel panel = new JPanel(new BorderLayout());
        //panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        panel.add(new JLabel(getResourceString("ChooseRecordSet"), IconManager.getImage(RecordSetTask.RECORD_SET, IconManager.IconSize.Std24), JLabel.LEFT), BorderLayout.NORTH);

        try
        {
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(RecordSet.class);
            recordSets = criteria.list();
            HibernateUtil.closeSession();
            
            ListModel listModel = new AbstractListModel() 
            {
                public int getSize() { return recordSets.size(); }
                public Object getElementAt(int index) { return ((RecordSet)recordSets.get(index)).getName(); }
            };
            
            @SuppressWarnings("serial") 
            class MyCellRenderer extends JLabel implements ListCellRenderer 
            {

                // This is the only method defined by ListCellRenderer.
                // We just reconfigure the JLabel each time we're called.

                public Component getListCellRendererComponent(JList list,
                                                              Object value,            // value to display
                                                              int index,               // cell index
                                                              boolean isSelected,      // is the cell selected
                                                              boolean cellHasFocus)    // the list and the cell have the focus
                {
                    String s = value.toString();
                    setText(s);
                    setIcon(icon);
                    if (isSelected) 
                    {
                        setBackground(list.getSelectionBackground());
                        setForeground(list.getSelectionForeground());
                    } else 
                    {
                        setBackground(list.getBackground());
                        setForeground(list.getForeground());
                    }
                    setEnabled(list.isEnabled());
                    setFont(list.getFont());
                    setOpaque(true);
                    return this;
                }
            }


            
            list = new JList(listModel);
            list.setCellRenderer(new MyCellRenderer());
            list.setVisibleRowCount(5);
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
        
        return panel;
        
    }

    /**
     * 
     *
     */
    protected void createUI()
    {
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add("Record Sets", tabbedPane);
        setContentPane(tabbedPane);
        pack();
        //setLocationRelativeTo(locationComp);
        
    }
    
    //Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e) 
    {
        setVisible(false);
    }
    
    public RecordSet getSelectedRecordSet()
    {
        int inx = list.getSelectedIndex();
        if (inx != -1)
        {
            return (RecordSet)recordSets.get(inx);
        }
        return null;
    }
}

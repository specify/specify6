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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * XXX (NOT USED RIGHT NOW)
 *
 * @code_status Unknown (auto-generated)
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
    protected List<?>        recordSets;
    
    
    public ChooseColObjIdsDlg(final Frame frame) throws HeadlessException
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
     * 
     *
     */
    protected JPanel createUIRecordSets()
    {
        JPanel panel = new JPanel(new BorderLayout());
        //panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        panel.add(createLabel(getResourceString("RECORDSET_CHOOSE"), 
                IconManager.getImage(RecordSetTask.RECORD_SET, IconManager.IconSize.Std24), SwingConstants.LEFT), 
                BorderLayout.NORTH);

        try
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            recordSets = session.getDataList(RecordSet.class);
            session.close();
            
            ListModel listModel = new AbstractListModel() 
            {
                public int getSize() { return recordSets.size(); }
                public Object getElementAt(int index) { return ((RecordSetIFace)recordSets.get(index)).getName(); }
            };
            
            @SuppressWarnings("serial") 
            class MyCellRenderer extends JLabel implements ListCellRenderer 
            {

                // This is the only method defined by ListCellRenderer.
                // We just reconfigure the JLabel each time we're called.

                public Component getListCellRendererComponent(JList renderList,
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
                        setBackground(renderList.getSelectionBackground());
                        setForeground(renderList.getSelectionForeground());
                    } else 
                    {
                        setBackground(renderList.getBackground());
                        setForeground(renderList.getForeground());
                    }
                    setEnabled(renderList.isEnabled());
                    setFont(renderList.getFont());
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
            cancelBtn         = createButton(getResourceString("CANCEL"));
            okBtn             = createButton(getResourceString("OK"));

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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ChooseColObjIdsDlg.class, ex);
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
        tabbedPane.add("Record Sets", tabbedPane);    // I18N
        setContentPane(tabbedPane);
        pack();
        //setLocationRelativeTo(locationComp);
        
    }
    
    //Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e) 
    {
        setVisible(false);
    }
    
    public RecordSetIFace getSelectedRecordSet()
    {
        int inx = list.getSelectedIndex();
        if (inx != -1)
        {
            return (RecordSetIFace)recordSets.get(inx);
        }
        return null;
    }
}

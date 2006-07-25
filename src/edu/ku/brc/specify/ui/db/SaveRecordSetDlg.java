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
package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class SaveRecordSetDlg extends JDialog
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(SaveRecordSetDlg.class);

    // Data Members
    protected JTable                srcTable;
    protected JTable                dstTable;
    protected ResultSetTableModelDM srcModel;
    protected ResultSetTableModelDM dstModel;
    
    protected JButton               selectAllBtn;
    protected JButton               deselectAllBtn;
    protected JButton               addAllBtn;
    protected JButton               addAllSelectedBtn;
    protected JButton               cancelBtn;
    protected JButton               okBtn;
    
    /**
     * 
     *
     */
    public SaveRecordSetDlg(ResultSetTableModelDM srcModel, final int[] preSelectedRows)
    {
        this.srcModel = srcModel;
        createUI(preSelectedRows);
    }
    
    /**
     * 
     *
     */
    protected void createUI(final int[] preSelectedRows)
    {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        namePanel.add(new JLabel(getResourceString("RecordSet_Name")+":", JLabel.RIGHT), BorderLayout.WEST);
        JTextField nameTxtFld = new JTextField("");
        namePanel.add(nameTxtFld, BorderLayout.CENTER);
        
        panel.add(namePanel, BorderLayout.NORTH);

        try
        {
            srcTable = new JTable(srcModel);
            srcTable.setRowSelectionAllowed(true);
            
            dstModel = new ResultSetTableModelDM(srcModel.getResultSet());
            dstTable = new JTable(dstModel);
            dstTable.setRowSelectionAllowed(true);
            dstModel.initializeDisplayIndexes();
            
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(srcTable), new JScrollPane(dstTable));
            splitPane.setDividerLocation(325);
            panel.add(splitPane, BorderLayout.CENTER);
   
            // Bottom Button UI
            selectAllBtn      = new JButton(getResourceString("SelectAll"));
            deselectAllBtn    = new JButton(getResourceString("DeselectAll"));
            addAllBtn         = new JButton(getResourceString("AddAll"));
            addAllSelectedBtn = new JButton(getResourceString("AddAllSelected"));
            cancelBtn         = new JButton(getResourceString("Cancel"));
            okBtn             = new JButton(getResourceString("OK"));

            ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
            //btnBuilder.addGlue();
             btnBuilder.addGriddedButtons(new JButton[] {selectAllBtn, deselectAllBtn, addAllBtn, addAllSelectedBtn, cancelBtn, okBtn}); 
 
            
            selectAllBtn.addActionListener(new ActionListener()
                  {  public void actionPerformed(ActionEvent ae) { srcTable.selectAll(); } });

 
            deselectAllBtn.addActionListener(new ActionListener()
                  {  public void actionPerformed(ActionEvent ae) { srcTable.clearSelection();} });
            
            addAllBtn.addActionListener(new ActionListener()
                      {  public void actionPerformed(ActionEvent ae) { addAll(); } });

            addAllSelectedBtn.addActionListener(new ActionListener()
                      {  public void actionPerformed(ActionEvent ae) { addAddSelected();} });
            
            cancelBtn.addActionListener(new ActionListener()
                    {  public void actionPerformed(ActionEvent ae) { setVisible(false);} });
            
            panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);
            
            setSize(btnBuilder.getPanel().getPreferredSize().width+20, 600);

        } catch (Exception ex)
        {
            log.error(ex);
        }
        
        dstModel.addDisplayIndexes(preSelectedRows);
        setContentPane(panel);
        
    }
    
    /**
     * Add all the items from the selection
     *
     */
    protected void addAll()
    {
        srcTable.selectAll();
        dstModel.addDisplayIndexes(srcTable.getSelectedRows());
        srcTable.clearSelection();
    }
    
    
    /**
     * Add only the selected items 
     *
     */
    protected void addAddSelected()
    {
        dstModel.addDisplayIndexes(srcTable.getSelectedRows());
        srcTable.clearSelection();
        // XXX need to remove duplicates and sort by index number
    }
    

}

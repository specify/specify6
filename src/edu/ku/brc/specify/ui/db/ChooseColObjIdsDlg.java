package edu.ku.brc.specify.ui.db;


import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.*;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ChooseColObjIdsDlg extends JDialog
{
    // Static Data Members
    private static Log log = LogFactory.getLog(ResultSetTableModel.class);

    // Data Members
    protected JTable              srcTable;
    protected JTable              dstTable;
    protected ResultSetTableModel srcModel;
    protected ResultSetTableModel dstModel;
    
    protected JButton             selectAllBtn;
    protected JButton             deselectAllBtn;
    protected JButton             addAllBtn;
    protected JButton             addAllSelectedBtn;
    protected JButton             cancelBtn;
    protected JButton             okBtn;
    
    /**
     * 
     *
     */
    public ChooseColObjIdsDlg(ResultSetTableModel srcModel, final int[] preSelectedRows)
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
            
            dstModel = new ResultSetTableModel(srcModel.getResultSet());
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

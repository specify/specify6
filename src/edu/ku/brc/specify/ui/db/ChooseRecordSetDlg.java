package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.*;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.*;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.HibernateUtil;

public class ChooseRecordSetDlg extends JDialog implements ActionListener 
{
    // Static Data Members
    private static Log log = LogFactory.getLog(ChooseRecordSetDlg.class);

    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JList          list;
    protected java.util.List recordSets;
    
    public ChooseRecordSetDlg() throws HeadlessException
    {
        super();
        createUI();
    }

    public ChooseRecordSetDlg(Frame arg0) throws HeadlessException
    {
        super(arg0);
        createUI();
    }

    /**
     * 
     *
     */
    protected void createUI()
    {
        JPanel panel = new JPanel(new BorderLayout());
        
        panel.add(new JLabel("Choose a RecordSet"), BorderLayout.NORTH);

        try
        {
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(RecordSet.class);
            recordSets = criteria.list();
              
            ListModel listModel = new AbstractListModel() 
            {
                public int getSize() { return recordSets.size(); }
                public Object getElementAt(int index) { return ((RecordSet)recordSets.get(index)).getName(); }
            };
            
            list = new JList(listModel);
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
        
        setContentPane(panel);
        pack();
        //setLocationRelativeTo(locationComp);
        
    }
    
    //Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e) 
    {
        /*
        if ("Set".equals(e.getActionCommand())) {
            ListDialog.value = (String)(list.getSelectedValue());
        }
        ListDialog.dialog.setVisible(false);
        */
    }

    
}

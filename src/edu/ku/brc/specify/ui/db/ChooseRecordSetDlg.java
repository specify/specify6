package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.specify.core.RecordSetTask;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.ui.IconListCellRenderer;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;

public class ChooseRecordSetDlg extends JDialog implements ActionListener 
{
    // Static Data Members
    private static Log log = LogFactory.getLog(ChooseRecordSetDlg.class);

    
    private final static ImageIcon icon = IconManager.getImage(RecordSetTask.RECORD_SET, IconManager.IconSize.Std16);

    // Data Members
    protected JButton        cancelBtn;
    protected JButton        okBtn;
    protected JList          list;
    protected java.util.List recordSets;
    
    public ChooseRecordSetDlg() throws HeadlessException
    {
        super((Frame)UICacheManager.getInstance().get(UICacheManager.FRAME), true);
        createUI();
        setLocationRelativeTo((JFrame)(Frame)UICacheManager.getInstance().get(UICacheManager.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setAlwaysOnTop(true);
    }

    /**
     * 
     *
     */
    protected void createUI()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        
        panel.add(new JLabel(getResourceString("ChooseRecordSet"), JLabel.CENTER), BorderLayout.NORTH);

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
            
            list = new JList(listModel);
            list.setCellRenderer(new IconListCellRenderer(icon)); // icon comes from the base class (it's probably size 16)
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

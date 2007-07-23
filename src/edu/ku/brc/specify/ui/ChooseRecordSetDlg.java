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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.ui.IconListCellRenderer;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * Choose a record set from the a list from the database
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ChooseRecordSetDlg extends JDialog implements ActionListener
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ChooseRecordSetDlg.class);


    private ImageIcon icon = IconManager.getImage(RecordSetTask.RECORD_SET, IconManager.IconSize.Std16);

    // Data Members
    protected JButton         cancelBtn;
    protected JButton         okBtn;
    protected JList           list;
    protected List<RecordSet> recordSets;

    public ChooseRecordSetDlg(final Frame frame, final int tableId) throws HeadlessException
    {
        super(frame, true);
        createUI(tableId);
        setLocationRelativeTo(UIRegistry.get(UIRegistry.FRAME));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     *
     *
     */
    @SuppressWarnings("unchecked")
    protected void createUI(final int tableId)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        panel.add(new JLabel(getResourceString("ChooseRecordSet"), SwingConstants.CENTER), BorderLayout.NORTH);

        try
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            if (tableId == -1)
            {
               recordSets = session.getDataList(RecordSet.class);
            } else
            {
                recordSets = (List<RecordSet>)session.getDataList("from recordset in class RecordSet where recordset.dbTableId = " + tableId);
            }
            session.close();

            ListModel listModel = new AbstractListModel()
            {
                public int getSize() { return recordSets.size(); }
                public Object getElementAt(int index) { return recordSets.get(index).getName(); }
            };

            if (recordSets.size() > 0)
            {
                DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(recordSets.get(0).getDbTableId());
                if (tblInfo != null)
                {
                    ImageIcon rsIcon = tblInfo.getIcon(IconManager.IconSize.Std16);
                    if (rsIcon != null)
                    {
                        icon = rsIcon;
                    }
                }
            }
            list = new JList(listModel);
            list.setCellRenderer(new IconListCellRenderer(icon)); // icon comes from the base class (it's probably size 16)
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

    //Handle clicks on the Set and Cancel buttons.
    public void actionPerformed(ActionEvent e)
    {
        setVisible(false);
    }

    /**
     * @return whether the list has any items
     */
    public boolean hasRecordSets()
    {
        return list.getModel().getSize() > 0;
    }

    /**
     * @return the List of RecordSets
     */
    public List<RecordSet> getRecordSets()
    {
        return recordSets;
    }

    /**
     * Returns the selected recordset
     * @return the selected recordset
     */
    public RecordSetIFace getSelectedRecordSet()
    {
        int inx = list.getSelectedIndex();
        if (inx != -1)
        {
            return recordSets.get(inx);
        }
        return null;
    }

}

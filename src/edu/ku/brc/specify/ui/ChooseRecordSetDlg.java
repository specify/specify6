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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.ui.db.RecordSetListCellRenderer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * Choose a record set from the a list from the database.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ChooseRecordSetDlg extends CustomDialog
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(ChooseRecordSetDlg.class);

    // Data Members
    protected JList                list       = null;
    protected List<RecordSetIFace> recordSets = null;

    /**
     * @param tableId
     * @throws HeadlessException
     */
    public ChooseRecordSetDlg(final int tableId) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("RECORDSET_CHOOSE"), true, OKCANCELHELP, null);
        
        initialize(tableId);
    }

    /**
     *
     *
     */
    @SuppressWarnings("unchecked")
    protected void initialize(final int tableId)
    {
        DataProviderSessionIFace session = null;
        try
        {
            String sql = "FROM recordset in class RecordSet WHERE type = 0";
            session = DataProviderFactory.getInstance().createSession();
            if (tableId == -1)
            {
                recordSets = (List<RecordSetIFace>)session.getDataList(sql);
            } else
            {
                recordSets = (List<RecordSetIFace>)session.getDataList(sql + " AND recordset.dbTableId = " + tableId);
            }

        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        okBtn.setEnabled(false);
        
        ListModel listModel = new AbstractListModel()
        {
            public int getSize() { return recordSets == null ? 0 : recordSets.size(); }
            public Object getElementAt(int index) { return recordSets == null ? null : recordSets.get(index); }
        };

        list = new JList(listModel);
        list.setCellRenderer(new RecordSetListCellRenderer());
        list.setVisibleRowCount(10);
        list.addMouseListener(new MouseAdapter() 
        {
            public void mouseClicked(MouseEvent e) 
            {
                okBtn.setEnabled(list.getSelectedIndex() > -1);
                
                if (e.getClickCount() == 2) 
                {
                    okBtn.doClick(); //emulate button click
                    return;
                }
                
            }
        });
        
        JPanel      panel        = new JPanel(new BorderLayout());
        JScrollPane listScroller = new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(listScroller, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        contentPanel = panel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        super.cleanUp();
        
        if (recordSets != null)
        {
            recordSets.clear();
        }
    }

    /**
     * @param additionalTableId
     * @param ids
     */
    public void addAdditionalObjectsAsRecordSets(final Vector<RecordSetIFace> additionalRS)
    {
        if (recordSets == null)
        {
            recordSets = new Vector<RecordSetIFace>();
        }
        recordSets.addAll(additionalRS);
    }
    
    /**
     * @return whether the list has any items
     */
    public boolean hasRecordSets()
    {
        return recordSets != null && recordSets.size() > 0;
    }

    /**
     * @return the List of RecordSets
     */
    public List<RecordSetIFace> getRecordSets()
    {
        return recordSets;
    }

    /**
     * Returns the selected recordset
     * @return the selected recordset
     */
    public RecordSetIFace getSelectedRecordSet()
    {
        if (list != null)
        {
            int inx = list.getSelectedIndex();
            if (inx != -1)
            {
                return recordSets.get(inx);
            }
        }
        return null;
    }

}

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
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.ui.db.RecordSetListCellRenderer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

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
    protected JList                       list       = null;
    protected List<Pair<String, Integer>> recordSets = null;
    protected boolean                     includeAddBtn = false;

    /**
     * @param tableId
     * @throws HeadlessException
     */
    public ChooseRecordSetDlg(final int tableId) throws HeadlessException
    {
        this(tableId, false);
    }

    /**
     * @param tableId
     * @param includeAddBtn
     * @throws HeadlessException
     */
    public ChooseRecordSetDlg(final int tableId, final boolean includeAddBtn) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("RECORDSET_CHOOSE"), true, OKCANCELHELP, null);
        
        Vector<Integer> id = new Vector<Integer>(1);
        id.add(tableId);
        initialize(id);
        
        this.includeAddBtn = includeAddBtn;
        if (includeAddBtn)
        {
            this.whichBtns = OKCANCELAPPLYHELP;
            setApplyLabel(getResourceString("New"));
        }
        
        this.helpContext = "RS_Add";
    }

    /**
     * @param tableIds
     * @throws HeadlessException
     */
    public ChooseRecordSetDlg(final Vector<Integer> tableIds) throws HeadlessException
    {
        this(tableIds, false);
    }
    
    /**
     * @param tableIds
     * @param includeAddBtn
     * @throws HeadlessException
     */
    public ChooseRecordSetDlg(final Vector<Integer> tableIds, final boolean includeAddBtn) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("RECORDSET_CHOOSE"), true, OKCANCELHELP, null);
        
        this.includeAddBtn = includeAddBtn;
        if (includeAddBtn)
        {
            this.whichBtns = OKCANCELAPPLYHELP;
            setApplyLabel(getResourceString("New"));
        }
        
        initialize(tableIds);
        
        this.helpContext = "ChooseRecordSet";
    }
    
    /**
     *
     *
     */
    protected void initialize(final Vector<Integer> tableIds)
    {
        String sql = "SELECT rs.Name,RecordSetID FROM recordset rs INNER JOIN specifyuser spu " + 
                     "ON spu.SpecifyUserID = rs.SpecifyUserID WHERE rs.Type = 0 AND "+
                     "rs.CollectionMemberID = COLLID AND spu.SpecifyUserID = SPECIFYUSERID";
        
        if (tableIds.size() > 0 && !(tableIds.size() == 0 && tableIds.get(0).intValue() < 0))
        {
        	StringBuilder sb = new StringBuilder(tableIds.get(0).toString());
        	for (int i = 1; i < tableIds.size(); i++)
        	{
        		sb.append(",");
        		sb.append(tableIds.get(i));
        	}
        	sql += " AND rs.TableID in(" + sb.toString() + ")";
        }
        sql += " ORDER BY rs.Name";
        
        sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
        System.out.println(sql);
        
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        if (rows != null && rows.size() > 0)
        {
        	recordSets = new Vector<Pair<String, Integer>>();
        	for (Object[] row : rows)
        	{
        		recordSets.add(new Pair<String, Integer>(row[0].toString(), (Integer)row[1]));
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
        list.setCellRenderer(new PairListRenderer());
        
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
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(UIHelper.createScrollPane(list), BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        contentPanel = panel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#applyButtonPressed()
     */
    @Override
    protected void applyButtonPressed()
    {
        super.applyButtonPressed();
        
        setVisible(false);
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
     */
    public void addAdditionalObjectsAsRecordSets(final Vector<RecordSetIFace> additionalRS)
    {
        if (recordSets == null)
        {
            recordSets = new Vector<Pair<String,Integer>>();
        }
        
        for (RecordSetIFace rsi : additionalRS)
        {
            recordSets.add(new Pair<String, Integer>(rsi.getName(), rsi.getOnlyItem().getRecordId()));
        }
    }
    
    /**
     * @return whether the list has any items
     */
    public boolean hasRecordSets()
    {
        return recordSets != null && recordSets.size() > 0;
    }

    /**
     * Returns the selected RecordSetIFace
     * @return the selected RecordSetIFace
     */
    public RecordSetIFace getSelectedRecordSet()
    {
        if (list != null)
        {
            int inx = list.getSelectedIndex();
            if (inx != -1)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    String sql = "FROM RecordSet WHERE recordSetId = " + recordSets.get(inx).second;
                    
                    session = DataProviderFactory.getInstance().createSession();
                    List<?> rvList = session.getDataList(sql);
                    if (rvList.size() == 1)
                    {
                        return (RecordSetIFace)rvList.get(0);
                    }

                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ChooseRecordSetDlg.class, ex);
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
        }
        return null;
    }

    class PairListRenderer extends DefaultListCellRenderer
    {
        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @SuppressWarnings("unchecked")
        @Override
        public Component getListCellRendererComponent(@SuppressWarnings("hiding") JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setText(((Pair<String, Integer>)value).first);
            return label;
        }
        
    }
}

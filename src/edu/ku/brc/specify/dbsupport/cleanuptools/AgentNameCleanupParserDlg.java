/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.specify.dbsupport.cleanuptools.FirstLastVerifier.appendSuffixTo;
import static edu.ku.brc.specify.dbsupport.cleanuptools.FirstLastVerifier.parseName;
import static edu.ku.brc.ui.UIHelper.autoResizeColWidth;
import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.ui.CustomDialog;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 29, 2012
 *
 */
public class AgentNameCleanupParserDlg extends CustomDialog
{
    
    private Connection    conn;
    private JTable        table;
    private NameDataModel model; 
    
    protected JButton     selectAllBtn;
    protected JButton     deselectAllBtn;
    
    private Vector<DataItem> list = new Vector<DataItem>();

    /**
     * @param connection
     * @throws HeadlessException
     */
    public AgentNameCleanupParserDlg(final Connection connection) throws HeadlessException
    {
        super((Frame)getTopWindow(), "", true, OKCANCEL, null);
        this.conn = connection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        
        CellConstraints cc  = new CellConstraints();
        PanelBuilder    pb  = new PanelBuilder(new FormLayout("f:p:g", "p,2px,f:p:g,2px,p"));

        this.contentPanel = pb.getPanel();

        setOkLabel(getResourceString("CLNUP_PROCESS"));
        //setCancelLabel(getResourceString("CLOSE"));
        super.createUI();
        
        fillModel();
        
        model = new NameDataModel();
        table = new JTable(model);
        
        autoResizeColWidth(table, model);
        
        pb.addSeparator(getResourceString("CLNUP_VERIFY_NMS"), cc.xy(1, 1));
        pb.add(createScrollPane(table, true), cc.xy(1, 3));
        
        PanelBuilder btnPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g", "p")); //$NON-NLS-1$ //$NON-NLS-2$
        selectAllBtn   = createI18NButton("SELECTALL"); //$NON-NLS-1$
        deselectAllBtn = createI18NButton("DESELECTALL"); //$NON-NLS-1$
        btnPB.add(selectAllBtn,   cc.xy(2, 1));
        btnPB.add(deselectAllBtn, cc.xy(4, 1));
        
        pb.add(btnPB.getPanel(), cc.xy(1, 5));
        
        pb.setDefaultDialogBorder();
        
        pack();
        
        Dimension d = getPreferredSize();
        d.width += 150;
        setSize(d);
        
        selectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (DataItem di : list)
                {
                    di.setIncluded(true);
                }
                model.fireTableDataChanged();
            }
        });
        deselectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for (DataItem di : list)
                {
                    di.setIncluded(false);
                }
                model.fireTableDataChanged();
            }
        });
    }
    
    /**
     * @param str
     * @return
     */
    private boolean doIncludeStr(final String str)
    {
        if (StringUtils.isNotEmpty(str))
        {
            String lower = StringUtils.remove(str.toLowerCase(), '.');
            if (lower.startsWith("ltd") || 
                lower.startsWith("exp") || 
                lower.equals("party") || 
                lower.equals("etal") || 
                lower.equals("et al") || 
                lower.equals("et") || 
                lower.equals("mrs") || 
                lower.equals("mr") || 
                StringUtils.isNumeric(lower))
            {
                return false;
            }
            return true;
        }
        return true;
    }
    
    /**
     * 
     */
    private void fillModel()
    {
        FirstLastVerifier flVerifier = new FirstLastVerifier();
        try
        {
            Statement stmt = conn.createStatement();
            String sql = String.format("SELECT LastName, AgentID FROM agent WHERE SpecifyUserID IS NULL AND LastName IS NOT NULL AND " +
            	                       "FirstName IS NULL AND MiddleInitial IS NULL AND DivisionID = DIVID AND AgentType = %d", Agent.PERSON);
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            //System.out.println(sql);
            
            int row = 1;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                String fullName = rs.getString(1);
                String lower    = fullName.toLowerCase();
                
                boolean doInclude = !lower.contains(" and ") && !lower.contains("+") && !lower.contains("&");
                
                String lastName  = "";
                String firstName = "";
                String midName   = "";
                
                if (doInclude)
                {
                    String[] nms = parseName(fullName.toString(), false);
                    if (nms != null)
                    {
                        if (nms.length > 1 && nms[1].length() > 2 && !flVerifier.isLastName(nms[0]) && flVerifier.isFirstName(nms[1]))
                        {
                            System.out.println(nms[0]+" "+nms[1]);
                            String tmp = nms[1];
                            nms[1] = nms[0];
                            nms[0] = tmp;
                        }
                        
                        lastName = appendSuffixTo(nms[0]);
                        if ( nms.length == 2)
                        {
                            firstName = nms[1];
                        } else if (nms.length == 3)
                        {
                            firstName = nms[1];
                            midName   = nms[2];
                        }
                        
                        if (!doIncludeStr(firstName) || !doIncludeStr(midName))
                        {
                            doInclude = false;
                        }
                    }
                    
                    DataItem di = new DataItem(row++, doInclude, fullName, lastName, firstName, midName, rs.getInt(2));
                    list.add(di);
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        flVerifier.shutdown();
    }
    
    /**
     * @return the list
     */
    public Vector<DataItem> getList()
    {
        return list;
    }


    //---------------------------------------------------------------------
    class NameDataModel extends DefaultTableModel
    {
        protected String[] headers = {"ROW", "ISINCL", "FULLNM", "LAST", "FIRST", "MID"};
        
        /**
         * 
         */
        public NameDataModel()
        {
            super();
            
            for (int i=0;i<headers.length;i++)
            {
                headers[i] = getResourceString("CLNUP_MERGE_" + headers[i]);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return list != null ? list.size() : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return headers != null ? headers.length : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return headers[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column != 0 && column != 2;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            DataItem di = list.get(row);
            switch (column)
            {
                case 0: return di.getRowNum();
                case 1: return di.isIncluded();
                case 2: return di.getFullName();
                case 3: return di.getLastName();
                case 4: return di.getFirstName();
                case 5: return di.getMidName();
            }
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object val, int row, int column)
        {
            DataItem di = list.get(row);
            switch (column)
            {
                case 1: 
                    di.setIncluded((Boolean)val);
                    break;
                case 2: 
                    di.setFullName((String)val);
                    break;
                case 3: 
                    di.setLastName((String)val);
                    break;
                case 4: 
                    di.setFirstName((String)val);
                    break;
                case 5: 
                    di.setMidName((String)val);
                    break;
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int column)
        {
            switch (column)
            {
                case 0: return Integer.class;
                case 1: return Boolean.class;
                default: return String.class;
            }
        }
    }
    
    //---------------------------------------------------------------------
    public class DataItem 
    {
        int     rowNum;
        boolean isIncluded;
        String  fullName;
        String  lastName;
        String  firstName;
        String  midName;
        Integer agentId;
        
        /**
         * @param rowNum
         * @param isIncluded
         * @param fullName
         * @param lastName
         * @param firstName
         * @param midName
         * @param agentId
         */
        public DataItem(final int rowNum,
                        final boolean isIncluded,
                        final String fullName, 
                        final String lastName, 
                        final String firstName,
                        final String midName, 
                        final int agentId)
        {
            super();
            this.rowNum     = rowNum;
            this.isIncluded = isIncluded;
            this.fullName   = fullName;
            this.lastName   = lastName;
            this.firstName  = firstName;
            this.midName    = midName;
            this.agentId    = agentId;
        }


        /**
         * @return the isIncluded
         */
        public boolean isIncluded()
        {
            return isIncluded;
        }


        /**
         * @param isIncluded the isIncluded to set
         */
        public void setIncluded(boolean isIncluded)
        {
            this.isIncluded = isIncluded;
        }


        /**
         * @return the agentId
         */
        public Integer getAgentId()
        {
            return agentId;
        }

        /**
         * @return the fullName
         */
        public String getFullName()
        {
            return fullName;
        }
        /**
         * @return the lastName
         */
        public String getLastName()
        {
            return lastName;
        }
        /**
         * @return the firstName
         */
        public String getFirstName()
        {
            return firstName;
        }
        /**
         * @return the midName
         */
        public String getMidName()
        {
            return midName;
        }


        /**
         * @param fullName the fullName to set
         */
        public void setFullName(String fullName)
        {
            this.fullName = fullName;
        }


        /**
         * @param lastName the lastName to set
         */
        public void setLastName(String lastName)
        {
            this.lastName = lastName;
        }


        /**
         * @param firstName the firstName to set
         */
        public void setFirstName(String firstName)
        {
            this.firstName = firstName;
        }


        /**
         * @param midName the midName to set
         */
        public void setMidName(String midName)
        {
            this.midName = midName;
        }


        /**
         * @return the rowNum
         */
        public int getRowNum()
        {
            return rowNum;
        }
        
    }
}

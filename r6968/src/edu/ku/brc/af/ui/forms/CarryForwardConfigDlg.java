/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.FormViewObj.FVOFieldInfo;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellLabel;
import edu.ku.brc.af.ui.forms.persist.FormCellSubView;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace.FieldType;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Mar 9, 2008
 *
 */
public class CarryForwardConfigDlg extends CustomDialog
{
    private static final Logger log = Logger.getLogger(CarryForwardConfigDlg.class);
    
    protected Color[]         toggleColors = {Color.WHITE, new Color(230,230,230)};
    protected int             togInx = 0;
    protected String          togTableName = "";
    
    protected MultiView       mvParent;
    protected JTable          table;
    protected CFTableModel    model;
    protected Vector<CFWItem> items = new Vector<CFWItem>();
    
    /**
     *
     */
    public CarryForwardConfigDlg(final MultiView mvParent)
    {
        super((Frame)getTopWindow(), getResourceString("MV_CFW_CONFIG_TITLE"), true, null);
        
        this.mvParent = mvParent;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        buildTableModel(mvParent);
        
        model = new CFTableModel();
        table = new JTable(model)
        {
            //  Returning the Class of each column will allow different
            //  renderers to be used based on Class
            public Class<?> getColumnClass(int column)
            {
                return getValueAt(0, column).getClass();
            }
 
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
            {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!c.getBackground().equals(getSelectionBackground()))
                {
                    CFWItem item = items.get(row);
                    c.setBackground( item.getColor());
                }
                return c;
            }
        };
        
        UIHelper.makeTableHeadersCentered(table, false);
        
        //((DefaultTableCellRenderer)table.getColumnModel().getColumn(1).getCellRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        
        JScrollPane sp = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        contentPanel = sp;//new JPanel(new BorderLayout()));
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * @param mvp
     */
    protected void buildTableModel(final MultiView mvp)
    {
        for (Viewable viewable : mvp.getViewables())
        {
            if (viewable instanceof FormViewObj && ((FormViewObj)viewable).isEditing)
            {
                Vector<String> ids = new Vector<String>();
                FormViewObj fvo = (FormViewObj)viewable;
                fvo.getFieldIds(ids, true);
                for (String id : ids)
                {
                    FVOFieldInfo fieldInfo = fvo.getFieldInfoForId(id);
                    boolean isOK = true;
                    if (fieldInfo.getFormCell() instanceof FormCellFieldIFace)
                    {
                        FieldType type = ((FormCellFieldIFace)fieldInfo.getFormCell()).getUiType();
                        if (type == FieldType.dsptextfield || type == FieldType.dsptextarea || type == FieldType.label)
                        {
                            isOK = false; 
                        }
                        
                    } else if (fieldInfo.getFormCell() instanceof FormCellSubView &&
                               fieldInfo.getSubView() != null)
                    {
                        //buildTableModel(fieldInfo.getSubView());
                        continue;
                    }
                    
                    if (isOK)
                    {
                        FVOFieldInfo labelInfo = fvo.getLabelInfoFor(id);
                        if (labelInfo != null)
                        {
                            if (!(fieldInfo.getFormCell() instanceof FormCellLabel))
                            {
                                String lbl = ((FormCellLabel)labelInfo.getFormCell()).getLabel();
                                fieldInfo.setLabel(lbl);
                                CFWItem item = new CFWItem(mvp, fvo, fieldInfo);
                                if (!item.getTableTitle().equals(togTableName))
                                {
                                    togTableName = item.getTableTitle();
                                    togInx++;
                                }
                                item.setColor(toggleColors[togInx % 2]);
                                items.add(item);
                            }
                        } else 
                        {
                            String fieldName = fieldInfo.getFormCell().getName();
                            DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(mvp.getView().getClassName());
                            if (ti != null)
                            {
                                DBFieldInfo fi = ti.getFieldByName(fieldName);
                                if (fi != null)
                                {
                                    fieldInfo.setLabel(fi.getTitle());
                                    CFWItem item = new CFWItem(mvp, fvo, fieldInfo);
                                    if (!item.getTableTitle().equals(togTableName))
                                    {
                                        togTableName = item.getTableTitle();
                                        togInx++;
                                    }
                                    item.setColor(toggleColors[togInx % 2]);
                                    items.add(item);
                                } else
                                {
                                    log.error("Couldn't find field ["+fieldName+"] in ["+ti.getTitle()+"]");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //------------------------------------------------------------
    class CFWItem
    {
        protected MultiView   mv;
        protected FormViewObj fvo;
        protected FVOFieldInfo   fieldInfo;
        protected DBTableInfo ti;
        protected Boolean     isChecked = false;
        protected Color       color;
        
        /**
         * @param mv
         * @param fvo
         * @param fieldName
         */
        public CFWItem(MultiView   mv, 
                       FormViewObj fvo, 
                       FVOFieldInfo   fieldInfo)
        {
            super();
            this.mv         = mv;
            this.fvo        = fvo;
            this.fieldInfo  = fieldInfo;
            
            ti = DBTableIdMgr.getInstance().getByClassName(mv.getView().getClassName());

        }
        /**
         * @return the mv
         */
        public MultiView getMv()
        {
            return mv;
        }
        /**
         * @return the fvo
         */
        public FormViewObj getFvo()
        {
            return fvo;
        }
        /**
         * @return the fieldInfo
         */
        public FVOFieldInfo getFieldInfo()
        {
            return fieldInfo;
        }
        
        public String getTableTitle()
        {
            return ti.getTitle();
        }
        /**
         * @return the isChecked
         */
        public Boolean getIsChecked()
        {
            return isChecked;
        }
        /**
         * @param isChecked the isChecked to set
         */
        public void setIsChecked(Boolean isChecked)
        {
            this.isChecked = isChecked;
        }
        /**
         * @return the color
         */
        public Color getColor()
        {
            return color;
        }
        /**
         * @param color the color to set
         */
        public void setColor(Color color)
        {
            this.color = color;
        }
        
    }

    //------------------------------------------------------------
    class CFTableModel extends DefaultTableModel
    {
        protected String[] colTitles = {"Field Name", "Form", "Carry Forward"};

        public CFTableModel()
        {
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int col)
        {
            return col == 2 ? Boolean.class : String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return colTitles != null ? colTitles.length : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int index)
        {
            return colTitles[index];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return items.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int col)
        {
            CFWItem item = items.get(row);
            if (col == 0)
            {
                String title = item.getFieldInfo().getLabel();
                if (StringUtils.isNotEmpty(title))
                {
                    title = item.getFieldInfo().getName();
                }
                return item.getFieldInfo().getLabel();
                
            } else if (col == 2)
            {
                return item.getIsChecked();
            }
            return item.getTableTitle();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column == 2;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object val, int row, int col)
        {
            CFWItem item = items.get(row);
            if (col == 2)
            {
                item.setIsChecked((Boolean)val);
                
            }
        }
        
    }
}

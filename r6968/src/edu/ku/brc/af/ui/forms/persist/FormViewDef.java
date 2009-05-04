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
package edu.ku.brc.af.ui.forms.persist;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;
import static edu.ku.brc.helpers.XMLHelper.xmlNode;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;


/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormViewDef extends ViewDef implements Cloneable, FormViewDefIFace
{
    protected JGDefItem            columnDef      = new JGDefItem();
    protected JGDefItem            rowDef         = new JGDefItem();
    protected Vector<FormRowIFace> rows           = new Vector<FormRowIFace>(); 
    protected String               definitionName = null;
    
    protected Hashtable<String, String>  enableRules = null;

    /**
     * @param type the type (could be form or field)
     * @param name the name
     * @param className the class name of the data object
     * @param gettableClassName the class name of the gettable
     * @param settableClassName the class name of the settable
     * @param desc description
     * @param useResourceLabels whether to use resource string
     */
    public FormViewDef(final ViewDef.ViewType type, 
                       final String  name, 
                       final String  className, 
                       final String  gettableClassName, 
                       final String  settableClassName, 
                       final String  desc,
                       final boolean useResourceLabels)
    {
        super(type, name, className, gettableClassName, settableClassName, desc, useResourceLabels);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDerivedInterface()
     */
    public Class<?> getDerivedInterface()
    {
        return FormViewDefIFace.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#addRow(edu.ku.brc.ui.forms.persist.FormRowIFace)
     */
    public FormRowIFace addRow(FormRowIFace row)
    {
        rows.add(row);
        return row;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getRows()
     */
    public Vector<FormRowIFace> getRows()
    {
        return rows;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getFormCellById(java.lang.String)
     */
    public FormCellIFace getFormCellById(String idStr)
    {
        for (FormRowIFace row : rows)
        {
            for (FormCellIFace c : row.getCells())
            {
                if (c.getIdent().equals(idStr))
                {
                    return c;
                }
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getFormCellByName(java.lang.String)
     */
    public FormCellIFace getFormCellByName(final String nameStr)
    {
        for (FormRowIFace row : rows)
        {
            for (FormCellIFace c : row.getCells())
            {
                if (c.getName().equals(nameStr))
                {
                    return c;
                }
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormView#cleanUp()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        super.cleanUp();
        for (FormRowIFace row : rows)
        {
            row.cleanUp();
        }
        rows.clear();
        enableRules.clear();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getColumnDef()
     */
    public String getColumnDef()
    {
        return columnDef.getDefStr();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setColumnDef(java.lang.String)
     */
    public void setColumnDef(String columnDef)
    {
        this.columnDef.setDefStr(columnDef);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getRowDef()
     */
    public String getRowDef()
    {
        return rowDef.getDefStr();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setRowDef(java.lang.String)
     */
    public void setRowDef(String rowDef)
    {
        this.rowDef.setDefStr(rowDef);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getEnableRules()
     */
    public Hashtable<String, String> getEnableRules()
    {
        return enableRules;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setEnableRules(java.util.Map)
     */
    public void setEnableRules(Hashtable<String, String> enableRules)
    {
        this.enableRules = enableRules;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#getDefinitionName()
     */
    public String getDefinitionName()
    {
        return definitionName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#setDefinitionName(java.lang.String)
     */
    public void setDefinitionName(String definitionName)
    {
        this.definitionName = definitionName;
    }
    
    /**
     * @return
     */
    public JGDefItem getRowDefItem()
    {
        return this.rowDef;
    }

    /**
     * @return
     */
    public JGDefItem getColumnDefItem()
    {
        return this.columnDef;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormViewDefIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormViewDef fvd = (FormViewDef)super.clone();
        fvd.rows        = new Vector<FormRowIFace>(); 
        fvd.columnDef   = (JGDefItem)columnDef.clone();
        fvd.rowDef      = (JGDefItem)rowDef.clone();
        fvd.definitionName = definitionName;
        for (FormRowIFace formRow : rows)
        {
            fvd.rows.add((FormRow)formRow.clone()); 
        }
        return fvd;      
    }
    
    //------------------------------------------
    //-- Inner Classes
    //------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDef#toXMLAttrs(java.lang.StringBuffer)
     */
    @Override
    protected void toXMLAttrs(final StringBuilder sb)
    {
        super.toXMLAttrs(sb);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDef#toXMLNodes(java.lang.StringBuffer)
     */
    @Override
    protected void toXMLNodes(final StringBuilder sb)
    {
        super.toXMLNodes(sb);
        
        //if (isNotEmpty(definitionName))
        {
            sb.append("  ");
            columnDef.toXML("columnDef", sb);
        }
        //if (isNotEmpty(definitionName)))
        {
            sb.append("  ");
            rowDef.toXML("rowDef", sb);
        }
        
        //System.out.println(hashCode()+" ["+definitionName+"]");
        if (isNotEmpty(definitionName))
        {
            sb.append("  ");
            xmlNode(sb, "definition", definitionName, false);
        }
        
        for (FormRowIFace row : rows)
        {
            row.toXML(sb);
        }
    }

    public class JGDefItem implements Cloneable
    {
        protected String  defStr;
        protected boolean isAuto;
        protected String  cellDefStr;
        protected String  sepDefStr;
        protected int     numItems = 0;
        
        /**
         * 
         */
        public JGDefItem()
        {
            // no op
        }

        /**
         * @param defStr
         * @param isAuto
         * @param singleDefStr
         * @param sepDefStr
         */
        public JGDefItem(final String defStr, final boolean isAuto, final String singleDefStr, final String sepDefStr)
        {
            this.defStr     = defStr;
            this.isAuto     = isAuto;
            this.cellDefStr = singleDefStr;
            this.sepDefStr  = sepDefStr;
            
            numItems = StringUtils.countMatches(defStr, ",") + 1;
        }

        /**
         * @return the defStr
         */
        public String getDefStr()
        {
            return defStr;
        }

        /**
         * @param defStr the defStr to set
         */
        public void setDefStr(String defStr)
        {
            this.defStr   = defStr;
            this.numItems = StringUtils.countMatches(defStr, ",") + 1;
        }

        /**
         * @return the isAuto
         */
        public boolean isAuto()
        {
            return isAuto;
        }

        /**
         * @param isAuto the isAuto to set
         */
        public void setAuto(boolean isAuto)
        {
            this.isAuto = isAuto;
        }

        /**
         * @return the singleDefStr
         */
        public String getCellDefStr()
        {
            return cellDefStr;
        }

        /**
         * @param singleDefStr the singleDefStr to set
         */
        public void setCellDefStr(String cellDefStr)
        {
            this.cellDefStr = cellDefStr;
        }

        /**
         * @return the sepDefStr
         */
        public String getSepDefStr()
        {
            return sepDefStr;
        }

        /**
         * @param sepDefStr the sepDefStr to set
         */
        public void setSepDefStr(String sepDefStr)
        {
            this.sepDefStr = sepDefStr;
        }
        
        public Object clone() throws CloneNotSupportedException
        {
            JGDefItem item = (JGDefItem)super.clone();
            
            item.defStr       = defStr;
            item.isAuto       = isAuto;
            item.cellDefStr = cellDefStr;
            item.sepDefStr    = sepDefStr;
            return item;      
        }
        
        /**
         * @return the numItems
         */
        public int getNumItems()
        {
            return numItems;
        }

        /**
         * @param numItems the numItems to set
         */
        public void setNumItems(int numItems)
        {
            this.numItems = numItems;
        }
        
        public void toXML(final String nodeName, final StringBuilder sb)
        {
            if (defStr != null)
            {
                sb.append("    <");
                sb.append(nodeName);
                if (isAuto)
                {
                    xmlAttr(sb, "auto", true);
                    xmlAttr(sb, "cell", cellDefStr);
                    xmlAttr(sb, "sep", sepDefStr);
                    sb.append("/>\n");
                } else
                {
                    sb.append('>');
                    sb.append(defStr);
                    sb.append("</");
                    sb.append(nodeName);
                    sb.append(">\n");
                }
            }
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("defStr:       "+defStr+"\n");
            sb.append("isAuto:       "+isAuto+"\n");
            sb.append("singleDefStr: "+cellDefStr+"\n");
            sb.append("sepDefStr:    "+sepDefStr+"\n");
            
            return sb.toString();
        }
        
    }
}

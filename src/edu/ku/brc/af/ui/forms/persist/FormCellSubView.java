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
package edu.ku.brc.af.ui.forms.persist;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormCellSubView extends FormCell implements FormCellSubViewIFace
{

    protected String  viewSetName;
    protected String  viewName;
    protected String  classDesc;
    protected boolean singleValueFromSet;
    protected String  description;
    protected String  defaultAltViewType;
    protected String  funcModes = null;
    
    //protected int     xCoord  = 0;
    //protected int     yCoord  = 0;
    //protected int     width   = 0;
    //protected int     height  = 0;
    
    // For Table/Grid SubViews
    protected int    tableRows = 3;
    
    protected List<Modes> modesList = null;
    
    /**
     * Constructor.
     *
     */
    public FormCellSubView()
    {
        type = CellType.subview;
    }
    
    /**
     * Constructor.
     * @param id unique id
     * @param name name of field for this view
     * @param viewSetName name of view set that this subview is referencing
     * @param viewName the name of the view within the view set
     * @param classDesc the class name of the field
     * @param description text descrption of the sub form (typically already localized)
     * @param defaultAltViewType preferred type of alt view (table or form)
     * @param tableRows when subview is a table this indicates how many rows should be displayed
     * @param colspan column span
     * @param rowspan row span
     * @param singleValueFromSet althught the data might be a "Set" pass in only the first data obj from the set
     */
    public FormCellSubView(final String id,
                           final String name, 
                           final String viewSetName, 
                           final String viewName, 
                           final String classDesc, 
                           final String description, 
                           final String defaultAltViewType, 
                           final int    tableRows, 
                           final int    colspan, 
                           final int    rowspan,
                           final boolean singleValueFromSet)
    {
        super(CellType.subview, id, name, colspan, rowspan);
        this.viewName    = viewName;
        this.classDesc   = classDesc;
        this.viewSetName = viewSetName;
        this.description = description;
        this.defaultAltViewType = defaultAltViewType;
        this.tableRows = tableRows;
        this.singleValueFromSet = singleValueFromSet;
    }
    
    /**
     * Constructor.
     * @param id unique id
     * @param name name of field for this view
     * @param viewSetName name of view set that this subview is referencing
     * @param viewName the name of the view within the view set
     * @param classDesc the class name of the field
     * @param description text descrption of the sub form (typically already localized)
     * @param colspan column span
     * @param rowspan row span
     * @param singleValueFromSet althught the data might be a "Set" pass in only the first data obj from the set
     */
    public FormCellSubView(final String id,
                           final String name, 
                           final String viewSetName, 
                           final String viewName, 
                           final String classDesc, 
                           final String description, 
                           final int    colspan, 
                           final int    rowspan)
    {
        this(id, name, viewSetName, viewName, classDesc, description, null, 5, colspan, rowspan, false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getClassDesc()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getClassDesc()
     */
    public String getClassDesc()
    {
        return classDesc;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#setClassDesc(java.lang.String)
     */
    public void setClassDesc(String classDesc)
    {
        this.classDesc = classDesc;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getViewName()
     */
    public String getViewName()
    {
        return viewName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#setView(java.lang.String)
     */
    public void setView(String viewName)
    {
        this.viewName = viewName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getViewSetName()
     */
    public String getViewSetName()
    {
        return viewSetName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#setViewSetName(java.lang.String)
     */
    public void setViewSetName(String viewSetName)
    {
        this.viewSetName = viewSetName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#isSingleValueFromSet()
     */
    public boolean isSingleValueFromSet()
    {
        return singleValueFromSet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getDescription()
     */
    public String getDescription()
    {
        return description;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getDefaultAltViewType()
     */
    public String getDefaultAltViewType()
    {
        return defaultAltViewType;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getTableRows()
     */
    public int getTableRows()
    {
        return tableRows;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#setTableRows(int)
     */
    public void setTableRows(int tableRows)
    {
        this.tableRows = tableRows;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getModes(java.util.List)
     */
    public void getModes(List<Modes> list)
    {
        list.addAll(modesList);
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#getXCoord()
     */
    public int getXCoord()
    {
        return xCoord;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#setXCoord(int)
     */
    public void setXCoord(int coord)
    {
        xCoord = coord;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#getYCoord()
     */
    public int getYCoord()
    {
        return yCoord;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#setYCoord(int)
     */
    public void setYCoord(int coord)
    {
        yCoord = coord;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#getWidth()
     */
    public int getWidth()
    {
        return width;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#setWidth(int)
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#getHeight()
     */
    public int getHeight()
    {
        return height;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#setHeight(int)
     */
    public void setHeight(int height)
    {
        this.height = height;
    }
    

    /**
     * @param funcModes
     */
    public void setFuncModes(final String funcModes)
    {
        this.funcModes = funcModes;
        
        if (StringUtils.isNotEmpty(funcModes))
        {
            modesList = new Vector<Modes>();
            for (String tok : StringUtils.split(funcModes, ','))
            {
                modesList.add(Modes.valueOf(tok));
            }
        }
    }

    /**
     * @return the funcModes
     */
    public String getFuncModes()
    {
        return funcModes;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#fillWithFuncModes(java.util.List)
     */
    public void fillWithFuncModes(List<Modes> list)
    {
        list.addAll(modesList);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormCellSubView subViewDef = (FormCellSubView)super.clone();
        subViewDef.viewName    = viewName;
        subViewDef.classDesc   = classDesc;
        subViewDef.viewSetName = viewSetName;
        subViewDef.singleValueFromSet = singleValueFromSet;
        subViewDef.description = description;
        subViewDef.tableRows   = tableRows;
        return subViewDef;      
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCell#toXMLAttrs(java.lang.StringBuilder)
     */
    public void toXMLAttrs(StringBuilder sb)
    {
        xmlAttr(sb, "viewname", viewName);
        xmlAttr(sb, "desc", description);
        //xmlAttr(sb, "funcmode", getFuncModes());
        xmlAttr(sb, "defaulttype", defaultAltViewType);
        if (ViewLoader.DEFAULT_SUBVIEW_ROWS != tableRows) xmlAttr(sb, "rows", tableRows);
        if (singleValueFromSet) xmlAttr(sb, "single", singleValueFromSet);
    }
}

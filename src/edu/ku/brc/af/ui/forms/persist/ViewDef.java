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
import static edu.ku.brc.helpers.XMLHelper.xmlNode;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import edu.ku.brc.af.ui.forms.DataObjectGettable;
import edu.ku.brc.af.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.af.ui.forms.DataObjectSettable;
import edu.ku.brc.af.ui.forms.DataObjectSettableFactory;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class ViewDef implements Cloneable, ViewDefIFace
{
    private static final Logger log = Logger.getLogger(ViewDef.class);
    
    protected ViewType             type;
    protected String               name;
    protected String               desc;
    protected String               className;
    protected String               dataGettableName;
    protected String               dataSettableName;
    protected boolean              isAbsoluteLayout = false;
    
    protected DataObjectGettable   dataGettable     = null;
    protected DataObjectSettable   dataSettable     = null;
    
    protected boolean              useResourceLabels;
    protected String               resourceLabels   = null;

    protected int                  xCoord  = -1;
    protected int                  yCoord  = -1;
    protected int                  width   = -1;
    protected int                  height  = -1;
    
    /**
     * Default Constructor
     *
     */
    public ViewDef()
    {
        // do nothing
    }
    
    /**
     * Create View Def
     * @param type the type of form (form, table, field)
     * @param name a unique name for the ViewDef
     * @param className the clas name that this view def can display
     * @param dataGettableName the gettable name
     * @param dataSettableName the settable name
     * @param desc a description of the view def
     * @param useResourceLabels whether to use resource string
     */
    public ViewDef(final ViewType type, 
                   final String   name, 
                   final String   className, 
                   final String   dataGettableName, 
                   final String   dataSettableName, 
                   final String   desc,
                   final boolean useResourceLabels)
    {
        this.type = type;
        this.name = name;
        this.className = className;
        this.dataGettableName = dataGettableName;
        this.dataSettableName = dataSettableName;
        this.desc = desc;
        this.useResourceLabels = useResourceLabels;
        this.resourceLabels    = useResourceLabels ? "true" : "false";
        
        try
        {
            // Can't imagine why you would not ALWAYS want to have a Gettable
            if (isNotEmpty(dataGettableName))
            {
                dataGettable = DataObjectGettableFactory.get(className, dataGettableName);
            } else
            {
                log.error("dataGettableName or is null for "+name);
            }
           
            // OK to NOT have a Settable
            if (isNotEmpty(dataSettableName))
            {
                dataSettable = DataObjectSettableFactory.get(className, dataSettableName);
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ViewDef.class, ex);
            ex.printStackTrace(); // XXX REMOVE ME
            log.error(ex);
        }
    }
    
    /**
     * Copy Constructor.
     * @param sep the ViewDef to be copied
     */
    public ViewDef(final ViewDef viewDef)
    {
        this(viewDef.type, 
             viewDef.name, 
             viewDef.className, 
             viewDef.dataGettableName, 
             viewDef.dataSettableName, 
             viewDef.desc, 
             viewDef.useResourceLabels);
        
        dataGettable = viewDef.dataGettable;
        dataSettable = viewDef.dataSettable;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDerivedInterface()
     */
    public Class<?> getDerivedInterface()
    {
        return ViewDefIFace.class;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#cleanUp()
     */
    public void cleanUp()
    {
        dataGettable = null;
        dataSettable = null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getType()
     */
    public ViewDefIFace.ViewType getType()
    {
        return type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#setType(edu.ku.brc.ui.forms.persist.ViewDef.ViewType)
     */
    public void setType(final ViewDefIFace.ViewType type)
    {
        this.type = type;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDesc()
     */
    public String getDesc()
    {
        return desc;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#setDesc(java.lang.String)
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getClassName()
     */
    public String getClassName()
    {
        return className;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#setClassName(java.lang.String)
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDataGettableName()
     */
    public String getDataGettableName()
    {
        return dataGettableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#setDataGettableName(java.lang.String)
     */
    public void setDataGettableName(String dataGettableName)
    {
        this.dataGettableName = dataGettableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDataGettable()
     */
    public DataObjectGettable getDataGettable()
    {
        return dataGettable;
    }

    /**
     * @return the dataSettableName
     */
    public String getDataSettableName()
    {
        return dataSettableName;
    }

    /**
     * @param dataSettableName the dataSettableName to set
     */
    public void setDataSettableName(String dataSettableName)
    {
        this.dataSettableName = dataSettableName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDataSettable()
     */
    public DataObjectSettable getDataSettable()
    {
        return dataSettable;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#isUseResourceLabels()
     */
    public boolean isUseResourceLabels()
    {
        return useResourceLabels;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getResourceLabels()
     */
    public String getResourceLabels()
    {
        return resourceLabels;
    }
    
    /**
     * @return the isAbsoluteLayout
     */
    public Boolean isAbsoluteLayout()
    {
        return isAbsoluteLayout;
    }

    /**
     * @param isAbsoluteLayout the isAbsoluteLayout to set
     */
    public void setAbsoluteLayout(boolean isAbsoluteLayout)
    {
        this.isAbsoluteLayout = isAbsoluteLayout;
    }
    
    /**
     * @return the xCoord
     */
    public int getXCoord()
    {
        return xCoord;
    }

    /**
     * @param coord the xCoord to set
     */
    public void setXCoord(int coord)
    {
        xCoord = coord;
    }

    /**
     * @return the yCoord
     */
    public int getYCoord()
    {
        return yCoord;
    }

    /**
     * @param coord the yCoord to set
     */
    public void setYCoord(int coord)
    {
        yCoord = coord;
    }

    /**
     * @return the width
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width)
    {
        this.width = width;
    }

    /**
     * @return the height
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height)
    {
        this.height = height;
    }
    
    protected void toXMLAttrs(@SuppressWarnings("unused") final StringBuilder sb)
    {
        // no op
    }
    
    protected void toXMLNodes(@SuppressWarnings("unused") final StringBuilder sb)
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(final StringBuilder sb)
    {
        /*
         <viewdef
            type="iconview"
            name="AccessionIconView"
            class="edu.ku.brc.specify.datamodel.Accession"
            gettable="edu.ku.brc.af.ui.forms.DataGetterForObj"
            settable="edu.ku.brc.af.ui.forms.DataSetterForObj">
            <desc><![CDATA[The Accession Agent Icon Viewer]]></desc>
        </viewdef>
         */
        String indent = "\n        ";
        sb.append("    <viewdef");
        xmlAttr(sb, "type", type.toString());
        sb.append(indent);
        xmlAttr(sb, "name", name);
        sb.append(indent);
        xmlAttr(sb, "class", className);
        sb.append(indent);
        xmlAttr(sb, "resourcelabels", useResourceLabels);
        sb.append(indent);
        xmlAttr(sb, "gettable", dataGettableName);
        sb.append(indent);
        xmlAttr(sb, "settable", dataSettableName);
        if (xCoord > -1)
        {
            sb.append(indent);
            xmlAttr(sb, "x", xCoord);
        }
        if (yCoord > -1)
        {
            sb.append(indent);
            xmlAttr(sb, "y", yCoord);
        }
        if (width > -1)
        {
            sb.append(indent);
            xmlAttr(sb, "width", width);
        }
        if (height > -1)
        {
            sb.append(indent);
            xmlAttr(sb, "height", height);
        }
        toXMLAttrs(sb);
        sb.append(">\n    ");
        xmlNode(sb, "desc", desc, true);
        toXMLNodes(sb);
        sb.append("    </viewdef>\n\n");        
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#compareTo(edu.ku.brc.ui.forms.persist.ViewDefIFace)
     */
    public int compareTo(ViewDefIFace obj)
    {
        return name.compareTo(obj.getName());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        ViewDef viewDef = (ViewDef)super.clone();
        viewDef.type             = type;
        viewDef.name             = name;
        viewDef.className        = className;
        viewDef.dataGettableName = dataGettableName;
        viewDef.dataSettableName = dataSettableName;
        viewDef.desc             = desc;
        viewDef.dataGettable     = dataGettable;
        viewDef.dataSettable     = dataSettable;
        viewDef.useResourceLabels = useResourceLabels;
        viewDef.resourceLabels    = resourceLabels;
        return viewDef;      
    }
    
    /**
     * @param enableRules
     * @return
     */
    protected String createEnableRulesXML(final Hashtable<String, String> enableRules)
    {
        if (enableRules.keySet().size() > 0)
        {
            StringBuilder sb = new StringBuilder("<enableRules>");
            for (String key : enableRules.keySet())
            {
                sb.append("<rule id=\"");
                sb.append(key);
                sb.append("\"><![CDATA[");
                sb.append(enableRules.get(key));
                sb.append("]]></rule>");
            }
            sb.append("</enableRules>");
            return sb.toString();
        }
        return null;
    }

}

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

package edu.ku.brc.ui.forms.persist;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import org.apache.log4j.Logger;

import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.ui.forms.DataObjectSettable;
import edu.ku.brc.ui.forms.DataObjectSettableFactory;

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
    
    protected DataObjectGettable   dataGettable   = null;
    protected DataObjectSettable   dataSettable   = null;
    
    protected int                  xCoord  = 0;
    protected int                  yCoord  = 0;
    protected int                  width   = 0;
    protected int                  height  = 0;
    
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
     */
    public ViewDef(final ViewType type, 
                   final String   name, 
                   final String   className, 
                   final String   dataGettableName, 
                   final String   dataSettableName, 
                   final String   desc)
    {
        this.type = type;
        this.name = name;
        this.className = className;
        this.dataGettableName = dataGettableName;
        this.dataSettableName = dataSettableName;
        this.desc = desc;
        
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
        this(viewDef.type, viewDef.name, viewDef.className, viewDef.dataGettableName, viewDef.dataSettableName, viewDef.desc);
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
        return viewDef;      
    }
}

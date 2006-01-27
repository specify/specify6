/* Filename:    $RCSfile: FormView.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms.persist;

import java.util.Vector;
import edu.ku.brc.specify.ui.forms.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

public class FormView implements Comparable<FormView>
{
    private final static Logger log = Logger.getLogger(FormView.class);
    
    public enum ViewType {form, table, field};
    
    protected ViewType             type;
    protected int                  id;
    protected String               name;
    protected String               desc;
    protected String               className;
    protected String               dataGettableName;
    protected String               dataSettableName;
    protected Vector<FormAltView>  altViews       = new Vector<FormAltView>();
    protected boolean              resourceLabels = false;
    protected boolean              validated      = false;
    
    protected String               viewSetName    = null;
    
    protected DataObjectGettable   dataGettable   = null;
    protected DataObjectSettable   dataSettable   = null;
    
    /**
     * Default Constructor
     *
     */
    public FormView()
    {
        
    }
    
    /**
     * CReate FormView
     * @param type the type of form (form, table, field)
     * @param id the unique id of the form
     */
    public FormView(final ViewType type, 
                    final int      id, 
                    final String   name, 
                    final String   className, 
                    final String   dataGettableName, 
                    final String   dataSettableName, 
                    final String   desc, 
                    final boolean  validated)
    {
        this.type = type;
        this.id   = id;
        this.name = name;
        this.className = className;
        this.dataGettableName = dataGettableName;
        this.dataSettableName = dataSettableName;
        this.desc = desc;
        this.validated = validated;
        
        try
        {
            // Can't imagine why you would not ALWAYS want to have a Gettable
            if (dataGettableName != null && dataGettableName.length() > 0)
            {
                dataGettable = (DataObjectGettable)DataObjectGettableFactory.get(className, dataGettableName);
            } else
            {
                log.info("dataGettableName or is null for "+id);
            }
           
            // OK to NOT have a Settable
            if (dataSettableName != null && dataSettableName.length() > 0)
            {
                dataSettable = (DataObjectSettable)DataObjectSettableFactory.get(className, dataSettableName);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace(); // XXX REMOVE ME
            log.error(ex);
        }
    }
    
    /**
     * Adds an alternative view
     * @param altView the alternate view
     * @return the form that was passed in
     */
    public FormAltView addAltView(final FormAltView altView)
    {
        altViews.add(altView);
        return altView;
    }

    /**
     * Clean up internal data 
     */
    public void cleanUp()
    {
        altViews.clear();
        dataGettable = null;
        dataSettable = null;
    }
    
    public int compareTo(FormView obj)
    {
        if (id == obj.getId())
        {
            return 0;
            
        } else
        {
           return id > obj.getId() ? 1 : -1;
        }
    }
    
    public int getId()
    {
        return id;
    }

    public void setId(final int id)
    {
        this.id = id;
    }

    public ViewType getType()
    {
        return type;
    }

    public void setType(final ViewType type)
    {
        this.type = type;
    }

    public Vector<FormAltView> getAltViews()
    {
        return altViews;
    }

    public void setAltViews(Vector<FormAltView> altViews)
    {
        this.altViews = altViews;
    }

    public boolean isResourceLabels()
    {
        return resourceLabels;
    }

    public void setResourceLabels(final boolean resourceLabels)
    {
        this.resourceLabels = resourceLabels;
    }

    public String getViewSetName()
    {
        return viewSetName;
    }

    public void setViewSetName(final String viewSetName)
    {
        this.viewSetName = viewSetName;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getDataGettableName()
    {
        return dataGettableName;
    }

    public void setDataGettableName(String dataGettableName)
    {
        this.dataGettableName = dataGettableName;
    }

    public DataObjectGettable getDataGettable()
    {
        return dataGettable;
    }

    public DataObjectSettable getDataSettable()
    {
        return dataSettable;
    }

    public boolean isValidated()
    {
        return validated;
    }

    public void setValidated(boolean validated)
    {
        this.validated = validated;
    }

    public String toString()
    {
        return this.viewSetName + " - " + this.name;
    }
     
}

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
package edu.ku.brc.af.core.expresssearch;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 12, 2007
 *
 */
public class RelatedQuery implements Comparable<RelatedQuery>, DisplayOrderingIFace, TableNameRendererIFace
{
    protected static boolean          addRealtedQueryTitle = true;
    protected static boolean          addActiveTitle       = false;
    
    protected static String           relatedQueryTitle    = UIRegistry.getResourceString("ES_RELATED_QUERY"); //$NON-NLS-1$
    protected static String           activeTitle          = UIRegistry.getResourceString("ES_RELATED_ACTIVE"); //$NON-NLS-1$
    
    protected String                  id;
    protected Integer                 displayOrder;
    protected Boolean                 isActive;
    
    // Transient
    protected ExpressResultsTableInfo erti      = null;
    protected boolean                 isInUse   = false;
    protected DBTableInfo             tableInfo = null;
    
    /**
     * 
     */
    public RelatedQuery()
    {
        // no-op
    }

    /**
     * @param id
     * @param displayOrder
     */
    public RelatedQuery(final String id, final Integer displayOrder, final Boolean isActive)
    {
        this.id           = id;
        this.displayOrder = displayOrder;
        this.isActive     = isActive;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the displayOrder
     */
    public Integer getDisplayOrder()
    {
        return displayOrder;
    }

    /**
     * @param displayOrder the displayOrder to set
     */
    public void setDisplayOrder(Integer displayOrder)
    {
        this.displayOrder = displayOrder;
    }

    /**
     * @return the erti
     */
    public ExpressResultsTableInfo getErti()
    {
        return erti;
    }

    /**
     * @param erti the erti to set
     */
    public void setErti(ExpressResultsTableInfo erti)
    {
        this.erti = erti;
        
        if (erti != null)
        {
            tableInfo = DBTableIdMgr.getInstance().getInfoById(erti.getTableId());
        }
    }

    /**
     * @return the isInUse
     */
    public boolean isInUse()
    {
        return isInUse;
    }

    /**
     * @param isInUse the isInUse to set
     */
    public void setInUse(boolean isInUse)
    {
        this.isInUse  = isInUse;
    }
    
    /**
     * @return the isActive
     */
    public Boolean getIsActive()
    {
        return isActive == null ? false : isActive;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setIsActive(Boolean isActive)
    {
        this.isActive = isActive;
    }

    /**
     * @param useRTIcon the useRTIcon to set
     */
    public static void setAddRealtedQueryTitle(boolean addRealtedQueryTitle)
    {
        RelatedQuery.addRealtedQueryTitle = addRealtedQueryTitle;
    }

    /**
     * @param addActiveTitle the addActiveTitle to set
     */
    public static void setAddActiveTitle(boolean addActiveTitle)
    {
        RelatedQuery.addActiveTitle = addActiveTitle;
    }
    
    /**
     * @return just the title from the erti
     */
    public String getPlainTitle()
    {
        return erti == null ? id : erti.getTitle();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getPlainTitle() + (addRealtedQueryTitle ? (" " + relatedQueryTitle) : "") + (addActiveTitle && getIsActive() ? (" (" + activeTitle + ")") : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.TableNameRendererIFace#getIconName()
     */
    //@Override
    public String getIconName()
    {
        return tableInfo == null ? "RelatedTable" : tableInfo.getClassObj().getSimpleName(); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.TableNameRendererIFace#getTitle()
     */
    //@Override
    public String getTitle()
    {
        return toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    //@Override
    public int compareTo(RelatedQuery o)
    {
        return toString().compareTo(o.toString());
    }  
    
    
}

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
/**
 * 
 */
package edu.ku.brc.af.core.expresssearch;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 12, 2007
 *
 */
public class RelatedQuery implements DisplayOrderingIFace, TableNameRendererIFace
{
    protected String  id;
    protected Integer displayOrder;
    
    // Transient
    protected ExpressResultsTableInfo erti      = null;
    protected boolean                 isInUse   = false;
    protected DBTableIdMgr.TableInfo  tableInfo = null;
    /**
     * 
     */
    public RelatedQuery()
    {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param id
     * @param displayOrder
     */
    public RelatedQuery(String id, Integer displayOrder)
    {
        this.id = id;
        this.displayOrder = displayOrder;
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
        this.isInUse = isInUse;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return UIHelper.makeNamePretty(tableInfo.getClassObj().getSimpleName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.TableNameRendererIFace#getIconName()
     */
    //@Override
    public String getIconName()
    {
        return "RelatedTable";//tableInfo.getClassObj().getSimpleName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.TableNameRendererIFace#getTitle()
     */
    //@Override
    public String getTitle()
    {
        return toString();
    }   
}

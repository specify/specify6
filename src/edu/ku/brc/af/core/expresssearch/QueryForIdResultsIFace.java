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

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.af.core.ERTICaptionInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 6, 2007
 *
 */
public interface QueryForIdResultsIFace
{
    /**
     * @return the Icon Name for the return results
     */
    public abstract String getIconName();
    
    /**
     * @return the title to be displayed on the results bar
     */
    public abstract String getTitle();
    
    /**
     * @return which column contains the primary key (zero-based index)
     */
    public abstract int getRecordSetColumnInx();
    
    /**
     * @return the list of record Ids used to create the SQL Statment
     */
    public abstract Vector<Integer> getRecIds();
    
    /**
     * Perform any clean up
     */
    public abstract void cleanUp();
    
    /**
     * @return the Table Id of the primary key results 
     */
    public abstract int getTableId();
    
    /**
     * @return returns the list of caption (column headers)
     */
    public abstract List<ERTICaptionInfo> getVisibleCaptionInfo();
    
    /**
     * @return returns the Top Bar (or banner) color
     */
    public abstract Color getBannerColor();
    
    /**
     * Sets a predefined SQL statement that will be returned by the 
     * getSQL method. Eccentially setting this overrides dynmaically creating the SQL.
     * 
     * @param sql the SQL that override the generated SQL
     */
    public abstract void setSQL(String sql);
    
    /**
     * Dynmaically generates an SQL statement from the Search Config and the list of Ids.
     * NOTE: The returned SQL statement may be what was set by setSQL method.
     * @param searchTerm the search term
     * @param ids the list of Ids to be searched
     * @return the the SQL statement
     */
    public abstract String getSQL(final String searchTerm, Vector<Integer> ids);
    
    /**
     * @return the search term
     */
    public abstract String getSearchTerm();
    
    /**
     * @return the displau position in the list of resultsets
     */
    public abstract Integer getDisplayOrder();
    
    /**
     * @return whether this should be processed as HQL or SQL
     */
    public abstract boolean isHQL();
    
}

/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.datamodel;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 22, 2007
 *
 */
@MappedSuperclass
public abstract class CollectionMember extends DataModelObjBase
{
    private static final Logger  log = Logger.getLogger(CollectionMember.class);
            
    protected Integer collectionMemberId;

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#init()
     */
    @Override
    protected void init()
    {
        super.init();
        
        if (AppContextMgr.getInstance().getClassObject(Collection.class) != null)
        {
            collectionMemberId = AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionId();
        } else
        {
            log.error("No default Collection has been set!");
        }
    }

    /**
     * @return the collectionMemberId
     */
    @Column(name = "CollectionMemberID", nullable = false)
    public Integer getCollectionMemberId()
    {
        return collectionMemberId;
    }

    /**
     * @param collectionMemberId the collectionMemberId to set
     */
    public void setCollectionMemberId(Integer collectionMemberId)
    {
        this.collectionMemberId = collectionMemberId;
    }
}

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
package edu.ku.brc.specify.security;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SecurityMgr;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Collection;

/**
 * 
 * @code_status Unknown
 * 
 * @author megkumin
 *
 */
public class SpecifySecurityMgr  extends SecurityMgr
{
    private static final Logger  log = Logger.getLogger(SpecifySecurityMgr.class);
    /**
     * Constructor 
     */
    public SpecifySecurityMgr()
    {
        super();
        //String databaseName = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDatabaseName();
        //String userName = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getUserName();
        //List<Collection> catSeries = Collection.getCurrentCollection();
        //((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDiscipline(name)
        //((SpecifyAppContextMgr)AppContextMgr.getInstance()).getDiscipline(name)
        //.getNumOfCollectionForUser();
        // TODO Auto-generated constructor stub
        
        
    }

    /**
     * @param args - 
     * void
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SecurityMgr#getPermissionLevel()
     */
    @Override
    public SECURITY_LEVEL getPermissionLevel()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SecurityMgr#authenticate()
     */
    @Override
    public boolean authenticate()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.AppContextMgr#getInstance()
     */
    public static SpecifySecurityMgr getInstance()
    {
        return (SpecifySecurityMgr)SecurityMgr.getInstance();
    }

}

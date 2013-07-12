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
package edu.ku.brc.specify.config;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;

/**
 * This class is responsible for performing the "database" based IO for the prefs.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class AppPrefsGlobalDBIOIImpl extends AppPrefsDBIOIImpl
{
    
    /**
     * Constructor.
     */
    public AppPrefsGlobalDBIOIImpl()
    {
        this.xmlTitle = "Global Prefs";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.AppPrefsDBIOIImpl#createResDir(edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.specify.config.SpecifyAppContextMgr)
     */
    @Override
    protected SpAppResourceDir createResDir(final DataProviderSessionIFace session,
                                            final SpecifyAppContextMgr specifyAppContext)
    {
        return specifyAppContext.getAppResDir(session, null, null, null, "Global Prefs", false, "Global Prefs", true, true);
    }
    
}

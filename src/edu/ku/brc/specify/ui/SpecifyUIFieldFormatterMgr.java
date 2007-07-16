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
package edu.ku.brc.specify.ui;

import edu.ku.brc.specify.datamodel.CatalogNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.dbsupport.CollectionAutoNumber;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 11, 2007
 *
 */
public class SpecifyUIFieldFormatterMgr extends UIFieldFormatterMgr
{
    //private static final Logger  log      = Logger.getLogger(SpecifyUIFieldFormatterMgr.class);
    
    protected UIFieldFormatterIFace catalogNumberAlphaNumeric;
    protected UIFieldFormatterIFace catalogNumberNumeric;
    
    public SpecifyUIFieldFormatterMgr()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#load()
     */
    @Override
    public void load()
    {
        super.load();
        
        catalogNumberAlphaNumeric = super.getFormatterInternal("CatalogNumber"); 
        catalogNumberNumeric      = super.getFormatterInternal("CatalogNumberNumeric"); 
        
        // Just in case it got removed accidently
        if (catalogNumberNumeric == null)
        {
            catalogNumberNumeric = new CatalogNumberUIFieldFormatter();
            catalogNumberNumeric.setAutoNumber(new CollectionAutoNumber());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#getFormatterInternal(java.lang.String)
     */
    @Override
    protected UIFieldFormatterIFace getFormatterInternal(String name)
    {
        if (name.equals("CatalogNumber"))
        {
            CatalogNumberingScheme cns = Collection.getCurrentCollection().getCatalogNumberingScheme();
            
            if (cns.getIsNumericOnly())
            {
                if (catalogNumberNumeric != null)
                {
                    return catalogNumberNumeric;
                }
            } else if (catalogNumberAlphaNumeric != null)
            {
                return catalogNumberAlphaNumeric;
            }
        }
        return super.getFormatterInternal(name);
    }
}

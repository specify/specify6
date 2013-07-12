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
package edu.ku.brc.specify.utilapps;

import org.dom4j.Element;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 6, 2007
 *
 */
public class LocalDiskUIFieldFormatterMgr extends SpecifyUIFieldFormatterMgr
{
    public LocalDiskUIFieldFormatterMgr()
    {
        super();
        load();
    }
    /**
     * Returns the DOM it is suppose to load the formatters from.
     * @return Returns the DOM it is suppose to load the formatters from.
     */
    protected Element getDOM() throws Exception
    {
        return XMLHelper.readDOMFromConfigDir("backstop/uiformatters.xml");
    }
    
    public UIFieldFormatterIFace getFmt(final String name)
    {
        return getFormatterInternal(name);

    }
}

/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tests;

import junit.framework.TestCase;

import org.dom4j.Element;
import org.junit.Before;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.ui.CatalogNumberUIFieldFormatter;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;


public class FormatterTest extends TestCase
{
    
    protected static MyFmtMgr fmtMgr  = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        fmtMgr = new MyFmtMgr();
    }
    
    public void testNumericCatNum()
    {
        CatalogNumberUIFieldFormatter catalogNumber = new CatalogNumberUIFieldFormatter();
        assertTrue(catalogNumber.isValid("000002001"));
        assertFalse(catalogNumber.isValid("2001"));
        assertFalse(catalogNumber.isValid("00000200A"));
    }
    
    public void testAccessionAlphaNumericDBNumOldYear()
    {
        UIFieldFormatterIFace fmt = fmtMgr.getFmt("AccessionNumber");
        
        assertTrue(fmt.isValid("2001-XX-001"));
        assertFalse(fmt.isValid("2001-XX-00A"));
        assertFalse(fmt.isValid("2001/XX-001"));
        assertFalse(fmt.isValid("2001X-X-001"));
    }
    
    //--------------------------------------------------
    class MyFmtMgr extends SpecifyUIFieldFormatterMgr
    {
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
}

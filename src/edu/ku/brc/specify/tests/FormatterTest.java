/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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

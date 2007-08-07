/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.utilapps;

import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterIFace;

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
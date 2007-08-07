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
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 6, 2007
 *
 */
public class LocalDiskDataObjFieldFormatMgr extends DataObjFieldFormatMgr
{
    public LocalDiskDataObjFieldFormatMgr()
    {
        super();
    }
    
    protected Element getDOM() throws Exception
    {
        return XMLHelper.readDOMFromConfigDir("backstop/dataobj_formatters.xml");
    }
}
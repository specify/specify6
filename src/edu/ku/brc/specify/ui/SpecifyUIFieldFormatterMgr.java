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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.AutoNumberIFace;
import edu.ku.brc.specify.datamodel.CatalogNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.dbsupport.CollectionAutoNumber;
import edu.ku.brc.specify.dbsupport.CollectionAutoNumberAlphaNum;
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
    private static final Logger  log      = Logger.getLogger(SpecifyUIFieldFormatterMgr.class);
    
    //protected UIFieldFormatterIFace catalogNumberAlphaNumeric;
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
        
        //catalogNumberAlphaNumeric = super.getFormatterInternal("CatalogNumber"); 
        catalogNumberNumeric      = super.getFormatterInternal("CatalogNumberNumeric"); 
        
        // Just in case it got removed accidently
        if (catalogNumberNumeric == null)
        {
            catalogNumberNumeric = new CatalogNumberUIFieldFormatter();
            catalogNumberNumeric.setAutoNumber(new CollectionAutoNumber());
        }
        
        /* This is experimental output
        File uiffOut = new File(XMLHelper.getConfigDirPath("backstop/uiformatters.out.xml"));
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<formats>\n");
        
        Vector<UIFieldFormatterIFace> list = new Vector<UIFieldFormatterIFace>(hash.values());
        Collections.sort(list, new Comparator<UIFieldFormatterIFace>() {
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
            
        });
        for (UIFieldFormatterIFace f : list)
        {
            f.toXML(sb);
        }
        catalogNumberNumeric.toXML(sb);
        sb.append("</formats>\n");
        try
        {
            FileUtils.writeStringToFile(uiffOut, sb.toString());
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        */
        
        //if (catalogNumberAlphaNumeric != null)
        //{
        //    catalogNumberAlphaNumeric.setAutoNumber(new CollectionAutoNumberAlphaNum());
        //}
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#createAutoNumber(java.lang.String, java.lang.String, java.lang.String)
     */
    protected static AutoNumberIFace createAutoNumber(final String autoNumberClassName, 
                                                      final String dataClassName, 
                                                      final String fieldName)
    {
        if (dataClassName.equals("edu.ku.brc.specify.datamodel.CollectionObject") && 
            fieldName.equals("catalogNumber"))
        {
            return new CollectionAutoNumberAlphaNum();
        }

        return UIFieldFormatterMgr.createAutoNumber(autoNumberClassName, dataClassName, fieldName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#getFormatterInternal(java.lang.String)
     */
    @Override
    protected UIFieldFormatterIFace getFormatterInternal(final String name)
    {
        if (StringUtils.isNotEmpty(name) && (name.equals("CatalogNumber") || name.equals("CatalogNumberNumeric")))
        {
            CatalogNumberingScheme cns = Collection.getCurrentCollection().getCatalogNumberingScheme();
            if (cns != null)
            {
                if (cns.getIsNumericOnly())
                {
                    if (catalogNumberNumeric != null)
                    {
                        return catalogNumberNumeric;
                    }
                } /*else if (catalogNumberAlphaNumeric != null)
                {
                    return catalogNumberAlphaNumeric;
                }*/
            } else
            {
                log.error("The CatalogNumberingScheme is null for the current Collection ["+Collection.getCurrentCollection().getCollectionName()+"] and should be!");
            }
        }
        return super.getFormatterInternal(name);
    }
}

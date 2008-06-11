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

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.dbsupport.AutoNumberIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.CatalogNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.dbsupport.CollectionAutoNumber;
import edu.ku.brc.specify.dbsupport.CollectionAutoNumberAlphaNum;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
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
public class SpecifyUIFieldFormatterMgr extends UIFieldFormatterMgr implements CommandListener
{
    private static final Logger  log      = Logger.getLogger(SpecifyUIFieldFormatterMgr.class);
    
    protected UIFieldFormatterIFace catalogNumberNumeric;
    protected UIFieldFormatterIFace catalogNumberString;
    
    /**
     * 
     */
    public SpecifyUIFieldFormatterMgr()
    {
        super();
        
        CommandDispatcher.register("Collection", this); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#load()
     */
    @Override
    public void load()
    {
        super.load();
        
        catalogNumberNumeric      = super.getFormatterInternal("CatalogNumberNumeric");  //$NON-NLS-1$
        
        // Just in case it got removed accidently
        if (catalogNumberNumeric == null)
        {
            catalogNumberNumeric = new CatalogNumberUIFieldFormatter();
            catalogNumberNumeric.setAutoNumber(new CollectionAutoNumber());
        }
        
        catalogNumberString = super.getFormatterInternal("CatalogNumberString");  //$NON-NLS-1$
        
        // Just in case it got removed accidently
        if (catalogNumberString == null)
        {
            // These absolutely have to be there
            catalogNumberString = new CatalogNumberStringUIFieldFormatter();
        }
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#getDOM()
     */
    protected Element getDOM() throws Exception
    {
        if (doingLocal)
        {
            return XMLHelper.readDOMFromConfigDir("backstop/uiformatters.xml"); //$NON-NLS-1$
        }

        AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir("Collection", "UIFormatters"); //$NON-NLS-1$ //$NON-NLS-2$
        if (appRes != null)
        {
            return AppContextMgr.getInstance().getResourceAsDOM(appRes);
        } 
        
        return XMLHelper.readDOMFromConfigDir("backstop/uiformatters.xml"); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#saveXML(java.lang.String)
     */
    public void saveXML(final String xml) 
    {

        if (doingLocal)
        {
            File outputFile = XMLHelper.getConfigDir("backstop/uiformatters.xml"); //$NON-NLS-1$
            try
            {
                FileUtils.writeStringToFile(outputFile, xml);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            AppResourceIFace appRes = AppContextMgr.getInstance().getResourceFromDir("Collection", "UIFormatters"); //$NON-NLS-1$ //$NON-NLS-2$
            if (appRes != null)
            {
                appRes.setDataAsString(xml);
                AppContextMgr.getInstance().saveResource(appRes);
               
            } else
            {
                AppContextMgr.getInstance().putResourceAsXML("UIFormatters", xml); //$NON-NLS-1$
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#createAutoNumber(java.lang.String, java.lang.String, java.lang.String)
     */
    protected static AutoNumberIFace createAutoNumber(final String autoNumberClassName, 
                                                      final String dataClassName, 
                                                      final String fieldName)
    {
        if (dataClassName.equals("edu.ku.brc.specify.datamodel.CollectionObject") &&  //$NON-NLS-1$
            fieldName.equals("catalogNumber")) //$NON-NLS-1$
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
        if (StringUtils.isNotEmpty(name))
        {
            // check for date to short circut the other checks
            if (name.equals("Date")) //$NON-NLS-1$
            {
                return super.getFormatterInternal(name);
                
            } else if (name.equals("CatalogNumberString")) //$NON-NLS-1$
            {
                if (catalogNumberString != null)
                {
                    return catalogNumberString;
                }
            } else 
            {
                CatalogNumberingScheme cns = Collection.getCurrentCollection() != null ? Collection.getCurrentCollection().getCatalogNumberingScheme() : null;
                if (cns != null)
                {
                    if (name.equals("CatalogNumberNumeric") || (name.equals("CatalogNumber") && cns.getIsNumericOnly())) //$NON-NLS-1$ //$NON-NLS-2$
                    {
                        if (catalogNumberNumeric != null)
                        {
                            return catalogNumberNumeric;
                        }
                    }
                } else
                {
                    log.error("The CatalogNumberingScheme is null for the current Collection ["+(Collection.getCurrentCollection() != null ? Collection.getCurrentCollection().getCollectionName() : "null") +"] and should be!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }
        } else
        {
            log.error("The name is empty!"); //$NON-NLS-1$
        }
        return super.getFormatterInternal(name);
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#getFormatterList(java.lang.Class, java.lang.String)
     */
    @Override
    public List<UIFieldFormatterIFace> getFormatterList(Class<?> clazz, String fieldName)
    {
        List<UIFieldFormatterIFace> list =  super.getFormatterList(clazz, fieldName);
        if (clazz == CollectionObject.class && (fieldName != null && fieldName.equals("catalogNumber")))
        {
            list.add(catalogNumberNumeric);
            list.add(catalogNumberString);
        }
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.CommandListener#doCommand(edu.ku.brc.af.ui.CommandAction)
     */
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType("Collection") && cmdAction.isAction("Changed")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            load();
        }
    }
}

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
package edu.ku.brc.af.ui.forms.validation;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.TextFieldWithInfo;
import edu.ku.brc.af.ui.forms.persist.FormDevHelper;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * This factory knows how to create AutoComplete Comboboxes that get their data from a query.
 
 * @code_status Beta
 **
 * @author rods
 *
 */
public class TypeSearchForQueryFactory
{
    public static final String factoryName    = "edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory";

    // Static Data Members
    private static final Logger log = Logger.getLogger(TypeSearchForQueryFactory.class);
    
    private static final String TYPESEARCHES = "TypeSearches";
    
    protected static TypeSearchForQueryFactory instance   = null;
    protected static boolean                   doingLocal = false;
    
    // Data Members
    protected Hashtable<String, TypeSearchInfo> hash = new Hashtable<String, TypeSearchInfo>();
    
    /**
     * Protected Constructor.
     */
    protected TypeSearchForQueryFactory()
    {
        
    }
    
    /**
     * @return
     */
    public Vector<TypeSearchInfo> getList()
    {
        Vector<TypeSearchInfo> list = new Vector<TypeSearchInfo>(instance.hash.values());
        Collections.sort(list);
        return list;
    }
    
    /**
     * @return the DOM
     */
    protected Element getDOM()
    {
        final String pathName = "backstop/typesearch_def.xml";
        if (doingLocal)
        {
            return XMLHelper.readDOMFromConfigDir(pathName);
        }
        return getDOMFromResource(TYPESEARCHES, pathName);
    }
    
    /**
     * Method for getting a Resource the Database or disk.
     * @param name
     * @param localPath
     * @return
     */
    public Element getDOMFromResource(final String name, final String localPath)
    {
        // Default impl is to get it from disk.
       return XMLHelper.readDOMFromConfigDir(localPath);
    }
    
    /**
     * Loads the formats from the config file.
     *
     */
    public void load()
    {
        if (hash.size() == 0)
        {
            try
            {
                Element root = getDOM();
                if (root != null)
                {
                    List<?> typeSearches = root.selectNodes("/typesearches/typesearch");
                    for (Object fObj : typeSearches)
                    {
                        Element tsElement = (Element)fObj;
                        String name = tsElement.attributeValue("name");
                        if (StringUtils.isNotBlank(name))
                        {
                            TypeSearchInfo tsi = new TypeSearchInfo(XMLHelper.getAttr(tsElement, "tableid", -1),
                                                                    name,
                                                                    tsElement.attributeValue("displaycols"),
                                                                    tsElement.attributeValue("searchfield"),
                                                                    XMLHelper.getAttr(tsElement, "format", null),
                                                                    XMLHelper.getAttr(tsElement, "uifieldformatter", null),
                                                                    XMLHelper.getAttr(tsElement, "dataobjformatter", null),
                                                                    XMLHelper.getAttr(tsElement, "system", true));
                            hash.put(name, tsi);
                            
                            String sqlTemplate = tsElement.getTextTrim();
                            if (StringUtils.isNotEmpty(sqlTemplate))
                            {
                                tsi.setSqlTemplate(sqlTemplate);
                            }
                        } else
                        {
                            log.error("TypeSearchInfo element is missing or has a blank name!");
                        }
                    }
                } else
                {
                    log.debug("Couldn't open typesearch_def.xml");
                }
            } catch (Exception ex)
            {
                log.error(ex);
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TypeSearchForQueryFactory.class, ex);
                ex.printStackTrace();
            }
        }
    }


    /**
     * Creates a new ValComboBoxFromQuery by name.
     * @param name the name of the ValComboBoxFromQuery to return
     * @param dataObjFormatterNameArg the name of the DataObjFormatter
     * @return a ValComboBoxFromQuery by name
     */
    public TextFieldWithInfo getTextFieldWithInfo(final String name,
                                                  final String dataObjFormatterNameArg)
    {
        instance.load();
        
        TypeSearchInfo typeSearchInfo = instance.hash.get(name);
        if (typeSearchInfo != null)
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(typeSearchInfo.getTableId());
            if (tblInfo != null)
            {
                // Let the one defined in the Schema Config override the one defined by the QCBX
                String dofName = StringUtils.isNotEmpty(dataObjFormatterNameArg) ? dataObjFormatterNameArg : typeSearchInfo.getDataObjFormatterName();
                if (StringUtils.isNotEmpty(tblInfo.getDataObjFormatter()))
                {
                    dofName = tblInfo.getDataObjFormatter();
                }
                return new TextFieldWithInfo(tblInfo.getClassName(),
                                             tblInfo.getIdFieldName(),
                                             typeSearchInfo.getSearchFieldName(),
                                             typeSearchInfo.getFormat(),
                                             typeSearchInfo.getUiFieldFormatterName(),
                                             dofName,
                                             tblInfo.getNewObjDialog(),
                                             tblInfo.getTitle());
    
            }
            // else
            log.error("Table with ID["+typeSearchInfo.getTableId()+"] not found.");
        } else
        {
            log.error("Object Type Search Name ["+name+"] not found.");
        }
        return null;
    }
    
    /**
     * For a given Formatter it returns the formatName.
     * @param name the name of the formatter to use
     * @return the name of the formatter
     */
    public String getDataObjFormatterName(final String name)
    {
        instance.load();
        
        TypeSearchInfo typeSearchInfo = instance.hash.get(name);
        if (typeSearchInfo != null)
        {
            return typeSearchInfo.getDataObjFormatterName();
        }            
        // else
        log.error("Object Type Search Name ["+name+"] not found.");
        
        UIRegistry.showError("Couldn't create ValComboBoxFromQuery because the entry ["+name+"] is not in the typesearch_def.xml");
        return null;
    }

    /**
     * Creates a new ValComboBoxFromQuery by name.
     * @param name the name of the ValComboBoxFromQuery to return
     * @return a ValComboBoxFromQuery by name
     */
    public ValComboBoxFromQuery createValComboBoxFromQuery(final String name, 
                                                           final int btnOpts,
                                                           final String dataObjFormatterNameArg,
                                                           final String helpContextArg)
    {
        instance.load();
        
        TypeSearchInfo typeSearchInfo = instance.hash.get(name);
        if (typeSearchInfo != null)
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(typeSearchInfo.getTableId());
            if (tblInfo != null)
            {
                String dofName = StringUtils.isNotEmpty(dataObjFormatterNameArg) ? dataObjFormatterNameArg : typeSearchInfo.getDataObjFormatterName();
                return new ValComboBoxFromQuery(tblInfo,
                                                typeSearchInfo.getSearchFieldName(),
                                                typeSearchInfo.getDisplayColumns(),
                                                typeSearchInfo.getSearchFieldName(),
                                                typeSearchInfo.getFormat(),
                                                typeSearchInfo.getUiFieldFormatterName(),
                                                dofName,
                                                typeSearchInfo.getSqlTemplate(),
                                                helpContextArg,
                                                btnOpts);

    
            }
            // else
            log.error("Table with ID["+typeSearchInfo.getTableId()+"] not found.");
        } else
        {
            log.error("Object Type Search Name ["+name+"] not found.");
        }
        String msg = "Couldn't create ValComboBoxFromQuery because the entry ["+name+"] is not in the typesearch_def.xml";
        FormDevHelper.appendFormDevError(msg);
        UIRegistry.showError(msg);
        return null;
    }

    /**
     * @param doingLocal the doingLocal to set
     */
    public static void setDoingLocal(boolean doingLocal)
    {
        TypeSearchForQueryFactory.doingLocal = doingLocal;
    }

    /**
     * @return the hash
     */
    public Hashtable<String, TypeSearchInfo> getHash()
    {
        return hash;
    }
    
    /**
     * @param tsi
     */
    public void remove(final TypeSearchInfo tsi)
    {
        hash.remove(tsi.getName());
    }
    
    /**
     * @param tsi
     */
    public void add(final TypeSearchInfo tsi)
    {
        if (hash.get(tsi.getName()) == null)
        {
            hash.put(tsi.getName(), tsi);
        } else
        {
            UIRegistry.showLocalizedError("Name is taken"); // I18N
        }
    }
    
    /**
     * 
     */
    public void save()
    {
        ArrayList<TypeSearchInfo> list = new ArrayList<TypeSearchInfo>(hash.values());
        Collections.sort(list);
        
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<typesearches>\n");
        for (TypeSearchInfo tsi : list)
        {
            sb.append(tsi.getXML());
        }
        sb.append("</typesearches>\n");
        
        if (doingLocal)
        {
            writeToFile(sb.toString());
            return;
        }
        
        /*AppContextMgr acm = AppContextMgr.getInstance();
        
        AppResourceIFace res = acm.getResource(TYPESEARCHES);
        
        res.setDataAsString(sb.toString());
        
        if (!acm.saveResource(res))
        {
            UIRegistry.showLocalizedError("");
        }*/
        saveResource(TYPESEARCHES, sb.toString());
    }
    
    /**
     * Saves an Discipline level XML document to a Database Resource.
     * @param resName the name of the resource
     * @param xml the XML to be saved.
     */
    public void saveResource(final String resName, final String xml)
    {
        // Default implementation is to disk
        writeToFile(xml.toString());
    }
    
    /**
     * @param xml
     */
    private void writeToFile(final String xml)
    {
        File file = XMLHelper.getConfigDir("backstop/typesearch_def.xml");
        
        PrintWriter pw;
        try
        {
            pw = new PrintWriter(file);
            pw.print(xml);
            pw.close();
            
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the instance to the singleton
     * 
     * @return the instance to the singleton
     */
    public static TypeSearchForQueryFactory getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        // else
        String factoryNameStr = AccessController.doPrivileged(new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (isNotEmpty(factoryNameStr)) 
        {
            try 
            {
                return instance = (TypeSearchForQueryFactory)Class.forName(factoryNameStr).newInstance();
                 
            } catch (Exception e) 
            {
                e.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SecurityMgr.class, e);
                InternalError error = new InternalError("Can't instantiate TypeSearchForQueryFactory factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return instance = new TypeSearchForQueryFactory();
    }

}

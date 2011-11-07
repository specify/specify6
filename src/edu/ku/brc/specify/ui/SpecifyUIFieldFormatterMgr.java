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
package edu.ku.brc.specify.ui;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.hibernate.Query;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.AutoNumberIFace;
import edu.ku.brc.af.ui.forms.formatters.GenericStringUIFieldFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.dbsupport.AccessionAutoNumberAlphaNum;
import edu.ku.brc.specify.dbsupport.CollectionAutoNumber;
import edu.ku.brc.specify.dbsupport.CollectionAutoNumberAlphaNum;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.UIRegistry;

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
    
    protected static String         DISCIPLINE   = "Discipline";
    protected static String         BACKSTOPDIR  = "BackStop";
    protected static String         UIFORMATTERS = "UIFormatters";
    
    private String                  localFilePath = null;
    private boolean                 pathWasSet    = false;
    
    protected UIFieldFormatterIFace catalogNumberNumeric;
    protected UIFieldFormatterIFace catalogNumberString;
    
    /**
     * 
     */
    public SpecifyUIFieldFormatterMgr()
    {
        super();
        
        CommandDispatcher.register(DISCIPLINE, this); //$NON-NLS-1$
    }
    
    /**
     * @return the path to the xml file.
     */
    private String getLocalPath()
    {
        if (localFilePath == null)
        {
            localFilePath = "backstop/uiformatters.xml";
        }
        return localFilePath;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#load()
     */
    @Override
    public void load()
    {
        super.load();
        
        catalogNumberNumeric  = super.getFormatterInternal("CatalogNumberNumeric");  //$NON-NLS-1$
        
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
        
        //loadStringFormatters();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#getDOM()
     */
    @Override
    protected Element getDOM() throws Exception
    {
        if (doingLocal)
        {
            return pathWasSet ? XMLHelper.readFileToDOM4J(new File(getLocalPath())) : XMLHelper.readDOMFromConfigDir(getLocalPath());
        }

        return getDisciplineDOMFromResource(getAppContextMgr(), UIFORMATTERS, getLocalPath());
    }
    
    /**
     * @param contextMgr
     * @return
     */
    protected Discipline getDiscipline(final AppContextMgr contextMgr)
    {
        return contextMgr.getClassObject(Discipline.class);
    }
    
    /**
     * Method for getting a Resource for a Discipline from the Database or disk.
     * @param name
     * @param localPath
     * @return
     */
    public Element getDisciplineDOMFromResource(final AppContextMgr contextMgr, final String name, final String localPath)
    {
        SpecifyAppContextMgr acMgr = (SpecifyAppContextMgr)contextMgr;
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            SpecifyUser user       = contextMgr.getClassObject(SpecifyUser.class);
            Discipline  discipline = getDiscipline(contextMgr);
            
            SpAppResourceDir appResDir = acMgr.getAppResDir(session, user, discipline, null, null, false, name, false);
            if (appResDir != null)
            {
                SpAppResource appRes = appResDir.getResourceByName(name);
                if (appRes != null)
                {
                    session.close();
                    session = null;
                    
                    return acMgr.getResourceAsDOM(appRes);
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyUIFieldFormatterMgr.class, ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        return XMLHelper.readDOMFromConfigDir(localPath); //$NON-NLS-1$
    }
    
    /**
     * @return
     * @throws Exception
     */
    protected Element getDOMForStringFmts() throws Exception
    {
        if (doingLocal)
        {
            return XMLHelper.readDOMFromConfigDir("backstop/uistrformatters.xml"); //$NON-NLS-1$
        }

        return getDisciplineDOMFromResource(getAppContextMgr(), UIFORMATTERS+"_STRS", "backstop/uistrformatters.xml");
    }
    
    /**
     * Sets the path that is when doing local.
     * @param localFilePath the localFilePath to set
     */
    public void setLocalFilePath(final String localFilePath)
    {
        this.localFilePath = localFilePath;
        this.pathWasSet    = true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr#reset()
     */
    @Override
    public void reset()
    {
        instance = null;
    }

    /**
     * 
     */
    public void loadStringFormatters()
    {
        try
        {
            Element root = getDOMForStringFmts();
            if (root != null)
            {
                List<?> formats = root.selectNodes("/formats/format");
                for (Object fObj : formats)
                {
                    Element formatElement = (Element) fObj;
                    String  name          = formatElement.attributeValue("name");
                    
                    UIFieldFormatterIFace fmt = super.getFormatterInternal(name);
                    if (fmt == null)
                    {
                        String  fieldName     = XMLHelper.getAttr(formatElement, "fieldname", null);
                        String  dataClassName = formatElement .attributeValue("class");
                        String  titleKey      = XMLHelper.getAttr(formatElement, "titlekey", null);
                        int     uiDisplayLen  = XMLHelper.getAttr(formatElement, "uilen", 10);
                        
                        
                        Class<?> dataClass = null;
                        try
                        {
                            dataClass = Class.forName(dataClassName);
                            
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyUIFieldFormatterMgr.class, ex);
                            log.error("Couldn't load class [" + dataClassName + "] for [" + name + "]");
                        }
                        
                        fmt = new GenericStringUIFieldFormatter(name, 
                                                                dataClass, 
                                                                fieldName, 
                                                                UIRegistry.getResourceString(titleKey), 
                                                                uiDisplayLen);
                        hash.put(name, fmt);
                    }
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyUIFieldFormatterMgr.class, ex);
            log.error(ex);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#saveXML(java.lang.String)
     */
    @Override
    public void saveXML(final String xml) 
    {
        if (doingLocal)
        {
            File outputFile = pathWasSet ? new File(getLocalPath()) : XMLHelper.getConfigDir(getLocalPath());
            try
            {
                FileUtils.writeStringToFile(outputFile, xml);
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyUIFieldFormatterMgr.class, ex);
                ex.printStackTrace();
            }
        } else
        {
            saveDisciplineResource(getAppContextMgr(), UIFORMATTERS, xml);
        }
    }
    
    /**
     * Saves an Discipline level XML document to a Database Resource.
     * @param resName the name of the resource
     * @param xml the XML to be saved.
     */
    public static void saveDisciplineResource(final AppContextMgr contextMgr, final String resName, final String xml)
    {
        SpecifyAppContextMgr acMgr = (SpecifyAppContextMgr)contextMgr;
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            SpecifyUser user       = acMgr.getClassObject(SpecifyUser.class);
            Discipline  discipline = acMgr.getClassObject(Discipline.class);
            
            boolean found = false;
            SpAppResourceDir appResDir = acMgr.getAppResDir(session, user, discipline, null, null, false, resName, false);
            if (appResDir != null)
            {
                SpAppResource appRes = appResDir.getResourceByName(resName);
                
                if (appRes != null)
                {
                    session.close();
                    session = null;
                    
                    appRes.setDataAsString(xml);
                    contextMgr.saveResource(appRes);
                    found = true;
                }
            }
            
            if (!found)
            {
                appResDir = acMgr.getAppResDir(session, user, discipline, null, null, false, resName, true);
                if (appResDir != null)
                {
                    SpAppResource diskAppRes = acMgr.getSpAppResourceDirByName(BACKSTOPDIR).getResourceByName(resName);
                    SpAppResource newAppRes  = new SpAppResource();
                    newAppRes.initialize();
                    if (diskAppRes != null)
                    {
                        newAppRes.setMetaData(diskAppRes.getMetaData());
                        newAppRes.setDescription(diskAppRes.getDescription());
                        newAppRes.setFileName(diskAppRes.getFileName());
                        newAppRes.setMimeType(diskAppRes.getMimeType());
                        newAppRes.setName(diskAppRes.getName());

                        newAppRes.setLevel(diskAppRes.getLevel());
                    } else
                    {
                        newAppRes.setName(resName);
                        newAppRes.setLevel((short)0);
                        newAppRes.setMimeType("text/xml");
                    }
                    
                    Agent agent = contextMgr.getClassObject(Agent.class);
                    newAppRes.setCreatedByAgent(agent);
                    newAppRes.setSpecifyUser(user);
                    
                    newAppRes.setSpAppResourceDir(appResDir);
                    appResDir.getSpAppResources().add(newAppRes);
                    newAppRes.setDataAsString(xml);
                    
                    if (session != null) 
                    {
                        session.close();
                        session = null;
                    }
                    ((SpecifyAppContextMgr) contextMgr).saveResource(newAppRes);
                    
                } else
                {
                    contextMgr.putResourceAsXML(resName, xml); //$NON-NLS-1$
                }
            }

        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SpecifyUIFieldFormatterMgr.class, ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr#createAutoNumber(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public AutoNumberIFace createAutoNumber(final String  autoNumberClassName, 
                                            final String  dataClassName, 
                                            final String  fieldName,
                                            final boolean isSingleField)
    {
        if (dataClassName.equals("edu.ku.brc.specify.datamodel.CollectionObject") &&  //$NON-NLS-1$
            fieldName.equals("catalogNumber")) //$NON-NLS-1$
        {
            return isSingleField ? new CollectionAutoNumber() : new CollectionAutoNumberAlphaNum();
        }
        
        if (dataClassName.equals("edu.ku.brc.specify.datamodel.Accession") &&  //$NON-NLS-1$
            fieldName.equals("accessionNumber")) //$NON-NLS-1$
        {
            return new AccessionAutoNumberAlphaNum();
        }

        return super.createAutoNumber(autoNumberClassName, dataClassName, fieldName, isSingleField);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr#getFormatterInternal(java.lang.String)
     */
    @Override
    protected UIFieldFormatterIFace getFormatterInternal(final String name)
    {
        if (StringUtils.isNotEmpty(name))
        {
            if (!doingLocal)
            {
            	if (!AppContextMgr.getInstance().hasContext() || AppContextMgr.getInstance().getClassObject(Collection.class) == null)
            	{
            		// collection is not set when running other applications (SchemaLocalizerFrame for example)
            		return null;
            	}
            	
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
                    Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
                    AutoNumberingScheme cns = collection != null ? collection.getNumberingSchemesByType(CollectionObject.getClassTableId()) : null;
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
                        log.error("The CatalogNumberingScheme is null for the current Collection ["+(AppContextMgr.getInstance().getClassObject(Collection.class) != null ? AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName() : "null") +"] and should be!"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
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
        /*if (clazz == CollectionObject.class && (fieldName != null && fieldName.equals("catalogNumber")))
        {
            list.add(catalogNumberNumeric);
            list.add(catalogNumberString);
        }*/
        return list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.CommandListener#doCommand(edu.ku.brc.af.ui.CommandAction)
     */
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(DISCIPLINE) && cmdAction.isAction("Changed")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            load();
        }
    }
}

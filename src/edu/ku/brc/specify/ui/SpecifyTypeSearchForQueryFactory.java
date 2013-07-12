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
package edu.ku.brc.specify.ui;

import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 27, 2010
 *
 */
public class SpecifyTypeSearchForQueryFactory extends TypeSearchForQueryFactory
{
    protected static String         BACKSTOPDIR  = "BackStop";

    /**
     * 
     */
    public SpecifyTypeSearchForQueryFactory()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory#load()
     */
    @Override
    public void load()
    {
        super.load();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory#save()
     */
    @Override
    public void save()
    {
        super.save();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory#getDOMFromResource(java.lang.String, java.lang.String)
     */
    public Element getDOMFromResource(final String name, final String localPath)
    {
        SpecifyAppContextMgr acMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            String[] dirs = {SpecifyAppContextMgr.COMMONDIR, SpecifyAppContextMgr.BACKSTOPDIR};
            for (String dirName : dirs)
            {
                SpAppResourceDir appResDir = acMgr.getSpAppResourceDirByName(dirName);
                if (appResDir != null)
                {
                    SpAppResource appRes = appResDir.getResourceByName(name);
                    if (appRes != null)
                    {
                        session.close();
                        session = null;
                        
                        return AppContextMgr.getInstance().getResourceAsDOM(appRes);
                    }
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
     * Saves an Discipline level XML document to a Database Resource.
     * @param resName the name of the resource
     * @param xml the XML to be saved.
     */
    public void saveResource(final String resName, final String xml)
    {
        //System.out.println("Saved");
        //System.out.println(xml);
        //System.out.println();
        
        SpecifyAppContextMgr acMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            SpAppResource    appRes       = null;
            SpAppResourceDir commonResDir = acMgr.getSpAppResourceDirByName(SpecifyAppContextMgr.COMMONDIR);
            if (commonResDir != null)
            {
                appRes = commonResDir.getResourceByName(resName);
                if (appRes == null)
                {
                    SpAppResourceDir backStopResDir = acMgr.getSpAppResourceDirByName(SpecifyAppContextMgr.BACKSTOPDIR);
                    appRes = backStopResDir.getResourceByName(resName);
                    if (appRes != null)
                    {
                        backStopResDir.getSpAppResources().remove(appRes);
                        commonResDir.getSpAppResources().add(appRes);
                        appRes.setSpAppResourceDir(commonResDir);
                    } else
                    {
                        // major error
                        UIRegistry.showError("Major Error");
                    }
                }
                
                
                if (appRes != null)
                {
                    session.close();
                    session = null;
                    
                    appRes.setDataAsString(xml);
                    AppContextMgr.getInstance().saveResource(appRes);
                    
                } else
                {
                    UIRegistry.showError("Major Error #2");
                }
            }
            
            /*if (appRes == null)
            {
                SpAppResource backStopDir = acMgr.getSpAppResourceDirByName(BACKSTOPDIR).getResourceByName(resName);
                if (appResDir != null)
                {
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
                    
                    Agent agent = AppContextMgr.getInstance().getClassObject(Agent.class);
                    newAppRes.setCreatedByAgent(agent);
                    newAppRes.setSpecifyUser(user);
                    
                    newAppRes.setSpAppResourceDir(appResDir);
                    appResDir.getSpAppResources().add(newAppRes);
                    newAppRes.setDataAsString(xml);
                    
                    session.close();
                    session = null;
                    ((SpecifyAppContextMgr) AppContextMgr.getInstance()).saveResource(newAppRes);
                    
                } else
                {
                    AppContextMgr.getInstance().putResourceAsXML(resName, xml); //$NON-NLS-1$
                }
            }*/

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
    

}

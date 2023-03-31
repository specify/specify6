/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;
import java.util.List;

import org.dom4j.Element;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewSetIFace;

/**
 * Abstract class for setting application context. It is designed that each application should implement its own.<br>
 * <br>
 * CONTEXT_STATUS is passed back and has the following meaning:<br>
 * <UL>
 * <LI>OK - The context was set correctly
 * <LI>Error - there was an error setting the context
 * <LI>Ignore - The context was not "reset" to a different value and caller should act as if the call didn't happen.
 * (Basbically a user action caused it to be abort, but it was OK)
 * <LI>Initial - This should never be passed outside to the caller, it is intended as a start up state for the object.
 * </UL>
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public abstract class AppContextMgr
{
    public static final String factoryName = "edu.ku.brc.af.core.AppContextMgrFactory"; //$NON-NLS-1$
    
    public enum CONTEXT_STATUS {OK, Error, Ignore, Initial}
    
    protected static AppContextMgr instance = null;
    
    protected CONTEXT_STATUS              currentStatus = CONTEXT_STATUS.Initial;
    protected Hashtable<Class<?>, Object> classObjHash  = new Hashtable<Class<?>, Object>();
    protected boolean                     hasContext    = false;

    
    /**
     * Returns a View by name, meaning a ViewSet name and a View name inside the ViewSet.
     * @param viewName the name of the view (cannot be null)
     * @return the view
     */
    public abstract ViewIFace getView(final String viewName);
    
    /**
     * Returns a View by name, meaning a ViewSet name and a View name inside the ViewSet.
     * @param viewSetName the name of the view set
     * @param viewName the name of the view (cannot be null)
     * @return the view
     */
    public abstract ViewIFace getView(final String viewSetName, String viewName);
    
    /**
     * Returns an Application Resource object by name
     * @param appResName the name of the resource
     * @return the application resource
     */
    public abstract AppResourceIFace getResource(String appResName);
   
    /**
     * Returns an Application Resource object by name from the user's area
     * @param appResDirName the name of the directory for resource (virtual directory).
     * @param appResName the name of the resource
     * @return the application resource
     */
    public abstract AppResourceIFace getResourceFromDir(String appResDirName, String appResName);
   
    /**
     * Returns Application Resource objects by mime type
     * @param mimeType the mime type of the files to be returned
     * @return the list application resource
     */
    public abstract List<AppResourceIFace> getResourceByMimeType(String mimeType);
   
    /**
     * Returns the DOM for an Resource that is an XML Resource. 
     * It will throw an exception if the MimeType is not of "text"xml"
     * @param name the name of the resource
     * @return the root element of the XML DOM
     */
    public abstract Element getResourceAsDOM(String name);
   
    /**
     * Returns the DOM for an Resource that is an XML Resource. 
     * It will throw an exception if the MimeType is not of "text"xml"
     * @param name the name of the resource
     * @return the root element of the XML DOM
     */
    public abstract Element getResourceAsDOM(AppResourceIFace appRes);
   
    /**
     * Returns the XML (String) for an Resource that is an XML Resource. 
     * It will throw an exception if the MimeType is not of "text"xml"
     * @param name the name of the resource
     * @return the root element of the XML DOM
     */
    public abstract String getResourceAsXML(final String name);
   
    /**
     * Returns the XML (String) for an Resource that is an XML Resource. 
     * It will throw an exception if the MimeType is not of "text"xml"
     * @param name the name of the resource
     * @return the root element of the XML DOM
     */
    public abstract String getResourceAsXML(AppResourceIFace appRes);
   
    /**
     * Returns the XML (String) for an Resource that is an XML Resource. 
     * It will throw an exception if the MimeType is not of "text"xml"
     * @param name the name of the resource
     * @param xmlStr the XML String
     */
    public abstract void putResourceAsXML(final String name, final String xmlStr);
   
    /**
     * Returns the XML (String) for an Resource that is an XML Resource. 
     * It will throw an exception if the MimeType is not of "text"xml"
     * @param name the name of the resource
     * @param xmlStr the XML String
     */
    public abstract void putResourceAsXML(AppResourceIFace appRes, final String xmlStr);
    
    /**
     * Creates an AppResource and associates it with an AppResourceDir.
     * @param appResDirName the name of the virtual directory
     * @return an empty AppResource that will be saved in the AppResourceDir
     */
    public abstract AppResourceIFace createAppResourceForDir(String appResDirName);
   
    /**
     * Removes a resource from a virtual directorey. Sometimes they cannot be removed because they are a backstop.
     * @param appResDirName
     * @return true if it was removed
     */
    public abstract boolean removeAppResource(String appResDirName, AppResourceIFace appResource);
    
    /**
     * @return true if save correctly.
     */
    public abstract boolean saveResource(AppResourceIFace appResource);
   
    /**
     * Sets the current context.
     * @param databaseName the name of the database 
     * @param userName the user name
     * @param startingOver indicates that the context should "start over" which means it may want to ask the user for specific things
     * @param doPrompt suggests for the a prompt for for the Context
     * (this may be ignored by some implementations) it is merely a suggestion.
     * @return  the status enum for what happened
     */
    public abstract CONTEXT_STATUS setContext(final String databaseName, 
                                              final String userName,
                                              final boolean startingOver,
                                              final boolean isFirstTime,
                                              final boolean doPrompt);
    
    /**
     * @return the hastable of ViewSetIFace objects
     */
    public abstract Hashtable<String, List<ViewSetIFace>> getViewSetHash();
    
    
    /**
     * @return a textual description of the currrent context, or null.
     */
    public abstract String getCurrentContextDescription();
    
    /**
     * Returns the formatter for a given class and field (may return null)
     * @param shortClassName the short class name (no package)
     * @param fieldName the field name with the desired formatter.
     * @return the formatter or null
     */
    public abstract UIFieldFormatterIFace getFormatter(String shortClassName, String fieldName);
    
    /**
     * @return the hasContext
     */
    public boolean hasContext()
    {
        return hasContext;
    }

    /**
     * @param hasContext the hasContext to set
     */
    public void setHasContext(boolean hasContext)
    {
        this.hasContext = hasContext;
    }
    
    /**
     * @param tableId the table id
     * @return the default Taskable from a table id
     */
    public Taskable getTaskFromTableId(int tableId) { return null; }
    
    public static boolean isSecurityOn() { return getInstance().isSecurity(); }

    /**
     * Copies all the fields except the data.
     * @param fromAppRes the from app res
     * @param toAppRes the to app res
     */
    public void copy(final AppResourceIFace fromAppRes, final AppResourceIFace toAppRes)
    {
        toAppRes.setName(fromAppRes.getName());
        toAppRes.setDescription(fromAppRes.getDescription());
        toAppRes.setDataAsString(fromAppRes.getDataAsString());
        toAppRes.setLevel(fromAppRes.getLevel());
        toAppRes.setMetaData(fromAppRes.getMetaData());
        toAppRes.setMimeType(fromAppRes.getMimeType());
        toAppRes.setFileName(fromAppRes.getFileName());
    }
    
    /**
     * Looks up a resource by name and copies the contents to a Resource in a virtual directory.
     * @param appResDirName
     * @param resourceName the name of an existing resource.
     * @return the new resource with the values form the existing resource.
     */
    public AppResourceIFace copyToDirAppRes(final String appResDirName, final String resourceName)
    {
        AppResourceIFace toAppRes   = null;
        AppResourceIFace fromAppRes = getResource(resourceName);
        if (fromAppRes != null)
        {
            toAppRes = createAppResourceForDir(appResDirName);
            copy(fromAppRes, toAppRes);
        }
        return toAppRes;
    }
    
    /**
     * Sets (registers) a single object so others can get it globally.
     * @param clazz the class of object being sets
     * @param object the object.
     */
    public void setClassObject(final Class<?> clazz, final Object object)
    {
        if (!hasContext)
        {
            throw new RuntimeException("No context has been set.");
        }
        
        if (object == null)
        {
            if (classObjHash.get(clazz) != null)
            {
                classObjHash.remove(clazz);
            }
        } else
        {
            classObjHash.put(clazz, object);
        }
    }
    
    /**
     * Gets the registered single object for a class.
     * @param clazz the class
     * @return the object
     */
    @SuppressWarnings("unchecked")
    public <T> T getClassObject(final Class<T> clazz)
    {
        if (!hasContext)
        {
            throw new RuntimeException("No context has been set.");
        }
        return (T)classObjHash.get(clazz);
    }
    /**
     * Returns the instance of the AppContextMgr.
     * @return the instance of the AppContextMgr.
     */
    public static AppContextMgr getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryNameStr = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(factoryName);
                    }
                });
            
        if (factoryNameStr != null) 
        {
            try 
            {
                instance = (AppContextMgr)Class.forName(factoryNameStr).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AppContextMgr.class, e);
                InternalError error = new InternalError("Can't instantiate AppContextMgr factory " + factoryNameStr); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }
    
    /**
     * Clears hashtable of ClassDataObjects 
     */
    public void clear()
    {
        classObjHash.clear();
    }
    
    /**
     * @return whether Security has been turned on
     */
    public abstract boolean isSecurity();

    
    /**
     * @param secVal true to turn on security
     */
    public abstract boolean setSecurity(final boolean secVal);

    /**
     * 
     */
    public static void reset()
    {
        instance = null;
    }
}

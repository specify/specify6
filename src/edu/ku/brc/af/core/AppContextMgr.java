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

package edu.ku.brc.af.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

import org.dom4j.Element;

import edu.ku.brc.ui.forms.persist.View;

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
    public static final String factoryName = "edu.ku.brc.af.core.AppContextMgrFactory";
    
    public enum CONTEXT_STATUS {OK, Error, Ignore, Initial}
    
    protected static AppContextMgr instance = null;
    
    protected CONTEXT_STATUS currentStatus = CONTEXT_STATUS.Initial;
    
    /**
     * Returns a View by name, meaning a ViewSet name and a View name inside the ViewSet.
     * @param viewSetName the ViewSet Name (can be null if the default is desired)
     * @param viewName the name of the view (cannot be null)
     * @return the view
     */
    public abstract View getView(final String viewSetName, final String viewName);
    
    /**
     * Returns an Application Resource object by name
     * @param name the name of the resource
     * @return the application resource
     */
    public abstract AppResourceIFace getResource(final String name);
   
    /**
     * Returns Application Resource objects by mime type
     * @param mimeType the mime type of the files to be returned
     * @return the list application resource
     */
    public abstract List<AppResourceIFace> getResourceByMimeType(final String mimeType);
   
    /**
     * Returns the DOM for an Resource that is an XML Resource. 
     * It will throw an exception if the MimeType is not of "text"xml"
     * @param name the name of the resource
     * @return the root element of the XML DOM
     */
    public abstract Element getResourceAsDOM(final String name);
   
    /**
     * Sets the current context.
     * @param databaseName the name of the database 
     * @param userName the user name
     * @param startingOver indicates that the context should "start over" which means it may want to ask the user for specific things
     * (this may be ignored by some implementations) it is merely a suggestion.
     * @return  the status enum for what happened
     */
    public abstract CONTEXT_STATUS setContext(final String databaseName, 
                                              final String userName,
                                              final boolean startingOver);
    
    
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
                InternalError error = new InternalError("Can't instantiate AppContextMgr factory " + factoryNameStr);
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }

}

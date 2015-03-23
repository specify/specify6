/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

/**
 * Default factory implementation, but it can be orridden by setting Systme property "edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory".
 * This factory creates DraggableRecordIdentifier objects.
 * 
 * @code_status Complete
 *
 * @author rods
 *
 */
public class DraggableRecordIdentifierFactory
{
    private static final Logger log = Logger.getLogger(DraggableRecordIdentifierFactory.class);
    private static final String className = "edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory";
    
    protected static DraggableRecordIdentifierFactory instance = null;
    
    /**
     * Default Constructor.
     */
    protected DraggableRecordIdentifierFactory()
    {
        
    }
    
    /**
     * Creates an instance of a DraggableRecordIdentifier.
     * @param icon an icon
     * @return the new instance
     */
    public DraggableRecordIdentifier createDraggableRecordIdentifier(final ImageIcon icon)
    {
        return new DraggableRecordIdentifier(icon); 
    }
    
    /**
     * Returns the instance of the DraggableRecordIdentifierFactory.
     * @return the instance of the DraggableRecordIdentifierFactory.
     */
    public static DraggableRecordIdentifierFactory getInstance()
    {
        if (instance != null)
        {
            return instance;
        }
        
        // Check to see if this factory is being orridden
        String factoryName = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(className);
                }
                });
            
        if (factoryName != null && !className.equals(factoryName)) 
        {
            try 
            {
                instance = (DraggableRecordIdentifierFactory)Class.forName(factoryName).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DraggableRecordIdentifierFactory.class, e);
                log.error(e);
                InternalError error = new InternalError("Can't instantiate DraggableRecordIdentifierFactory factory " + factoryName);
                error.initCause(e);
                throw error;
            }
        }
        // else
        // If not, then create the "default" factory
        instance = new DraggableRecordIdentifierFactory();
        
        return null;
    }

}

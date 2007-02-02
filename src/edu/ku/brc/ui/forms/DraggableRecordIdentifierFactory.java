/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.ui.forms;

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
                log.error(e);
                InternalError error = new InternalError("Can't instantiate DraggableRecordIdentifierFactory factory " + factoryName);
                error.initCause(e);
                throw error;
            }
        } else
        {
            // If not, then create the "default" factory
            instance = new DraggableRecordIdentifierFactory();
        }
        return null;
    }

}

/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui.weblink;

/**
 * Any object that is set into a WebLinkButton can implement this interface to provide data
 * to the WebLnkButton that may not be available via directory using a getter. For example,
 * a Taxon Species Object can be asked for the Genus name and it will provide it. 
 * Note: that the 'dataName' is not necessarily a 'field name'. It should be considered 
 * to a logical data name. Also, the data object implementing this must be able to provide
 * all the data fields, because the WebLinkButton will not ask the object directly if this
 * interface is available.
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 15, 2008
 *
 */
public interface WebLinkDataProviderIFace
{

    /**
     * Ask the provider for a single data object from a name.
     * @param dataName the name of the data that is to be returned
     * @return the data or null
     */
    public abstract String getWebLinkData(final String dataName);
    
}

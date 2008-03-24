/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.core;

import java.util.List;

/**
 * Interface that can provide Services for Search Results title bar. 
 * For any class that implements QueryForIdResultsIFace interface 
 * they can also implement this interface to add additional services.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Mar 21, 2008
 *
 */
public interface ServiceProviderIFace
{
    /**
     * @return additional services that should be loaded.
     */
    public abstract List<ServiceInfo> getServices();
}

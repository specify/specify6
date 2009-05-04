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
package edu.ku.brc.af.ui.weblink;

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

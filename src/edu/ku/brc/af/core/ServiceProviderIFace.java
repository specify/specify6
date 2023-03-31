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
    public abstract List<ServiceInfo> getServices(final Object serviceConsumer);
}

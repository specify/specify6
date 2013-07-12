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
package edu.ku.brc.af.auth.specify.module;

import java.util.Collections;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

/**
 * This JAAS login configuration class (implemented as a singleton) is hard-coded to the 
 * only JAAS login module available in Specify so far. This class replaces the file based 
 * security configuration provided by default, and simplifies JAAS login configuration.
 * 
 * @code_status alpha
 * 
 * @author Ricardo
 *
 *
 */
public class SpDBConfiguration extends Configuration {

	protected static SpDBConfiguration instance;

	/**
	 * Returns the singleton instance of this class
	 * @return one and only (singleton) instance of configuration class 
	 */
	public static SpDBConfiguration getInstance()
	{
		if (instance == null)
		{
			instance = new SpDBConfiguration();
		}
		return instance;
	}

	/**
	 * Returns a single configuration entry hard-coded to the Specify JAAS Login Module
	 * This method is overridden from Configuration base class
	 */
	public AppConfigurationEntry[] getAppConfigurationEntry(String name) {

		AppConfigurationEntry[] entries = new AppConfigurationEntry[1];
		
		Map<String, ?> emptyMap = Collections.emptyMap();
		entries[0] = new AppConfigurationEntry(SpDBLoginModule.class.getName(),
    			AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, emptyMap);

		return entries;
	}

    /* (non-Javadoc)
     * @see javax.security.auth.login.Configuration#refresh()
     */
    @Override
    public void refresh()
    {
        
    }

}

package edu.ku.brc.af.auth.specify.module;

import java.util.Collections;

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
	 * Returns a single copnfiguration entry hard-coded to the Specify JAAS Login Module
	 * This method is overridden from Configuration base class
	 */
	public AppConfigurationEntry[] getAppConfigurationEntry(String name) {

		AppConfigurationEntry[] entries = new AppConfigurationEntry[1];
		
		entries[0] = new AppConfigurationEntry(SpDBLoginModule.class.getName(),
    			AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, Collections.EMPTY_MAP);

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

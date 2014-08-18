/**
 * 
 */
package edu.ku.brc.specify.tools.export;

import edu.ku.brc.specify.config.SpecifyAppContextMgr;

/**
 * @author timo
 *
 */
public class SpecifyExpCmdAppContextMgr extends SpecifyAppContextMgr {

	
	/**
	 * 
	 */
	public SpecifyExpCmdAppContextMgr() {
		super();
	}

	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.config.SpecifyAppContextMgr#isSecurity()
	 */
	@Override
	public boolean isSecurity() {
		//The command line exporter runs its own check that User is a manager. 
		//normal rules do not apply.
		return false;
	}

}

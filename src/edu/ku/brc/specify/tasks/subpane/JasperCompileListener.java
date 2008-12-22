/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import java.io.File;

/**
 * @author timo
 *
 *Interface for handling completion of threaded Jasper compile process.
 */
//XXX Jasper defines a JasperCompilerListener class??
public interface JasperCompileListener 
{
    /**
     * The compile is complete
     * @param report the compiled report, or null if there was a compiling error
     */
	public void compileComplete(final File compiledFile);
}

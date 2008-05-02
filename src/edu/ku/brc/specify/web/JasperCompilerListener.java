/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.web;

import java.io.File;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2008
 *
 */
public interface JasperCompilerListener
{
    /**
     * @param compiledFile
     */
    public abstract void compileComplete(final File compiledFile);
}

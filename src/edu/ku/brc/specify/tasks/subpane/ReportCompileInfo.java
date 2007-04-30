/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */

package edu.ku.brc.specify.tasks.subpane;

import java.io.File;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 29, 2007
 *
 */
public class ReportCompileInfo
{
    protected File    reportFile;
    protected File    compiledFile;
    protected boolean needsCompiled;
    
    public ReportCompileInfo(final File reportFile, final File compiledFile, final boolean needsCompiled)
    {
        super();
        this.reportFile    = reportFile;
        this.compiledFile  = compiledFile;
        this.needsCompiled = needsCompiled;
    }

    public File getCompiledFile()
    {
        return compiledFile;
    }

    public boolean isCompiled()
    {
        return needsCompiled;
    }

    public File getReportFile()
    {
        return reportFile;
    }
}


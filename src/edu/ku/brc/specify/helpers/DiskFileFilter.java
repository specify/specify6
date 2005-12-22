package edu.ku.brc.specify.helpers;

import java.io.*;

public class DiskFileFilter implements java.io.FileFilter 
{
    protected String ext;
    
    public DiskFileFilter(final String ext)
    {
        this.ext = ext;
    }
    
    public boolean accept(File f) 
    {
        return f.getName().toLowerCase().endsWith(ext);
    }
}
/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.toycode;

import com.thoughtworks.xstream.XStream;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 14, 2008
 *
 */
public class UpdateEntry
{

    protected String targetMediaFileId;
    protected String updatableVersionMin;
    protected String updatableVersionMax;
    protected String fileName;
    protected String newVersion;
    protected String newMediaFileId;
    protected String fileSize;
    protected String bundledJre;
    protected String archive     = "false";
    
    protected String comment = "";
    
    /**
     * 
     */
    public UpdateEntry()
    {
        super();
    }
    /**
     * @return the targetMediaFileId
     */
    public String getTargetMediaFileId()
    {
        return targetMediaFileId;
    }
    /**
     * @param targetMediaFileId the targetMediaFileId to set
     */
    public void setTargetMediaFileId(String targetMediaFileId)
    {
        this.targetMediaFileId = targetMediaFileId;
    }
    /**
     * @return the updatableVersionMin
     */
    public String getUpdatableVersionMin()
    {
        return updatableVersionMin;
    }
    /**
     * @param updatableVersionMin the updatableVersionMin to set
     */
    public void setUpdatableVersionMin(String updatableVersionMin)
    {
        this.updatableVersionMin = updatableVersionMin;
    }
    /**
     * @return the updatableVersionMax
     */
    public String getUpdatableVersionMax()
    {
        return updatableVersionMax;
    }
    /**
     * @param updatableVersionMax the updatableVersionMax to set
     */
    public void setUpdatableVersionMax(String updatableVersionMax)
    {
        this.updatableVersionMax = updatableVersionMax;
    }
    /**
     * @return the fileName
     */
    public String getFileName()
    {
        return fileName;
    }
    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    /**
     * @return the newVersion
     */
    public String getNewVersion()
    {
        return newVersion;
    }
    /**
     * @param newVersion the newVersion to set
     */
    public void setNewVersion(String newVersion)
    {
        this.newVersion = newVersion;
    }
    /**
     * @return the newMediaFileId
     */
    public String getNewMediaFileId()
    {
        return newMediaFileId;
    }
    /**
     * @param newMediaFileId the newMediaFileId to set
     */
    public void setNewMediaFileId(String newMediaFileId)
    {
        this.newMediaFileId = newMediaFileId;
    }
    /**
     * @return the fileSize
     */
    public String getFileSize()
    {
        return fileSize;
    }
    /**
     * @param fileSize the fileSize to set
     */
    public void setFileSize(String fileSize)
    {
        this.fileSize = fileSize;
    }
    /**
     * @return the bundledJre
     */
    public String getBundledJre()
    {
        return bundledJre;
    }
    /**
     * @param bundledJre the bundledJre to set
     */
    public void setBundledJre(String bundledJre)
    {
        this.bundledJre = bundledJre;
    }
    /**
     * @return the archive
     */
    public String getArchive()
    {
        return archive;
    }
    /**
     * @param archive the archive to set
     */
    public void setArchive(String archive)
    {
        this.archive = archive;
    }
    
    public static void config(XStream xstream)
    {
        xstream.alias("entry", UpdateEntry.class);
        
        xstream.useAttributeFor(UpdateEntry.class, "targetMediaFileId");
        xstream.useAttributeFor(UpdateEntry.class, "updatableVersionMin");
        xstream.useAttributeFor(UpdateEntry.class, "updatableVersionMax");
        xstream.useAttributeFor(UpdateEntry.class, "fileName");
        xstream.useAttributeFor(UpdateEntry.class, "newVersion");
        xstream.useAttributeFor(UpdateEntry.class, "newMediaFileId");
        xstream.useAttributeFor(UpdateEntry.class, "fileSize");
        xstream.useAttributeFor(UpdateEntry.class, "bundledJre");
        xstream.useAttributeFor(UpdateEntry.class, "archive");
    }
    
    
}

/*
 * Copyright (C) 2009  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.exceptions;

/**
 * This represents a single Exception.
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 10, 2009
 *
 */
public class ExceptionItem
{
    protected String taskName;
    protected String title;
    protected String bug;
    protected String comments;
    protected String stackTrace;
    protected String className;
    
    /**
     * 
     */
    public ExceptionItem()
    {
        super();
    }

    /**
     * @param taskName
     * @param title
     * @param bug
     * @param comments
     * @param stackTrace
     * @param className
     */
    public ExceptionItem(String taskName, String title, String bug, String comments, String stackTrace, String className)
    {
        super();
        this.taskName = taskName;
        this.title = title;
        this.bug = bug;
        this.comments = comments;
        this.stackTrace = stackTrace;
        this.className = className;
    }

    /**
     * @return the taskName
     */
    public String getTaskName()
    {
        return taskName;
    }

    /**
     * @param taskName the taskName to set
     */
    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }

    /**
     * @return the bug
     */
    public String getBug()
    {
        return bug;
    }

    /**
     * @param bug the bug to set
     */
    public void setBug(String bug)
    {
        this.bug = bug;
    }

    /**
     * @return the comments
     */
    public String getComments()
    {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(String comments)
    {
        this.comments = comments;
    }

    /**
     * @return the stackTrace
     */
    public String getStackTrace()
    {
        return stackTrace;
    }

    /**
     * @param stackTrace the stackTrace to set
     */
    public void setStackTrace(String stackTrace)
    {
        this.stackTrace = stackTrace;
    }

    /**
     * @return the className
     */
    public String getClassName()
    {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

}

/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.ui;

import java.util.Properties;

/**
 * This represents a single FeedBackItem. It was especially designed 
 * for bug reporting but can used for other things.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Jan 10, 2009
 *
 */
public class FeedBackSenderItem
{
    protected String     taskName;
    protected String     title;
    protected String     bug;
    protected String     comments;
    protected String     stackTrace;
    protected String     className;
    protected boolean    includeEmail = true;
    protected Properties props = null;
    
    /**
     * 
     */
    public FeedBackSenderItem()
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
    public FeedBackSenderItem(String taskName, 
                              String title, 
                              String bug, 
                              String comments, 
                              String stackTrace, 
                              String className)
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
     * @return the includeEmail
     */
    public boolean isIncludeEmail()
    {
        return includeEmail;
    }

    /**
     * @param includeEmail the includeEmail to set
     */
    public void setIncludeEmail(boolean includeEmail)
    {
        this.includeEmail = includeEmail;
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

    /**
     * @return the props
     */
    public Properties getProps()
    {
        return props;
    }

    /**
     * @param props the props to set
     */
    public void setProps(Properties props)
    {
        this.props = props;
    }

}

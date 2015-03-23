/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.weblink;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class WebLinkDefArg implements Cloneable
{
    protected String  name;
    protected String  title;
    protected boolean prompt;
    
    // Transient
    protected boolean isField    = false;
    protected boolean isEditable = false;
    
    /**
     * 
     */
    public WebLinkDefArg()
    {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * @param name
     * @param title
     * @param prompt
     */
    public WebLinkDefArg(String name, String title, boolean prompt)
    {
        super();
        this.name   = name;
        this.prompt = prompt;
        this.title  = title;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the prompt
     */
    public boolean isPrompt()
    {
        return prompt;
    }

    /**
     * @param prompt the prompt to set
     */
    public void setPrompt(boolean prompt)
    {
        this.prompt = prompt;
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

    public boolean isField()
    {
        return isField;
    }

    public void setField(boolean isField)
    {
        this.isField = isField;
    }

    /**
     * @return the isEditable
     */
    public boolean isEditable()
    {
        return isEditable;
    }

    /**
     * @param isEditable the isEditable to set
     */
    public void setEditable(boolean isEditable)
    {
        this.isEditable = isEditable;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        WebLinkDefArg arg = (WebLinkDefArg)super.clone();
        arg.name   = name;
        arg.prompt = prompt;
        arg.title  = title;
        return arg;
    }

    /**
     * @param xstream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.alias("weblinkdefarg", WebLinkDefArg.class);  //$NON-NLS-1$
        xstream.omitField(WebLinkDefArg.class, "isField");
    }
}

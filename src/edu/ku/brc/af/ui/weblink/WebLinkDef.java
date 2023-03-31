/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class WebLinkDef
{
    protected String                name;
    protected String                tableName;
    protected String                desc;
    protected String                baseURLStr;
    protected Vector<WebLinkDefArg> args;
    protected Vector<WebLinkUsedBy> usedByList;
    
    /**
     * 
     */
    public WebLinkDef()
    {
        this(null, null, null, null);
        this.args       = new Vector<WebLinkDefArg>();
        this.usedByList = new Vector<WebLinkUsedBy>();
    }
    
    /**
     * 
     */
    public WebLinkDef(final String name, 
                      final String tableName)
    {
        this(name, tableName, null, null);
        this.args       = new Vector<WebLinkDefArg>();
        this.usedByList = new Vector<WebLinkUsedBy>();
    }
    
    
    /**
     * @param name
     * @param desc
     * @param baseURLStr
     */
    public WebLinkDef(final String name, 
                      final String tableName, 
                      final String desc, 
                      final String baseURLStr)
    {
        this.name       = name;
        this.tableName  = tableName;
        this.desc       = desc;
        this.baseURLStr = baseURLStr;
        this.args       = null;
        this.usedByList = null;
    }
    
    /**
     * @param tblName
     * @return
     */
    public boolean isOwnedByTable(final String tblName)
    {
        return StringUtils.isNotEmpty(tableName) && StringUtils.isNotEmpty(tblName) && tableName.equals(tblName);
    }
    
    /**
     * @param tblName
     * @return
     */
    public boolean isUSedByTable(final String tblName)
    {
        if (usedByList == null || usedByList.size() == 0)
        {
            return false;
        }
        
        for (WebLinkUsedBy ub : usedByList)
        {
            if (tblName.equals(ub.getTableName()))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return whether there is at least one arg with a prompt.
     */
    public int getPromptCount()
    {
        int cnt = 0;
        for (WebLinkDefArg arg : args)
        {
            if (arg.isPrompt())
            {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
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
     * @return the desc
     */
    public String getDesc()
    {
        return desc;
    }


    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc)
    {
        this.desc = desc;
    }


    /**
     * @return the baseURLStr
     */
    public String getBaseURLStr()
    {
        return baseURLStr;
    }


    /**
     * @param baseURLStr the baseURLStr to set
     */
    public void setBaseURLStr(String baseURLStr)
    {
        this.baseURLStr = baseURLStr;
    }


    /**
     * @return the args
     */
    public Vector<WebLinkDefArg> getArgs()
    {
        return args;
    }


    /**
     * @param args the args to set
     */
    public void setArgs(Vector<WebLinkDefArg> args)
    {
        this.args = args;
    }

    /**
     * @return the usedByList
     */
    public Vector<WebLinkUsedBy> getUsedByList()
    {
        return usedByList;
    }

    /**
     * @param usedByList the usedByList to set
     */
    public void setUsedByList(Vector<WebLinkUsedBy> usedByList)
    {
        this.usedByList = usedByList;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name;
    }

    /**
     * @param xstream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.alias("weblinkdef", WebLinkDef.class); //$NON-NLS-1$
    }
}

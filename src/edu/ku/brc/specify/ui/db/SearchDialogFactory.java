/* Filename:    $RCSfile: SearchDialogFactory.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/10 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.ui.db;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.util.Hashtable;

/**
 * This factory knows how to create GenericSearchDialogs by name.
 * 
 * @author rods
 *
 */
public class SearchDialogFactory
{
    protected static SearchDialogFactory instance = new SearchDialogFactory();

    protected Hashtable<String, SearchDialogInfo> dialogs = new Hashtable<String, SearchDialogInfo>();
    
    /**
     * Protected Constructor
     */
    protected  SearchDialogFactory()
    {
        // These will eventually be defined in an XML file.
        
        dialogs.put("AgentSearch", new SearchDialogInfo("Search", 555, "AgentAddressSearch", 
                getResourceString("AgentSearchTitle"), 
                "edu.ku.brc.specify.datamodel.Agent",
                "agentId"));
        
    }
    
    /**
     * Creates a new GenericSearchDialog by name
     * @param name the name of the GenericSearchDialog to return
     * @return a GenericSearchDialog by name
     */
    public static GenericSearchDialog createDialog(final String name)
    {
        SearchDialogInfo info =  instance.dialogs.get(name);
        if (info != null)
        {
            return new GenericSearchDialog(info.getViewSetName(), 
                                             info.getFormId(), 
                                             info.getSearchName(),
                                             info.getTitle(),
                                             info.getClassName(),
                                             info.getIdFieldName()
                                             );
        } else
        {
            throw new RuntimeException("Couldn't create GenericSearchDialog by name["+name+"]");
        }
    }
    
    //-----------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------
    class SearchDialogInfo
    {
        protected String viewSetName; 
        protected int    formId; 
        protected String searchName;
        protected String title;
        protected String className;
        protected String idFieldName;
        
        public SearchDialogInfo(String viewSetName, int formId, String searchName, String title, String className, String idFieldName)
        {
            super();
            // TODO Auto-generated constructor stub
            this.viewSetName = viewSetName;
            this.formId = formId;
            this.searchName = searchName;
            this.title = title;
            this.className = className;
            this.idFieldName = idFieldName;
        }

        public String getClassName()
        {
            return className;
        }

        public int getFormId()
        {
            return formId;
        }

        public String getIdFieldName()
        {
            return idFieldName;
        }

        public String getSearchName()
        {
            return searchName;
        }

        public String getTitle()
        {
            return title;
        }

        public String getViewSetName()
        {
            return viewSetName;
        }
    }


}

/* Filename:    $RCSfile: ComboBoxFromQueryFactory.java,v $
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
package edu.ku.brc.specify.ui.validation;

import java.util.Hashtable;

/**
 * This factory knows how to create AutoComplete Comboboxes that get their data from a query.
 *
 * @author rods
 *
 */
public class ComboBoxFromQueryFactory
{
    protected static ComboBoxFromQueryFactory instance = new ComboBoxFromQueryFactory();

    protected Hashtable<String, ComboBoxFromQueryInfo> comboBoxes = new Hashtable<String, ComboBoxFromQueryInfo>();

    /**
     * Protected Constructor
     */
    protected  ComboBoxFromQueryFactory()
    {
        // These will eventually be defined in an XML file.

        comboBoxes.put("Agent", new ComboBoxFromQueryInfo("agent",
                "AgentID",
                "lastName",
                "LastName,FirstName",
                "edu.ku.brc.specify.datamodel.Agent",
                "agentId",
                "lastName,firstName",
                "%s, %s",
                "AgentSearch"));

        comboBoxes.put("Taxon", new ComboBoxFromQueryInfo("taxon",
                "TreeID",
                "Name",
                null,
                "edu.ku.brc.specify.datamodel.Taxon",
                "treeId",
                "name",
                null,
                null));
    }

    /**
     * Creates a new ValComboBoxFromQuery by name
     * @param name the name of the ValComboBoxFromQuery to return
     * @return a ValComboBoxFromQuery by name
     */
    public static ValComboBoxFromQuery getValComboBoxFromQuery(final String name)
    {
        ComboBoxFromQueryInfo info =  instance.comboBoxes.get(name);
        if (info != null)
        {
            return new ValComboBoxFromQuery(info.getTableName(),
                                             info.getIdColumn(),
                                             info.getKeyColumn(),
                                             info.getDisplayColumn(),
                                             info.getClassName(),
                                             info.getIdName(),
                                             info.getKeyName(),
                                             info.getFormat(),
                                             info.getSearchDialogName()
                                             );
        } else
        {
            throw new RuntimeException("Couldn't create ValComboBoxFromQuery by name["+name+"]");
        }
    }

    //-----------------------------------------------------
    //-- Inner Classes
    //-----------------------------------------------------
    class ComboBoxFromQueryInfo
    {
        protected String tableName;
        protected String idColumn;
        protected String keyColumn;
        protected String displayColumn;
        protected String className;
        protected String idName;
        protected String keyName;
        protected String format;
        protected String searchDialogName;

        public ComboBoxFromQueryInfo(String tableName,
                                     String idColumn,
                                     String keyColumn,
                                     String displayColumn,
                                     String className,
                                     String idName,
                                     String keyName,
                                     String format,
                                     String searchDialogName)
        {
            this.tableName = tableName;
            this.idColumn = idColumn;
            this.keyColumn = keyColumn;
            this.displayColumn = displayColumn;
            this.className = className;
            this.idName = idName;
            this.keyName = keyName;
            this.format = format;
            this.searchDialogName = searchDialogName;
        }

        public String getClassName()
        {
            return className;
        }

        public String getDisplayColumn()
        {
            return displayColumn;
        }

        public String getFormat()
        {
            return format;
        }

        public String getIdColumn()
        {
            return idColumn;
        }

        public String getIdName()
        {
            return idName;
        }

        public String getKeyColumn()
        {
            return keyColumn;
        }

        public String getKeyName()
        {
            return keyName;
        }

        public String getTableName()
        {
            return tableName;
        }

        public String getSearchDialogName()
        {
            return searchDialogName;
        }

    }


}

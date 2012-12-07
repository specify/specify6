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
package edu.ku.brc.specify.tasks.subpane.qb;

import javax.swing.JButton;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public interface QueryFieldPanelContainerIFace
{
    /**
     * @return the columnDefStr
     */
    String getColumnDefStr();
    /**
     * @param columnDefStr
     */
    void setColumnDefStr(final String columnDefStr);
    /**
     * @return Add Button control if it exists.
     */
    JButton getAddBtn();
    /**
     * @param qfp The panel to select.
     */
    void selectQFP(final QueryFieldPanel qfp);
    /**
     * @param qfp Then panel to remove.
     */
    void removeQueryFieldItem(final QueryFieldPanel qfp);
    /**
     * @return the number fields in the query.
     */
    int getFields();
    /**
     * @param index
     * @return field with index.
     */
    QueryFieldPanel getField(int index);
    /**
     * @return true if container contains prompts
     */
    boolean isPromptMode();
    
    /**
     * For ExportSchema Mapper
     * 
     * update available concepts for a schema mapping
     */
    void updateAvailableConcepts();
    
    /**
     * @return true if available concepts are being updated
     */
    boolean isUpdatingAvailableConcepts();
    
    /**
     * @param qfp
     * @param name
     * @return true if name if not used by another contained qfp.
     */
    boolean isAvailableExportFieldName(final QueryFieldPanel qfp, String name);
    /**
     * run the query.
     */
    void doSearch();
    
}

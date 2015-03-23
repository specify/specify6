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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Vector;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 * Used to define workbench mappings for fields belonging to Treeable classes.
 */
public class UploadMappingDefTree extends UploadMappingDef
{
	/**
	 * TreeMapElements on the ranks being uploaded.
	 */
	protected UploadMappingDefTreeLevels levels;
	/**
	 * The name of the field which serves as the parent pointer in the tree.
	 */
	protected String parentField;
	/**
	 * the value of the parent of the highest required level in the tree
	 */
	protected  String root; 
	
	/**
	 * @param tableName
	 * @param fieldName
	 * @param parentField
	 * @param root
	 * @param levels
	 */
	public UploadMappingDefTree(String tableName, String fieldName, String parentField, String root, 
	                            Vector<Vector<TreeMapElement>> levels, String wbFldName) throws Exception
	{
		super(tableName, fieldName);
		this.parentField = parentField;
		this.root = root;
		this.levels = new UploadMappingDefTreeLevels(levels);	
        this.wbFldName = wbFldName; //actually just using tree name for wbFldName for now.
	}
		
	/**
	 * @return the levels
	 */
	public UploadMappingDefTreeLevels getLevels()
	{
		return levels;
	}

	/**
	 * @return the root
	 */
	public String getRoot()
	{
		return root;
	}

	/**
	 * @return the parentField
	 */
	public String getParentField()
	{
		return parentField;
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMappingDef#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder(super.toString() + " (");
        for (int l = 0; l < levels.size(); l++)
        {
            TreeMapElements level = levels.get(l);
            for (int r = 0; r < level.size(); r++)
            {
                result.append("(" + level.getElement(r) + ") ");
            }
        }
        return result.toString();
    }
}

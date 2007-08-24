/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
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
	protected Vector<Vector<TreeMapElement>> levels;
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
	public UploadMappingDefTree(String tableName, String fieldName, String parentField, String root, Vector<Vector<TreeMapElement>> levels)
	{
		super(tableName, fieldName);
		this.parentField = parentField;
		this.root = root;
		this.levels = levels;	
	}
		
	/**
	 * @return the levels
	 */
	public Vector<Vector<TreeMapElement>> getLevels()
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
}

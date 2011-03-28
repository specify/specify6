/**
 * 
 */
package edu.ku.brc.specify.treeutils;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author timo
 *
 */
@SuppressWarnings("serial")
public class TreeMergeException extends Exception
{
	
	public static final int UNSPECIFIED = 0;
	public static final int MULTIPLE_MATCHES = 1;

	private TreeMerger<?,?,?> merger;
	private String toMergeName;
	private Integer toMergeId;
	private Integer mergeToId;
	private Integer mergeToParentId;
	private int exceptionType;

	/**
	 * @param toMergeName
	 * @param toMergeId
	 * @param mergeToId
	 * @param mergeToParentId
	 */
	public TreeMergeException(TreeMerger<?,?,?> merger, String toMergeName, Integer toMergeId,
			Integer mergeToId, Integer mergeToParentId, int exceptionType)
	{
		super();
		this.merger = merger;
		this.toMergeName = toMergeName;
		this.toMergeId = toMergeId;
		this.mergeToId = mergeToId;
		this.mergeToParentId = mergeToParentId;
		this.exceptionType = exceptionType;
	}

	/**
	 * @param toMergeName
	 * @param mergeToParentId
	 */
	public TreeMergeException(TreeMerger<?,?,?> merger, String toMergeName, Integer mergeToParentId)
	{
		this(merger, toMergeName, null, null, mergeToParentId, MULTIPLE_MATCHES);
	}
	
	/**
	 * @return the toMergeName
	 */
	public String getToMergeName() 
	{
		return toMergeName;
	}

	/**
	 * @param toMergeName the toMergeName to set
	 */
	public void setToMergeName(String toMergeName) 
	{
		this.toMergeName = toMergeName;
	}

	/**
	 * @return the toMergeId
	 */
	public Integer getToMergeId() 
	{
		return toMergeId;
	}

	/**
	 * @param toMergeId the toMergeId to set
	 */
	public void setToMergeId(Integer toMergeId) 
	{
		this.toMergeId = toMergeId;
	}

	/**
	 * @return the mergeToId
	 */
	public Integer getMergeToId() 
	{
		return mergeToId;
	}

	/**
	 * @param mergeToId the mergeToId to set
	 */
	public void setMergeToId(Integer mergeToId) 
	{
		this.mergeToId = mergeToId;
	}

	/**
	 * @return the mergeToParentId
	 */
	public Integer getMergeToParentId() 
	{
		return mergeToParentId;
	}

	/**
	 * @param mergeToParentId the mergeToParentId to set
	 */
	public void setMergeToParentId(Integer mergeToParentId) 
	{
		this.mergeToParentId = mergeToParentId;
	}

	/**
	 * @return the exceptionType
	 */
	public int getExceptionType() 
	{
		return exceptionType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() 
	{
		String mergeNodeText = getToMergeName();
		if (StringUtils.isBlank(mergeNodeText) && getToMergeId() != null)
		{
			String sql = "select " + merger.getFullNameFld() + " from "
					+ merger.getNodeTable().getName() + " where " + merger.getNodeTable().getIdColumnName()
					+ " = " + getToMergeId();
			mergeNodeText = BasicSQLUtils.querySingleObj(sql);
		}
		
		if (StringUtils.isBlank(mergeNodeText))
		{
			return UIRegistry.getResourceString("TreeMergeException.GenericMsg");
		}
		
		if (getExceptionType() == MULTIPLE_MATCHES && getMergeToParentId() != null)
		{
			String sql = "select " + merger.getFullNameFld() + " from "
				+ merger.getNodeTable().getName() + " where " + merger.getNodeTable().getIdColumnName()
				+ " = " + getMergeToParentId();
			String parentName = BasicSQLUtils.querySingleObj(sql);
			if (StringUtils.isNotBlank(parentName))
			{
				return String.format(UIRegistry.getResourceString("TreeMergeException.MultipleMatchMsg"), mergeNodeText, parentName);
			}
		}
		
		return String.format(UIRegistry.getResourceString("TreeMergeException.MergeErrorMsg"), mergeNodeText);
	}

	
	
}

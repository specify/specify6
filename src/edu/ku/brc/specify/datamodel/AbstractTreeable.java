/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public abstract class AbstractTreeable implements Treeable
{
    /**
     * A <code>Logger</code> object used for all log messages eminating from
     * this class.
     */
    protected static final Logger log = Logger.getLogger(AbstractTreeable.class);

	/**
	 * An indicator that node full names should start with highest order
	 * nodes and continue to the lowest order nodes.
	 * @see #REVERSE
	 */
	public static final int FORWARD = 1;
	/**
	 * An indicator that node full names should start with lowest order
	 * nodes and continue to the highest order nodes.
	 * @see #FORWARD
	 */
	public static final int REVERSE = -1;

	/**
	 * Generates the 'full name' of a node using the <code>IsInFullName</code> field from the tree
	 * definition items and following the parent pointer until we hit the root node.  Also used
	 * in the process is a "direction indicator" for the tree determining whether the name
	 * should start with the higher nodes and work down to the given node or vice versa.
	 * 
	 * @param node the node to get the full name for
	 * @return the full name
	 */
	public String getFullName( Treeable node )
	{
		Vector<String> parts = new Vector<String>();
		parts.add(node.getName());
		Treeable parent = node.getParentNode();
		while( parent != null )
		{
			Boolean include = parent.getDefItem().getIsInFullName();
			if( include != null && include.booleanValue() == true )
			{
				parts.add(parent.getName());
			}
			
			parent = parent.getParentNode();
		}
		int direction = getFullNameDirection();
		String sep = getFullNameSeparator();
		
		StringBuilder fullName = new StringBuilder(parts.size() * 10);
		
		switch( direction )
		{
			case FORWARD:
			{
				for( int i = parts.size()-1; i > -1; --i )
				{
					fullName.append(parts.get(i));
					fullName.append(sep);
				}
				break;
			}
			case REVERSE:
			{
				for( int i = 0; i < parts.size(); ++i )
				{
					fullName.append(parts.get(i));
					fullName.append(sep);
				}
				break;
			}
			default:
			{
				log.error("Invalid tree walk direction (for creating fullname field) found in tree definition");
				return null;
			}
		}
		
		fullName.delete(fullName.length()-sep.length(), fullName.length());
		return fullName.toString();
	}
	
	/**
	 * Returns the number of proper descendants for node.
	 * 
	 * @param node the node to count descendants for
	 * @return the number of proper descendants
	 */
	public int getDescendantCount()
	{
		int totalDescendants = 0;
		for( Treeable child: getChildNodes() )
		{
			totalDescendants += 1 + child.getDescendantCount();
		}
		return totalDescendants;
	}
	
	/**
	 * Determines if children are allowed for the given node.
	 * 
	 * @param item the node to examine
	 * @return <code>true</code> if children are allowed as defined by the node's tree definition, false otherwise
	 */
	public boolean childrenAllowed()
	{
		if( getDefItem() == null || getDefItem().getChildItem() == null )
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	/**
	 * Returns a <code>List</code> of all descendants of the called <code>node</code>.
	 * 
	 * @return all descendants of <code>node</code>
	 */
	public List<Treeable> getAllDescendants()
	{
		Vector<Treeable> descendants = new Vector<Treeable>();
		for( Treeable child: getChildNodes() )
		{
			descendants.add(child);
			descendants.addAll(child.getAllDescendants());
		}
		return descendants;
	}

	/**
	 * Fixes the fullname for the given node and all of its descendants.
	 */
	public void fixFullNameForAllDescendants()
	{
		setFullName(getFullName());
		for( Treeable child: getChildNodes() )
		{
			child.fixFullNameForAllDescendants();
		}
	}
	
	/**
	 * Updates the created and modified timestamps to now.  Also
	 * updates the <code>lastEditedBy</code> field to the current
	 * value of the <code>user.name</code> system property.
	 */
	public void setTimestampsToNow()
	{
		Date now = new Date();
		setTimestampCreated(now);
		setTimestampModified(now);

		//TODO: fix this somehow
		String user = System.getProperty("user.name");
		setLastEditedBy(user);
	}
	
	/**
	 * Updates the modified timestamp to now.  Also updates the
	 * <code>lastEditedBy</code> field to the current value
	 * of the <code>user.name</code> system property.
	 */
	public void updateModifiedTimeAndUser()
	{
		Date now = new Date();
		setTimestampModified(now);
		
		//TODO: fix this somehow
		String user = System.getProperty("user.name");
		setLastEditedBy(user);
	}

	public boolean isDescendantOf(Treeable node)
	{
		if( node==null )
		{
			throw new NullPointerException();
		}
		
		Treeable i = getParentNode();
		while( i != null )
		{
			if( i == node )
			{
				return true;
			}
			
			i = i.getParentNode();
		}
		return false;
	}
}

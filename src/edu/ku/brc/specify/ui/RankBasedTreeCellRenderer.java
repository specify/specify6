package edu.ku.brc.specify.ui;

import java.awt.Component;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A DefaultTreeCellRenderer that queries a Map to determine which Icon
 * to use to display nodes of a given rank.  This renderer assumes that
 * the underlying tree contains DefaultMutableTreeNodes whos user objects
 * implement the treeable interface.
 * 
 * @author jstewart
 */
public class RankBasedTreeCellRenderer extends DefaultTreeCellRenderer
{
	protected Map<Integer,Icon> iconMap;
	protected Map<Integer,TreeCellRenderer> subRenderers;
	protected boolean subsEnabled;
	protected Icon defaultIcon;
	
	/**
	 * Creates a new RandBasedTreeCellRenderer using the passed in Map to
	 * determine which icon to use for each rank
	 * 
	 * @param ranksToIcons
	 */
	public RankBasedTreeCellRenderer( Map<Integer,Icon> ranksToIcons )
	{
		super();
		iconMap = ranksToIcons;
		subRenderers = new Hashtable<Integer,TreeCellRenderer>();
		subsEnabled = true;
	}
	
	public void setSubRendererForRank( TreeCellRenderer subRenderer, int rank )
	{
		subRenderers.put(rank, subRenderer);
	}
	
	public void setSubRenderersEnabled( boolean enabled )
	{
		subsEnabled = enabled;
	}
	
	public void setDefaultIcon(Icon icon)
	{
		defaultIcon = icon;
	}
	
	/**
	 * Configures the renderer based on the underlying DefaultTreeCellRenderers choices
	 * and modifies the node icon based on the rank of <i>value</i>, which must be
	 * a DefaultMutableTreeNode containing a Treeable object as the user object.
	 * 
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
    public Component getTreeCellRendererComponent(JTree tree,
    												Object value,
    												boolean sel,
    												boolean expanded,
    												boolean leaf,
    												int row,
    												boolean hasFocus)
    {
	    // TODO Auto-generated method stub
	    Component c = super.getTreeCellRendererComponent(tree,
	    												value,
	    												sel,
	    												expanded,
	    												leaf,
	    												row,
	    												hasFocus);
	    
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
	    Treeable t = (Treeable)node.getUserObject();

	    JLabel l = (JLabel)c;
	    l.setText(t.getName());
	    
	    Integer rank = t.getRankId();
	    if( rank != null )
	    {
	    	if( subsEnabled )
	    	{
		    	TreeCellRenderer sub = subRenderers.get(rank);
		    	if( sub != null )
		    	{
		    		return sub.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		    	}	    		
	    	}

	    	Icon icon = iconMap.get(rank);
	    	if( icon != null )
	    	{
	    		l.setIcon(icon);
	    	}
	    	else if( defaultIcon != null )
	    	{
	    		l.setIcon(defaultIcon);
	    	}
	    }

	    return l;
    }
}

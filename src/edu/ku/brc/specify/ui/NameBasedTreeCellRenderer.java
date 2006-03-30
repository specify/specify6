package edu.ku.brc.specify.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A DefaultTreeCellRenderer that queries a Map to determine which Icon
 * to use to display nodes of a given rank.  This renderer assumes that
 * the underlying tree contains DefaultMutableTreeNodes whos user objects
 * implement the treeable interface.
 * 
 * @author jstewart
 */
public class NameBasedTreeCellRenderer extends DefaultTreeCellRenderer
{
	protected Map<String,Icon> iconMap;
	
	/**
	 * Creates a new RandBasedTreeCellRenderer using the passed in Map to
	 * determine which icon to use for each rank
	 * 
	 * @param ranksToIcons
	 */
	public NameBasedTreeCellRenderer( Map<String,Icon> namesToIcons )
	{
		super();
		iconMap = namesToIcons;
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
	    Color color;
	    if( sel )
	    {
	    	color = getBackgroundSelectionColor();
	    }
	    else
	    {
	    	color = getBackgroundNonSelectionColor();
	    }
	    l.setBorder(BorderFactory.createMatteBorder(1,1,1,1,color));
	    
	    if( t.getName() != null )
	    {
	    	String lowerCaseName = t.getName().toLowerCase();
	    	ImageIcon icon = (ImageIcon)iconMap.get(lowerCaseName);
	    	if( icon != null )
	    	{
	    		l.setIcon(new BorderedIcon(icon,1,Color.BLACK));
	    	}
	    }

	    return l;
    }
}

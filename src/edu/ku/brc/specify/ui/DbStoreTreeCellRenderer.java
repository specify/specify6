package edu.ku.brc.specify.ui;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.ku.brc.specify.datamodel.Treeable;

@SuppressWarnings("serial")
public class DbStoreTreeCellRenderer extends DefaultTreeCellRenderer
{
	public DbStoreTreeCellRenderer()
	{
		
	}
	
	public Component getTreeCellRendererComponent( JTree tree, Object node,
			boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6 )
	{
		Component c = super.getTreeCellRendererComponent(tree, node, arg2, arg3, arg4, arg5, arg6);
		JLabel l = (JLabel)c;
		Treeable dbNode = (Treeable)node;
		l.setText(dbNode.getName()+" : "+dbNode.getNodeNumber()+" : "+dbNode.getHighestChildNodeNumber());
		return l;
	}

}

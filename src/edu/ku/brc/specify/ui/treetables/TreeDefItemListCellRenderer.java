/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import edu.ku.brc.specify.datamodel.TreeDefinitionItemIface;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TreeDefItemListCellRenderer extends JPanel implements ListCellRenderer
{
	protected int fixedCellHeight;
	protected int cellWidth;
	protected Icon enforcedIcon;
	
	protected TreeDefinitionItemIface item;
	protected int index;
	protected boolean selected;
	protected boolean focus;
	
	public TreeDefItemListCellRenderer(int fixedCellHeight, Icon enforcedIcon)
	{
		this.fixedCellHeight = fixedCellHeight;
		this.enforcedIcon = enforcedIcon;
	}
	
	/**
	 *
	 *
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 * @param list
	 * @param value
	 * @param index
	 * @param isSelected
	 * @param cellHasFocus
	 * @return
	 */
	public Component getListCellRendererComponent(	JList list,
													Object value,
													int index,
													boolean isSelected,
													boolean cellHasFocus)
	{
		if(!(value instanceof TreeDefinitionItemIface))
		{
			return null;
		}

		if( selected )
		{
			this.setBackground(list.getSelectionBackground());
			this.setForeground(list.getSelectionForeground());
		}
		else
		{
			this.setBackground(list.getBackground());
			this.setForeground(list.getForeground());
		}
		cellWidth = list.getWidth();
		item = (TreeDefinitionItemIface)value;
		selected = isSelected;
		focus = cellHasFocus;
		
		return this;
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(cellWidth,fixedCellHeight);
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		FontMetrics fm = g.getFontMetrics();
		Rectangle origClip = g.getClipBounds();
		Rectangle nameClip = new Rectangle(origClip);
		
		int spaceBetweenIconAndString = 10;
		int beforeAndAfterSpacing = 5;
		int iconWidth = enforcedIcon.getIconWidth();
		nameClip.width = cellWidth - 2*beforeAndAfterSpacing - spaceBetweenIconAndString - iconWidth;
		nameClip.x+=beforeAndAfterSpacing;
		g.setClip(nameClip.x,nameClip.y,nameClip.width,nameClip.height);
		
		int baselineAdjust = (int)(.5*fixedCellHeight) + (int)(.5*fm.getHeight()) - fm.getDescent();
		g.drawString(item.getName(),beforeAndAfterSpacing,baselineAdjust);
		
		if( item.getIsEnforced() != null && item.getIsEnforced().booleanValue() == true )
		{
			enforcedIcon.paintIcon(this,g,cellWidth - beforeAndAfterSpacing - iconWidth,0);
		}
	}
}

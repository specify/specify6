/**
 * 
 */
package edu.ku.brc.specify.ui.treetables;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
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
	protected Icon checkmarkIcon;
	protected Icon blankIcon;
	protected int iconSpacer;
	
	protected TreeDefinitionItemIface item;
	protected int index;
	
	protected JLabel textLabel;
	protected JLabel enforcedLabel;
	protected JLabel fullnameLabel;
	
	public TreeDefItemListCellRenderer(int fixedCellHeight, Icon checkmarkIcon)
	{
		super();
		this.fixedCellHeight = fixedCellHeight;
		this.checkmarkIcon = checkmarkIcon;
		
		this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
		
		textLabel = new JLabel();
		enforcedLabel = new JLabel();
		fullnameLabel = new JLabel();
		
		createBlankIcon();
		
		Dimension spacer = new Dimension(5,0);
		this.add(Box.createRigidArea(spacer));
		this.add(textLabel);
		this.add(Box.createHorizontalGlue());
		this.add(fullnameLabel);
		this.add(Box.createRigidArea(new Dimension(30,0)));
		this.add(enforcedLabel);
		this.add(Box.createRigidArea(spacer));
	}
	
	protected void createBlankIcon()
	{
		blankIcon = new Icon()
		{
			public int getIconHeight()
			{
				return checkmarkIcon.getIconHeight();
			}
			public int getIconWidth()
			{
				return checkmarkIcon.getIconHeight();
			}
			public void paintIcon(Component c, Graphics g, int x, int y)
			{
			}
		};
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

		// if row is selected, fixup the colors
		if( isSelected )
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
		
		textLabel.setText(item.getName());
		if( item.getIsEnforced()!=null && item.getIsEnforced().booleanValue()==true )
		{
			enforcedLabel.setIcon(checkmarkIcon);
		}
		else
		{
			enforcedLabel.setIcon(blankIcon);
		}
		if( item.getIsInFullName()!=null && item.getIsInFullName().booleanValue()==true)
		{
			fullnameLabel.setIcon(checkmarkIcon);
		}
		else
		{
			fullnameLabel.setIcon(blankIcon);
		}
		
		return this;
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(cellWidth,fixedCellHeight);
	}

//	@Override
//	protected void paintComponent(Graphics g)
//	{
//		super.paintComponent(g);
//		FontMetrics fm = g.getFontMetrics();
//		Rectangle origClip = g.getClipBounds();
//		Rectangle nameClip = new Rectangle(origClip);
//		
//		int spaceBetweenIconAndString = 10;
//		int beforeAndAfterSpacing = 5;
//		int iconWidth = checkmarkIcon.getIconWidth();
//		nameClip.width = cellWidth - 2*beforeAndAfterSpacing - spaceBetweenIconAndString - iconWidth;
//		nameClip.x+=beforeAndAfterSpacing;
//		g.setClip(nameClip.x,nameClip.y,nameClip.width,nameClip.height);
//		
//		int baselineAdjust = (int)(.5*fixedCellHeight) + (int)(.5*fm.getHeight()) - fm.getDescent();
//		g.drawString(item.getName(),beforeAndAfterSpacing,baselineAdjust);
//		
//		if( item.getIsEnforced() != null && item.getIsEnforced().booleanValue() == true )
//		{
//			checkmarkIcon.paintIcon(this,g,cellWidth - beforeAndAfterSpacing - iconWidth,0);
//		}
//	}
}

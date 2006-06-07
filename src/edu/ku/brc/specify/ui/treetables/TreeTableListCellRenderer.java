package edu.ku.brc.specify.ui.treetables;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.IconManager;

@SuppressWarnings("serial")
public class TreeTableListCellRenderer extends DefaultListCellRenderer implements ListDataListener
{
	protected TreeDataListModel				model;
	protected JList							list;
	protected boolean						lengthsValid;
	protected SortedMap<Integer, Integer>	rankToIconWidthMap;
	protected Icon							open;
	protected Icon							closed;
	protected Treeable						currentTreeable;
	
	public TreeTableListCellRenderer(JList list, TreeDataListModel listModel)
	{
		this.list = list;
		setModel(listModel);
		lengthsValid = false;

		rankToIconWidthMap = new TreeMap<Integer, Integer>();

		open = IconManager.getIcon("Down",IconManager.IconSize.Std16);
		closed = IconManager.getIcon("Forward",IconManager.IconSize.Std16);

		if( open==null )
		{
			open = new Icon()
			{
				public void paintIcon(Component c, Graphics g, int x, int y)
				{
					// draw a big minus sign
					g.drawLine(x+2,y+7,x+14,y+7);
					g.drawLine(x+2,y+8,x+14,y+8);
				}

				public int getIconWidth()
				{
					return 16;
				}

				public int getIconHeight()
				{
					return 16;
				}
			};
		}
		if( closed==null )
		{
			closed = new Icon()
			{
				public void paintIcon(Component c, Graphics g, int x, int y)
				{
					// draw a big plus sign
					g.drawLine(x+2,y+7,x+14,y+7);
					g.drawLine(x+2,y+8,x+14,y+8);

					g.drawLine(x+7,y+2,x+7,y+14);
					g.drawLine(x+8,y+2,x+8,y+14);
				}

				public int getIconWidth()
				{
					return 16;
				}

				public int getIconHeight()
				{
					return 16;
				}
			};
		}
	}

	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
	{
		this.currentTreeable = (Treeable)value;
		JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		return l;
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		//super.paintComponent(g);
		g.drawString(this.toString(), getX(), getY());
		System.out.println("Painting cell renderer for " + currentTreeable.getName() );
		Rectangle clipRect = g.getClipBounds();
		System.out.println("     Clip rect: " + clipRect.x + "," + clipRect.y + "," + clipRect.width + "," + clipRect.height );		
	}

	public void setModel(TreeDataListModel tdlm)
	{
		if( model!=null )
		{
			model.removeListDataListener(this);
		}

		model = tdlm;
		if( model!=null )
		{
			model.addListDataListener(this);
		}
	}

	/**
	 * @return Returns the closed.
	 */
	public Icon getClosed()
	{
		return closed;
	}

	/**
	 * @param closed
	 *            The closed to set.
	 */
	public void setClosed(Icon closed)
	{
		this.closed = closed;
	}

	/**
	 * @return Returns the open.
	 */
	public Icon getOpen()
	{
		return open;
	}

	/**
	 * @param open
	 *            The open to set.
	 */
	public void setOpen(Icon open)
	{
		this.open = open;
	}

	public void intervalAdded(ListDataEvent e)
	{
		lengthsValid = false;
	}

	public void intervalRemoved(ListDataEvent e)
	{
		lengthsValid = false;
	}

	public void contentsChanged(ListDataEvent e)
	{
		lengthsValid = false;
	}
	
	public class TreeTableListRendererComponent extends JPanel
	{
		protected Treeable t;
		public TreeTableListRendererComponent(Treeable t)
		{
			super();
			this.t = t;
			this.setSize(list.getWidth(),getFont().getSize()*2);
		}
		
		@Override
		protected void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.drawString(t.getName(), getX(), getY()+this.getFont().getSize());
		}
	}
}

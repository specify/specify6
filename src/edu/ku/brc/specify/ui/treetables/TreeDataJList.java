package edu.ku.brc.specify.ui.treetables;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;

import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.dnd.GhostActionable;
import edu.ku.brc.specify.ui.dnd.GhostMouseInputAdapter;

@SuppressWarnings("serial")
public class TreeDataJList extends JList implements DragSourceListener,
		DropTargetListener, DragGestureListener, GhostActionable
{

	protected static DataFlavor	localObjectFlavor;
	static
	{
		try
		{
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		}
		catch( ClassNotFoundException cnfe )
		{
			cnfe.printStackTrace();
		}
	}
	protected static DataFlavor[]	supportedFlavors	=
											{ localObjectFlavor };
	protected DragSource			dragSource;
	protected DropTarget			dropTarget;
	protected Object				dropTargetCell;
	protected int					draggedIndex		= -1;
	protected TreeDataListModel	treeDataModel;
	
    protected GhostMouseInputAdapter  mouseDropAdapter = null;
    protected List<DataFlavor>       dropFlavors  = new ArrayList<DataFlavor>();
    protected List<DataFlavor>       dragFlavors  = new ArrayList<DataFlavor>();

	public TreeDataJList( TreeDataListModel model )
	{
		super(model);
		this.treeDataModel = model;
		
		this.setFixedCellHeight(this.getFont().getSize()*2);
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE,this);
		dropTarget = new DropTarget(this, this);
		createMouseInputAdapter();
	}

	// DragGestureListener
	public void dragGestureRecognized(DragGestureEvent dge)
	{
		System.out.println("dragGestureRecognized");
		// find object at this x,y
		Point clickPoint = dge.getDragOrigin();
		int index = locationToIndex(clickPoint);
		if( index == -1 )
			return;
		Object target = getModel().getElementAt(index);
		Transferable trans = new RJLTransferable(target);
		draggedIndex = index;
		dragSource.startDrag(dge, Cursor.getDefaultCursor(), trans, this);
	}

	// DragSourceListener events
	public void dragDropEnd(DragSourceDropEvent dsde)
	{
		System.out.println("dragDropEnd()");
		dropTargetCell = null;
		draggedIndex = -1;
		repaint();
	}

	public void dragEnter(DragSourceDragEvent dsde)
	{
	}

	public void dragExit(DragSourceEvent dse)
	{
	}

	public void dragOver(DragSourceDragEvent dsde)
	{
	}

	public void dropActionChanged(DragSourceDragEvent dsde)
	{
	}

	// DropTargetListener events
	public void dragEnter(DropTargetDragEvent dtde)
	{
		System.out.println("dragEnter");
		if( dtde.getSource() != dropTarget )
			dtde.rejectDrag();
		else
		{
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
			System.out.println("accepted dragEnter");
		}

	}

	public void dragExit(DropTargetEvent dte)
	{
	}

	public void dragOver(DropTargetDragEvent dtde)
	{
		// figure out which cell it's over, no drag to self
		if( dtde.getSource() != dropTarget )
			dtde.rejectDrag();
		Point dragPoint = dtde.getLocation();
		int index = locationToIndex(dragPoint);
		if( index == -1 )
			dropTargetCell = null;
		else
			dropTargetCell = getModel().getElementAt(index);
		repaint();
	}

	public void drop(DropTargetDropEvent dtde)
	{
		System.out.println("drop()!");
		if( dtde.getSource() != dropTarget )
		{
			System.out.println("rejecting for bad source ("
					+ dtde.getSource().getClass().getName() + ")");
			dtde.rejectDrop();
			return;
		}
		Point dropPoint = dtde.getLocation();
		int index = locationToIndex(dropPoint);
		System.out.println("drop index is " + index);
		boolean dropped = false;
		try
		{
			if( (index == -1) || (index == draggedIndex) )
			{
				System.out.println("dropped onto self");
				dtde.rejectDrop();
				return;
			}
			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			System.out.println("accepted");

			// move items - note that indicies for insert will
			// change if [removed] source was before target
			System.out.println("drop " + draggedIndex + " to " + index);
			boolean sourceBeforeTarget = (draggedIndex < index);
			System.out.println("source is" + (sourceBeforeTarget ? "" : " not")
					+ " before target");
			System.out.println("insert at "
					+ (sourceBeforeTarget ? index - 1 : index));
			Object draggedObj = treeDataModel.getElementAt(draggedIndex);
			Object dropObj = treeDataModel.getElementAt(index);
			Treeable dragNode = (Treeable)draggedObj;
			Treeable dropNode = (Treeable)dropObj;
			treeDataModel.reparent(dragNode,dropNode);
			dropped = true;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		dtde.dropComplete(dropped);
	}

	public void dropActionChanged(DropTargetDragEvent dtde)
	{
	}

	class RJLTransferable implements Transferable
	{
		Object	object;

		public RJLTransferable(Object o)
		{
			object = o;
		}

		public Object getTransferData(DataFlavor df)
				throws UnsupportedFlavorException, IOException
		{
			if( isDataFlavorSupported(df) )
				return object;
			else
				throw new UnsupportedFlavorException(df);
		}

		public boolean isDataFlavorSupported(DataFlavor df)
		{
			return (df.equals(localObjectFlavor));
		}

		public DataFlavor[] getTransferDataFlavors()
		{
			return supportedFlavors;
		}
	}

	public void doAction(GhostActionable source)
	{
		// TODO Auto-generated method stub
		
	}

	public void setData(Object data)
	{
		// TODO Auto-generated method stub
		
	}

	public Object getData()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Object getDataForClass(Class classObj)
	{
		// TODO Auto-generated method stub
		return null;
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#createMouseDropAdapter()
     */
    public void createMouseInputAdapter()
    {
        mouseDropAdapter = new GhostMouseInputAdapter(UICacheManager.getGlassPane(), "action", this);
        addMouseListener(mouseDropAdapter);
        addMouseMotionListener(mouseDropAdapter);
    }

    /**
     * Returns the adaptor for tracking mouse drop gestures
     * @return Returns the adaptor for tracking mouse drop gestures
     */
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return mouseDropAdapter;
    }

	public BufferedImage getBufferedImage()
	{
//		BufferedImage bi = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
//		Graphics g = bi.getGraphics();
//		this.paint(g);
//		
//		return bi;
		
		BufferedImage bi = new BufferedImage(300,45,BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(0, 0, 300, 45);
		g.setColor(Color.BLACK);
		g.drawString("Imagine your image here", 20, 20);
		return bi;
	}

	public List<DataFlavor> getDropDataFlavors()
	{
		return dropFlavors;
	}

	public List<DataFlavor> getDragDataFlavors()
	{
		return dragFlavors;
	}
}

package edu.ku.brc.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
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

import javax.swing.JList;
import javax.swing.ListModel;

import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.dnd.GhostActionable;
import edu.ku.brc.specify.ui.dnd.GhostMouseInputAdapter;

/**
 * A custom {@link JList} with enhanced drag and drop features.
 *
 * @author jstewart
 * @version %I% %G%
 */
@SuppressWarnings("serial")
public class TreeDataJList extends JList implements DragSourceListener,
		DropTargetListener, DragGestureListener, GhostActionable
{

	/** A static handle to <code>DataFlavor.javaJVMLocalObjectMimeType</code>. */
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
	/** A static array holding only {@link #localObjectFlavor}. */
	protected static DataFlavor[]	supportedFlavors	=
											{ localObjectFlavor };
	/** */
	protected DragSource			dragSource;
	/** */
	protected DropTarget			dropTarget;
	/** */
	protected Object				dropTargetCell;
	/** */
	protected int					draggedIndex		= -1;
	/** */
	protected DragDropCallback	dragDropCallback;
	
    /** */
    protected GhostMouseInputAdapter  mouseDropAdapter = null;
    /** */
    protected List<DataFlavor>       dropFlavors  = new ArrayList<DataFlavor>();
    /** */
    protected List<DataFlavor>       dragFlavors  = new ArrayList<DataFlavor>();

	/**
	 * 
	 *
	 * @param model
	 */
	public TreeDataJList( ListModel model, DragDropCallback dragDropCallback )
	{
		super(model);
		this.dragDropCallback = dragDropCallback;
		
		this.setFixedCellHeight(this.getFont().getSize()*2);
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE,this);
		dropTarget = new DropTarget(this, this);
		createMouseInputAdapter();
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 * @param dge
	 */
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

	/**
	 *
	 *
	 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
	 * @param dsde
	 */
	public void dragDropEnd(DragSourceDropEvent dsde)
	{
		System.out.println("dragDropEnd()");
		dropTargetCell = null;
		draggedIndex = -1;
		repaint();
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
	 * @param dsde
	 */
	public void dragEnter(DragSourceDragEvent dsde)
	{
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
	 * @param dse
	 */
	public void dragExit(DragSourceEvent dse)
	{
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
	 * @param dsde
	 */
	public void dragOver(DragSourceDragEvent dsde)
	{
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
	 * @param dsde
	 */
	public void dropActionChanged(DragSourceDragEvent dsde)
	{
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
	 * @param dtde
	 */
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

	/**
	 *
	 *
	 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 * @param dte
	 */
	public void dragExit(DropTargetEvent dte)
	{
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
	 * @param dtde
	 */
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

	/**
	 *
	 *
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 * @param dtde
	 */
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
			Object draggedObj = getModel().getElementAt(draggedIndex);
			Object dropObj = getModel().getElementAt(index);
			dragDropCallback.dropOccurred(draggedObj,dropObj);
			dropped = true;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		dtde.dropComplete(dropped);
	}
	

	/**
	 *
	 *
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
	 * @param dtde
	 */
	public void dropActionChanged(DropTargetDragEvent dtde)
	{
	}

	
	/**
	 *
	 *
	 * @author jstewart
	 * @version %I% %G%
	 */
	class RJLTransferable implements Transferable
	{
		/** */
		Object	object;

		/**
		 *
		 *
		 * @param o
		 */
		public RJLTransferable(Object o)
		{
			object = o;
		}

		/**
		 *
		 *
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 * @param df
		 * @return
		 * @throws UnsupportedFlavorException
		 * @throws IOException
		 */
		public Object getTransferData(DataFlavor df)
				throws UnsupportedFlavorException, IOException
		{
			if( isDataFlavorSupported(df) )
				return object;
			else
				throw new UnsupportedFlavorException(df);
		}

		/**
		 *
		 *
		 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
		 * @param df
		 * @return
		 */
		public boolean isDataFlavorSupported(DataFlavor df)
		{
			return (df.equals(localObjectFlavor));
		}

		/**
		 *
		 *
		 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
		 * @return
		 */
		public DataFlavor[] getTransferDataFlavors()
		{
			return supportedFlavors;
		}
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.ui.dnd.GhostActionable#doAction(edu.ku.brc.specify.ui.dnd.GhostActionable)
	 * @param source
	 */
	public void doAction(GhostActionable source)
	{
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.ui.dnd.GhostActionable#setData(java.lang.Object)
	 * @param data
	 */
	public void setData(Object data)
	{
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.ui.dnd.GhostActionable#getData()
	 * @return
	 */
	public Object getData()
	{
		return null;
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
	 * @param classObj
	 * @return
	 */
	public Object getDataForClass(Class classObj)
	{
		return null;
	}

    /**
     *
     *
     * @see edu.ku.brc.specify.ui.dnd.GhostActionable#createMouseInputAdapter()
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

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.ui.dnd.GhostActionable#getBufferedImage()
	 * @return
	 */
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
		g.setColor(Color.BLUE);
		g.drawString("TODO: implement this.  draggedIndex = " + draggedIndex, 20, 20);
		return bi;
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.ui.dnd.GhostActionable#getDropDataFlavors()
	 * @return
	 */
	public List<DataFlavor> getDropDataFlavors()
	{
		return dropFlavors;
	}

	/**
	 *
	 *
	 * @see edu.ku.brc.specify.ui.dnd.GhostActionable#getDragDataFlavors()
	 * @return
	 */
	public List<DataFlavor> getDragDataFlavors()
	{
		return dragFlavors;
	}
}

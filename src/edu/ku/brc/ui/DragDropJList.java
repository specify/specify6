package edu.ku.brc.ui;

import java.awt.Cursor;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.ListModel;

/**
 * A custom {@link JList} with enhanced drag and drop features.
 *
 * @code_status Unknown (auto-generated)
 * @author jstewart
 * @version %I% %G%
 */
@SuppressWarnings("serial")
public class DragDropJList extends JList implements DragSourceListener,
		DropTargetListener, DragGestureListener
{

	/** A static handle to <code>DataFlavor.javaJVMLocalObjectMimeType</code>. */
	protected static DataFlavor	localObjectFlavor;
	static
	{
		try
		{
			localObjectFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
		}
		catch( ClassNotFoundException cnfe )
		{
			cnfe.printStackTrace();
		}
	}
	/** A static array holding only {@link #localObjectFlavor}. */
	protected static DataFlavor[]	supportedFlavors = {localObjectFlavor};
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
    //protected GhostMouseInputAdapter  mouseDropAdapter = null;
    /** */
    protected List<DataFlavor>       dropFlavors  = new ArrayList<DataFlavor>();
    /** */
    protected List<DataFlavor>       dragFlavors  = new ArrayList<DataFlavor>();

	/**
	 * 
	 *
	 * @param model
	 */
	public DragDropJList( ListModel model, DragDropCallback dragDropCallback )
	{
		super(model);
		this.dragDropCallback = dragDropCallback;
		
		this.setFixedCellHeight(this.getFont().getSize()*2);
		dragSource = new DragSource();
		int actions = DnDConstants.ACTION_MOVE | DnDConstants.ACTION_COPY | DnDConstants.ACTION_NONE;
		dragSource.createDefaultDragGestureRecognizer(this,actions,this);
		dropTarget = new DropTarget(this, this);
		//createMouseInputAdapter();
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
	 * @param dge
	 */
	public void dragGestureRecognized(DragGestureEvent dge)
	{
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
		if( shouldAccept(dtde) )
		{
			dtde.acceptDrag(dtde.getDropAction());
			System.out.println("Accepted dragEnter");
		}
		else
		{
			dtde.rejectDrag();
			System.out.println("Rejected dragEnter");
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
		if( shouldAccept(dtde) )
		{
			dtde.acceptDrag(dtde.getDropAction());
			System.out.println("Accepted dragOver");
		}
		else
		{
			dtde.rejectDrag();
			System.out.println("Rejected dragOver");
		}
	}
	
	protected boolean shouldAccept(DropTargetDragEvent dtde)
	{
		Point loc = dtde.getLocation();
		int index = locationToIndex(loc);
		Object droppedOn = getModel().getElementAt(index);
		Object dragged = null;
		try
		{
			dragged = dtde.getTransferable().getTransferData(localObjectFlavor);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		if( dragDropCallback.dropAcceptable(dragged,droppedOn,dtde.getDropAction()) )
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 *
	 *
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 * @param dtde
	 */
	public void drop(DropTargetDropEvent dtde)
	{
		Point loc = dtde.getLocation();
		int index = locationToIndex(loc);
		Object droppedOn = getModel().getElementAt(index);
		Object dragged = null;
		try
		{
			dragged = dtde.getTransferable().getTransferData(localObjectFlavor);
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		boolean dropped = false;
		if( dragDropCallback.dropAcceptable(dragged,droppedOn,dtde.getDropAction()) )
		{
			dropped = dragDropCallback.dropOccurred(dragged,droppedOn,dtde.getDropAction());
		}
		else
		{
			dropped = false;
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
}

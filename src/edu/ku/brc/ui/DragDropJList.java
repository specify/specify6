package edu.ku.brc.ui;

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

import javax.swing.JList;
import javax.swing.ListModel;

/**
 * A custom {@link JList} with enhanced drag and drop features.
 *
 * @code_status Beta
 * @author jstewart
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
	protected static DataFlavor[] supportedFlavors = {localObjectFlavor};
	/** */
	protected DragSource          dragSource;
	/** */
	protected DropTarget          dropTarget;
	/** */
	protected int                 draggedIndex     = -1;
	/** */
	protected DragDropCallback    dragDropCallback;

	/**
     * Constructor.
     * 
	 * @param model a list model managing the data to be displayed by this JList
	 * @param dragDropCallback the object to notify during drag and drop events
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
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
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
		dragSource.startDrag(dge, null, trans, this);
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
	 */
	public void dragDropEnd(DragSourceDropEvent dsde)
	{
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dragEnter(DragSourceDragEvent dsde)
	{
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
	 */
	public void dragExit(DragSourceEvent dse)
	{
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dragOver(DragSourceDragEvent dsde)
	{
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
	 */
	public void dropActionChanged(DragSourceDragEvent dsde)
	{
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragEnter(DropTargetDragEvent dtde)
	{
		if( shouldAccept(dtde) )
		{
			dtde.acceptDrag(dtde.getDropAction());
		}
		else
		{
			dtde.rejectDrag();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent dte)
	{
		// do nothing
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragOver(DropTargetDragEvent dtde)
	{
		if( shouldAccept(dtde) )
		{
			dtde.acceptDrag(dtde.getDropAction());
		}
		else
		{
			dtde.rejectDrag();
		}
	}
	
	/**
     * Determines if a drop should be accepted.
     * 
	 * @param dtde the {@link DropTargetDragEvent} in question
	 * @return true if a drop is acceptable, false otherwise
	 */
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
		return false;
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
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
	
	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent dtde)
	{
		// do nothing
	}
	
	/**
     * This class serves as a wrapper to allow any object to be transfered in a
     * drag and drop operation.
     * 
	 * @author jstewart
	 * @code_status Complete
	 */
	class RJLTransferable implements Transferable
	{
		/** The actual object being transfered. */
		Object	object;

		/**
         * Constructor.
         * 
		 * @param o the object being transfered.
		 */
		public RJLTransferable(Object o)
		{
			object = o;
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
		 */
		public Object getTransferData(DataFlavor df)
				throws UnsupportedFlavorException
		{
			if( isDataFlavorSupported(df) )
				return object;
			throw new UnsupportedFlavorException(df);
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
		 */
		public boolean isDataFlavorSupported(DataFlavor df)
		{
			return (df.equals(localObjectFlavor));
		}

		/* (non-Javadoc)
		 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
		 */
		public DataFlavor[] getTransferDataFlavors()
		{
			return supportedFlavors;
		}
	}
}

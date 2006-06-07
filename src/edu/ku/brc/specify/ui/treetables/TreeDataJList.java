package edu.ku.brc.specify.ui.treetables;

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

import javax.swing.JList;

import edu.ku.brc.specify.datamodel.Treeable;

@SuppressWarnings("serial")
public class TreeDataJList extends JList implements DragSourceListener,
		DropTargetListener, DragGestureListener
{

	static DataFlavor	localObjectFlavor;
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
	static DataFlavor[]	supportedFlavors	=
											{ localObjectFlavor };
	DragSource			dragSource;
	DropTarget			dropTarget;
	Object				dropTargetCell;
	int					draggedIndex		= -1;
	TreeDataListModel	treeDataModel;

	public TreeDataJList( TreeDataListModel model )
	{
		super(model);
		this.treeDataModel = model;
		
		this.setFixedCellHeight(this.getFont().getSize()*2);
		dragSource = new DragSource();
		dragSource.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_MOVE,this);
		dropTarget = new DropTarget(this, this);
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
}

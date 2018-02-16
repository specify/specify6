package edu.ku.brc.ui.tmanfe;

import edu.ku.brc.ui.UIHelper;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

/**
 * MouseAdapter for selecting rows by clicking and dragging on the Row Headers.
 */
public class RHCellMouseAdapter extends MouseAdapter
{
    protected static final Logger log = Logger.getLogger(RHCellMouseAdapter.class);
    protected RHCellOwner table;
    protected Hashtable<Integer, Boolean> selectionHash = new Hashtable<Integer, Boolean>();
    protected Hashtable<Integer, Boolean> doubleSelected = new Hashtable<Integer, Boolean>();
    protected int selAnchor = -1;
    protected int selLead   = -1;

    // these fields are important when a user ctrl-clicks a row and then drags
    protected boolean ctrlWasDown = false;
    protected boolean dragIsDeselecting = false;

    public RHCellMouseAdapter(final RHCellOwner table)
    {
        this.table = table;
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
     */
    @SuppressWarnings("synthetic-access")
    @Override
    public void mousePressed(MouseEvent e)
    {
        log.debug("mousePressed entered");
        log.debug("anchor: " + selAnchor);
        log.debug("lead   :" + selLead);

        if (table.getTable().isEditing())
        {
            table.getTable().getCellEditor().stopCellEditing();
        }

        RowHeaderLabel lbl = (RowHeaderLabel)e.getSource();
        int            row = lbl.getRowNum()-1;

        // toggle the selection state of the clicked row
        // and set the current row as the new anchor
        boolean ctrlDown = false;
        if (UIHelper.getOSType() == UIHelper.OSTYPE.MacOSX)
        {
            ctrlDown = e.isMetaDown();
        }
        else
        {
            ctrlDown = e.isControlDown();
        }
        if (ctrlDown)
        {
            ListSelectionModel selModel = table.getTable().getSelectionModel();

            // figure out the selection state of this row
            boolean wasSelected = table.getTable().getSelectionModel().isSelectedIndex(row);

            // toggle the selection state of this row
            if (wasSelected)
            {
                // deselect it
                selModel.removeSelectionInterval(row, row);
                dragIsDeselecting = true;
            }
            else
            {
                // select it and make it the new anchor
                selModel.addSelectionInterval(row, row);
                dragIsDeselecting = false;
            }
            selAnchor = row;
            selLead   = row;
            ctrlWasDown = true;
        }
        else if (e.isShiftDown())
        {
            ListSelectionModel selModel = table.getTable().getSelectionModel();

            selModel.removeSelectionInterval(selAnchor, selLead);
            selModel.addSelectionInterval(selAnchor, row);
            selLead = row;
        }
        else // no modifier keys are down
        {
            // just select the current row
            // and set it as the new anchor
            table.getTable().setRowSelectionInterval(row, row);

            table.getTable().setColumnSelectionInterval(0, table.getTable().getColumnCount()-1);
            selAnchor = selLead = row;

            table.setPrevRowSelInx(table.getTable().getSelectedRow());
            table.setPrevColSelInx(0);
        }

        table.setRowSelectionStarted(true);
        table.getTable().getSelectionModel().setValueIsAdjusting(true);

        log.debug("anchor: " + selAnchor);
        log.debug("lead   :" + selLead);
        log.debug("mousePressed exited");
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
     */
    @SuppressWarnings("synthetic-access")
    @Override
    public void mouseReleased(MouseEvent e)
    {
        log.debug("mouseReleased entered");
        log.debug("anchor: " + selAnchor);
        log.debug("lead   :" + selLead);

        // the user has released the mouse button, so we're done selecting rows
        //RowHeaderLabel lbl = (RowHeaderLabel)e.getSource();
        //int            row = lbl.getRowNum()-1;

        table.setRowSelectionStarted(false);
        table.getTable().getSelectionModel().setValueIsAdjusting(false);
        ctrlWasDown = false;
        dragIsDeselecting = false;

        log.debug("anchor: " + selAnchor);
        log.debug("lead   :" + selLead);
        log.debug("mouseReleased exited");
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mouseEntered(java.awt.event.MouseEvent)
     */
    @SuppressWarnings("synthetic-access")
    @Override
    public void mouseEntered(MouseEvent e)
    {
        // the user has clicked and is dragging, we are (de)selecting multiple rows...
        if (table.isRowSelectionStarted())
        {
            log.debug("mouseEntered entered");
            log.debug("anchor: " + selAnchor);
            log.debug("lead   :" + selLead);

            RowHeaderLabel lbl = (RowHeaderLabel)e.getSource();
            int row    = lbl.getRowNum()-1;
            selLead = row;
            if (ctrlWasDown)
            {
                if (dragIsDeselecting)
                {
                    table.getTable().removeRowSelectionInterval(selAnchor, row);
                }
                else
                {
                    table.getTable().addRowSelectionInterval(selAnchor, row);
                }
            }
            else
            {
                table.getTable().setRowSelectionInterval(selAnchor, row);
            }
            table.getTable().setColumnSelectionInterval(0, table.getTable().getColumnCount()-1);
            log.debug("anchor: " + selAnchor);
            log.debug("lead   :" + selLead);
            log.debug("mouseEntered exited");
        }
    }

    /**
     * Cleans up references.
     */
    public void cleanUp()
    {
        this.table = null;
    }
}

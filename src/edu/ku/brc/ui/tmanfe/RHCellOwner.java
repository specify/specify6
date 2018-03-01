package edu.ku.brc.ui.tmanfe;

import javax.swing.*;

public interface RHCellOwner {
    void setPrevRowSelInx(int inx);
    void setPrevColSelInx(int inx);
    void setRowSelectionStarted(boolean b);
    boolean isRowSelectionStarted();
    JTable getTable();
}

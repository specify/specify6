/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Jun 9, 2011
 *
 */
public class SGRFormPane extends JPanel implements FormPaneWrapper
{
    private FormPane          formPane;
    private SGRResultsForForm sgrPanel;
    private JSplitPane        splitPane;

    public SGRFormPane(final WorkbenchPaneSS workbenchPaneSS, Workbench workbench, boolean isReadOnly)
    {
        formPane = new FormPane(workbenchPaneSS, workbench, isReadOnly);
        sgrPanel = new SGRResultsForForm(workbenchPaneSS, workbench);
        
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, formPane.getPane(), sgrPanel.scrollPane);
        splitPane.setResizeWeight(0.2);
        
        Action prevRecord = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                workbenchPaneSS.getResultSetController().prevRecord();
            }
        };
        
        Action nextRecord = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                workbenchPaneSS.getResultSetController().nextRecord();                
            }
        };
        
        workbenchPaneSS.addRecordKeyMappings(splitPane, KeyEvent.VK_PAGE_UP, 
                "prev_record", prevRecord, InputEvent.SHIFT_DOWN_MASK);
        
        workbenchPaneSS.addRecordKeyMappings(splitPane, KeyEvent.VK_PAGE_DOWN, 
                "next_record", nextRecord, InputEvent.SHIFT_DOWN_MASK);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    @Override
    public void doAction(GhostActionable source)
    {
        formPane.doAction(source);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    @Override
    public void setData(Object data)
    {
        formPane.setData(data);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    @Override
    public Object getData()
    {
        return formPane.getData();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    @Override
    public Object getDataForClass(Class<?> classObj)
    {
        return formPane.getDataForClass(classObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseInputAdapter()
     */
    @Override
    public void createMouseInputAdapter()
    {
        formPane.createMouseInputAdapter();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getMouseInputAdapter()
     */
    @Override
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return formPane.getMouseInputAdapter();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getBufferedImage()
     */
    @Override
    public BufferedImage getBufferedImage()
    {
        return formPane.getBufferedImage();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDropDataFlavors()
     */
    @Override
    public List<DataFlavor> getDropDataFlavors()
    {
        return formPane.getDropDataFlavors();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDragDataFlavors()
     */
    @Override
    public List<DataFlavor> getDragDataFlavors()
    {
        return formPane.getDragDataFlavors();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setActive(boolean)
     */
    @Override
    public void setActive(boolean isActive)
    {
        formPane.setActive(isActive);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    @Override
    public void indexChanged(int newIndex)
    {
        formPane.indexChanged(newIndex);
        sgrPanel.indexChanged(newIndex);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    @Override
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        return formPane.indexAboutToChange(oldIndex, newIndex);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    @Override
    public void newRecordAdded()
    {
        formPane.newRecordAdded();
        sgrPanel.newRecordAdded();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#setWorkbench(edu.ku.brc.specify.datamodel.Workbench)
     */
    @Override
    public void setWorkbench(Workbench workbench)
    {
        formPane.setWorkbench(workbench);
        sgrPanel.setWorkbench(workbench);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#getScrollPane()
     */
    @Override
    public JSplitPane getPane()
    {
        return splitPane;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#getWorkbenchPane()
     */
    @Override
    public WorkbenchPaneSS getWorkbenchPane()
    {
        return formPane.getWorkbenchPane();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#getControlPropsBtn()
     */
    @Override
    public JButton getControlPropsBtn()
    {
        return formPane.getControlPropsBtn();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#cleanup()
     */
    @Override
    public void cleanup()
    {
        formPane.cleanup();
        sgrPanel.cleanup();
        splitPane.removeAll();
        removeAll();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#swapTextFieldType(edu.ku.brc.specify.tasks.subpane.wb.InputPanel, short)
     */
    @Override
    public void swapTextFieldType(InputPanel inputPanel, short fieldLen)
    {
        formPane.swapTextFieldType(inputPanel, fieldLen);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#aboutToShowHide(boolean)
     */
    @Override
    public void aboutToShowHide(boolean show)
    {
        formPane.aboutToShowHide(show);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#showingPane(boolean)
     */
    @Override
    public void showingPane(boolean show)
    {
        formPane.showingPane(show);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#copyDataFromForm()
     */
    @Override
    public void copyDataFromForm()
    {
        formPane.copyDataFromForm();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.FormPaneWrapper#updateValidationUI()
     */
    @Override
    public void updateValidationUI()
    {
        formPane.updateValidationUI();
    }
}

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
package edu.ku.brc.ui;

import java.util.Vector;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;

//import edu.ku.brc.ui.xxxTable.FindAllTable.FindAllTableSearchable;

/**
 * @author megkumin
 *
 * @code_status Alpha
 *
 * Created Date: Mar 12, 2007
 *
 */
@SuppressWarnings("serial")
public class SearchableJXTable  extends JXTable
{
    private static final Logger log                     = Logger.getLogger(SearchableJXTable.class);

    //SearchReplacePanel findPanel = new SearchReplacePanel(this);

    public SearchableJXTable(TableModel dm)
    {
        super(dm);
        //setSearchableJXTableProperties();
    }
//    
//    public SearchReplacePanel getFindReplacePanel()
//    {
//        log.debug("Getting mySearchPanel");
//        return findPanel;
//    }
    
    public SearchableJXTable()
    {
        super();
        //setSearchableJXTableProperties();
    }

    public SearchableJXTable(int arg0, int arg1)
    {
        super(arg0, arg1); 
        //setSearchableJXTableProperties();
    }

    public SearchableJXTable(TableModel arg0, TableColumnModel arg1, ListSelectionModel arg2)
    {
        super(arg0, arg1, arg2);
       // setSearchableJXTableProperties();
    }

    public SearchableJXTable(TableModel arg0, TableColumnModel arg1)
    {
        super(arg0, arg1);
        //setSearchableJXTableProperties();
    }

    public SearchableJXTable(Vector<?> arg0, Vector<?> arg1)
    {
        super(arg0, arg1);
        //setSearchableJXTableProperties();
    }

    public SearchableJXTable(Object[][] arg0, Object[] arg1)
    {
        super(arg0, arg1);
        //setSearchableJXTableProperties();
    }
    
//    public void setSearchableJXTableProperties()
//    {
//    	log.debug("setSearchableJXTableProperties");
////        addMouseListener(new MouseAdapter() {
////            @Override
////            public void mouseEntered(MouseEvent arg0)
////            {
////                // TODO Auto-generated method stub
////                int col = getSelectedColumn();
////                int row = getSelectedRow();                
////                super.mouseEntered(arg0);
////                if(row!=-1||col!=-1)
////                System.out.println("mouseEntered: " + getValueAt(row, col));
////            }
////
////            public void mouseClicked(MouseEvent e) {
////                //super.mouseClicked(e);
////                //printDebugData(table);
////                int col = getSelectedColumn();
////                int row = getSelectedRow();
////                System.out.println("addMouseListener: " +getValueAt(row, col));
////            }
////        });
//        //table.
////        getSelectionModel().addListSelectionListener(new ListSelectionListener()
////        {
////            public void valueChanged(ListSelectionEvent lse)
////            {
////                if (lse.getValueIsAdjusting())
////                    return;
////                int row = getSelectedRow(), col = getSelectedColumn();
////            }
////        });
//        setColumnSelectionAllowed(true);
//        setRowSelectionAllowed(true);
//        setCellSelectionEnabled(true);
    
    
//        
//        //override find dialog shiped with Jtable
//        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), "find");
//        getActionMap().put("find", new AbstractAction()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                System.out.println("cont f hit");
//                //findPanel.createFindPanel();
//                findPanel.createFindAndReplacePanel();
//                findPanel.showFindAndReplacePanel(true);
//                findPanel.repaint();
//                //findPanel.p
//            }
//        });
//        
////        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK), "replace");
////        getActionMap().put("replace", new AbstractAction()
////        {
////            public void actionPerformed(ActionEvent e)
////            {
////                System.out.println("cont r hit");
////                findPanel.createFindAndReplacePanel();
////                findPanel.showFindAndReplacePanel(true);
////                findPanel.repaint();
////            }
////        });
//    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }
}

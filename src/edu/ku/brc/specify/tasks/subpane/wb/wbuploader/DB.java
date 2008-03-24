/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraph;
import edu.ku.brc.specify.tasks.subpane.wb.graph.DirectedGraphException;
import edu.ku.brc.specify.tasks.subpane.wb.schema.DBSchema;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Table;
import edu.ku.brc.ui.CustomFrame;
import edu.ku.brc.ui.UIHelper;

/**
 * @author timo
 * 
 */
public class DB 
{
    protected DBSchema                           schema;
    protected DirectedGraph<Table, Relationship> dbGraph;
    
    /**
     * Probably will be dropping bogusStorages, but keeping around for now in case it comes handy for validation or something 
     */
    protected boolean useBogusStorage = false;
    protected Map<String, Vector<Vector<String>>>      bogusStorages;

    /**
     * @return the schema
     */
    public DBSchema getSchema()
    {
        return schema;
    }

    public DB() throws DirectedGraphException
    {
        schema = new DBSchema(DBTableIdMgr.getInstance(), this);
        dbGraph = schema.getGraph();
        if (!useBogusStorage)
        {
            bogusStorages = null;
        }
        else
        {
            bogusStorages = new HashMap<String, Vector<Vector<String>>>();
            for (Table t : schema.getTables())
            {
                bogusStorages.put(t.getName(), new Vector<Vector<String>>());
            }
        }
    }

    /**
     * @return the dbGraph
     */
    public DirectedGraph<Table, Relationship> getGraph()
    {
        return dbGraph;
    }

    /**
     * @param t
     * @return the bogus storage for table t.
     */
    protected Vector<Vector<String>> getBogusStorage(Table t)
    {
        return bogusStorages.get(t.getName());
    }

    /**
     * @param t
     * @return a blank bogus record for table t.
     */
    public BogusRecord getBogusRecord(Table t)
    {
        return new BogusRecord(t, getBogusStorage(t));
    }

    /**
     * Dumps contents of bogus storage to System.out
     */
    public void dumpBogus()
    {
        for (Table t : schema.getTables())
        {
            if (getBogusStorage(t).size() > 0)
            {
                System.out.println();
                System.out.println(t.getName());
                TreeMap<Integer, String> header = new TreeMap<Integer, String>();
                for (Field f : t.getFields())
                {
                    header.put(f.getColumnIndex(), f.getName());
                 }
                for (String h : header.values())
                {
                    System.out.print(h + ", ");
                }
                System.out.println();
                Vector<Vector<String>> bogus = getBogusStorage(t);
                for (Vector<String> row : bogus)
                {
                    for (String val : row)
                    {
                        System.out.print(val + ", ");
                    }
                    System.out.println();
                }
            }
        }
    }

    /**
     * opens window with list of non-empty bogus storages.
     */
    public void viewBogusData()
    {
        new BogusViewer().viewBogus();
    }
    
    public class BogusViewer implements ActionListener
    {
        protected boolean showAllFields = false;
        protected Map<String, Vector<Vector<String>>> myStorages;
        protected Vector<CustomFrame> tblForms;
        
        public BogusViewer(Map<String, Vector<Vector<String>>> myStorages)
        {
            this.myStorages = myStorages;
            tblForms = new Vector<CustomFrame>();
            
        }
        
        public BogusViewer()
        {
            myStorages = null;
        }
        
        public void actionPerformed(ActionEvent e)
        {
           showAllFields = !showAllFields;   
        }
        public void viewBogus()
        {
            JPanel mainPane = new JPanel(new BorderLayout());
            JLabel msg = createLabel("Tables containing imported data"); // I18N ??
            msg.setFont(msg.getFont().deriveFont(Font.BOLD));
            mainPane.add(msg, BorderLayout.NORTH);
            JCheckBox cb = createCheckBox("show all fields"); // I1bN ??
            cb.addActionListener(this);
            mainPane.add(cb, BorderLayout.SOUTH);
            Vector<String> tblNames = new Vector<String>();

            if (myStorages == null)
            {
                for (Table t : schema.getTables())
                {
                    if (getBogusStorage(t).size() > 0)
                    {
                        tblNames.add(t.getName());
                    }
                }
            }
            else
            {
                tblNames.addAll(myStorages.keySet());
            }
            
            JList mods = new JList(tblNames);
            mods.addMouseListener(new MouseListener()
            {
                public void mouseClicked(MouseEvent me)
                {
                    if (me.getClickCount() == 2)
                    {
                        viewBogusTbl(((JList) me.getSource()).getSelectedValue().toString(), !showAllFields);
                    }
                }

                public void mouseEntered(MouseEvent me)
                {
                    //who cares?
                }

                public void mouseExited(MouseEvent me)
                {
                    //who cares?
                }

                public void mousePressed(MouseEvent me)
                {
                    //who cares?
                }

                public void mouseReleased(MouseEvent me)
                {
                    //who cares?
                }
            });
            mainPane.add(new JScrollPane(mods), BorderLayout.CENTER);

            CustomFrame cwin = new CustomFrame("Imported Data", CustomFrame.OK_BTN, mainPane);
            cwin.addWindowListener(new WindowListener()
            {
                public void windowActivated(WindowEvent e)
                { //documented
                }
                public void windowIconified(WindowEvent e)
                {//documented
                }
                public void windowDeiconified(WindowEvent e)
                {//documented
                }
                public void windowDeactivated(WindowEvent e)
                {//documented
                }
                public void windowOpened(WindowEvent e)
                {//documented
                }
                public void windowClosed(WindowEvent e)
                {
                    System.out.println("closed");
                }
                public void windowClosing(WindowEvent e)
                {
                    System.out.println("closing");
                }
                
////                    if (imp != null && e.getNewState() == WindowEvent.WINDOW_CLOSED)
////                    {
////                        imp.undoUpload();
////                    }
//                    
//                    if (UIRegistry.displayConfirm("WB_CANCEL_UPLOAD_TITLE", 
//                            "WB_CANCEL_UPLOAD_MSG", 
//                            "OK",
//                            "Cancel", 
//                            JOptionPane.QUESTION_MESSAGE))
//                    {
//                        System.out.println("OK");
//                    }
//                    else
//                    {
//                        System.out.println("Cancel");
//                    }
//                }
            });
            UIHelper.centerAndShow(cwin);
        }

        protected boolean fldHasData(String tblName, Integer fldIdx)
        {
            for (Vector<String> row : getBogusStorage(schema.getTable(tblName)))
            {
                if (row.get(fldIdx) != null) { return true; }
            }
            return false;
        }

        protected CustomFrame findTblForm(String tblName)
        {
            for (CustomFrame result : tblForms)
            {
                if (result.getTitle().equals(tblName))
                {
                    return result;
                }
            }
            return null;
        }
        public void viewBogusTbl(String tblName, boolean hideEmptyFields)
        {
            CustomFrame cwin = findTblForm(tblName);
            if (cwin != null)
            {
                if (!cwin.isVisible())
                {
                    cwin.setVisible(true);
                }
                if (cwin.getExtendedState() == Frame.ICONIFIED)
                {
                    cwin.setExtendedState(Frame.NORMAL);
                }
                cwin.toFront();
                return;
            }
            JPanel mainPane = new JPanel(new BorderLayout());
            JTable table;
            Vector<String> fldNames = new Vector<String>();
            Vector<Vector<String>> vals;
            if (myStorages == null)
            {
                TreeMap<Integer, String> header = new TreeMap<Integer, String>();
                for (Field fld : schema.getTable(tblName).getFields())
                {
                    header.put(fld.getColumnIndex(), fld.getName());
                }
                for (String fldName : header.values())
                {
                    fldNames.add(fldName);
                }
            }
            else
            {
                if (myStorages.containsKey(tblName))
                {
                    fldNames.addAll(myStorages.get(tblName).get(0));
                }
                else
                {
                    return;
                }
            }
            
            if (myStorages == null)
            {
                vals = (getBogusStorage(schema.getTable(tblName)));
            }
            else
            {
                vals = new Vector<Vector<String>>();
                vals.addAll(myStorages.get(tblName));
                vals.remove(0);
            }
            table = new JTable(vals, fldNames);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int col = 0; col < table.getColumnModel().getColumnCount(); col++)
            {
                table.getColumnModel().getColumn(col).sizeWidthToFit();
            }
            if (hideEmptyFields)
            {
                for (int col = table.getModel().getColumnCount() - 1; col >= 0; col--)
                {
                    boolean hasData = false;
                    for (int row = 0; row < table.getModel().getRowCount(); row++)
                    {
                        Object valObj = table.getModel().getValueAt(row, col);
                        String val = valObj == null ? null : valObj.toString();
                        if (val != null && !val.equals(""))
                        {
                            hasData = true;
                            break;
                        }
                    }
                    if (!hasData)
                    {
                        table.removeColumn(table.getColumnModel().getColumn(col));
                    }
                }
            }

            //mainPane.add(table, BorderLayout.CENTER);
            mainPane.add(new JScrollPane(table), BorderLayout.CENTER);
            cwin = new CustomFrame(tblName, CustomFrame.OK_BTN, mainPane);
            tblForms.add(cwin);
            UIHelper.centerAndShow(cwin);
        }
        
        public void closeViewers()
        {
            for (CustomFrame frm : tblForms)
            {
                if (frm.isVisible())
                {
                    frm.setVisible(false);
                }
                frm.dispose();
            }
        }    
    }
    public class BogusRecord
    {
        protected Table            table;
        protected Vector<String>         values;
        protected Vector<Vector<String>> bogusStorage;
        protected Set<Integer> assignedFldIdxs;

        public BogusRecord(Table table)
        {
            this.table = table;
            values = new Vector<String>(this.table.getFields().size());
            for (int v = 0; v < this.table.getFields().size(); v++)
            {
                values.add(null);
            }
            this.bogusStorage = new Vector<Vector<String>>();
            assignedFldIdxs = new HashSet<Integer>();
        }

        public BogusRecord(Table table, Vector<Vector<String>> bogus)
        {
            this.table = table;
            values = new Vector<String>(this.table.getFields().size());
            for (int v = 0; v < this.table.getFields().size(); v++)
            {
                values.add(null);
            }
            this.bogusStorage = bogus;
            assignedFldIdxs = new HashSet<Integer>();
        }

        protected int getColIdx(String fldName)
        {
            Field fld = table.getField(fldName);
            if (fld != null)
            {
                return fld.getColumnIndex();
            }
            return -1;
        }

        public void setField(String fldName, String value)
        {
            int idx = getColIdx(fldName);
            values.set(idx, value);
            assignedFldIdxs.add(idx);
        }

        public String getField(final String fldName)
        {
            return values.get(getColIdx(fldName));
        }
        protected int getKeyIdx()
        {
            if (table.getKey() != null)
            {
                return table.getKey().getColumnIndex();
            }
            return 0; //what the hey?
            
        }
        public Integer writeValues() throws DuplicateMatchException
        {
            Vector<Integer> matches = findMatches(values, true);
            if (matches.size() == 0)
            {
                bogusStorage.add(values);
                values.set(getKeyIdx(), String.valueOf(bogusStorage.size() - 1));
                Integer result = new Integer(values.get(getKeyIdx()));
                values = new Vector<String>(this.table.getFields().size());
                for (int v = 0; v < this.table.getFields().size(); v++)
                {
                    values.add(null);
                }
                return result;
            }
            if (matches.size() == 1)
            {
                return matches.get(0);
            }
            throw new DuplicateMatchException(matches);
        }

        public class DuplicateMatchException extends Exception
        {
            protected Vector<Integer> matchIds;
            DuplicateMatchException(final Vector<Integer> matchIds)
            {
                this.matchIds = matchIds;
            }
            /**
             * @return the matchIds
             */
            public final Vector<Integer> getMatchIds()
            {
                return matchIds;
            }
        }
        
        public Vector<Integer> findMatches(Vector<String> recVals, boolean compareAssignedFldsOnly)
        {
            Vector<Integer> result = new Vector<Integer>();
            for (Vector<String> rec : bogusStorage)
            {
                if (recsEqual(recVals, rec, compareAssignedFldsOnly))
                {
                    result.add(new Integer(rec.get(getKeyIdx())));
                }
            }
            return result;
        }


        protected boolean recValsEqual(String v1, String v2)
        {
            if (v1 != null || v2 != null)
            {
                if (v1 == null ^ v2 == null) { return false; }
                if (!v1.equals(v2)) { return false; }
            }
            return true;
        }
        
        protected boolean recsEqual(Vector<String> rec1, Vector<String> rec2, boolean compareAssignedFldsOnly)
        {
            if (compareAssignedFldsOnly)
            {
                for (Integer f : assignedFldIdxs)
                {
                    if (!recValsEqual(rec1.get(f), rec2.get(f))) { return false; }
                }
                return true;    
            }
            for (int f = 0; f < rec1.size(); f++)
            {
                if (f != getKeyIdx())
                {
                    if (!recValsEqual(rec1.get(f), rec2.get(f))) { return false; }
                }
            }
            return true;
        }

    }
}

/* Copyright (C) 2012, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.config;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.CollectingEventAttachment;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.ConservDescriptionAttachment;
import edu.ku.brc.specify.datamodel.ConservEventAttachment;
import edu.ku.brc.specify.datamodel.DNASequencingRunAttachment;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.FieldNotebookAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPageAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSetAttachment;
import edu.ku.brc.specify.datamodel.LoanAttachment;
import edu.ku.brc.specify.datamodel.LocalityAttachment;
import edu.ku.brc.specify.datamodel.ObjectAttachmentIFace;
import edu.ku.brc.specify.datamodel.PermitAttachment;
import edu.ku.brc.specify.datamodel.PreparationAttachment;
import edu.ku.brc.specify.datamodel.RepositoryAgreementAttachment;
import edu.ku.brc.specify.datamodel.TaxonAttachment;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Feb 1, 2012
 *
 */
public class FixAttachments
{

    /**
     * 
     */
    public FixAttachments()
    {
        super();
    }
    
    /**
     * @param resultsHashMap
     * @param tableHash
     * @param totalFiles
     */
    private void reattachFiles(final HashMap<Integer, Vector<Object[]>> resultsHashMap,
                               final HashMap<Integer, AttchTableModel> tableHash,
                               final int totalFiles)
    {
        final String CNT  = "CNT";
        final SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            int filesCnt = 0;
            
            @Override
            protected Integer doInBackground() throws Exception
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                if (session != null)
                {
                    try
                    {
                        for (int tblId : resultsHashMap.keySet())
                        {
                            AttchTableModel model = tableHash.get(tblId);
                            int cnt = 0;
                            for (int r=0;r<model.getRowCount();r++)
                            {
                                if (model.isRecoverable(r))
                                {
                                    Thread.sleep(100);
                                    
                                    session.beginTransaction();
                                    Integer    attachID   = model.getAttachmentId(r);
                                    Attachment attachment = session.get(Attachment.class, attachID);
                                    AttachmentUtils.getAttachmentManager().setStorageLocationIntoAttachment(attachment, false);
                                    try
                                    {
                                    	attachment.storeFile(true); // false means do not display an error dialog
                                    	session.saveOrUpdate(attachment);
                                    	session.commit();
                                    	model.setRecovered(r, true);
                                    	filesCnt++;
                                    	
                                    } catch (IOException ex)
                                    {
                                    	 session.rollback();
                                    }
                                }
                                cnt++;
                                firePropertyChange(CNT, 0, (int)((double)cnt / (double)totalFiles * 100.0));
                            }
                        }
                    } catch (Exception ex)
                    {
                        session.rollback();
                        
                    } finally
                    {
                        session.close();
                    }
                }
                return null;
            }

            @Override
            protected void done()
            {
                UIRegistry.clearSimpleGlassPaneMsg();
                UIRegistry.displayInfoMsgDlg(String.format("Files recovered: %d / %d", filesCnt, totalFiles));
                
                File file = produceSummaryReport(resultsHashMap,tableHash, totalFiles);
                if (file != null)
                {
                    try
                    {
                        AttachmentUtils.openFile(file);
                    } catch (Exception e) {}
                }
                
                if (getNumberofBadAttachments() == 0)
                {
                    AppPreferences.getGlobalPrefs().putBoolean("CHECK_ATTCH_ERR", true);
                }
                super.done();
            }
        };
        
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(String.format("Recovering %d files.", totalFiles), 24);
        glassPane.setProgress(0);
        
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (CNT.equals(evt.getPropertyName())) 
                        {
                            glassPane.setProgress((Integer)evt.getNewValue());
                        }
                    }
                });
        
        worker.execute();
    }
    
    /**
     * @param data
     * @return
     */
    private String getVal(final Object data)
    {
        String str = "&nbsp;";
        if (data != null)
        {
            if (data instanceof Boolean)
            {
                if (((Boolean)data))
                {
                    str = "<CENTER>Y</CENTER>";
                }
            } else
            {
                str = data.toString();
            }
        }
        return str;
    }
    
    /**
     * @param resultsHashMap
     * @param tableHash
     * @param totalFiles
     */
    private void doAttachmentRefCleanup(final HashMap<Integer, Vector<Object[]>> resultsHashMap,
                                        final HashMap<Integer, AttchTableModel> tableHash,
                                        final int totalFiles)
    {
        final int numAttachs = getNumberofBadAttachments();
        
        final String CNT  = "CNT";
        final SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            int filesCnt = 0;
            int errs     = 0;
            
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    for (int tblId : resultsHashMap.keySet())
                    {
                        DBTableInfo     ti    = DBTableIdMgr.getInstance().getInfoById(tblId);
                        AttchTableModel model = tableHash.get(tblId);
                        for (int r=0;r<model.getRowCount();r++)
                        {
                            int attachId     = model.getAttachmentId(r);
                            int attachJoinId = model.getAttachmentJoinId(r);
                            
                            String sql = String.format("DELETE FROM %s WHERE %s = %d", ti.getName(), ti.getIdColumnName(), attachJoinId);
                            int rv = BasicSQLUtils.update(sql);
                            if (rv == 1)
                            {
                                rv = BasicSQLUtils.update("DELETE FROM attachment WHERE AttachmentID = "+attachId);
                                if (rv == 1)
                                {
                                    filesCnt++;
                                } else
                                {
                                    errs++;
                                }
                            } else
                            {
                                errs++;
                            }
                            
                            firePropertyChange(CNT, 0, (int)((double)filesCnt / (double)totalFiles * 100.0));
                        }
                    }
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done()
            {
                UIRegistry.clearSimpleGlassPaneMsg();
                UIRegistry.displayInfoMsgDlg(String.format("Attachments removed: %d / %d", filesCnt, numAttachs));
                
                if (errs > 0)
                {
                    UIRegistry.displayErrorDlg(String.format("There were %d errors when deleting the attachments.", errs));
                } else
                {
                    if (getNumberofBadAttachments() == 0)
                    {
                        AppPreferences.getGlobalPrefs().putBoolean("CHECK_ATTCH_ERR", false);
                    }
                }
                super.done();
            }
        };
        
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(String.format("Removing %d attachments.", numAttachs), 24);
        glassPane.setProgress(0);
        
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (CNT.equals(evt.getPropertyName())) 
                        {
                            glassPane.setProgress((Integer)evt.getNewValue());
                        }
                    }
                });
        
        worker.execute();        
    }
    
    /**
     * @param resultsHashMap
     * @param tableHash
     * @param totalFiles
     */
    private File produceSummaryReport(final HashMap<Integer, Vector<Object[]>> resultsHashMap,
                                      final HashMap<Integer, AttchTableModel> tableHash,
                                      final int totalFiles)
    {
        String path = UIRegistry.getAppDataDir() + File.separator + "att_rec_summary.html";
        try
        {
            TableWriter tw = new TableWriter(path, "Attachment Recovery Summary", true);
            boolean first = true;
            for (int tblId : resultsHashMap.keySet())
            {
                DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tblId);
                AttchTableModel model = tableHash.get(tblId);
                if (!first)
                {
                    tw.endTable();
                    tw.log("<BR/>");
                }
                first = false;
                tw.startTable();
                tw.log("<TR><TD COLSPAN=\"6\">"+ti.getTitle()+"</TD></TR>");
                tw.logHdr(model.getHeaders());
                for (int r=0;r<model.getRowCount();r++)
                {
                    tw.print(TableWriter.TR);
                    for (int i=0;i<5;i++)
                    {
                        tw.logTDCls(null, getVal(model.getValueAt(r, i)));
                    }
                    tw.logTDCls(null, getVal(model.getValueAt(r, 5)));
                    tw.print(TableWriter.TR_);
                }
            }
            tw.endTable();
            tw.close();
            
            return new File(path);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            UIRegistry.displayErrorDlg("Unable to create Summary report.");
        }
        return null;
    }
    
    /**
     * @param tblId
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    private String getIdentityTitle(DataProviderSessionIFace session, final DBTableInfo ti, final int id)
    {
        try
        {
            ObjectAttachmentIFace<DataModelObjBase> dataObj = (ObjectAttachmentIFace<DataModelObjBase>)session.get(ti.getClassObj(), id);
            if (dataObj != null)
            {
                return dataObj.getObject().getIdentityTitle();
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return "N/A";
    }
    
    /**
     * @param btn
     * @param url
     */
    private void hookupAction(final JButton btn, 
                              final URI uri)
    {
        for (ActionListener al : btn.getActionListeners())
        {
            btn.removeActionListener(al);
        }
        
        if (uri != null)
        {
            btn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    try
                    {
                        AttachmentUtils.openURI(uri);
                    } catch (Exception e) {}
                }
            });
        }
    }

    /**
     * @param agentId
     * @param tableId
     * @param tblTypeHash
     * @return
     * @throws Exception
     */
    private Vector<Object[]> getImageData(final int agentId, 
                                          final int tableId,
                                          final HashMap<Integer, String> tblTypeHash) throws Exception
    {
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);
        
        String sql = String.format("SELECT a.AttachmentID, a.AttachmentLocation, a.OrigFilename, ag.AgentID, %s " +
                     "FROM attachment a INNER JOIN %s x ON a.AttachmentID = x.AttachmentID " +
                     "INNER JOIN agent ag ON a.CreatedByAgentID = ag.AgentID " +
                     "WHERE (a.AttachmentLocation IS NULL OR a.AttachmentLocation LIKE 'xxx.att.%c')",// AND ag.AgentID = %d", 
                     ti.getIdColumnName(), ti.getName(), '%', agentId);
        
        String title = ti.getTitle();
        if (ti.getRelationshipByName("division") != null)
        {
            Division div = AppContextMgr.getInstance().getClassObject(Division.class);
            sql += " AND x.DivisionID = " + div.getId();
            title += " (Division Level)";//div.getName());
            
        } else if (ti.getFieldByColumnName("CollectionMemberID") != null)
        {
            Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            sql += " AND x.CollectionMemberID = " + collection.getId();
            title += " (Collection Level)";//tblTypeHash.put(tableId, collection.getCollectionName());
            
        } else if (ti.getRelationshipByName("discipline") != null)
        {
            Discipline dsp = AppContextMgr.getInstance().getClassObject(Discipline.class);
            sql += " AND x.DisciplineID = " + dsp.getId();
            title += " (Discipline Level)";//tblTypeHash.put(tableId, "Discipline: "+dsp.getName());
        } else
        {
            //System.err.println("Error: "+title);
            title += " (Global Level)";
        }
        tblTypeHash.put(tableId, title);
        System.out.println(sql);
        return BasicSQLUtils.query(sql);
    }
    
    /**
     * @return
     */
    private int getNumberofBadAttachments()
    {
        String sql = "SELECT COUNT(*) FROM attachment WHERE AttachmentLocation LIKE 'xxx.att%'";
        return BasicSQLUtils.getCountAsInt(sql);
    }
    
    /**
     * 
     */
    public void checkForBadAttachments()
    {
        int count = getNumberofBadAttachments();
        if (count == 0)
        {
            AppPreferences.getGlobalPrefs().putBoolean("CHECK_ATTCH_ERR", false);
            return;
        }
        
        URL url = null;
        JEditorPane htmlPane;
        try
        {
            url = new URL("http://files.specifysoftware.org/attachment_recovery.html");
            htmlPane = new JEditorPane(url);
        } catch (Exception e)
        {
            e.printStackTrace();
            htmlPane = new JEditorPane("text/html", "<html><body><h1>Network Error - You must have a network conneciton to get the instructions.</H1></body>"); //$NON-NLS-1$
        }
        JScrollPane scrollPane = UIHelper.createScrollPane(htmlPane);
        htmlPane.setEditable(false);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        CustomDialog infoDlg = new CustomDialog((Dialog)null, "Recovery Information", true, CustomDialog.OKCANCEL, panel);
        
        infoDlg.setCancelLabel("Close");
        infoDlg.setOkLabel("Print in Browser");
        infoDlg.createUI();
        infoDlg.setSize(1024,600);
        try
        {
            hookupAction(infoDlg.getOkBtn(), url != null ? url.toURI() : null);
            infoDlg.setVisible(true);
        	
        } catch (Exception ex)
        {
        	
        }

        
        final String CNT  = "CNT";
        final SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            int totalFiles = 0;
            
            HashMap<Integer, Vector<Object[]>> resultsHashMap = new HashMap<Integer, Vector<Object[]>>();
            HashMap<Integer, String>           tblTypeHash    = new HashMap<Integer, String>();
            HashMap<Integer, String>           agentHash = new HashMap<Integer, String>();
            HashMap<Integer, AttchTableModel>  tableHash = new HashMap<Integer, AttchTableModel>();
            ArrayList<JTable>                  tableList   = new ArrayList<JTable>();
            ArrayList<Integer>                 tableIdList = new ArrayList<Integer>();

            @Override
            protected Integer doInBackground() throws Exception
            {
                DataProviderSessionIFace session = null;

                try
                {
                    // This doesn't need to include the new attachments
                    int[] tableIds = 
                    { 
                            AccessionAttachment.getClassTableId(),           AgentAttachment.getClassTableId(), 
                            CollectingEventAttachment.getClassTableId(),     CollectionObjectAttachment.getClassTableId(), 
                            ConservDescriptionAttachment.getClassTableId(),  ConservEventAttachment.getClassTableId(), 
                            DNASequencingRunAttachment.getClassTableId(),    FieldNotebookAttachment.getClassTableId(), 
                            FieldNotebookPageAttachment.getClassTableId(),   FieldNotebookPageSetAttachment.getClassTableId(), 
                            LoanAttachment.getClassTableId(),                LocalityAttachment.getClassTableId(), 
                            PermitAttachment.getClassTableId(),              PreparationAttachment.getClassTableId(), 
                            RepositoryAgreementAttachment.getClassTableId(), TaxonAttachment.getClassTableId()
                    };
            
                    Agent userAgent  = AppContextMgr.getInstance().getClassObject(Agent.class);
                    
                    int totFiles = 0;
                    firePropertyChange(CNT, 0, 0);
                    int cnt = 0;
                    for (int tableId : tableIds)
                    {
                        Vector<Object[]> results = getImageData(userAgent.getId(), tableId, tblTypeHash);
                        if (results != null && results.size() > 0)
                        {
                            resultsHashMap.put(tableId, results);
                            totFiles += results.size();
                            //System.out.println(tableId+"  ->  "+results.size());
                        }
                        firePropertyChange(CNT, 0, (int)((double)cnt / (double)tableIds.length * 100.0));
                        cnt++;
                    }
                    
                    if (resultsHashMap.size() == 0) // Shouldn't happen
                    {
                        return null;
                    }
                    
                    session = DataProviderFactory.getInstance().createSession();
                    
                    firePropertyChange(CNT, 0, 0);
                    int i = 1;
                    for (int tblId : resultsHashMap.keySet())
                    {
                        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tblId);
                        
                        Vector<Object[]> dataRows = new Vector<Object[]>();
                        Vector<Object[]> results = resultsHashMap.get(tblId);
                        for (Object[] row : results)
                        {
                            Integer agentId = (Integer)row[3];
                            String userName = agentHash.get(agentId); 
                            if (userName == null)
                            {
                                userName = BasicSQLUtils.querySingleObj("SELECT su.Name FROM agent a INNER JOIN specifyuser su ON a.SpecifyUserID = su.SpecifyUserID WHERE a.AgentID = "+row[3]);
                                agentHash.put(agentId, userName);
                            }
                            //userName = i == 1 ? "bill.johnson" : "joe.smith";
                            
                            int attachJoinID = (Integer)row[4];
                            String identTitle = getIdentityTitle(session, ti, attachJoinID);
                            
                            String fullPath   = (String)row[2];
                            //fullPath = StringUtils.replace(fullPath, "darwin\\", "darwin2\\");
                            
                            //boolean doesExist = (new File(fullPath)).exists() && i != 1;
                            boolean doesExist = (new File(fullPath)).exists();
                            //String str        = i != 1 ? "/Users/joe/Desktop/xxx.png" : "/Users/bill/Desktop/xxx.png";
                            //String fullPath   = FilenameUtils.getFullPath(str) + String.format("DSC_%05d.png", i);
                            
                            Object[] rowObjs  = new Object[8];
                            rowObjs[0] = StringUtils.isEmpty(identTitle) ? "" : (identTitle.length() > 30 ? identTitle.substring(0, 30) + "..." : identTitle);
                            rowObjs[1] = FilenameUtils.getName(fullPath);
                            rowObjs[2] = fullPath;
                            rowObjs[3] = userName;
                            rowObjs[4] = doesExist;
                            rowObjs[5] = Boolean.FALSE;
                            rowObjs[6] = row[0];
                            rowObjs[7] = attachJoinID;
                            
                            dataRows.add(rowObjs);
                            
                            if (doesExist)
                            {
                                totalFiles++;
                            }
                            firePropertyChange(CNT, 0, (int)((double)i / (double)totFiles * 100.0));
                            i++;
                        }
                        AttchTableModel model = new AttchTableModel(dataRows);
                        JTable          table = new JTable(model);
                        tableHash.put(tblId, model);
                        tableList.add(table);
                        tableIdList.add(tblId);
                    }
                }  catch (Exception ex)
                {
                    ex.printStackTrace();
                } finally
                {
                    session.close();
                }

                return null;
            }

            @Override
            protected void done()
            {
                UIRegistry.clearSimpleGlassPaneMsg();
                
                if (tableList.size() > 0)
                {
                	displayBadAttachments(tableList, tableIdList, resultsHashMap, tblTypeHash, tableHash, totalFiles);
                }
                super.done();
            }
        };
        
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg("Verifying attachments in the repository...", 24);
        glassPane.setProgress(0);
        
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (CNT.equals(evt.getPropertyName())) 
                        {
                            glassPane.setProgress((Integer)evt.getNewValue());
                        }
                    }
                });
        
        worker.execute();
    }
    
    /**
     * @param tableList
     * @param tableIdList
     * @param resultsHashMap
     * @param tblTypeHash
     * @param tableHash
     * @param totalFiles
     */
    private void displayBadAttachments(final ArrayList<JTable>  tableList,
                                       final ArrayList<Integer> tableIdList,
                                       final HashMap<Integer, Vector<Object[]>> resultsHashMap,
                                       final HashMap<Integer, String>           tblTypeHash,
                                       final HashMap<Integer, AttchTableModel>  tableHash,
                                       final int totalFiles)
    {
        CellConstraints cc = new CellConstraints();
        
        int          maxWidth = 200;
        int          y        = 1;
        String       rowDef   = tableList.size() == 1? "f:p:g" : UIHelper.createDuplicateJGoodiesDef("p", "10px", tableList.size());
        PanelBuilder pb       = new PanelBuilder(new FormLayout("f:p:g", rowDef));
        if (tableList.size() > 1)
        {
            int i = 0;
            for (JTable table : tableList)
            {
                Integer tblId   = tableIdList.get(i++);
                int     numRows = table.getModel().getRowCount();
                
                PanelBuilder    pb2   = new PanelBuilder(new FormLayout("f:p:g", "p,2px,f:p:g"));
                if (resultsHashMap.size() > 1)
                {
                    UIHelper.calcColumnWidths(table, numRows < 15 ? numRows+1 : 15, maxWidth);
                } else
                {
                    UIHelper.calcColumnWidths(table, 15, maxWidth);
                }
                pb2.addSeparator(tblTypeHash.get(tblId), cc.xy(1, 1));
                pb2.add(UIHelper.createScrollPane(table), cc.xy(1, 3));
                pb.add(pb2.getPanel(), cc.xy(1, y));
                y += 2;
            }
        } else
        {
            UIHelper.calcColumnWidths(tableList.get(0), 15, maxWidth);
            pb.add(UIHelper.createScrollPane(tableList.get(0)), cc.xy(1, 1));
        }
        tableList.clear();

        pb.setDefaultDialogBorder();
        
        JScrollPane panelSB = UIHelper.createScrollPane(pb.getPanel());
        panelSB.setBorder(BorderFactory.createEmptyBorder());
        Dimension dim = panelSB.getPreferredSize();
        panelSB.setPreferredSize(new Dimension(dim.width+10, 600));

        final int totFiles = totalFiles;
        String title = String.format("Attachment Information - %d files to recover.", totalFiles);
        CustomDialog dlg = new CustomDialog((Dialog)null, title, true, CustomDialog.OKCANCELAPPLYHELP, panelSB)
        {
            @Override
            protected void helpButtonPressed()
            {
                File file = produceSummaryReport(resultsHashMap, tableHash, totFiles);
                try
                {
                    AttachmentUtils.openURI(file.toURI());
                } catch (Exception e) {}
            }
            @Override
            protected void applyButtonPressed()
            {
                boolean isOK = UIRegistry.displayConfirm("Clean up", "Are you sure you want to remove all references to the missing attachments?", "Remove", "Cancel", JOptionPane.WARNING_MESSAGE);
                if (isOK)
                {
                    super.applyButtonPressed();
                }
            }
        };
        
        dlg.setCloseOnApplyClk(true);
        dlg.setCancelLabel("Skip");
        dlg.setOkLabel("Recover Files");
        dlg.setHelpLabel("Show Summary");
        dlg.setApplyLabel("Delete References");
        dlg.createUI();
        dlg.pack();
        
        dlg.setVisible(true);
        
        if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
        {
            reattachFiles(resultsHashMap, tableHash, totalFiles);
            
        } else if (dlg.getBtnPressed() == CustomDialog.APPLY_BTN)
        {
            doAttachmentRefCleanup(resultsHashMap, tableHash, totFiles);
        }

    }
    
    
    class AttchTableModel extends DefaultTableModel
    {
        protected String[] headers = {"Owner", "File Name", "Full Path", "User", "Is Recoverable", "Was Recovered"};
        protected Vector<Object[]> dataRows;
        
        /**
         * @param fileName
         * @param agentName
         */
        public AttchTableModel(final Vector<Object[]> dataRows)
        {
            super();
            this.dataRows = dataRows;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return headers != null ? headers.length - 1 : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return headers[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int col)
        {
            return col == 4 ? Boolean.class : String.class;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return dataRows != null ? dataRows.size() : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            Object[] r = dataRows.get(row);
            return r[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }
        
        public int getAttachmentId(final int row)
        {
            Object[] r = dataRows.get(row);
            return (Integer)r[r.length-2];
        }
        
        public int getAttachmentJoinId(final int row)
        {
            Object[] r = dataRows.get(row);
            return (Integer)r[r.length-1];
        }
        
        public boolean isRecoverable(final int row)
        {
            Object[] r = dataRows.get(row);
            return (Boolean)r[4];
        }
        
        public void setRecovered(final int row, final boolean val)
        {
            Object[] r = dataRows.get(row);
            r[5] = (Boolean)val;
        }
        
        public boolean isRecovered(final int row)
        {
            Object[] r = dataRows.get(row);
            return (Boolean)r[5];
        }

        /**
         * @return the headers
         */
        public String[] getHeaders()
        {
            return headers;
        }
    }

}

/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIHelper.*;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import static edu.ku.brc.specify.utilapps.BuildSampleDatabase.UpdateType;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 3, 2007
 *
 */
public class SchemaToolsDlg extends CustomDialog
{
    private static final String SL_CHS_LOC = "SL_CHS_LOC";
    private static final String SL_CHS_IMP = "SL_CHS_IMP";
    
    protected JButton      editSchemaBtn        = createI18NButton("SL_EDIT_SCHEMA");
    protected JButton      removeLocaleBtn      = createI18NButton("SL_REMOVE_SCHEMA_LOC");
    protected JButton      exportSchemaLocBtn   = createI18NButton("SL_EXPORT_SCHEMA_LOC");
    protected JButton      importSchemaLocBtn   = createI18NButton("SL_IMPORT_SCHEMA_LOC");
    protected JList        localeList;
    protected Byte         schemaType;
    protected DBTableIdMgr tableMgr;

    /**
     * @param frame
     * @param schemaType
     * @param tableMgr
     * @throws HeadlessException
     */
    public SchemaToolsDlg(final Frame        frame, 
                          final Byte         schemaType,
                          final DBTableIdMgr tableMgr) throws HeadlessException
    {
        super(frame, getResourceString("SL_TOOLS_TITLE"), true, OKHELP, null);
        this.schemaType = schemaType;
        this.tableMgr   = tableMgr;
        
        helpContext = "SL_TOOLS_HELP_CONTEXT";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setOkLabel(getResourceString("CLOSE"));
        
        super.createUI();
        

        Vector<DisplayLocale> localeDisplays = new Vector<DisplayLocale>();
        for (Locale locale : SchemaLocalizerDlg.getLocalesInUseInDB(schemaType))
        {
            localeDisplays.add(new DisplayLocale(locale));
        }
        
        localeList = new JList(localeDisplays);
        JScrollPane sp   = UIHelper.createScrollPane(localeList, true);

        CellConstraints cc = new CellConstraints();
        
        PanelBuilder builder   = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,2px,p,16px,p,4px,p,8px,p,10px"));
        builder.addSeparator(getResourceString("SL_LOCALES_IN_USE"), cc.xywh(1, 1, 3, 1));
        builder.add(sp, cc.xywh(1,3,3,1));
        
        builder.addSeparator(getResourceString("SL_TASKS"), cc.xywh(1, 5, 3, 1));
        builder.add(editSchemaBtn,        cc.xy(1,7));
        builder.add(removeLocaleBtn,      cc.xy(3,7));
        builder.add(exportSchemaLocBtn,   cc.xy(1,9));
        builder.add(importSchemaLocBtn,   cc.xy(3,9));
        
        builder.setDefaultDialogBorder();
        
        contentPanel = builder.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        enableBtns(false);
        
        localeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                localeSelected();
            }
        });
        localeList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me)
            {
                if (me.getClickCount() == 2)
                {
                    editSchema();
                }
            }
        });
        
        editSchemaBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                editSchema();
            }
        });
        
        removeLocaleBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                removeSchemaLocale();
            }
        });
        
        exportSchemaLocBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                exportSchemaLocales();
            }
        });
        
        importSchemaLocBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                chooseImportType();
            }
        });
        
        pack();
    }
    
    /**
     * @param enable
     */
    protected void enableBtns(final boolean enable)
    {
        
        editSchemaBtn.setEnabled(enable);
        // XXX Fix Me when remove is implemented.
        //removeLocaleBtn.setEnabled(localeList.getModel().getSize() > 1);
        removeLocaleBtn.setEnabled(false);
        exportSchemaLocBtn.setEnabled(enable);
    }

    /**
     * 
     */
    protected void localeSelected()
    {
        DisplayLocale dispLocale = (DisplayLocale)localeList.getSelectedValue();
        if (dispLocale != null)
        {
            enableBtns(true);
            
        } else
        {
            enableBtns(false);
        }
    }
    
    
    /**
     * 
     */
    protected void editSchema()
    {
        SwingUtilities.invokeLater(new Runnable() {

            public void run()
            {
                okButtonPressed();
                
                DisplayLocale dispLocale = (DisplayLocale)localeList.getSelectedValue();
                if (dispLocale != null)
                {
                    Locale currLocale = SchemaI18NService.getCurrentLocale();
                    
                    SchemaI18NService.setCurrentLocale(dispLocale.getLocale());

                    SchemaLocalizerDlg dlg = new SchemaLocalizerDlg((Frame)UIRegistry.getTopWindow(), schemaType, tableMgr); // MUST BE MODAL!
                    dlg.setVisible(true);
                    SchemaI18NService.setCurrentLocale(currLocale);
                    
                    isCancelled = true; // We need to do this here so we don't get a StatsPane we don't want

                    //if (dlg.wasSaved())
                    //{
                        //UIRegistry.showLocalizedMsg("Specify.ABT_EXIT");
                        //CommandDispatcher.dispatch(new CommandAction(BaseTask.APP_CMD_TYPE, BaseTask.APP_REQ_EXIT));
                        
                    //} else
                    //{
                        ContextMgr.getTaskByName("Startup").requestContext();
                    //}
                }
            }
        });
    }
    
    private void chooseImportType()
    {
        int rv = UIRegistry.askYesNoLocalized(SL_CHS_IMP, SL_CHS_LOC, getResourceString("SL_CHOOSEIMPMSG"), "SL_CHOOSEIMPMSG_TITLE");
        if (rv == JOptionPane.YES_OPTION || rv == JOptionPane.NO_OPTION)
        {
            importSchema(rv == JOptionPane.NO_OPTION);
        }
    }
    
    /**
     * 
     */
    private void importSchema(final boolean doLocalization)
    {
        FileDialog fileDlg = new FileDialog((Dialog)null);
        fileDlg.setTitle(getResourceString(doLocalization ? SL_CHS_LOC : SL_CHS_IMP));
        UIHelper.centerAndShow(fileDlg);
        
        String fileName = fileDlg.getFile();
        if (StringUtils.isNotEmpty(fileName))
        {
            String title = getResourceString(doLocalization ? "SL_L10N_SCHEMA" : "SL_IMPORT_SCHEMA");
            
            final File            file      = new File(fileDlg.getDirectory() + File.separator + fileName);
            final SimpleGlassPane glassPane = new SimpleGlassPane(title, 18);
            glassPane.setBarHeight(12);
            glassPane.setFillColor(new Color(0, 0, 0, 85));
            
            setGlassPane(glassPane);
            glassPane.setVisible(true);
            
            SwingWorker<Integer, Integer> importWorker = new SwingWorker<Integer, Integer>()
            {
                private boolean isOK = false;
                @Override
                protected Integer doInBackground() throws Exception
                {
                    DataProviderSessionIFace localSession = null;
                    try
                    {
                        localSession = DataProviderFactory.getInstance().createSession();
                        
                        localSession.beginTransaction();
                        
                        BuildSampleDatabase bsd = new BuildSampleDatabase();
                        
                        Discipline discipline = localSession.get(Discipline.class, AppContextMgr.getInstance().getClassObject(Discipline.class).getId());
                        
                        isOK = bsd.loadSchemaLocalization(discipline, 
                                                            schemaType, 
                                                            DBTableIdMgr.getInstance(),
                                                            null, //catFmtName,
                                                            null, //accFmtName,
                                                            doLocalization ? UpdateType.eLocalize : UpdateType.eImport, // isDoingUpdate
                                                            file, // external file
                                                            glassPane,
                                                            localSession);
                        if (isOK)
                        {
                            localSession.commit();
                        } else
                        {
                            localSession.rollback();
                        }
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildSampleDatabase.class, ex);
                        
                    } finally 
                    {
                        if (localSession != null)
                        {
                            localSession.close();
                        }
                    }

                    return null;
                }

                @Override
                protected void done()
                {
                    super.done();
                    
                    glassPane.setVisible(false);
                    
                    if (isOK)
                    {
                        UIRegistry.showLocalizedMsg("Specify.ABT_EXIT");
                        CommandDispatcher.dispatch(new CommandAction("App", "AppReqExit"));
                    }
                }
            };
            importWorker.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public  void propertyChange(final PropertyChangeEvent evt) {
                            if (evt.getPropertyName().equals("progress")) 
                            {
                                glassPane.setProgress((Integer)evt.getNewValue());
                            }
                        }
                    });
            importWorker.execute();
        }
    }
    
    /**
     * 
     */
    protected void removeSchemaLocale()
    {
        
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void exportSchemaLocales()
    {
        FileDialog dlg = new FileDialog(((Frame)UIRegistry.getTopWindow()), getResourceString("Save"), FileDialog.SAVE);
        dlg.setVisible(true);
        
        String fileName = dlg.getFile();
        if (fileName != null)
        {
            final File    outFile = new File(dlg.getDirectory() + File.separator + fileName);
            //final File    outFile = new File("xxx.xml");
        
            final SimpleGlassPane glassPane = new SimpleGlassPane(getResourceString("SL_EXPORT_SCHEMA"), 18);
            glassPane.setBarHeight(12);
            glassPane.setFillColor(new Color(0, 0, 0, 85));
            
            setGlassPane(glassPane);
            glassPane.setVisible(true);
            
            SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
            {
                @Override
                protected Integer doInBackground() throws Exception
                {
                    
                    DataProviderSessionIFace  session    = null;
                    try
                    {
                        session = DataProviderFactory.getInstance().createSession();
                        
                        int    dispId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
                        String sql    = String.format("FROM SpLocaleContainer WHERE disciplineId = %d AND schemaType = %d", dispId, schemaType);
                        List<SpLocaleContainer> spContainers = (List<SpLocaleContainer>)session.getDataList(sql);
                        try
                        {
                            FileWriter fw   = new FileWriter(outFile);

                            //fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<vector>\n");
                            fw.write("<vector>\n");

                            BeanWriter      beanWriter = new BeanWriter(fw);
                            XMLIntrospector introspector = beanWriter.getXMLIntrospector();
                            introspector.getConfiguration().setWrapCollectionsInElement(true);
                            beanWriter.getBindingConfiguration().setMapIDs(false);
                            beanWriter.setWriteEmptyElements(false);

                            beanWriter.enablePrettyPrint();
                            
                            double step  = 100.0 / (double)spContainers.size();
                            double total = 0.0;
                            for (SpLocaleContainer container : spContainers)
                            {
                                // force Load of lazy collections
                                container.getDescs().size();
                                container.getNames().size();
                                
                                // Leaving this Code as an example of specifying the bewtixt file.
                                /*InputStream inputStream = Specify.class.getResourceAsStream("datamodel/SpLocaleContainer.betwixt");
                                //InputStream inputStream = Specify.class.getResourceAsStream("/edu/ku/brc/specify/tools/schemalocale/SpLocaleContainer.betwixt");
                                InputSource inputSrc    = new InputSource(inputStream); 
                                beanWriter.write(container, inputSrc);
                                inputStream.close(); */
                                
                                beanWriter.write(container);
                                
                                total += step;
                                firePropertyChange("progress", 0, (int)total);
                            }
                            
                            fw.write("</vector>\n");
                            fw.close();

                        } catch(Exception ex)
                        {
                            ex.printStackTrace();
                        }
                        
                    } catch (Exception e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, e);
                        e.printStackTrace();
                        
                    } finally
                    {
                        if (session != null)
                        {
                            session.close();
                        }
                    }

                    return null;
                }

                @Override
                protected void done()
                {
                    super.done();
                    
                    glassPane.setVisible(false);
                }
            };
            
            backupWorker.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public  void propertyChange(final PropertyChangeEvent evt) {
                            if (evt.getPropertyName().equals("progress")) 
                            {
                                glassPane.setProgress((Integer)evt.getNewValue());
                            }
                        }
                    });
            backupWorker.execute();
        }
    }
}

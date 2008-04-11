/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpAppResourceDir;
import edu.ku.brc.specify.datamodel.SpViewSetObj;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.persist.ViewIFace;
import edu.ku.brc.ui.forms.persist.ViewSetIFace;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Dec 1, 2007
 *
 */
public class ResourceImportExportDlg extends CustomDialog
{
    //private static final Logger  log = Logger.getLogger(ResourceImportExportDlg.class);
    
    protected JComboBox              levelCBX;
    protected JList                  viewSetsList;
    protected DefaultListModel       viewSetsModel = new DefaultListModel();
    
    protected JList                  viewsList;
    protected DefaultListModel       viewsModel = new DefaultListModel();

    protected JList                  resList;
    protected DefaultListModel       resModel = new DefaultListModel();
    protected JTabbedPane            tabbedPane;
    
    protected JButton                exportBtn;
    protected JButton                importBtn;
    protected JButton                reverBtn;

    protected List<SpAppResource>    resources = new Vector<SpAppResource>();
    protected List<SpAppResourceDir> dirs;

    /**
     * @throws HeadlessException
     */
    public ResourceImportExportDlg() throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), 
                getResourceString("RIE_TITLE"), 
                true, 
                OK_BTN,
                null);
        okLabel = getResourceString("Close");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
        
        levelCBX = createComboBox();
        
        SpecifyAppContextMgr context = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        dirs = context.getSpAppResourceList();
        for (SpAppResourceDir dir : dirs)
        {
            levelCBX.addItem(dir.getIdentityTitle());
        }
        
        PanelBuilder  centerPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        centerPB.add(levelCBX, cc.xy(2,1));

        tabbedPane = new JTabbedPane();
        
        PanelBuilder viewPanel = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g", "p,2px,p"));
        viewPanel.add(createLabel(getResourceString("RIE_VIEWSETS"), SwingConstants.CENTER),   cc.xy(1,1));
        viewSetsList = new JList(viewSetsModel);
        viewSetsList.setCellRenderer(new ARListRenderer());
        JScrollPane sp = new JScrollPane(viewSetsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        viewPanel.add(sp, cc.xy(1,3));
        
        viewPanel.add(createLabel(getResourceString("RIE_VIEWS"), SwingConstants.CENTER),   cc.xy(3,1));
        viewsList = new JList(viewsModel);
        viewsList.setCellRenderer(new ViewRenderer());
        sp = new JScrollPane(viewsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        viewPanel.add(sp, cc.xy(3,3));
        
        PanelBuilder resPanel = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p"));
        resPanel.add(createLabel(getResourceString("RIE_OTHER_RES"), SwingConstants.CENTER), cc.xy(1,1));
        resList   = new JList(resModel);
        resList.setCellRenderer(new ARListRenderer());
        sp = new JScrollPane(resList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        resPanel.add(sp, cc.xy(1,3));

        tabbedPane.addTab(getResourceString("RIE_VIEWSETS"), viewPanel.getPanel());
        tabbedPane.addTab(getResourceString("RIE_OTHER_RES"), resPanel.getPanel());
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,4px,p,2px,p"));
        pb.add(centerPB.getPanel(), cc.xy(1,1));
        pb.add(tabbedPane,          cc.xy(1,3));
        
        exportBtn = createButton(getResourceString("RIE_EXPORT"));
        importBtn = createButton(getResourceString("RIE_IMPORT"));
        reverBtn  = createButton(getResourceString("RIE_REVERT"));
        PanelBuilder btnPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g,p,f:p:g", "p,10px"));
        btnPB.add(exportBtn, cc.xy(2,1));
        btnPB.add(importBtn, cc.xy(4,1));
        btnPB.add(reverBtn,  cc.xy(6,1));
        
        pb.add(btnPB.getPanel(), cc.xy(1,5));
        
        pb.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        levelCBX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        levelSelected();
                    }
                });
            }
        });
        
        levelCBX.setSelectedIndex(0);
        
        pack();
        
        exportBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                exportResource();
            }
        });
        
        importBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                importResource();
            }
        });
        
        reverBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                revertResource();
            }
        });
        
        viewSetsList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (viewSetsList.getSelectedIndex() > -1)
                    {
                        resList.clearSelection(); 
                    }
                    fillViewsList();
                    enableUI();
                }
            }
        });
        
        resList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (resList.getSelectedIndex() > -1)
                    {
                        viewSetsList.clearSelection(); 
                    }
                    enableUI();
                }
            }
        });
    }
    
    /**
     * Fill the list with the view names.
     */
    protected void fillViewsList()
    {
        int index = levelCBX.getSelectedIndex();
        if (index > -1)
        {
            SpAppResourceDir dir = dirs.get(index);
            viewsModel.clear();
            index = viewSetsList.getSelectedIndex();
            if (index > -1)
            {
                ViewSetIFace vs = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getViewSetList(dir).get(index);
                Vector<ViewIFace> views = new Vector<ViewIFace>(vs.getViews().values());
                Collections.sort(views);
                for (ViewIFace view : views)
                {
                    viewsModel.addElement(view);
                }
            }
        }
    }
    
    /**
     * Enables the Import / Export and Revert Buttons
     */
    protected void enableUI()
    {
        boolean enable = !viewSetsList.isSelectionEmpty() || !resList.isSelectionEmpty();
        
        importBtn.setEnabled(enable && levelCBX.getSelectedIndex() < 2);
        exportBtn.setEnabled(enable);
        
        SpViewSetObj   vso   = (SpViewSetObj)viewSetsList.getSelectedValue();
        SpAppResource appRes = (SpAppResource)resList.getSelectedValue();
        
        reverBtn.setEnabled((vso != null && vso.getId() != null) || (appRes != null && appRes.getId() != null));
    }
    
    /**
     * Revert a resource. Re-read it from the disk.
     */
    protected void revertResource()
    {
        int index = levelCBX.getSelectedIndex();
        if (index > -1)
        {
            String exportedName   = null;
            String virtualDirName = SpecifyAppContextMgr.getVirtualDirName(index);
            
            index = resList.getSelectedIndex();
            if (index > -1)
            {
                AppResourceIFace appRes = resources.get(index);
                //fileName = FilenameUtils.getName(appRes.getName());
                //data = appRes.getDataAsString();
                
                AppResourceIFace revertedNewAR = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).revertResource(virtualDirName, appRes);
                if (revertedNewAR != null)
                {
                    resModel.insertElementAt(revertedNewAR, index);
                    resList.setSelectedIndex(index);
                }
                
            } else
            {
                SpViewSetObj vso = (SpViewSetObj)viewSetsList.getSelectedValue();
                if (vso != null)
                {
                    exportedName = vso.getName();
                    
                    index = viewSetsList.getSelectedIndex();
                    viewSetsModel.remove(index);
                    SpViewSetObj revertedNewVSO = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).revertViewSet(virtualDirName, vso.getName());
                    if (revertedNewVSO != null)
                    {
                        viewSetsModel.insertElementAt(revertedNewVSO, index);
                        viewSetsList.setSelectedIndex(index);
                    }
                }
            }
            
            if (exportedName != null)
            {
                UIRegistry.getStatusBar().setText(UIRegistry.getLocalizedMessage("RIE_RES_REVERTED", exportedName));
            }
            
            enableUI();
        }
    }
    
    /**
     * 
     */
    protected void exportResource()
    {
        
        int index = levelCBX.getSelectedIndex();
        if (index > -1)
        {
            String exportedName = null;
            
            String data     = null;
            String fileName = null;

            index = resList.getSelectedIndex();
            if (index > -1)
            {
                
                AppResourceIFace appRes = resources.get(index);
                exportedName = appRes.getName();
                fileName     = FilenameUtils.getName(exportedName);
                data         = appRes.getDataAsString();
                
            } else
            {
                index = viewSetsList.getSelectedIndex();
                if (index > -1)
                {
                    SpViewSetObj vso = (SpViewSetObj)viewSetsList.getSelectedValue();
                    exportedName = vso.getName();
                    fileName     = FilenameUtils.getName(vso.getName());
                    data         = vso.getDataAsString();
                }
            }

            if (StringUtils.isNotEmpty(data))
            {
                FileDialog fileDlg = new FileDialog(this, "Export Resource", FileDialog.SAVE);
                fileDlg.setFile(fileName);
                fileDlg.setVisible(true);
                
                String dirStr = fileDlg.getDirectory();
                fileName      = fileDlg.getFile();
                
                if (StringUtils.isNotEmpty(dirStr) && StringUtils.isNotEmpty(fileName))
                {
                    File expFile  = new File(dirStr + File.separator + fileName);
                    try
                    {
                        FileUtils.writeStringToFile(expFile, data);
                        
                    } catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            
            if (exportedName != null)
            {
                UIRegistry.getStatusBar().setText(UIRegistry.getLocalizedMessage("RIE_RES_EXPORTED", exportedName));
            }
        }
    }
    
    /**
     * 
     */
    protected void importResource()
    {
        int index = levelCBX.getSelectedIndex();
        if (index > -1)
        {
            String exportedName = null;
            
            FileDialog fileDlg = new FileDialog(this, getResourceString("RIE_IMPORT_RES"), FileDialog.LOAD);
            fileDlg.setVisible(true);
            
            String dirStr   = fileDlg.getDirectory();
            String fileName = fileDlg.getFile();
            
            if (StringUtils.isNotEmpty(dirStr) && StringUtils.isNotEmpty(fileName))
            {
                String data        = null;
                File   importFile  = new File(dirStr + File.separator + fileName);
                try
                {
                    data = FileUtils.readFileToString(importFile);
                    
                } catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                
                index = resList.getSelectedIndex();
                if (index > -1)
                {
                    AppResourceIFace appRes = resources.get(index);
                    exportedName = appRes.getName();
                    String           fName  = FilenameUtils.getName(exportedName);
                    String           dbBaseName = FilenameUtils.getBaseName(fileName);
                    if (dbBaseName.equals(fName))
                    {
                        appRes.setDataAsString(data);
                        ((SpecifyAppContextMgr)AppContextMgr.getInstance()).saveResource(appRes);
                    }
                    
                } else
                {
                    index = viewSetsList.getSelectedIndex();
                    if (index > -1)
                    {
                        DataProviderSessionIFace session = null;
                        try
                        {
                            session = DataProviderFactory.getInstance().createSession();
                            session.beginTransaction();
                            
                            SpViewSetObj     vso       = (SpViewSetObj)viewSetsList.getSelectedValue();
                            SpAppResourceDir appResDir = vso.getSpAppResourceDir();
                            exportedName = vso.getName();
                            
                            if (vso.getSpViewSetObjId() == null)
                            {
                                appResDir.getSpPersistedViewSets().add(vso);
                                vso.setSpAppResourceDir(appResDir);
                            }
                            vso.setDataAsString(data);

                            session.saveOrUpdate(appResDir);
                            session.saveOrUpdate(vso);
                            session.commit();
                            session.flush();
                            
                            viewSetsList.repaint();
                            
                        } catch (Exception ex)
                        {
                            session.rollback();
                            
                            ex.printStackTrace();
                            
                        } finally
                        {
                            try
                            {
                                session.close();
                                
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
            
            if (exportedName != null)
            {
                UIRegistry.getStatusBar().setText(UIRegistry.getLocalizedMessage("RIE_RES_IMPORTED", exportedName));
            }
            
            enableUI();
        }
    }
    
    /**
     * A Virtual Directory Level has been choosen.
     */
    protected void levelSelected()
    {
        int index = levelCBX.getSelectedIndex();
        if (index > -1)
        {
            SpAppResourceDir dir = dirs.get(index);
            viewSetsModel.clear();
            viewsModel.clear();
            resModel.clear();
            
            resources.clear();
            resources.addAll(dir.getSpAppResources());
            for (SpAppResource appRes : resources)
            {
                resModel.addElement(appRes);
            }
            
            for (SpViewSetObj vso : dir.getSpViewSets())
            {
                viewSetsModel.addElement(vso);
            }
            
            if (viewSetsModel.size() > 0)
            {
                viewSetsList.setSelectedIndex(0);
            }
        }
    }
    
    
    //------------------------------------------------------------------------------------------------------
    //--
    //------------------------------------------------------------------------------------------------------
    class ARListRenderer extends DefaultListCellRenderer
    {
        private String databaseStr = getResourceString("RIE_FROM_DATABASE");
        
        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @SuppressWarnings("unchecked")
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            String title;
            Integer id;
            if (value instanceof SpViewSetObj)
            {
                title = ((SpViewSetObj)value).getName();
                id    = ((SpViewSetObj)value).getId();
            } else
            {
                title = ((SpAppResource)value).getName();
                id    = ((SpAppResource)value).getId();
            }
            if (id != null)
            {
                title += " " + databaseStr;
            }
            
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            label.setText(title);
            
            return label;
        }
    }
    
    class ViewRenderer extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            return super.getListCellRendererComponent(list, value, index, false, false);
        }
    }
}

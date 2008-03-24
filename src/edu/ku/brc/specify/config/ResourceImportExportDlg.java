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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
 * @code_status Alpha
 *
 * Dec 1, 2007
 *
 */
public class ResourceImportExportDlg extends CustomDialog
{
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

    protected List<SpAppResource>    resources = new Vector<SpAppResource>();
    protected List<SpAppResourceDir> dirs;

    /**
     * @throws HeadlessException
     */
    public ResourceImportExportDlg() throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("RIE_TITLE"), true, null);
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
        
        PanelBuilder viewPanel = new PanelBuilder(new FormLayout("p,10px,p", "p,2px,p"));
        viewPanel.add(createLabel(getResourceString("RIE_VIEWSETS"), SwingConstants.CENTER),   cc.xy(1,1));
        viewSetsList = new JList(viewSetsModel);
        JScrollPane sp = new JScrollPane(viewSetsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        viewPanel.add(sp, cc.xy(1,3));
        
        viewPanel.add(createLabel(getResourceString("RIE_VIEWS"), SwingConstants.CENTER),   cc.xy(3,1));
        viewsList = new JList(viewsModel);
        sp = new JScrollPane(viewsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        viewPanel.add(sp, cc.xy(3,3));
        
        PanelBuilder resPanel = new PanelBuilder(new FormLayout("p,10px,p", "p,2px,p"));
        resPanel.add(createLabel(getResourceString("RIE_OTHER_RES"), SwingConstants.CENTER), cc.xy(1,1));
        resList   = new JList(resModel);
        sp = new JScrollPane(resList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        resPanel.add(sp, cc.xy(1,3));

        tabbedPane.addTab(getResourceString("RIE_VIEWSETS"), viewPanel.getPanel());
        tabbedPane.addTab(getResourceString("RIE_OTHER_RES"), resPanel.getPanel());
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p", "p,4px,p,4px,p"));
        pb.add(centerPB.getPanel(), cc.xy(1,1));
        pb.add(tabbedPane,          cc.xy(1,3));
        
        exportBtn = createButton(getResourceString("RIE_EXPORT"));
        importBtn = createButton(getResourceString("RIE_IMPORT"));
        PanelBuilder btnPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g", "p"));
        btnPB.add(exportBtn, cc.xy(2,1));
        btnPB.add(importBtn, cc.xy(4,1));
        
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
     * 
     */
    protected void enableUI()
    {
        boolean enable = !viewSetsList.isSelectionEmpty() || !resList.isSelectionEmpty();
        
        importBtn.setEnabled(enable && levelCBX.getSelectedIndex() < 2);
        exportBtn.setEnabled(enable);
    }
    
    /**
     * 
     */
    protected void exportResource()
    {
        int index = levelCBX.getSelectedIndex();
        if (index > -1)
        {
            SpAppResourceDir dir = dirs.get(index);
            
            String data     = null;
            String fileName = null;

            index = resList.getSelectedIndex();
            if (index > -1)
            {
                AppResourceIFace appRes = resources.get(index);
                fileName = FilenameUtils.getName(appRes.getName());
                data = appRes.getDataAsString();
                
            } else
            {
                index = viewSetsList.getSelectedIndex();
                if (index > -1)
                {
                    ViewSetIFace vs = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getViewSetList(dir).get(index);
                    for (SpViewSetObj vso : dir.getSpViewSets())
                    {
                        if (vs.getFileName().equals(vso.getFileName()))
                        {
                            fileName = FilenameUtils.getName(vso.getName());
                            data     = vso.getDataAsString();
                            break;
                        }
                    }
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
            SpAppResourceDir dir = dirs.get(index);
            
            FileDialog fileDlg = new FileDialog(this, "Import Resource", FileDialog.LOAD);
            fileDlg.setVisible(true);
            
            String dirStr = fileDlg.getDirectory();
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
                    String fName = FilenameUtils.getName(appRes.getName());
                    if (fileName.equals(fName))
                    {
                        appRes.setDataAsString(data);
                        ((SpecifyAppContextMgr)AppContextMgr.getInstance()).saveResource(appRes);
                    }
                    
                } else
                {
                    index = viewSetsList.getSelectedIndex();
                    if (index > -1)
                    {
                        ViewSetIFace vs = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getViewSetList(dir).get(index);
                        for (SpViewSetObj vso : dir.getSpViewSets())
                        {
                            String vsFileName  = FilenameUtils.getName(vs.getFileName());
                            String vsoFileName = FilenameUtils.getName(vso.getFileName());
                            if (vsFileName.equals(vsoFileName))
                            {
                                if (vso.getSpViewSetObjId() == null)
                                {
                                    SpAppResourceDir appResDir = vso.getSpAppResourceDir();
                                    appResDir.getSpViewSets().remove(vso);
                                    appResDir.getSpPersistedViewSets().add(vso);
                                }
                                vso.setDataAsString(data);
                                
                                DataProviderSessionIFace session = null;
                                try
                                {
                                    session = DataProviderFactory.getInstance().createSession();
                                    session.beginTransaction();
                                    session.saveOrUpdate(vso.getSpAppResourceDir());
                                    session.saveOrUpdate(vso);
                                    session.commit();
                                    session.flush();
                                    
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
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 
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
                resModel.addElement(appRes.getName());
            }
            
            for (ViewSetIFace viewSet : ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getViewSetList(dir))
            {
                viewSetsModel.addElement(viewSet.getName());
            }
            if (viewSetsModel.size() > 0)
            {
                viewSetsList.setSelectedIndex(0);
            }
        }
    }
}

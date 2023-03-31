/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.prefs;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Properties;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.SearchBox;
import edu.ku.brc.af.ui.db.JAutoCompTextField;
import edu.ku.brc.af.ui.forms.validation.DataChangeListener;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.JTiledPanel;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolbarLayoutManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.ui.skin.SkinItem;
import edu.ku.brc.ui.skin.SkinsMgr;


/**
 *
 * This is the main content panel of the Dialog. It is also responsible for animating the resizing of the dialog.<br>
 * The Preference dialog can be configured to have a toolbar with each major section across the top or as a grid.
 * Currently it is using the toolbar and the grid may need more testing.<br>
 * The preferences are being loaded from an XML file.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class PreferencesDlg extends CustomDialog implements DataChangeListener, PrefsPanelMgrIFace
{
    protected static final Logger log = Logger.getLogger(PreferencesDlg.class);
    
    protected int MILLI_SEC_DELAY = 10; // for Fade In/Out
    protected int MAX_DELAY       = (int)(MILLI_SEC_DELAY * 1.5);
    
    public static final String PREFERENCES = "Preferences"; //$NON-NLS-1$

    private Window prevTopWindow = null;

    protected SimpleGlassPane       glassPane;
    protected JAutoCompTextField    searchText;

    protected PrefsToolbar          prefsToolbar   = null;
    protected PrefsToolbar          prefsPane      = null;
    protected PrefIndicatorPanel    delegateRenderer = new PrefIndicatorPanel();
    

    protected Color                 textBGColor    = null;
    protected Color                 badSearchColor = new Color(255,235,235);

    protected Component             currentComp    = null;
    protected SkinItem              skinItem       = null;

    protected Hashtable<String, Component> compsHash      = new Hashtable<String, Component>();
    protected String                       firstPanelName = null;

    protected Hashtable<String, PrefsPanelIFace>        prefPanelsHash = new Hashtable<String, PrefsPanelIFace>();
    
    /**
     * Constructor.
     * @param addSearchUI  true adds the search ui
     */
    public PreferencesDlg(final boolean addSearchUI)
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("PreferencesDlg.PREFERENCES"), true, OKCANCELHELP, null); //$NON-NLS-1$

        this.skinItem = SkinsMgr.getSkinItem("PrefDlg");
        
        glassPane = new SimpleGlassPane("", 24, false);
        setGlassPane(glassPane);
        glassPane.setVisible(false);
        glassPane.setFillColor(new Color(0,0,0, 96));
        glassPane.setDelegateRenderer(delegateRenderer);
        
        createUI();
        initAsToolbar(addSearchUI);
        pack();
        okBtn.setEnabled(false);

        int borderHeight = 2;
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); //assuming height is not an issue
        try {
            Integer scrollBarHeight = Double.valueOf(scrollPane.getHorizontalScrollBar().getPreferredSize().getHeight()).intValue();
            borderHeight += scrollBarHeight;
        } catch (Exception x) {
            //making room for the scroll bar failed and nobody cared.
        }
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2,2,borderHeight,2));
        scrollPane.setViewportView(mainPanel);
        setContentPane(scrollPane);
        pack();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createMainPanel()
     */
    @Override
    protected JPanel createMainPanel()
    {
        JPanel panel = new JTiledPanel();

        if (skinItem != null)
        {
            skinItem.setupPanel(panel);
        }

        panel.setLayout(new BorderLayout());
        
        return panel;
    }
    
    /**
     * Configure as a toolbar.
     * @param addSearchUI  true adds the search ui
     */
    protected void initAsToolbar(final boolean addSearchUI)
    {
        prefsToolbar = new PrefsToolbar(this);
        prefsToolbar.setOpaque(false);
        
        if (prefsToolbar.getNumPrefs() > 1)
        {
            JPanel panel = new JPanel();
            if (skinItem != null)
            {
                skinItem.setupPanel(panel);
            }
            if (addSearchUI)
            {
                ((ToolbarLayoutManager)prefsToolbar.getLayout()).setAdjustRightLastComp(true);
                prefsToolbar.add(createSearchPanel());
            }
            
            mainPanel.add(prefsToolbar, BorderLayout.NORTH);
            Dimension size = prefsToolbar.getLayout().preferredLayoutSize(mainPanel);
            prefsToolbar.setPreferredSize(size);
            prefsToolbar.setSize(size);
            prefsToolbar.setMinimumSize(size);
        }
        
        showPanel(firstPanelName);
        
        addDataChangeListeners();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
    @Override
    protected void cancelButtonPressed()
    {
        removeDataChangeListeners();
        super.cancelButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        final Properties changesHash = new Properties();
        
        if (!saveChangedPrefs(changesHash))
        {
            return;
        }
        
        super.okButtonPressed();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                CommandAction cmdAction = new CommandAction(PREFERENCES, "Updated", AppPreferences.getRemote()); //$NON-NLS-1$
                cmdAction.addProperties(changesHash);
                CommandDispatcher.dispatch(cmdAction);
            }
        });
    }

    /**
     * Remove self as a validation listener
     */
    protected void removeDataChangeListeners()
    {
        for (PrefsPanelIFace pp : prefPanelsHash.values())
        {
            if (pp.getValidator() != null)
            {
                pp.getValidator().removeDataChangeListener(this);
            }
        }
    }

    /**
     * Save any Preferences that have changed.
     */
    protected synchronized boolean saveChangedPrefs(final Properties changesHash)
    {
        for (PrefsPanelIFace pp : prefPanelsHash.values())
        {
            if (!((PrefsSavable)pp).isOKToSave())
            {
                return false;
            }
        }

        for (PrefsPanelIFace pp : prefPanelsHash.values())
        {
            pp.getChangedFields(changesHash);
            
            ((PrefsSavable)pp).savePrefs();
        }

        try
        {
            AppPreferences.getRemote().flush();

        } catch (BackingStoreException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PreferencesDlg.class, ex);
            // XXX FIXME
            log.error(ex);
        }
        return true;
    }

    /**
     * Congigure as a multi-row grid
     * @param addSearchUI  true adds the search ui
     */
    protected void initAsGrid(final boolean addSearchUI)
    {
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("l:p", "p")); //$NON-NLS-1$ //$NON-NLS-2$
        CellConstraints cc         = new CellConstraints();

        JButton showAllBtn = createButton(getResourceString("PreferencesDlg.SHOW_ALL")); //$NON-NLS-1$

        builder.add(showAllBtn, cc.xy(1,1));
        if (addSearchUI)
        {
            ((ToolbarLayoutManager)prefsToolbar.getLayout()).setAdjustRightLastComp(true);
            prefsToolbar.add(createSearchPanel());
        }

        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(builder.getPanel(), BorderLayout.NORTH);

        prefsPane = new PrefsToolbar(this);

        firstPanelName = "Main"; //$NON-NLS-1$
        addPanel(firstPanelName, prefsPane);
        
        showPanel(firstPanelName);

        showAllBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                showPanel(firstPanelName);
            }
        });
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelMgrIFace#showPanel(java.lang.String)
     */
    @Override
    public void showPanel(final String name)
    {
        if (StringUtils.isNotEmpty(name))
        {
            Component comp = compsHash.get(name);
            if (comp == currentComp)
            {
                log.error(String.format("The pref pane by the name '%s' is was not found.", name));
                return;
            }
    
            if (false)
            {
                boolean   makeVis = false;
                Dimension oldSize = null;
                if (currentComp != null)
                {
                    oldSize = currentComp.getSize();
                    mainPanel.remove(currentComp);
        
                } else
                {
                    makeVis = true;
                }
                
                String hContext = prefPanelsHash.get(name).getHelpContext();
                if (StringUtils.isNotEmpty(hContext))
                {
                    HelpMgr.registerComponent(helpBtn, hContext);
                }
        
                if (comp != null)
                {
                    comp.setVisible(makeVis);
                    mainPanel.add(comp, BorderLayout.CENTER);
                    comp.invalidate();
                    doLayout();
                    repaint();
                    currentComp = comp;
                    if (oldSize != null)
                    {
                        Dimension winDim = getSize();
                        winDim.width += currentComp.getPreferredSize().width - oldSize.width;
                        winDim.width  = Math.max(winDim.width, 400);
                        winDim.height = Math.max(winDim.height, 250);
                        
                        Dimension pSize = prefsToolbar.getPreferredSize();
                        winDim.width  = Math.max(winDim.width, pSize.width+30);
                        
                        setSize(winDim);
                        currentComp.setSize(new Dimension(currentComp.getPreferredSize().width, oldSize.height));
                        
                        // With Animation
                        //startAnimation(this, comp, currentComp.getPreferredSize().height - oldSize.height, false);
                        
                        ((PrefsPanelIFace)comp).setShadeColor(new Color(255, 255, 255, 255));
                        
                        // Without Animation
                        comp.setVisible(true);
                        winDim.height += currentComp.getPreferredSize().height - oldSize.height;
                        winDim.height = Math.max(winDim.height, 250);
                        setSize(winDim);
                        
                        new Timer(MILLI_SEC_DELAY, new FadeInAnimation(comp, 7)).start();
                    }
                }
            } else
            {
                boolean   makeVis = false;
                Dimension oldSize = null;
                if (currentComp != null)
                {
                    oldSize = currentComp.getSize();
        
                } else
                {
                    makeVis = true;
                }
                
                String hContext = prefPanelsHash.get(name).getHelpContext();
                if (StringUtils.isNotEmpty(hContext))
                {
                    HelpMgr.registerComponent(helpBtn, hContext);
                }
        
                if (comp != null)
                {
                    if (currentComp == null)
                    {
                        comp.setVisible(makeVis);
                        mainPanel.add(comp, BorderLayout.CENTER);
                        comp.invalidate();
                        doLayout();
                        repaint();
                        currentComp = comp;
                        return;
                    }
                    
                    if (oldSize != null)
                    {
                        ((PrefsPanelIFace)currentComp).setShadeColor(new Color(255, 255, 255, 0));
                         
                        new Timer(MILLI_SEC_DELAY, new FadeOutAnimation(comp, oldSize, 12)).start();
                    }
                }

            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelMgrIFace#addPanel(java.lang.String, java.awt.Component)
     */
    @Override
    public boolean addPanel(final String name, final Component comp)
    {
        if (compsHash.get(name) != null)
        {
            log.error(String.format("PrePanel with name '%s'has already been added.", name));
            return false;
        }

        compsHash.put(name, comp);

        if (!(comp instanceof PrefsSavable) || !(comp instanceof PrefsPanelIFace))
        {
            return false;
        }

        if (comp instanceof PrefsPanelIFace)
        {
            PrefsPanelIFace pp = (PrefsPanelIFace)comp;
            prefPanelsHash.put(name, pp);
        } else
        {
            log.error(String.format("PrePanel with name '%s' doesn't implement the xxx interface.", name));
        }

        if (firstPanelName == null)
        {
            firstPanelName = name;
        }

        return true;
    }
    
    /**
     * Hooks up the data changes listeners to the PrefPanels.
     */
    protected void addDataChangeListeners()
    {
        for (PrefsPanelIFace pp : prefPanelsHash.values())
        {
            if (pp.getValidator() != null)
            {
                pp.getValidator().addDataChangeListener(this);
            }
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            pack();
            doLayout();
            Dimension size = getPreferredSize();
            size.width = Math.max(size.width, 400);
            size.height = Math.max(size.height, 250);
            setPreferredSize(size);

        }
        super.setVisible(visible);
    }

    /**
     * Performs the search for a pref
     */
    protected void doPrefSearch()
    {
        // TODO needs implementing using Lucene
    }

    /**
     * Creates a search panel for the prefs.
     * @return a JPanel
     */
    protected JPanel createSearchPanel()
    {
        DocumentListener searchDL = new DocumentAdaptor()
        {
            @Override
            protected void changed(@SuppressWarnings("unused") DocumentEvent e)
            { 
                delegateRenderer.clearArrows();
                for (int i=0;i<prefsToolbar.getComponentCount();i++)
                {
                    Component c = prefsToolbar.getComponent(i);
                    if (c instanceof RolloverCommand)
                    {
                        Rectangle r = c.getBounds();
                        delegateRenderer.addArrow(r.x + r.width/2, 10);
                        delegateRenderer.setIndexVisible(i, true);
                    }
                }
                
                glassPane.setMargin(new Insets(prefsToolbar.getSize().height, 0, 0, 0));
                glassPane.setVisible(true);
                glassPane.setUseBGImage(true);
                glassPane.setHideOnClick(true);
                /*glassPane.addMouseListener(new MouseAdapter() {
                    @Override public void mousePressed(MouseEvent e)
                    { 
                        glassPane.setVisible(false);
                    }
                });*/
            }
        };
        searchText = new JAutoCompTextField(UIHelper.isMacOS() ? 15 : 22);
        searchText.getDocument().addDocumentListener(searchDL);
        SearchBox searchBox = new SearchBox(searchText, null);
        return searchBox;
    }

   /**
     * Start animation where painting will occur for the given rectangle
     * @param window the window to start it in
     * @param comp the component
     * @param delta the delta each time
     * @param fullStep the step
     */
    public void startAnimation(final Window window, final Component comp, final int delta, final boolean fullStep)
    {
        new Timer(10, new SlideInOutAnimation(window, comp, delta, fullStep)).start();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.PrefsPanelMgrIFace#closePrefs()
     */
    @Override
    public boolean closePrefs()
    {
        if (okBtn.isEnabled())
        {
            Object[] options = { getResourceString("PrefsDlg.SAVE_PREFS"),  //$NON-NLS-1$
                                 getResourceString("PrefsDlg.DONT_SAVE_PREFS"),  //$NON-NLS-1$
                                 getResourceString("CANCEL")  //$NON-NLS-1$
                  };
            int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                         getResourceString("PrefsDlg.MSG"),  //$NON-NLS-1$
                                                         getResourceString("PREFERENCES"),  //$NON-NLS-1$
                                                         JOptionPane.YES_NO_CANCEL_OPTION,
                                                         JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (userChoice == JOptionPane.YES_OPTION)
            {
                okButtonPressed();
               
            } else if (userChoice == JOptionPane.NO_OPTION)
            {
                cancelButtonPressed();
            } else
            {
                return false;
            }
        } else
        {
            cancelButtonPressed();
        }
        return true;
    }
    
    /**
     * @return
     */
    protected boolean areThePrefsOK()
    {
        boolean okToEnable = true;
        for (PrefsPanelIFace pp : prefPanelsHash.values())
        {
            // but check all the forms
            if (!pp.isFormValid() &&  pp.getValidator() != null && pp.getValidator().hasChanged())
            {
                log.debug("false="+pp.getValidator().getName()+" "+pp.isFormValid()+"  "+pp.getValidator()+"  "+pp.getValidator().hasChanged()); //$NON-NLS-1$
                okToEnable = false;
                break;
            }
        }
        return okToEnable;
    }
    
    /**
     * @param comp
     * @param oldSize
     */
    protected void showAndResizePane(final Component comp, 
                                     final Dimension oldSize)
    {
        mainPanel.remove(currentComp);
        
        currentComp.setVisible(false);
        mainPanel.add(comp, BorderLayout.CENTER);
        comp.invalidate();
        comp.validate();
        comp.invalidate();
        comp.doLayout();
        comp.repaint();
        doLayout();
        repaint();
        currentComp = comp;
        currentComp.setVisible(true);
        
        
        Dimension oldWinDim = getSize();
        Dimension winDim    = getSize();
        winDim.width += currentComp.getPreferredSize().width - oldSize.width;
        winDim.width  = Math.max(winDim.width, 400);
        winDim.height = Math.max(winDim.height, 250);
        
        Dimension pSize = prefsToolbar.getPreferredSize();
        winDim.width  = Math.max(winDim.width, pSize.width+30);
        
        if (winDim.equals(oldWinDim)) // needed because JGoodies doesn't want to relayout when it is the same size.
        {
            winDim.height += 2;
        }
        setSize(winDim);
        currentComp.setSize(new Dimension(currentComp.getPreferredSize().width, oldSize.height));
        
        // Not sure why this combination works
        mainPanel.invalidate();
        mainPanel.validate();
        mainPanel.invalidate();
        mainPanel.doLayout();
        mainPanel.repaint();
        
        invalidate();
        validate();
        invalidate();
        doLayout();

        doLayout();
        repaint();
        // to here
        
        ((PrefsPanelIFace)comp).setShadeColor(null);
        
        // Without Animation
        comp.setVisible(true);
        winDim.height += currentComp.getPreferredSize().height - oldSize.height;
        winDim.height = Math.max(winDim.height, 250);
        setSize(winDim);
    }
    
    
    //-----------------------------------------------------
    // DataChangeListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.forms.validation.DataChangeNotifier)
     */
    public void dataChanged(String name, Component comp, DataChangeNotifier dcn)
    {
        okBtn.setEnabled(areThePrefsOK());
    }

    //------------------------------------------------------------
    // Inner Class
    //------------------------------------------------------------
    
    private class FadeInAnimation implements ActionListener
    {
        private int        delta;
        private Component  comp;
        private int        alpha = 255;
        private Long       startTime = null;
        

        FadeInAnimation(final Component comp, final int delta)
        {
            this.delta     = delta;
            this.comp      = comp;
        }

        public void actionPerformed(ActionEvent e)
        {
            
            alpha -= delta;
            
            if (startTime != null)
            {
                int elapsedTime = (int)(System.currentTimeMillis() - startTime);
                if (elapsedTime > MAX_DELAY)
                {
                    alpha = 0;
                }
            }
            startTime = System.currentTimeMillis();
            
            ((PrefsPanelIFace)comp).setShadeColor(new Color(255, 255, 255, Math.max(alpha, 0)));
            
            if (alpha <= 0)
            {
                ((PrefsPanelIFace)comp).setShadeColor(null);
                ((Timer)e.getSource()).stop();
            }
            comp.repaint();
         }
    }
    
    private class FadeOutAnimation implements ActionListener
    {
        private int        delta;
        private int        alpha = 0;
        
        private Component  comp;
        private Dimension  oldSize;
        private Long       startTime = null;

        /**
         * @param currComp
         * @param comp
         * @param oldSize
         * @param delta
         */
        FadeOutAnimation(final Component comp, final Dimension oldSize, final int delta)
        {
            this.delta     = delta;
            this.comp      = comp;
            this.oldSize   = oldSize;
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                alpha += delta;
                
                if (startTime != null)
                {
                    int elapsedTime = (int)(System.currentTimeMillis() - startTime);
                    if (elapsedTime > MAX_DELAY)
                    {
                        alpha = 255;
                    }
                }
                startTime = System.currentTimeMillis();
                
                Color bc = currentComp.getBackground();
                ((PrefsPanelIFace)currentComp).setShadeColor(new Color(bc.getRed(), bc.getGreen(), bc.getBlue(), Math.min(alpha, 255)));
                
                if (alpha >= 255)
                {
                    ((PrefsPanelIFace)currentComp).setShadeColor(null);
                    ((Timer)e.getSource()).stop();
                    
                    showAndResizePane(comp, oldSize);
                }
                currentComp.validate();
                currentComp.repaint();
                comp.repaint();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private class SlideInOutAnimation implements ActionListener
    {
        private int        endHeight;
        private int        delta;
        private int        pixelStep;
        private Window     window;
        private Rectangle  rect;
        private Component  comp;

        SlideInOutAnimation(final Window window, final Component comp, final int delta, final boolean fullStep)
        {
            this.window    = window;
            this.delta     = delta;
            this.comp      = comp;
            rect           = window.getBounds();
            endHeight      = rect.height + delta;
            pixelStep      = fullStep ? delta : delta / 10;
        }

        public void actionPerformed(ActionEvent e)
        {
            rect.height += pixelStep;
            delta       -= pixelStep;

            if (delta < 0 && pixelStep > 0)
            {
                rect.height = endHeight;
                comp.setVisible(true);
                ((Timer) e.getSource()).stop();
                //window.pack();
                
            } else if (delta > 0 && pixelStep < 0)
            {
                rect.height = endHeight;
                comp.setVisible(true);
                ((Timer) e.getSource()).stop();
            }
            window.setBounds(rect);
         }
    }

}


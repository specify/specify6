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
package edu.ku.brc.af.prefs;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.validation.DataChangeListener;
import edu.ku.brc.ui.validation.DataChangeNotifier;


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
@SuppressWarnings("serial")
public class PreferencesDlg extends CustomDialog implements DataChangeListener
{
    protected static final Logger log = Logger.getLogger(PreferencesDlg.class);
    
    protected JTextField    searchText;
    protected JButton       searchBtn;

    protected PrefsToolbar  prefsToolbar  = null;
    protected PrefsToolbar  prefsPane     = null;

    protected Color         textBGColor = null;
    protected Color         badSearchColor = new Color(255,235,235);

    protected Component     currentComp = null;

    protected Hashtable<String, Component> compsHash      = new Hashtable<String, Component>();
    protected String                       firstPanelName = null;

    protected List<PrefsPanelIFace>        prefPanels = new ArrayList<PrefsPanelIFace>();

    /**
     * Constructor.
     * @param addSearchUI  true adds the search ui
     */
    public PreferencesDlg(final boolean addSearchUI)
    {
        super((Frame)UIRegistry.get(UIRegistry.TOPFRAME), getResourceString("preferences"), true, null);

        createUI();
        initAsToolbar(addSearchUI);
        pack();
        okBtn.setEnabled(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
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
            PanelBuilder    builder    = new PanelBuilder(new FormLayout("l:p, p, r:p:g", "p"));
            CellConstraints cc         = new CellConstraints();
    
            builder.add( prefsToolbar, cc.xy(1,1));
            if (addSearchUI)
            {
                builder.add( createSearchPanel(), cc.xy(3,1));
            }
    
            builder.getPanel().setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));//.createEmptyBorder(1,1,0,1));
            builder.getPanel().setBackground(Color.WHITE);
            mainPanel.add(builder.getPanel(), BorderLayout.NORTH);
        }
        
        showPanel(firstPanelName);

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
        saveChangedPrefs();
        try
        {
            AppPreferences.getRemote().flush();
            
        } catch (BackingStoreException ex)
        {
            log.error(ex);
        }
        super.okButtonPressed();
    }

    /**
     * Remove self as a validation listener
     */
    protected void removeDataChangeListeners()
    {
        for (PrefsPanelIFace pp : prefPanels)
        {
            if (pp.getValidator() != null)
            {
                pp.getValidator().removeDataChangeListener(this);
            }
        }
    }

    /**
     * Save any prefs that have changed
     */
    protected void saveChangedPrefs()
    {
        //if (currentComp instanceof PrefsSavable)
        //{
        //    ((PrefsSavable)currentComp).savePrefs();
        //}
        for (PrefsPanelIFace pp : prefPanels)
        {
            ((PrefsSavable)pp).savePrefs();
        }

        try
        {
            AppPreferences.getRemote().flush();

        } catch (BackingStoreException ex)
        {
            // XXX FIXME
            log.error(ex);
        }
    }

    /**
     * Congigure as a multi-row grid
     * @param addSearchUI  true adds the search ui
     */
    protected void initAsGrid(final boolean addSearchUI)
    {

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("l:p, p:g, r:p:g", "p"));
        CellConstraints cc         = new CellConstraints();

        JButton showAllBtn = new JButton(getResourceString("showall"));

        builder.add(showAllBtn, cc.xy(1,1));
        if (addSearchUI)
        {
            builder.add( createSearchPanel(), cc.xy(3,1));
        }

        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(builder.getPanel(), BorderLayout.NORTH);

        prefsPane = new PrefsToolbar(this);

        firstPanelName = "Main";
        addPanel(firstPanelName, prefsPane);
        
        showPanel(firstPanelName);

        showAllBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                showPanel(firstPanelName);
            }
        });
    }

    /**
     * Show a named panel.
     * @param name the name of the panel to be shown
     */
    public void showPanel(final String name)
    {
        if (StringUtils.isNotEmpty(name))
        {
            Component comp = compsHash.get(name);
            if (comp == currentComp)
            {
                return;
            }
    
            boolean   makeVis = false;
            Dimension oldSize = null;
            if (currentComp != null)
            {
                oldSize = currentComp.getSize();
                remove(currentComp);
    
            } else
            {
                makeVis = true;
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
                    setSize(winDim);
                    currentComp.setSize(new Dimension(currentComp.getPreferredSize().width, oldSize.height));
                    startAnimation(this, comp, currentComp.getPreferredSize().height - oldSize.height, false);
                }
            }
        }
    }

    /**
     * Added named sub panel to
     * @param name the name of the panel
     * @param comp the comp (Panel) to be added
     */
    public boolean addPanel(final String name, final Component comp)
    {
        // XXX need to check for duplicates

        compsHash.put(name, comp);

        if (!(comp instanceof PrefsSavable) || !(comp instanceof PrefsPanelIFace))
        {
            return false;
        }

        if (comp instanceof PrefsPanelIFace)
        {
            PrefsPanelIFace pp = (PrefsPanelIFace)comp;
            if (pp.getValidator() != null)
            {
                pp.getValidator().addDataChangeListener(this);
            }
            prefPanels.add(pp);
        }

        if (firstPanelName == null)
        {
            firstPanelName = name;
        }

        return true;
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
            setPreferredSize(getPreferredSize());
            setSize(getPreferredSize());
            UIHelper.centerWindow(this);
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
        // Create Search Panel
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        JPanel     searchPanel = new JPanel(gridbag);
        JLabel     spacer      = new JLabel(" ");

        searchBtn   = new JButton(getResourceString("Search"));

        searchText  = new JTextField("", 10);
        textBGColor = searchText.getBackground();

        searchText.setMinimumSize(new Dimension(50, searchText.getPreferredSize().height));

        ActionListener doQuery = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                String text = searchText.getText();
                if (isNotEmpty(text))
                {
                    doPrefSearch();
                }
            }
        };

        searchBtn.addActionListener(doQuery);
        searchText.addActionListener(doQuery);
        searchText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (searchText.getBackground() != textBGColor)
                {
                    searchText.setBackground(textBGColor);
                }
            }
        });


        c.weightx = 1.0;
        gridbag.setConstraints(spacer, c);
        searchPanel.add(spacer);

        c.weightx = 0.0;
        gridbag.setConstraints(searchText, c);
        searchPanel.add(searchText);

        searchPanel.add(spacer);

        gridbag.setConstraints(searchBtn, c);
        searchPanel.add(searchBtn);

        return searchPanel;
    }

   /**
     * Start animation where painting will occur for the given rect
     * @param window the window to start it in
     * @param comp the component
     * @param delta the delta each time
     * @param fullStep the step
     */
    public void startAnimation(final Window window, final Component comp, final int delta, final boolean fullStep)
    {
        new Timer(10, new SlideInOutAnimation(window, comp, delta, fullStep)).start();
    }


    //-----------------------------------------------------
    // DataChangeListener
    //-----------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.validation.DataChangeNotifier)
     */
    public void dataChanged(String name, Component comp, DataChangeNotifier dcn)
    {
        boolean okToEnable = true;
        for (PrefsPanelIFace pp : prefPanels)
        {
            // but check all the forms
            if (!pp.getValidator().isFormValid())
            {
                log.debug("false="+pp);
                okToEnable = false;
                break;
            }
        }
        okBtn.setEnabled(okToEnable);
    }

    //------------------------------------------------------------
    // Inner Class
    //------------------------------------------------------------
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

            //System.out.println(delta+"  "+pixelStep+"  "+rect);
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

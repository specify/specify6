/* Filename:    $RCSfile: PrefMainPanel.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.prefs;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * 
 * This is the main content panel of the Dialog. It is also responsible for animating the resizing
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PrefMainPanel extends JPanel
{
    protected JDialog       dialog;
    protected JTextField    searchText;
    protected JButton       searchBtn;
    
    protected PrefsToolbar  prefsToolbar  = null;  
    protected PrefsToolbar  prefsPane     = null;  

    protected Color         textBGColor = null;
    protected Color         badSearchColor = new Color(255,235,235);
    
    protected Component currentComp = null;
    protected Hashtable<String, Component> compsHash      = new Hashtable<String, Component>();
    protected String                       firstPanelName = null;

    /**
     * Constructor
     */
    public PrefMainPanel(JDialog dialog)
    {
        super(new BorderLayout());

        this.dialog = dialog;
        
        initAsToolbar();
        
        dialog.setTitle(getResourceString("preferences"));

    }
    
    /**
     * Configure as a toolbar
     */
    protected void initAsToolbar()
    {
        
        Color gray = new Color(230,230,230);
        int   delta = 8;
        Color lighter = new Color(gray.getRed()+delta, gray.getRed()+delta, gray.getRed()+delta);

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("l:p, p, r:p:g", "p,"));
        CellConstraints cc         = new CellConstraints();
       
        prefsToolbar = new PrefsToolbar(this);
        prefsToolbar.setBackground(lighter);
       
        
        builder.add( prefsToolbar, cc.xy(1,1));
        builder.add( createSearchPanel(), cc.xy(3,1));
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        builder.getPanel().setBackground(lighter);
        add(builder.getPanel(), BorderLayout.NORTH);
        
        JButton okButton = new javax.swing.JButton ("OK");
        JButton cancelButton = new javax.swing.JButton ("Cancel");
        Component buttonBar = com.jgoodies.forms.factories.ButtonBarFactory.buildRightAlignedBar(new JButton[] {okButton, cancelButton});
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                saveChangedPrefs();
                dialog.setVisible(false);
            }
        });    
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                dialog.setVisible(false);
            }
        });    
        add(buttonBar, BorderLayout.SOUTH);
        showPanel(firstPanelName);

    }
    
    /**
     * Save any prefs that have changed
     */
    protected void saveChangedPrefs()
    {
        if (currentComp instanceof PrefsSavable)
        {
            ((PrefsSavable)currentComp).savePrefs();
        }
    }
    
    /**
     * Congigure as a multi-row grid
     */
    protected void initAsGrid()
    {
        
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("l:p, p:g, r:p:g", "p"));
        CellConstraints cc         = new CellConstraints();
        
        JButton showAllBtn = new JButton(getResourceString("showall"));
        
        builder.add(showAllBtn, cc.xy(1,1));
        builder.add( createSearchPanel(), cc.xy(3,1));
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(builder.getPanel(), BorderLayout.NORTH);
       
        prefsPane    = new PrefsToolbar(this);
        
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
     * Show a named panel
     * @param name the name of the panel to be shown
     */
    public void showPanel(final String name)
    {
        Component comp = compsHash.get(name);
        if (comp == currentComp)
        {
            return;
        }
        
        boolean makeVis = false;
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
            add(comp, BorderLayout.CENTER);
            comp.invalidate();
            doLayout();
            repaint();
            currentComp = comp;
            if (oldSize != null)
            {
                System.out.println(currentComp.getPreferredSize()+" = "+oldSize);
                startAnimation(dialog, comp, currentComp.getPreferredSize().height - oldSize.height, false);
            }
        }
    }
    
    /**
     * Added named sub panel to 
     * @param name the name of the panel
     * @param comp the comp (Panel) to be added
     */
    public void addPanel(final String name, final Component comp)
    {
        // XXX need to check for duplicates

        compsHash.put(name, comp);
        
        if (firstPanelName == null)
        {
            firstPanelName = name;
        }

    }
    
    /**
     * Performs the search for a pref 
     */
    protected void doPrefSearch()
    {
        
    }
    
    /**
     * Creates a search panel for the prefs
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
        
        searchText  = new JTextField("megalotis", 10);
        textBGColor = searchText.getBackground();
        
        searchText.setMinimumSize(new Dimension(50, searchText.getPreferredSize().height));
        
        ActionListener doQuery = new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                String text = searchText.getText();
                if (text != null && text.length() > 0)
                {
                    doPrefSearch();
                }
            }
        };
        
        searchBtn.addActionListener(doQuery);
        searchText.addActionListener(doQuery);
        searchText.addKeyListener(new KeyAdapter()
        {
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
     * @param visibleRect the rect to be painted
     */
    public void startAnimation(final Window window, final Component comp, final int delta, final boolean fullStep) 
    {
        new Timer(10, new SlideInOutAnimation(window, comp, delta, fullStep)).start();
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

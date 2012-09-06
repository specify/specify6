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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.UIManager;

import edu.ku.brc.af.ui.SearchBox;
import edu.ku.brc.af.ui.db.JAutoCompTextField;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.specify.tasks.ExpressSearchTask.SearchBoxMenuCreator;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 19, 2012
 *
 */
public class SearchBoxComponent extends JPanel
{
    // Data Members
    protected SearchBox                     searchBox;
    protected JTextField                    searchText;
    protected JButton                       searchBtn;
    protected Color                         textBGColor      = null;
    protected Color                         badSearchColor   = new Color(255,235,235);

    // For initialization
    protected SearchBoxMenuCreator   sbCreator;
    protected ActionListener         actionListener;
    protected boolean                includeClearIcon;
    protected PickListDBAdapterIFace plAdapter;
    
    /**
     * @param sbCreator
     * @param actionListener
     */
    public SearchBoxComponent(final SearchBoxMenuCreator sbCreator,
                              final ActionListener actionListener,
                              final boolean includeClearIcon,
                              final PickListDBAdapterIFace plAdapter)
    {
        super();
        
        this.sbCreator        = sbCreator;
        this.actionListener   = actionListener;
        this.includeClearIcon = includeClearIcon;
        this.plAdapter        = plAdapter;
    }

    /**
     * @param sbCreator
     * @param actionListener
     */
    public SearchBoxComponent(final ActionListener actionListener,
                              final boolean includeClearIcon)
    {
        this(null, actionListener, includeClearIcon, null);
    }

    /**
     * @param sbCreator
     * @param actionListener
     */
    public SearchBoxComponent(final SearchBoxMenuCreator sbCreator,
                              final ActionListener actionListener)
    {
        this(sbCreator, actionListener, false, null);
    }


    /**
     * @param sbCreator
     * @param actionListener
     */
    public void createUI()
    {
        // Create Search Panel
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridbag);
        
        JLabel spacer = createLabel("  ");

        searchBtn  = createButton(getResourceString("SEARCH"));
        if (plAdapter != null)
        {
            JAutoCompTextField autoCompText = new JAutoCompTextField(15, plAdapter);
            autoCompText.setAskBeforeSave(false);
            searchText = autoCompText;
        } else
        {
            searchText = new JTextField(15);
        }
        searchBox = new SearchBox(searchText, sbCreator, includeClearIcon);
        
        textBGColor = searchText.getBackground();

        searchBtn.addActionListener(actionListener);
        searchText.addActionListener(actionListener);
        searchText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (searchText.getBackground() != textBGColor)
                {
                    searchText.setBackground(textBGColor);
                    searchText.setForeground(UIManager.getColor("TextField.foreground"));
                }
            }
        });
        
        searchText.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                showContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                showContextMenu(e);

            }
        });

        c.weightx = 1.0;
        gridbag.setConstraints(spacer, c);
        this.add(spacer);

        c.weightx = 0.0;
        gridbag.setConstraints(searchBox, c);
        this.add(searchBox);

        this.add(spacer);
        this.setOpaque(false);
        
        if (!UIHelper.isMacOS())
        {
            gridbag.setConstraints(searchBtn, c);
            this.add(searchBtn);
        }
    }
    
    /**
     * @param e
     */
    protected void showContextMenu(MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(UIRegistry.getResourceString("ES_TEXT_RESET"));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ex)
                {
                    searchText.setEnabled(true);
                    searchText.setBackground(textBGColor);
                    searchText.setText("");
                    
                    /*if (statusBar != null)
                    {
                        statusBar.setProgressDone(EXPRESSSEARCH);
                    }*/
                }
            });
            popup.add(menuItem);
            popup.show(e.getComponent(), e.getX(), e.getY());

        }
    }

    /**
     * @return the searchBox
     */
    public SearchBox getSearchBox()
    {
        return searchBox;
    }

    /**
     * @return the searchText
     */
    public JTextField getSearchText()
    {
        return searchText;
    }

    /**
     * @return the searchBtn
     */
    public JButton getSearchBtn()
    {
        return searchBtn;
    }

    /**
     * @return the textBGColor
     */
    public Color getTextBGColor()
    {
        return textBGColor;
    }

    /**
     * @return the badSearchColor
     */
    public Color getBadSearchColor()
    {
        return badSearchColor;
    }

}

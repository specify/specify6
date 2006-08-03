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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

/**
 *
 * This class creates a grid of AppPrefsIFace icon (commands) where each icon will dispay a panel.
 * It creates a row (or section) for each grouping of preferences and then makes sure all the columns and rows are aligned.
 * (Currently not in use)
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class PrefsPane extends JPanel
{
    //private static final Logger log = Logger.getLogger(PrefsPane.class);

    public static final String NAME        = "name";
    public static final String TITLE       = "title";
    public static final String PANEL_CLASS = "panelClass";
    public static final String ICON_PATH   = "iconPath";

    protected PrefMainPanel mainPanel;

    /**
     *
     */
    public PrefsPane(final PrefMainPanel mainPanel)
    {
        super();

        this.mainPanel = mainPanel;
        setLayout(new PrefsPaneLayoutManager());
        init();
    }

    /**
     *
     */
    protected void init()
    {
        /*
        try
        {
            Color gray = new Color(230,230,230);
            int   delta = 8;
            Color lighter = new Color(gray.getRed()+delta, gray.getRed()+delta, gray.getRed()+delta);


            // First Get Main Categories
            Font newFont = null;
            String[] childrenNames = appPrefs.childrenNames();
            System.out.println("Keys: "+childrenNames.length);
            System.out.println("childrenNames: "+appPrefs.childrenNames().length);

            for (String name : childrenNames)
            {
                System.out.println("Section: "+name);
            }

            int row = 0;
            for (String sectionName : childrenNames)
            {
                AppPrefsIFace section = appPrefs.node(sectionName);
                String  title = sectionName;
                if (title != null)
                {
                    boolean isAppPref = section.getBoolean("isApp", false);
                    if (isAppPref)
                    {
                        PrefPanelRow rowPanel = new PrefPanelRow(getResourceString(title));
                        if (newFont == null)
                        {
                            Font font = rowPanel.getTitle().getFont();
                            newFont = new Font(font.getFontName(), Font.BOLD, font.getSize()+1);
                        }
                        rowPanel.getTitle().setFont(newFont);

                        loadSectionPrefs(section, rowPanel);

                        rowPanel.setBackground((row % 2 == 0) ? lighter : gray);
                        add(rowPanel);
                        row++;
                    }
                }
            }

        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        */
    }

    /**
     * @param parentPref
     * @param rowPanel
     */
    protected void loadSectionPrefs(final String parentPref,
                                    final PrefPanelRow rowPanel)
    {
        /*
        try
        {
            String[] childrenNames = parentPref.childrenNames();
            for (String childName : childrenNames)
            {
                AppPrefsIFace pref  = parentPref.node(childName);

                String title      = pref.get(TITLE, null);
                String panelClass = pref.get(PANEL_CLASS, null);
                String iconPath   = pref.get(ICON_PATH, null);

                if (title != null && panelClass != null && iconPath != null)
                {
                    ImageIcon icon = new ImageIcon(new URL(iconPath));
                    if (icon == null)
                    {
                        log.error("Icon was created - path["+iconPath+"]");
                    }

                    JButton btn = new JButton(getResourceString(title), icon);
                    btn.setHorizontalTextPosition(JLabel.CENTER);
                    btn.setVerticalTextPosition(JLabel.BOTTOM);
                    btn.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
                    btn.setBorderPainted(false);
                    btn.setOpaque(false);

                    try
                    {
                        Class panelClassObj = Class.forName(panelClass);
                        Component comp = (Component)panelClassObj.newInstance();
                        if (!mainPanel.addPanel(title, comp))
                        {
                            log.error("The Class ["+panelClass+"] couldn't loaded into prefs because it doesn't implement the proper interfaces");
                        } else
                        {
                            rowPanel.add(btn);
                        }

                    } catch (Exception ex)
                    {
                        log.error(ex); // XXX FIXME
                    }
                    btn.addActionListener(new ShowAction(title));
                }
            }

        } catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
        */
    }

    protected void showPanel(final String panelName)
    {
        mainPanel.showPanel(panelName);

    }

    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------


    /**
     *
     * @author rods
     *
     */
    class ShowAction implements ActionListener
    {
        private String panelName;

        public ShowAction(final String panelName)
        {
            this.panelName = panelName;
        }
        public void actionPerformed(ActionEvent e)
        {
            showPanel(panelName);
        }
    }

}

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
package edu.ku.brc.af.tasks.subpane;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import edu.ku.brc.af.core.Taskable;

/**
 * A default pane for display a simple label telling what it is suppose to do.
 *
 * @code_status Code Freeze
 *
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class SimpleDescPane extends BaseSubPane
{
    //private static final Logger log = Logger.getLogger(SimpleDescPane.class);
    protected ImageIcon splashImage = null;

    /**
     * Constructor with string to be centered.
     * @param name name of pane
     * @param task the task
     * @param splashImage  image for background
     */
    public SimpleDescPane(final String name,
                          final Taskable task,
                          final ImageIcon splashImage)
    {
        super(name, task);
        
        setLayout(null);
        removeAll();

        setBackground(Color.WHITE);
        this.splashImage = splashImage;
    }


    /**
     * Constructor with string to be centered.
     * @param name name of pane
     * @param task the task
     * @param desc  a description that displays in the center
     */
    public SimpleDescPane(final String name,
                          final Taskable task,
                          final String desc)
    {
        super(name, task);

        setBackground(Color.WHITE);

        JLabel label = createLabel(desc, SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

    }

    /**
     * Constructor with a panel component to be centered
     * @param name name of panel
     * @param task the owning task
     * @param panel a panel to place in the center
     */
    public SimpleDescPane(final String name,
                          final Taskable task,
                          final JPanel panel)
    {
        super(name, task);

        setBackground(Color.WHITE);
        add(panel, BorderLayout.CENTER);

    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        if (splashImage != null)
        {
            Dimension size = getSize();
            int imgW = Math.min(size.width, splashImage.getIconWidth());
            int imgH = Math.min(size.height, splashImage.getIconHeight());
            int x = (size.width - imgW) / 2;
            int y = (size.height - imgH) / 2;
            g.drawImage(splashImage.getImage(), x, y, imgW, imgH, null);
        }
    }

    /**
     * Returns the splash image.
     * @return the splash image
     */
    public ImageIcon getSplashImage()
    {
        return splashImage;
    }

    /**
     * @param splashImage
     */
    public void setSplashImage(ImageIcon splashImage)
    {
        this.splashImage = splashImage;
    }
}

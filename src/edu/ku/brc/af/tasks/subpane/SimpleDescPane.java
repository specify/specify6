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

package edu.ku.brc.af.tasks.subpane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

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
@SuppressWarnings("serial")
public class SimpleDescPane extends BaseSubPane
{
    //private static final Logger log = Logger.getLogger(SimpleDescPane.class);
    protected Image splashImage = null;

    /**
     * @param name
     * @param task
     * @param desc
     */
    public SimpleDescPane(final String name,
                          final Taskable task,
                          final String desc)
    {
        super(name, task);

        setBackground(Color.WHITE);

        JLabel label = new JLabel(desc, SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

    }

    /**
     * @param name
     * @param task
     * @param panel
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
     * @see java.awt.Component#paintAll(java.awt.Graphics)
     */
    @Override
    public void paintAll(Graphics g)
    {
        super.paintAll(g);

        if (splashImage != null)
        {
            Dimension dim = getSize();
            int w = splashImage.getWidth(null);
            int h = splashImage.getHeight(null);
            g.drawImage(splashImage, (dim.width - w) / 2, (dim.height - h) / 2, w, h, null);
        }
    }

    /**
     * Returns the splash image.
     * @return the splash image
     */
    public Image getSplashImage()
    {
        return splashImage;
    }

    /**
     * @param splashImage
     */
    public void setSplashImage(Image splashImage)
    {
        this.splashImage = splashImage;
    }
}

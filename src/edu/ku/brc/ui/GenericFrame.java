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
package edu.ku.brc.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class GenericFrame extends JFrame 
{
    protected Dimension defaultSize = new Dimension(200, 200);

    public GenericFrame() 
    {
        super();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        JMenuItem item = null;
        
        //close
        item = new JMenuItem(UIRegistry.getResourceString("CLOSE"));
        item.setMnemonic(KeyEvent.VK_C);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Close window");
                GenericFrame.this.setVisible(false);
                GenericFrame.this.dispose();
            }
        });
        menu.add(item);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);
        setJMenuBar(menuBar);

        setSize(defaultSize);
    }
}

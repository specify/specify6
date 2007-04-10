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
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.UIHelper;

/**
 * THis class is used to show Card Images that are "linked" to a row of data.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Mar 16, 2007
 *
 */
public class ImageFrame extends JFrame
{
    //protected JPanel       panel                      = new JPanel(new BorderLayout());
    protected JLabel       cardImageLabel             = new JLabel("", SwingConstants.CENTER);
    protected JProgressBar progress                   = new JProgressBar();
    protected WorkbenchRow row                        = null;
    
    protected JPanel       noCardImageMessagePanel    = null;
    protected boolean      showingCardImageLabel      = true;
    protected ImageIcon    cardImage                  = null;
    protected JButton      loadImgBtn                 = null;
    protected JPanel       mainPane;
    protected JScrollPane  scrollPane;

    protected JMenu             viewMenu;
    protected JMenu             imageMenu;
    protected JMenuItem         closeMI;
    protected JMenuItem         replaceMI;
    protected JMenuItem         clearMI;
    protected JCheckBoxMenuItem origMI;
    protected JCheckBoxMenuItem reduceMI;
    
    /** This hash keeps track of the size that a given image was last displayed as.  If an image is displayed as full size, we add an entry
     * to this hash where the key is row.hashCode() and the value is 1.  If the image is displayed as reduced size, the value is -1.  (We
     * don't hash from WorkbenchRows to the size in order to not hold a handle to the WorkbenchRow objects.)
     */
    protected Hashtable<Integer,Integer> rowToImageSizeHash = new Hashtable<Integer, Integer>();
    
    /**
     * Constructor. 
     */
    public ImageFrame(final int mapSize)
    {
        Dimension minSize = new Dimension(mapSize, mapSize);
        cardImageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g,c:p,f:p:g", "f:p:g,p,5px,p,f:p:g"));
        CellConstraints cc      = new CellConstraints();
        
        loadImgBtn = new JButton(getResourceString("WB_LOAD_NEW_IMAGE"));
        
        builder.add(new JLabel(getResourceString("WB_NO_IMAGE_ROW"), SwingConstants.CENTER), cc.xy(2, 2));
        builder.add(loadImgBtn, cc.xy(2, 4));
        
        noCardImageMessagePanel = builder.getPanel();
        
        mainPane = new JPanel(new BorderLayout());
        mainPane.setSize(minSize);
        mainPane.setPreferredSize(minSize);
        mainPane.setMinimumSize(minSize);
        mainPane.add(cardImageLabel, BorderLayout.CENTER);
        scrollPane = new JScrollPane(mainPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setContentPane(scrollPane);
        
        JMenuBar  menuBar   = new JMenuBar();
        JMenu     fileMenu  = UIHelper.createMenu(menuBar, "File", "FileMneu");
        closeMI = UIHelper.createMenuItem(fileMenu, "Close", "CloseMneu", "", true, null);
        
        viewMenu  = UIHelper.createMenu(menuBar, "View", "ViewMneu");
        
        reduceMI = UIHelper.createCheckBoxMenuItem(viewMenu, "WB_REDUCED_SIZE", "ReducedSizeMneu", "", true, null);
        reduceMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                showReducedImage();
            }
        });
        
        origMI = UIHelper.createCheckBoxMenuItem(viewMenu, "WB_ORIG_SIZE", "OrigMneu", "", true, null);
        origMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                showOriginalSizeImage();
            }
        });
        
        imageMenu = UIHelper.createMenu(menuBar, "Image", "ImageMneu");
        clearMI   = UIHelper.createMenuItem(imageMenu, "Clear", "ClearMneu", "", true, null);
        replaceMI = UIHelper.createMenuItem(imageMenu, "Replace", "ReplaceMneu", "", true, null);
        
        JMenu helpMenu = new JMenu(getResourceString("Help"));
        menuBar.add(HelpMgr.createHelpMenuItem(helpMenu, getResourceString("WB_IMAGE_WINDOW")));

        enableMenus(false);
        
        setJMenuBar(menuBar);
        
        pack();
        
        //HelpMgr.setHelpID(this, "OnRampImageWindow");
    }
    
    public void setHelpContext(final String helpContext)
    {
        HelpMgr.setHelpID(this, helpContext);
    }
    
    /**
     * Displays the card image in a reduced size.
     */
    public void showReducedImage()
    {
        if (row == null)
        {
            return;
        }
        
        rowToImageSizeHash.put(row.hashCode(), -1);
        
        cardImage = row.getCardImage();
        cardImageLabel.setIcon(cardImage);
        reduceMI.setSelected(true);
        origMI.setSelected(false);
        if (cardImage != null)
        {
            cardImageLabel.setSize(cardImage.getIconWidth(), cardImage.getIconHeight());
            mainPane.setSize(cardImage.getIconWidth(), cardImage.getIconHeight());
            mainPane.setPreferredSize(new Dimension(cardImage.getIconWidth(), cardImage.getIconHeight()));
        }
        mainPane.repaint();
    }
    
    /**
     * Displays the full size card image.
     */
    public void showOriginalSizeImage()
    {
        if (row == null)
        {
            return;
        }
        
        rowToImageSizeHash.put(row.hashCode(), 1);
        
        cardImage = row.getFullSizeImage();
        if (cardImage != null)
        {
            cardImageLabel.setIcon(cardImage);
            reduceMI.setSelected(false);
            origMI.setSelected(true);
            if (cardImage != null)
            {
                mainPane.setSize(cardImage.getIconWidth(), cardImage.getIconHeight());
                mainPane.setPreferredSize(new Dimension(cardImage.getIconWidth(), cardImage.getIconHeight()));
            }
        } else
        {
            WorkbenchTask.showLoadStatus(row);
        }
        mainPane.repaint();
    }
    
    /**
     * When there is no image the user can press "load" and load a new image.
     * @param al the action listener for the load button
     */
    public void installLoadActionListener(final ActionListener al)
    {
        loadImgBtn.addActionListener(al);
        replaceMI.addActionListener(al);
    }
    
    /**
     * When there is no image the user can press "load" and load a new image.
     * @param al the action listener for the load button
     */
    public void installClearActionListener(final ActionListener al)
    {
        clearMI.addActionListener(al);
    }
    
    /**
     * Registers the given ActionListener as action to perform when the window is closed.
     * @param al the action listener for the close button
     */
    public void installCloseActionListener(final ActionListener al)
    {
        closeMI.addActionListener(al);
    }
    
    /**
     * Enables Menus per image state.
     * @param enable true/false
     */
    protected void enableMenus(final boolean enable)
    {
        viewMenu.setEnabled(enable);
        imageMenu.setEnabled(enable);
        clearMI.setEnabled(enable);
        replaceMI.setEnabled(enable);
        origMI.setEnabled(enable);
        reduceMI.setEnabled(enable);
    }
    
    /**
     * Sests the row into the frame.
     * @param row the row
     */
    public void setRow(final WorkbenchRow row)
    {
        this.row = row;
        if (row != null)
        {
            Integer lastDisplayedSize = rowToImageSizeHash.get(row.hashCode());
            if (lastDisplayedSize == null || lastDisplayedSize == -1)
            {
                showReducedImage();
            }
            else
            {
                showOriginalSizeImage();
            }
            
            // card image should have been set by now by either showReducedImage() or showOriginalSizeImage()
            if (cardImage == null)
            {
                if (showingCardImageLabel)
                {
                    // swap out the cardImageLabel for the noCardImageMessagePanel
                    mainPane.remove(cardImageLabel);
                    mainPane.add(noCardImageMessagePanel, BorderLayout.CENTER);
                    showingCardImageLabel = false;
                }
                enableMenus(false);
            }
            else
            {
                if (!showingCardImageLabel)
                {
                    // swap out the noCardImageMessagePanel for the cardImageLabel
                    mainPane.remove(noCardImageMessagePanel);
                    mainPane.add(cardImageLabel, BorderLayout.CENTER);
                    showingCardImageLabel = true;
                }
                cardImageLabel.setText(null);
                enableMenus(true);
            }
            
        } else
        {
            if (!showingCardImageLabel)
            {
                // swap out the noCardImageMessagePanel for the cardImageLabel
                mainPane.remove(noCardImageMessagePanel);
                mainPane.add(cardImageLabel, BorderLayout.CENTER);
                showingCardImageLabel = true;
            }
            
            cardImage = null;
            cardImageLabel.setText("No row selected"); // XXX I18N
        }
        validate();
        repaint();
    }

    /* (non-Javadoc)
     * @see java.awt.Window#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean b)
    {
        row = null;
        cardImageLabel.setIcon(null);
        cardImageLabel.setText(null);
        cardImage = null;
        super.setVisible(b);
    }
}

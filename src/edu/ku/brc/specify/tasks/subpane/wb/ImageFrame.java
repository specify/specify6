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
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.ImageFilter;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;

/**
 * This frame is used to display a set of images linked to a record in a workbench data set.
 * 
 * @author jstewart
 *
 * @code_status Beta
 */
public class ImageFrame extends JFrame
{
    private static final Logger log                      = Logger.getLogger(ImageFrame.class);
            
    protected JLabel          cardImageLabel             = new JLabel("", SwingConstants.CENTER);
    protected JProgressBar    progress                   = new JProgressBar();
    protected WorkbenchRow    row                        = null;
    protected WorkbenchPaneSS wbPane                     = null;
    protected int             imageIndex                 = -1;
    protected Workbench       workbench;
    
    protected JPanel       noCardImageMessagePanel    = null;
    protected boolean      showingCardImageLabel      = true;
    protected ImageIcon    cardImage                  = null;
    protected JButton      loadImgBtn                 = null;
    protected JPanel       mainPane;
    protected JScrollPane  scrollPane;
    protected JStatusBar   statusBar;
    protected JSlider      indexSlider;

    protected JMenu             viewMenu;
    protected JMenu             imageMenu;
    protected JMenuItem         closeMI;
    protected JMenuItem         replaceMI;
    protected JMenuItem         deleteMI;
    protected JMenuItem         addMI;
    protected JRadioButtonMenuItem origMI;
    protected JRadioButtonMenuItem reduceMI;
    protected JCheckBoxMenuItem alwaysOnTopMI;
    
    protected static int REDUCED_SIZE = -1;
    protected static int FULL_SIZE    =  1;
    
    /** This hash keeps track of the size that a given image was last displayed as.  If an image is displayed as full size, we add an entry
     * to this hash where the key is row.hashCode() and the value is 1.  If the image is displayed as reduced size, the value is -1.  (We
     * don't hash from WorkbenchRows to the size in order to not hold a handle to the WorkbenchRow objects.)
     */
    protected Hashtable<Integer,Integer> rowToImageSizeHash = new Hashtable<Integer, Integer>();
    
    /**
     * Constructor. 
     */
    public ImageFrame(final int mapSize, final WorkbenchPaneSS wbPane, final Workbench workbench)
    {
        this.workbench = workbench;
        this.wbPane = wbPane;
        
        setIconImage(IconManager.getImage("AppIcon").getImage());
        
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
        
        statusBar = new JStatusBar();
        
        indexSlider = new JSlider(SwingConstants.HORIZONTAL);
        indexSlider.setMajorTickSpacing(1);
        indexSlider.setPaintTicks(true);
        indexSlider.setPaintLabels(true);
        indexSlider.setSnapToTicks(true);
        indexSlider.setMinimum(1);
        indexSlider.setMaximum(1);
        indexSlider.getModel().addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent ce)
            {
                int value = indexSlider.getValue();
                setImageIndex(value-1);
            }
        });
        indexSlider.setEnabled(false);
        
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        
        southPanel.add(indexSlider,BorderLayout.NORTH);
        southPanel.add(statusBar, BorderLayout.SOUTH);

        JPanel basePanel = new JPanel();
        basePanel.setLayout(new BorderLayout());
        basePanel.add(scrollPane,BorderLayout.CENTER);
        basePanel.add(southPanel,BorderLayout.SOUTH);
        
        setContentPane(basePanel);
        
        JMenuBar  menuBar   = new JMenuBar();
        JMenu     fileMenu  = UIHelper.createMenu(menuBar, "File", "FileMneu");
        JMenuItem importImagesMI  = UIHelper.createMenuItem(fileMenu, getResourceString("WB_IMPORT_CARDS"), getResourceString("WB_IMPORT_CARDS_MNEU"), "", true, null);
        
        closeMI = UIHelper.createMenuItem(fileMenu, getResourceString("Close"), getResourceString("CloseMneu"), "", true, null);
        closeMI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                indexSlider.setEnabled(false);
                ImageFrame.this.setVisible(false);
            }
        });
        
        viewMenu  = UIHelper.createMenu(menuBar, "View", "ViewMneu");
        
        reduceMI = UIHelper.createRadioButtonMenuItem(viewMenu, "WB_REDUCED_SIZE", "ReducedSizeMneu", "", true, null);
        reduceMI.setSelected(true);
        reduceMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (row == null)
                {
                    return;
                }
                
                // simply 'remember' that we want to show reduced images for this row
                rowToImageSizeHash.put(row.hashCode(), REDUCED_SIZE);
                // then 'reshow' the current image
                showImage();
            }
        });
        
        origMI = UIHelper.createRadioButtonMenuItem(viewMenu, "WB_ORIG_SIZE", "OrigMneu", "", true, null);
        origMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (row == null)
                {
                    return;
                }
                
                // simply 'remember' that we want to show fill size images for this row
                rowToImageSizeHash.put(row.hashCode(), FULL_SIZE);
                // then 'reshow' the current image
                showImage();
            }
        });
        
        ButtonGroup btnGrp = new ButtonGroup();
        btnGrp.add(reduceMI);
        btnGrp.add(origMI);        
        
        viewMenu.addSeparator();
        
        alwaysOnTopMI = UIHelper.createCheckBoxMenuItem(viewMenu, "WB_ALWAYS_ON_TOP", null, "", true, null);
        alwaysOnTopMI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                ImageFrame.this.setAlwaysOnTop(alwaysOnTopMI.isSelected());
            }
        });
        
        importImagesMI.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                WorkbenchTask workbenchTask = (WorkbenchTask)TaskMgr.getDefaultTaskable();
                workbenchTask.importCardImages(workbench);
            }
        });
        
        ActionListener deleteImg = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                deleteImage();
            }
        };
        
        ActionListener replaceImg = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                replaceImage();
            }
        };
        
        ActionListener addImg = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addImages();
            }
        };
        
        imageMenu = UIHelper.createMenu(menuBar, "Image", "ImageMneu");
        deleteMI   = UIHelper.createMenuItem(imageMenu, getResourceString("WB_DEL_IMG_LINK"), getResourceString("WB_DEL_IMG_LINK_MNEU"), "", true, deleteImg);
        replaceMI = UIHelper.createMenuItem(imageMenu, getResourceString("WB_REPLACE_IMG"), getResourceString("WB_REPLACE_IMG_MNEU"), "", true, replaceImg);
        addMI     = UIHelper.createMenuItem(imageMenu, getResourceString("WB_ADD_IMG"), getResourceString("WB_ADD_IMG_MNEM"), "", true, addImg);
        
        loadImgBtn.addActionListener(addImg);
        
        JMenu helpMenu = new JMenu(getResourceString("Help"));
        menuBar.add(HelpMgr.createHelpMenuItem(helpMenu, getResourceString("WB_IMAGE_WINDOW")));

        enableMenus(false);
        
        setJMenuBar(menuBar);
        
        pack();
        
        //HelpMgr.setHelpID(this, "OnRampImageWindow");
    }
    
    /**
     * Sets text into the statubar of the image frame.
     * @param text the message
     */
    public void setStatusBarText(final String text)
    {
        statusBar.setText(text);
    }
    
    /**
     * Returns the index of the selected image
     * 
     * @return
     */
    public int getImageIndex()
    {
        return imageIndex;
    }

    /**
     * @param imageIndex
     */
    public void setImageIndex(int imageIndex)
    {
        this.imageIndex = imageIndex;
        showImage();
    }

//    /**
//     * Clear the hash so when when it is displayed later without an image it calls the reduced one. 
//     */
//    public void clearImage()
//    {
//        if (row != null)
//        {
//            rowToImageSizeHash.remove(row.hashCode());
//        }
//        // swap out the cardImageLabel for the noCardImageMessagePanel
//        mainPane.remove(cardImageLabel);
//        mainPane.add(noCardImageMessagePanel, BorderLayout.CENTER);
//        showingCardImageLabel = false;
//        
//        enableMenus(false);
//        validate();
//        repaint();
//    }
//    
//    /**
//     * Displays the card image in a reduced size.
//     */
//    public void showReducedImage()
//    {
//        if (row == null)
//        {
//            return;
//        }
//        
//        rowToImageSizeHash.put(row.hashCode(), -1);
//        
//        cardImage = row.getCardImage();
//        cardImageLabel.setIcon(cardImage);
//        if (cardImage != null)
//        {
//            cardImageLabel.setSize(cardImage.getIconWidth(), cardImage.getIconHeight());
//            mainPane.setSize(cardImage.getIconWidth(), cardImage.getIconHeight());
//            mainPane.setPreferredSize(new Dimension(cardImage.getIconWidth(), cardImage.getIconHeight()));
//        }
//        mainPane.repaint();
//    }
//    
//    /**
//     * Displays the full size card image.
//     */
//    public void showOriginalSizeImage()
//    {
//        if (row == null)
//        {
//            return;
//        }
//        
//        rowToImageSizeHash.put(row.hashCode(), 1);
//        
//        cardImage = row.getFullSizeImage();
//        if (cardImage != null)
//        {
//            cardImageLabel.setIcon(cardImage);
//            if (cardImage != null)
//            {
//                mainPane.setSize(cardImage.getIconWidth(), cardImage.getIconHeight());
//                mainPane.setPreferredSize(new Dimension(cardImage.getIconWidth(), cardImage.getIconHeight()));
//            }
//        } else
//        {
//            WorkbenchTask.showLoadStatus(row, false);
//        }
//        mainPane.repaint();
//    }
    
    protected void showImage()
    {
        if (row == null)
        {
            return;
        }
        
        Set<WorkbenchRowImage> rowImages = row.getWorkbenchRowImages();
        if (rowImages == null || rowImages.size() == 0)
        {
            noImagesLinked();
            return;
        }
        
        // adjust the imageIndex to be within the proper bounds (in order to avoid a few error possibilities)
        if (imageIndex < 0)
        {
            imageIndex = 0;
        }
        else if (imageIndex > rowImages.size()-1)
        {
            imageIndex = rowImages.size()-1;
        }
        
        indexSlider.setMinimum(1);
        indexSlider.setMaximum(rowImages.size());
        indexSlider.setValue(imageIndex+1);
        if (rowImages.size() > 1)
        {
            indexSlider.setEnabled(true);
        }
        else
        {
            indexSlider.setEnabled(false);
        }

        // try to get the appropriate WorkbenchRowImage
        WorkbenchRowImage rowImage = row.getRowImage(imageIndex);
        if (rowImage == null)
        {
            // What do we do here?
            // This should never happen.
            // There were images available, but none of them had the right index.
            // Just give the first one, I guess.
            rowImage = rowImages.iterator().next();
            imageIndex = 0;
            statusBar.setWarningMessage("Unable to locate an image with the proper index.  Showing first image."); // XXX i18n
        }

        // at this point, we know at least one image is available
        
        // update the title and status bar
        String fullFilePath = rowImage.getCardImageFullPath();
        String filename = FilenameUtils.getName(fullFilePath);
        setTitle( String.format(getResourceString("WB_IMAGE_X_OF_Y"), imageIndex+1, rowImages.size()) + ((filename != null) ? ": " + filename : "") );
        setStatusBarText(fullFilePath);
        
        ImageIcon image = null;
        
        Integer lastDisplayedSize = rowToImageSizeHash.get(row.hashCode());
        if (lastDisplayedSize == null || lastDisplayedSize == REDUCED_SIZE)
        {
            image = rowImage.getImage();
        }
        else
        {
            image = rowImage.getFullSizeImage();
        }
        
        cardImageLabel.setIcon(image);
        
        if (image == null) // no image available
        {
            statusBar.setErrorMessage("Unable to load image"); // XXX i18n
        }
        else // we've got an image
        {
            // if we're NOT currently showing the image label
            if (!showingCardImageLabel)
            {
                // swap out the "no image" text for the image label
                mainPane.remove(noCardImageMessagePanel);
                mainPane.add(cardImageLabel, BorderLayout.CENTER);
                showingCardImageLabel = true;
            }
            
            int w = image.getIconWidth();
            int h = image.getIconHeight();
            cardImageLabel.setText(null);
            cardImageLabel.setSize(w, h);
            mainPane.setSize(w, h);
            mainPane.setPreferredSize(new Dimension(w, h));
            enableMenus(true);
            
            mainPane.validate();
            mainPane.repaint();
        }
    }
    
    /**
     * Handles UI tasks related to showing the user that no images are available for display.
     */
    protected void noImagesLinked()
    {
        // are we currently showing the image label
        if (showingCardImageLabel)
        {
            // remove the image label and show the "no images" text
            // swap out the cardImageLabel for the noCardImageMessagePanel
            mainPane.remove(cardImageLabel);
            mainPane.add(noCardImageMessagePanel, BorderLayout.CENTER);
            showingCardImageLabel = false;
        }
        indexSlider.setEnabled(false);
        setTitle(getResourceString("WB_NO_IMAGES_LINKED"));
        setStatusBarText(null);
        enableMenus(false);
        validate();
        repaint();
    }
    
    /**
     * Enables Menus per image state.
     * @param enable true/false
     */
    protected void enableMenus(final boolean enable)
    {
        deleteMI.setEnabled(enable);
        replaceMI.setEnabled(enable);
        origMI.setEnabled(enable);
        reduceMI.setEnabled(enable);
    }
    
    /**
     * Adds a new image to the current {@link WorkbenchRow}.
     */
    public void addImages()
    {
        UsageTracker.incrUsageCount("WB.AddWBRowImage");
        File[] imageFiles = askUserForImageFiles();
        if (imageFiles == null || imageFiles.length == 0)
        {
            return;
        }
        
        log.debug("addImages: " + imageFiles.length + " files selected");
        for (File f: imageFiles)
        {
            try
            {
                int newIndex = row.addImage(f);
                wbPane.setChanged(true);
                this.imageIndex = newIndex;
                showImage();
            }
            catch (IOException e)
            {
                statusBar.setErrorMessage("Exception while adding a new image", e);
            }
        }
        wbPane.repaint();
    }
    
    public void replaceImage()
    {
        UsageTracker.incrUsageCount("WB.EditWBRowImage");
        File imageFile = askUserForImageFile();
        log.debug("replaceImage: " + ((imageFile!=null) ? imageFile.getAbsolutePath() : "NULL"));
        try
        {
            row.setImage(imageIndex, imageFile);
            wbPane.setChanged(true);
            
            // call showImage() to update the visible image
            showImage();
        }
        catch (IOException e)
        {
            statusBar.setErrorMessage("Exception while replacing image", e);
        }
    }
    
    public void deleteImage()
    {
        UsageTracker.incrUsageCount("WB.DeleteWBRowImage");
        row.deleteImage(imageIndex);
        imageIndex--;

        // call showImage() to update the visible image
        showImage();
        
        wbPane.setChanged(true);
        wbPane.repaint();
    }
    
    protected File askUserForImageFile()
    {
        ImageFilter imageFilter = new ImageFilter();
        JFileChooser fileChooser = new JFileChooser(WorkbenchTask.getDefaultDirPath(WorkbenchTask.IMAGES_FILE_PATH));
        fileChooser.setFileFilter(imageFilter);
        fileChooser.setDialogTitle(getResourceString("WB_CHOOSE_IMAGE"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int          userAction  = fileChooser.showOpenDialog(this);
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        
        // remember the directory the user was last in
        localPrefs.put(WorkbenchTask.IMAGES_FILE_PATH, fileChooser.getCurrentDirectory().getAbsolutePath());
        
        if (userAction == JFileChooser.APPROVE_OPTION)
        {
            String fullPath = fileChooser.getSelectedFile().getAbsolutePath();
            if (imageFilter.isImageFile(fullPath))
            {
                return fileChooser.getSelectedFile();
            }
        }
        
        // if for any reason we got to this point...
        return null;
    }
    
    protected File[] askUserForImageFiles()
    {
        ImageFilter imageFilter = new ImageFilter();
        JFileChooser fileChooser = new JFileChooser(WorkbenchTask.getDefaultDirPath(WorkbenchTask.IMAGES_FILE_PATH));
        fileChooser.setFileFilter(imageFilter);
        fileChooser.setDialogTitle(getResourceString("WB_CHOOSE_IMAGES"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);

        int          userAction  = fileChooser.showOpenDialog(this);
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        
        // remember the directory the user was last in
        localPrefs.put(WorkbenchTask.IMAGES_FILE_PATH, fileChooser.getCurrentDirectory().getAbsolutePath());

        if (userAction == JFileChooser.APPROVE_OPTION)
        {
            return fileChooser.getSelectedFiles();
        }
        
        // if for any reason we got to this point...
        return null;
    }
    
    /**
     * Sests the row into the frame.
     * @param row the row
     */
    public void setRow(final WorkbenchRow row)
    {
        // if nothing changed, ignore this call
        if (this.row == row)
        {
            return;
        }
        
        this.row = row;
        
        // set the index so the first image is displayed
        imageIndex = 0;
        if (row != null)
        {
            showImage();
        }
        else
        {
            setTitle(getResourceString("WB_NO_ROW_SELECTED"));
            setStatusBarText(null);
        }
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

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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.ImageFilter;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchRowImage;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.DefaultModifiableListModel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.util.thumbnails.ImageThumbnailGenerator;

/**
 * This frame is used to display a set of images linked to a record in a workbench data set.
 * 
 * @author jstewart
 *
 * @code_status Beta
 */
public class ImageFrame extends JFrame
{
    private static final Logger log                        = Logger.getLogger(ImageFrame.class);
            
    protected JProgressBar      progress                   = new JProgressBar();
    protected WorkbenchRow      row;
    protected WorkbenchPaneSS   wbPane;
    protected int               imageIndex                 = -1;
    protected Workbench         workbench;

    // the three things that are viewed in the main display area of the frame
    protected JLabel            cardImageLabel             = new JLabel("", SwingConstants.CENTER);
    protected JPanel            noCardImageMessagePanel;
    protected JPanel            noRowSelectedMessagePanel;
    
    protected ImageIcon         cardImage;
    protected JButton           loadImgBtn;
    protected JPanel            mainPane;
    protected JScrollPane       scrollPane;
    protected JStatusBar        statusBar;
    protected ThumbnailTray     tray;

    protected JMenu             viewMenu;
    protected JMenu             imageMenu;
    protected JMenuItem         closeMI;
    protected JMenuItem         replaceMI;
    protected JMenuItem         deleteMI;
    protected JMenuItem         addMI;
    protected JCheckBoxMenuItem alwaysOnTopMI;
    protected JRadioButtonMenuItem origMI;
    protected JRadioButtonMenuItem reduceMI;
    
    protected ImageIcon         defaultThumbIcon;
    
    protected ImageThumbnailGenerator thumbnailer;
    
    protected boolean           allowCloseWindow;
    
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
        this.allowCloseWindow = true;
        this.defaultThumbIcon = IconManager.getIcon("image", IconSize.Std32);
        
        setIconImage(IconManager.getImage("AppIcon").getImage());
        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if (allowCloseWindow)
                {
                    setVisible(false);
                    wbPane.setImageFrameVisible(false);
                }
            }
        });
        
        Dimension minSize = new Dimension(mapSize, mapSize);
        cardImageLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:p:g,c:p,f:p:g", "f:p:g,p,5px,p,f:p:g"));
        CellConstraints cc      = new CellConstraints();
        
        loadImgBtn = new JButton(getResourceString("WB_LOAD_NEW_IMAGE"));
        
        builder.add(new JLabel(getResourceString("WB_NO_IMAGE_ROW"), SwingConstants.CENTER), cc.xy(2, 2));
        builder.add(loadImgBtn, cc.xy(2, 4));
        
        noCardImageMessagePanel = builder.getPanel();
        noCardImageMessagePanel.setPreferredSize(minSize);
        noCardImageMessagePanel.setSize(minSize);
        
        builder = new PanelBuilder(new FormLayout("f:p:g,c:p,f:p:g", "f:p:g,c:p,f:p:g"));
        builder.add(new JLabel(getResourceString("WB_NO_ROW_SELECTED"), SwingConstants.CENTER), cc.xy(2,2));
        
        noRowSelectedMessagePanel = builder.getPanel();
        noRowSelectedMessagePanel.setPreferredSize(minSize);
        noRowSelectedMessagePanel.setSize(minSize);
        
        mainPane = new JPanel(new BorderLayout());
        mainPane.setSize(minSize);
        mainPane.setPreferredSize(minSize);
        mainPane.setMinimumSize(minSize);
        
        mainPane.add(cardImageLabel, BorderLayout.CENTER);
        scrollPane = new JScrollPane(mainPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        statusBar = new JStatusBar();
        
        thumbnailer = new ImageThumbnailGenerator();
        thumbnailer.setMaxHeight(32);
        thumbnailer.setMaxWidth(32);
        thumbnailer.setQuality(1);
        
        tray = new ThumbnailTray();
        tray.getModel().removeAllElements();
        tray.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting())
                {
                    return;
                }
                
                setImageIndex(tray.getSelectedIndex());
            }
        });
        
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        
        //southPanel.add(indexSlider,BorderLayout.NORTH);
        southPanel.add(tray, BorderLayout.CENTER);
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
        tray.setSelectedIndex(imageIndex);
        showImage();
    }

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
            tray.setSelectedIndex(imageIndex);
        }
        else if (imageIndex > rowImages.size()-1)
        {
            imageIndex = rowImages.size()-1;
            tray.setSelectedIndex(imageIndex);
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
            tray.setSelectedIndex(imageIndex);
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
            reduceMI.setSelected(true);
        }
        else
        {
            image = rowImage.getFullSizeImage();
            origMI.setSelected(true);
        }
        
        cardImageLabel.setIcon(image);
        
        if (image == null) // no image available
        {
            statusBar.setErrorMessage("Unable to load image"); // XXX i18n
        }
        else // we've got an image
        {
            // this method is simpler than tracking what we're showing and changing to the correct view
            mainPane.remove(noRowSelectedMessagePanel);
            mainPane.remove(noCardImageMessagePanel);
            mainPane.remove(cardImageLabel);
            mainPane.add(cardImageLabel);
            
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
        // this method is simpler than tracking what we're showing and changing to the correct view
        mainPane.remove(noRowSelectedMessagePanel);
        mainPane.remove(noCardImageMessagePanel);
        mainPane.remove(cardImageLabel);
        mainPane.add(noCardImageMessagePanel);

        tray.getModel().removeAllElements();
        setTitle(getResourceString("WB_NO_IMAGES_LINKED"));
        setStatusBarText(null);
        enableMenus(false);
        Dimension prefSize = noCardImageMessagePanel.getPreferredSize();
        mainPane.setSize(prefSize);
        mainPane.setPreferredSize(prefSize);
        validate();
        repaint();
    }
    
    protected void noRowSelected()
    {
        // this method is simpler than tracking what we're showing and changing to the correct view
        mainPane.remove(noRowSelectedMessagePanel);
        mainPane.remove(noCardImageMessagePanel);
        mainPane.remove(cardImageLabel);
        mainPane.add(noRowSelectedMessagePanel);

        tray.getModel().removeAllElements();
        setTitle(getResourceString("WB_NO_ROW_SELECTED"));
        setStatusBarText(null);
        enableMenus(false);
        Dimension prefSize = noRowSelectedMessagePanel.getPreferredSize();
        mainPane.setSize(prefSize);
        mainPane.setPreferredSize(prefSize);
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
        
        final WorkbenchRow wbRow = this.row;
        
        final File[] imageFiles = askUserForImageFiles();
        if (imageFiles == null || imageFiles.length == 0)
        {
            return;
        }
        
        allowCloseWindow = false;
        this.setEnabled(false);
        
        log.debug("addImages: " + imageFiles.length + " files selected");
        
        SwingWorker loadImagesTask = new SwingWorker()
        {
            private List<WorkbenchRowImage> rowImagesNeedingThumbnails = new Vector<WorkbenchRowImage>();
            
            @Override
            public Object construct()
            {
                Vector<Integer> newIndexes = new Vector<Integer>();
                for (int i = 0; i < imageFiles.length; ++i)
                {
                    final int index = i;
                    File f = imageFiles[i];
                    try
                    {
                        int newIndex = wbRow.addImage(f);

                        newIndexes.add(newIndex);
                        
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_ADDING_IMAGE_X_OF_Y"), index+1, imageFiles.length), 24);
                            }
                        });
                        
                        WorkbenchRowImage rowImage = row.getRowImage(newIndex);
                        rowImagesNeedingThumbnails.add(rowImage);
                        wbPane.setChanged(true);
                    }
                    catch (IOException e)
                    {
                        statusBar.setErrorMessage("Exception while adding a new image", e);
                    }
                }
                Collections.sort(newIndexes);
                return newIndexes;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void finished()
            {
                Object retVal = get();
                
                if (retVal != null && retVal instanceof List)
                {
                    Vector<Integer> newIndexes = (Vector<Integer>)retVal;
                    int lowestNewIndex = newIndexes.get(0);

                    // add a bunch of placeholder icons
                    for (int i = 0; i < newIndexes.size(); ++i)
                    {
                        tray.getModel().add(defaultThumbIcon);
                    }
                    wbPane.setChanged(true);

                    if (lowestNewIndex < row.getWorkbenchRowImages().size())
                    {
                        imageIndex = lowestNewIndex;
                        tray.setSelectedIndex(imageIndex);
                        showImage();
                    }

                    generateThumbnailsInBackground(rowImagesNeedingThumbnails);
                }
                
                UIRegistry.clearGlassPaneMsg();
                setEnabled(true);
                allowCloseWindow = true;
                wbPane.repaint();
            }
        };
        loadImagesTask.start();
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
            
            WorkbenchRowImage rowImage = row.getRowImage(imageIndex);
            Vector<WorkbenchRowImage> needNewThumbs = new Vector<WorkbenchRowImage>();
            needNewThumbs.add(rowImage);
            generateThumbnailsInBackground(needNewThumbs);
            
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
        tray.getModel().remove(imageIndex);
        imageIndex--;
        tray.setSelectedIndex(imageIndex);

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
        
        if (row == null)
        {
            noRowSelected();
            return;
        }

        // put the correct thumbs in the tray UI
        tray.getModel().removeAllElements();
        
        // add the default, pre-thumbnail-generation icons to the tray for each image
        Set<WorkbenchRowImage> rowImages = row.getWorkbenchRowImages();
        for (int i = 0; i < rowImages.size(); ++i)
        {
            tray.getModel().add(defaultThumbIcon);
        }
        
        // start the background thumbnail generation process
        // replacing the default icons as thumbnails become available
        List<WorkbenchRowImage> rowImagesNeedingThumbnails = new Vector<WorkbenchRowImage>();
        
        for (WorkbenchRowImage img: rowImages)
        {
            // load any cached thumbnails
            ImageIcon thumb = img.getThumbnail();
            if (thumb != null)
            {
                tray.getModel().set(img.getImageOrder(), thumb);
            }
            else // generate any missing thumbnails
            {
                log.debug("Workbench row image is missing its thumbnail.  Adding it to the list of row images for thumbnail generation work.  " + img);
                rowImagesNeedingThumbnails.add(img);
            }
        }
        
        generateThumbnailsInBackground(rowImagesNeedingThumbnails);
        
        // set the index so the first image is displayed
        imageIndex = 0;
        tray.setSelectedIndex(imageIndex);
        showImage();
    }
    
    protected ImageIcon generateThumbnail(WorkbenchRowImage rowImage) throws IOException
    {
        File orig = new File(rowImage.getCardImageFullPath());
        byte[] origData = FileUtils.readFileToByteArray(orig);
        byte[] thumbData = thumbnailer.generateThumbnail(origData);
        return new ImageIcon(thumbData);
    }
    
    protected void generateThumbnailsInBackground(final List<WorkbenchRowImage> rowImages)
    {
        Collections.sort(rowImages);
        
        Thread thumbGenTask = new Thread()
        {
            @Override
            @SuppressWarnings("synthetic-access")
            public void run()
            {
                // This is just a weird workaround.
                // For some reason, using the List directly resulted in a ConcurrentModificationException everytime
                // this method was called from addImages().
                // It doesn't look like it should throw an exception at all.
                WorkbenchRowImage[] imgs = new WorkbenchRowImage[rowImages.size()];
                rowImages.toArray(imgs);
                for (WorkbenchRowImage rowImage: imgs)
                {
                    final WorkbenchRowImage ri = rowImage;
                    try
                    {
                        final ImageIcon thumb = generateThumbnail(rowImage);
                        
                        // cache it so we don't have to do this again and again
                        rowImage.setThumbnail(thumb);
                        
                        // update the UI
                        Runnable updateTrayUI = new Runnable()
                        {
                            public void run()
                            {
                                log.info("Thumbnail generation complete.  Updating the UI.  " + ri);
                                if (row == ri.getWorkbenchRow())
                                {
                                    tray.getModel().set(ri.getImageOrder(), thumb);
                                    tray.repaint();
                                }
                            }
                        };
                        SwingUtilities.invokeLater(updateTrayUI);
                    }
                    catch (IOException e)
                    {
                        log.warn("Failed to generate a thumbnail for " + rowImage.getCardImageFullPath(), e);
                    }
                }
            }
        };
        
        thumbGenTask.setName("GenThumbs");
        thumbGenTask.setDaemon(true);
        thumbGenTask.setPriority(Thread.MIN_PRIORITY);
        thumbGenTask.start();
    }

    /*
     * (non-Javadoc)
     * 
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
    
    class ThumbnailTray extends JPanel
    {
        /** A JList used to display the thumbnails representing the items. */
        protected JList listWidget;
        /** The model holding the included items. */
        protected DefaultModifiableListModel<Icon> listModel;
        /** A JScrollPane containing the iconListWidget. */
        protected JScrollPane listScrollPane;
     
        protected int minHeight = 64;

        /**
         * Creates a new IconTray containing zero items.
         */
        public ThumbnailTray()
        {
            listModel = new DefaultModifiableListModel<Icon>();
            ListCellRenderer renderer = new DefaultListCellRenderer()
            {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
                {
                    JLabel l = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Icon)
                    {
                        l.setText(null);
                        l.setIcon((Icon)value);
                    }
                    return l;
                }
            };
            listWidget = new JList(listModel);
            listWidget.setCellRenderer(renderer);
            listWidget.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            listWidget.setVisibleRowCount(1);
            listWidget.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            JPanel listPanel = new JPanel();
            listPanel.setBackground(listWidget.getBackground());
            listPanel.setLayout(new BoxLayout(listPanel,BoxLayout.LINE_AXIS));
            listPanel.add(Box.createHorizontalGlue());
            listPanel.add(listWidget);
            listPanel.add(Box.createHorizontalGlue());
            
            listScrollPane = new JScrollPane(listPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            
            this.setLayout(new BorderLayout());
            this.add(listScrollPane,BorderLayout.CENTER);
        }
        
        public DefaultModifiableListModel<Icon> getModel()
        {
            return listModel;
        }

        /**
         * Sets the height of every cell in the list.
         *
         * @param height an integer giving the height, in pixels, for all cells in this list
         * @see JList#setFixedCellHeight(int)
         */
        public synchronized void setFixedCellHeight(int height)
        {
            listWidget.setFixedCellHeight(height);
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#getPreferredSize()
         */
        @Override
        public Dimension getPreferredSize()
        {
            // need to set the min height to something other than 0 so
            // that empty trays don't get flattened by containers that
            // use preferred size
            int height = minHeight;
            Dimension d = super.getPreferredSize();        
            if ((int)d.getHeight() > height)
            {
                height = (int)d.getHeight();
            }
            return new Dimension(this.getWidth(),height);
        }

        /**
         * @see JList#getSelectedIndex()
         * @return the index of the selection
         */
        public int getSelectedIndex()
        {
            return listWidget.getSelectedIndex();
        }
        
        /**
         * @see JList#setSelectedIndex(int)
         * @param index the index of the selection
         */
        public void setSelectedIndex(int index)
        {
            if (row == null || index < 0 || index > row.getWorkbenchRowImages().size()-1)
            {
                return;
            }

            listWidget.setSelectedIndex(index);

            Rectangle cellBounds = listWidget.getUI().getCellBounds(listWidget, index, index);
            if (cellBounds != null)
            {
                listWidget.scrollRectToVisible(cellBounds);
            }
        }

        /**
         * @see JList#addListSelectionListener(ListSelectionListener)
         * @param listener a {@link ListSelectionListener}
         */
        public void addListSelectionListener(ListSelectionListener listener)
        {
            listWidget.addListSelectionListener(listener);
        }

        /**
         * @see JList#removeListSelectionListener(ListSelectionListener)
         * @param listener a {@link ListSelectionListener}
         */
        public void removeListSelectionListener(ListSelectionListener listener)
        {
            listWidget.removeListSelectionListener(listener);
        }
    }
}

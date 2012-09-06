/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * An implementation of AttachmentManagerIface that uses the underlying filesystem to
 * provide storage of the attachments and thumbnails.
 *
 * @author jstewart
 * @author rods
 * @code_status Beta
 */
public class FileStoreAttachmentManager implements AttachmentManagerIface
{
    private static final Logger  log   = Logger.getLogger(FileStoreAttachmentManager.class);
    
    private static final String ORIGINAL   = "originals";
    private static final String THUMBNAILS = "thumbnails";
    
    
    /** The base directory path of where the files will be stored. */
    protected String baseDirectory;
    
    /** The directory inside the base that will store the original files. */
    protected File originalsDir;
    
    /** The directory inside the base that will store the thumbnail files. */
    protected File thumbsDir;
    
    /** 
     * A collection of all files created by calls to setStorageLocationIntoAttachment
     * that have not yet been filled by calls to storeAttachmentFile.
     */
    protected Vector<String> unfilledFiles;
    
    /**
     * Creates a new instance, setting baseDirectory to null.
     * @throws IOException if either of the storage directories is not writable
     */
    public FileStoreAttachmentManager(final File baseDirectory) throws IOException
    {
        setDirectory(baseDirectory);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#isInitialized(java.lang.String)
     */
    @Override
    public boolean isInitialized(final String urlStr)
    {
        return false;
    }

    /**
     * @param baseDir
     * @throws IOException
     */
    public void setDirectory(final File baseDir) throws IOException
    {
        this.baseDirectory = baseDir.getAbsolutePath();
        this.originalsDir  = new File(baseDirectory + File.separator + ORIGINAL);
        this.thumbsDir     = new File(baseDirectory + File.separator + THUMBNAILS);
        
        // create the directories, if they don't already exist
        if (!originalsDir.exists() && !originalsDir.mkdirs())
        {
            log.error("setDirectory - failed to create originals["+originalsDir.getAbsolutePath()+"]");
        }
        
        if (!thumbsDir.exists() && !thumbsDir.mkdirs())
        {
            log.error("setDirectory - failed to create thumbsDir["+thumbsDir.getAbsolutePath()+"]");
        }
        
        // make sure the directories are writable
        if (!originalsDir.canWrite())
        {
            throw new IOException("Storage directory not writable: " + originalsDir.getAbsolutePath());
        }
        if (!thumbsDir.canWrite())
        {
            throw new IOException("Storage directory not writable: " + originalsDir.getAbsolutePath());
        }
        
        unfilledFiles = new Vector<String>();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getDirectory()
     */
    @Override
    public File getDirectory()
    {
        return new File(this.baseDirectory);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#setStorageLocationIntoAttachment(edu.ku.brc.specify.datamodel.Attachment, boolean)
     */
    @Override
    public boolean setStorageLocationIntoAttachment(final Attachment attachment, final boolean doDisplayErrors)
    {
        String attName    = attachment.getOrigFilename();
        int    lastPeriod = attName.lastIndexOf('.');
        String suffix     = ".att";
        
        if (lastPeriod != -1)
        {
            // Make sure the file extension (if any) remains the same so the host
            // filesystem still sees the files as the proper types.  This is simply
            // to make the files browsable from a system file browser.
            suffix = ".att" + attName.substring(lastPeriod);
        }
        
        String errMsg          = null;
        String storageFilename = "";
        try
        {
            if (originalsDir == null || !originalsDir.exists() || !originalsDir.canWrite())
            {
                errMsg = UIRegistry.getLocalizedMessage("ATTCH_STRG_DIR_ERR", (originalsDir != null ? originalsDir.getAbsolutePath() : "(missing dir name)"));
                log.error("originalsDir doesn't exist["+(originalsDir != null ? originalsDir.getAbsolutePath() : "null ")+"]");
            }
            
            // find an unused filename in the originals dir
            File storageFile = File.createTempFile("sp6-", suffix, originalsDir);
            System.err.println("["+storageFile.getAbsolutePath()+"] "+storageFile.canWrite());
            FileOutputStream fos = new FileOutputStream(storageFile);
            fos.write(1);
            fos.flush();
            fos.close();
            if (storageFile.exists())
            {
                attachment.setAttachmentLocation(storageFile.getName());
                unfilledFiles.add(attachment.getAttachmentLocation());
                return true;
            }
            errMsg = UIRegistry.getLocalizedMessage("ATTCH_NOT_SAVED_REPOS", (storageFile != null ? storageFile.getAbsolutePath() : "(missing file name)"));
            log.error("storageFile doesn't exist["+(storageFile != null ? storageFile.getAbsolutePath() : "null")+"]");
        } catch (IOException e)
        {
            e.printStackTrace();
            
            if (doDisplayErrors)
            {
                errMsg = UIRegistry.getLocalizedMessage("ATTCH_NOT_SAVED_REPOS", storageFilename);
            } else
            {
                // This happens when errors are not displayed.
                e.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FileStoreAttachmentManager.class, e);
            }
        }
        
        if (doDisplayErrors && errMsg != null)
        {
            UIRegistry.showError(errMsg);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(edu.ku.brc.specify.datamodel.Attachment)
     */
    public File getOriginal(final Attachment attachment)
    {
        String attachLoc = attachment.getAttachmentLocation();
        String origLoc   = attachment.getOrigFilename();
        return getFile(attachLoc, origLoc, false, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public File getOriginal(final String attachLoc, final String originalLoc, final String mimeType)
    {
        return getFile(attachLoc, originalLoc, false, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginalScaled(java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public File getOriginalScaled(String attachLoc,
                                  String originalLoc,
                                  String mimeType,
                                  int maxSideInPixels)
    {
        return getFile(attachLoc, originalLoc, false, maxSideInPixels);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(java.lang.String, java.lang.String, java.lang.String)
     */
    private File getFile(String attachLoc, String originalLoc, final boolean isThumb, final Integer scale)
    {
        boolean skip = true;
        
        if (StringUtils.isNotEmpty(attachLoc))
        {
            String base = baseDirectory + File.separator + (isThumb ? THUMBNAILS : ORIGINAL);
            File storedFile = new File(base + File.separator + attachLoc);
            if (storedFile.exists())
            {
                if (scale != null)
                {
                    String pth     = storedFile.getAbsolutePath();
                    String newPath = FilenameUtils.removeExtension(pth);
                    String ext     = FilenameUtils.getExtension(pth);
                    newPath = String.format("%s_%d%s%s", newPath, scale, FilenameUtils.EXTENSION_SEPARATOR_STR, ext);
                    File scaledFile = new File(newPath);
                    if (!skip && scaledFile.exists())
                    {
                        return scaledFile;
                    }
                    
                    try
                    {
                        Image scaledImg = GraphicsUtils.getScaledImage(new ImageIcon(storedFile.getAbsolutePath()), scale, scale, true);
                        BufferedImage bi = new BufferedImage (scaledImg.getWidth(null), scaledImg.getHeight(null),BufferedImage.TYPE_INT_RGB);
                        Graphics bg = bi.getGraphics();
                        bg.drawImage(scaledImg, 0, 0, null);
                        bg.dispose();
                        ImageIO.write(bi, ext.toUpperCase(), new FileOutputStream(scaledFile));
                        return scaledFile;
                        
                    } catch (FileNotFoundException e)
                    {
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                return storedFile;
            }
        } else
        {
            log.error("AttachmentLocation is null for originalLoc["+originalLoc+"]");
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getThumbnail(edu.ku.brc.specify.datamodel.Attachment)
     */
    @Override
    public File getThumbnail(final String attachmentLoc, final String mimeType)
    {
        if (StringUtils.isNotEmpty(attachmentLoc))
        {
            File storedFile = new File(baseDirectory + File.separator + THUMBNAILS + File.separator + attachmentLoc);
            if (storedFile.exists())
            {
                return storedFile;
            }
            
            try
            {
                return regenerateThumbnail(attachmentLoc);
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
            
        } else
        {
            log.error("AttachmentLocation is null for attachmentLoc["+attachmentLoc+"]");
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getThumbnail(edu.ku.brc.specify.datamodel.Attachment)
     */
    public File getThumbnail(final Attachment attachment)
    {
        return getThumbnail(attachment.getAttachmentLocation(), attachment.getMimeType());
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#storeAttachmentFile(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     */
    public void storeAttachmentFile(final Attachment attachment, final File attachmentFile, final File thumbnail) throws IOException
    {
        // copy the original into the storage system
        String attachLoc      = attachment.getAttachmentLocation();
        File   repositoryFile = new File(baseDirectory + File.separator + ORIGINAL + File.separator + attachLoc);
        
        // Copy it to the Repository
        FileUtils.copyFile(attachmentFile, repositoryFile);
        
        // copy the thumbnail, if any, into the storage system
        if (thumbnail != null)
        {
            File thumbFile = new File(baseDirectory + File.separator + THUMBNAILS + File.separator + attachLoc);
            FileUtils.copyFile(thumbnail, thumbFile);
        }
        
        // since we have now made use of the temp file we created earlier, we don't
        // need to keep track of it as an 'unfilled' file to be cleaned up later
        unfilledFiles.remove(attachment.getAttachmentLocation());
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#storeAttachmentFile(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     */
    public File regenerateThumbnail(final String attachLoc, final String origFileName) throws IOException
    {
        File repositoryFile = null;
        File thumbFile      = null;
        
        if (StringUtils.isNotEmpty(attachLoc))
        {
            repositoryFile = new File(baseDirectory + File.separator + ORIGINAL + File.separator + attachLoc);
            thumbFile      = new File(baseDirectory + File.separator + THUMBNAILS + File.separator + attachLoc);
            
        } else if (StringUtils.isNotEmpty(origFileName))
        {
            repositoryFile = new File(origFileName);
            thumbFile      = File.createTempFile("sp6-", ".tmp");
            thumbFile.deleteOnExit();
            
        } else
        {
            return null;
        }

        Thumbnailer thumbnailGen   = AttachmentUtils.getThumbnailer();
        thumbnailGen.generateThumbnail(repositoryFile.getAbsolutePath(), 
                                       thumbFile.getAbsolutePath(),
                                       false);
        return thumbFile;

    }


    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#storeAttachmentFile(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     */
    public File regenerateThumbnail(final String attachLoc) throws IOException
    {
        return regenerateThumbnail(attachLoc, null);

    }


    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#storeAttachmentFile(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     */
    public File regenerateThumbnail(final Attachment attachment) throws IOException
    {
        // copy the original into the storage system
        String      attachLoc      = attachment.getAttachmentLocation();
        File        repositoryFile = null;
        File        thumbFile      = null;
        if (StringUtils.isNotEmpty(attachLoc))
        {
            repositoryFile = new File(baseDirectory + File.separator + ORIGINAL + File.separator + attachLoc);
            thumbFile      = new File(baseDirectory + File.separator + THUMBNAILS + File.separator + attachLoc);
            
        } else if (StringUtils.isNotEmpty(attachment.getOrigFilename()))
        {
            repositoryFile = new File(attachment.getOrigFilename());
            thumbFile      = File.createTempFile("sp6-", ".tmp");
            thumbFile.deleteOnExit();
            
        } else
        {
            return null;
        }

        Thumbnailer thumbnailGen   = AttachmentUtils.getThumbnailer();
        thumbnailGen.generateThumbnail(repositoryFile.getAbsolutePath(), 
                                       thumbFile.getAbsolutePath(),
                                       false);
        return thumbFile;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#replaceOriginal(edu.ku.brc.specify.datamodel.Attachment, java.io.File)
     */
    public void replaceOriginal(final Attachment attachment, final File newOriginal, final File newThumbnail) throws IOException
    {
        String attachLoc = attachment.getAttachmentLocation();
        File origFile = new File(baseDirectory + File.separator + ORIGINAL + File.separator + attachLoc);
        File thumbFile = new File(baseDirectory + File.separator + THUMBNAILS + File.separator + attachLoc);

        replaceFile(origFile, newOriginal);
        replaceFile(thumbFile, newThumbnail);
    }
    
    /**
     * Replace origFile with newFile, attempting to recover from an exception if it occurs.
     * 
     * @param origFile the original file
     * @param newFile the replacement version
     * @throws IOException a disk IO error occurred during the process
     */
    protected void replaceFile(final File origFile, final File newFile) throws IOException
    {
        File tmpOrig = File.createTempFile("sp6-", ".tmp");
        FileUtils.copyFile(origFile, tmpOrig);
        try
        {
            // try to copy the new attachment into place
            // if this fails, we should try to copy the old original back into place
            FileUtils.copyFile(newFile, origFile);
        }
        catch (IOException e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FileStoreAttachmentManager.class, e);
            // if the attachment file differs from the original that we copied to a tmp location...
            if( !FileUtils.contentEquals(origFile, tmpOrig) )
            {
                // copy the tmp version back into place
                FileUtils.copyFile(tmpOrig, origFile);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#deleteAttachmentFiles(edu.ku.brc.specify.datamodel.Attachment)
     */
    public void deleteAttachmentFiles(final Attachment attachment) throws IOException
    {
        String attachLoc = attachment.getAttachmentLocation();
        if (StringUtils.isNotEmpty(attachLoc))
        {
            File   origFile  = new File(baseDirectory + File.separator + ORIGINAL + File.separator + attachLoc);
            File   thumbFile = new File(baseDirectory + File.separator + THUMBNAILS + File.separator + attachLoc);
            
            if (origFile.exists())
            {
                origFile.delete();
            }
            
            if (thumbFile.exists())
            {
                thumbFile.delete();
            }
            
        } else
        {
            log.error("The AttachmentLocation was null/empty for attachment id: "+attachment.getId());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#cleanup()
     */
    public void cleanup()
    {
        // delete all of the temp files we created that were never used for storing
        // attachment originals
        for (String unusedFile: unfilledFiles)
        {
            File f = new File(originalsDir.getAbsolutePath() + File.separator + unusedFile);
            f.delete();
        }
    }
}

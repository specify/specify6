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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.helpers.ImageMetaDataHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
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
    private static final String UNKNOWN    = "unknown";

    private Calendar releaseDate = Calendar.getInstance();
    
    /** The base directory path of where the files will be stored. */
    protected String baseDirectory;
    
    /** The directory inside the base that will store the original files. */
    protected File originalsDir;
    
    /** The directory inside the base that will store the thumbnail files. */
    protected File thumbsDir;
    
    protected int thumbSize = 256;
    
    private ArrayList<AttachmentMgrListener> listeners = new ArrayList<AttachmentMgrListener>();
    
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
        
        releaseDate.set(2013, 1, 22);
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
            errMsg = UIRegistry.getLocalizedMessage("ATTCH_NOT_SAVED_REPOS", (storageFile != null ? storageFile.getAbsolutePath() : "(missing file name)"), "File may not exist.");
            log.error("storageFile doesn't exist["+(storageFile != null ? storageFile.getAbsolutePath() : "null")+"]");
        } catch (IOException e)
        {
            e.printStackTrace();
            
            if (doDisplayErrors)
            {
                errMsg = UIRegistry.getLocalizedMessage("ATTCH_NOT_SAVED_REPOS", storageFilename, e.getLocalizedMessage());
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
    public File getOriginal(final Attachment attachment, final byte[] bytes)
    {
        String attachLoc = attachment.getAttachmentLocation();
        String origLoc   = attachment.getOrigFilename();
        return getFile(attachLoc, origLoc, attachment.getMimeType(), null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public File getOriginal(final String attachLoc, final String originalLoc, final String mimeType, final byte[] bytes)
    {
        return getFile(attachLoc, originalLoc, mimeType, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginalScaled(java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public File getOriginalScaled(final String attachLoc,
                                  final String originalLoc,
                                  final String mimeType,
                                  final int maxSideInPixels,
                                  final byte[] bytes)
    {
        return getFile(attachLoc, originalLoc, mimeType, maxSideInPixels);
    }

    /**
     * @param attachLoc
     * @param originalLoc
     * @param isThumb
     * @param mimeType
     * @param scale
     * @return
     */
//    private File getFile(final String  attachLoc, 
//                         final String  originalLoc, 
//                         final boolean isThumb,
//                         final String  mimeType,
//                         final Integer scale)
//    {
//        boolean skip = true;
//        
//        if (StringUtils.isNotEmpty(attachLoc))
//        {
//            String base       = baseDirectory + File.separator + (isThumb ? THUMBNAILS : ORIGINAL);
//            File   storedFile = new File(base + File.separator + attachLoc);
//            if (storedFile.exists())
//            {
//                if (scale != null)
//                {
//                    Boolean isNotImage = mimeType == null || !mimeType.startsWith("image/");
//                    
//                    String pth     = storedFile.getAbsolutePath();
//                    String newPath = FilenameUtils.removeExtension(pth);
//                    String ext     = isNotImage ? "png" : FilenameUtils.getExtension(pth);
//                    newPath = String.format("%s_%d%s%s", newPath, scale, FilenameUtils.EXTENSION_SEPARATOR_STR, ext);
//                    
//                    File scaledFile = new File(newPath);
//                    if (!skip && scaledFile.exists())
//                    {
//                        return scaledFile;
//                    }
//                    
//                    if (isNotImage)
//                    {
//                        Thumbnailer tn = Thumbnailer.getInstance();
//                        try
//                        {
//                            tn.generateThumbnail(pth, newPath, false);
//                            return scaledFile;
//                            
//                        } catch (IOException e)
//                        {
//                            e.printStackTrace();
//                        }
//                        
//                        String iconName = Thumbnailer.getIconNameFromExtension(FilenameUtils.getExtension(scaledFile.getName().toLowerCase()));
//                        IconEntry entry = IconManager.getIconEntryByName(iconName);
//                        if (entry != null)
//                        {
//                            try
//                            {
//                                return new File(entry.getUrl().toURI());
//                            } catch (URISyntaxException e)
//                            {
//                                e.printStackTrace();
//                            }
//                        }
//                        return null;
//                        
//                    } else
//                    {
//                        try
//                        {
//                            Image         scaledImg = GraphicsUtils.getScaledImage(new ImageIcon(storedFile.getAbsolutePath()), scale, scale, true);
//                            BufferedImage bi        = new BufferedImage (scaledImg.getWidth(null), scaledImg.getHeight(null),BufferedImage.TYPE_INT_RGB);
//                            Graphics bg = bi.getGraphics();
//                            bg.drawImage(scaledImg, 0, 0, null);
//                            bg.dispose();
//                            ImageIO.write(bi, ext.toUpperCase(), new FileOutputStream(scaledFile));
//                            return scaledFile;
//                            
//                        } catch (FileNotFoundException e)
//                        {
//                            e.printStackTrace();
//                        } catch (IOException e)
//                        {
//                            e.printStackTrace();
//                        }
//                        return null;
//                    }
//                }
//                return storedFile;
//            }
//        } else
//        {
//            log.error("AttachmentLocation is null for originalLoc["+originalLoc+"]");
//        }
//        return null;
//    }

    /**
     * @param srcFile
     * @param destFileName
     * @param mimeType
     * @param scale
     * @return
     */
    private File getThumnailFromFile(final File    srcFile,
                                     final String  destFileName,
                                     final String  mimeType,
                                     final Integer scale)
    {
        try
        {
//            String absPath     = srcFile.getAbsolutePath();
//            String newDestPath = FilenameUtils.getPrefix(absPath) + FilenameUtils.getPath(absPath) + FilenameUtils.getName(destFileName);
//            File   destFile    = new File(newDestPath);
            File   destFile    = new File(destFileName);
            if (destFile.exists())
            {
                return destFile;
            }

            // Now generate the thumbnail 
            Thumbnailer thumbnailGen  = AttachmentUtils.getThumbnailer();
            thumbnailGen.generateThumbnail(srcFile.getAbsolutePath(), destFileName, false);
            
            if (!destFile.exists())
            {
                return getFileForIconName(UNKNOWN);
            }
            
            return destFile;
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return getFileForIconName(UNKNOWN);
    }
    
    /**
     * @param fileName
     * @return
     */
    private File getIconFromFileName(final String fileName)
    {
        String iconName = Thumbnailer.getIconNameFromExtension(FilenameUtils.getExtension(fileName.toLowerCase()));
        if (iconName != null)
        {
            File iconFile = getFileForIconName(iconName);
            if (iconFile != null) 
            { 
                return iconFile;
            }
        }
        // No thumbnail, no icon, use the 'unknown' icon
        return getFileForIconName(UNKNOWN);
    }
    

    /**
     * @param iconName
     * @return
     */
    private File getFileForIconName(final String iconName)
    {
        IconEntry entry = IconManager.getIconEntryByName(iconName);
        if (entry != null)
        {
            try
            {
                //System.err.println("****** entry.getUrl(): "+entry.getUrl().toExternalForm());

                String fullPath = entry.getUrl().toExternalForm();
                if (fullPath.startsWith("jar:"))
                {
                    String[] segs = StringUtils.split(fullPath, "!");
                    if (segs.length != 2) return null;
                    
                    String jarPath  = segs[1];
                    InputStream stream = IconManager.class.getResourceAsStream(jarPath);
                    if (stream == null) {
                        //send your exception or warning
                        return null;
                    }
                    
                    String fileName = FilenameUtils.getName(jarPath);
                    String path     = baseDirectory + File.separator + THUMBNAILS + File.separator + fileName;
                    File   outfile  = new File(path);
                    //System.err.println("Path: "+ path+"|"+jarPath+"|"+fileName);
                    OutputStream resStreamOut;
                    int          readBytes;
                    byte[] buffer =  new byte[10240];
                    try {
                        resStreamOut = new FileOutputStream(outfile);
                        while ((readBytes = stream.read(buffer)) > 0) 
                        {
                            resStreamOut.write(buffer, 0, readBytes);
                        }
                        resStreamOut.close();
                        stream.close();
                        
                        return outfile;
                        
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        return null;
                    }
                } else {
                    
                    return new File(entry.getUrl().toURI());
                }
            } catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * @param fileName
     * @param scale
     * @return
     */
    private String getScaledFileName(final String fileName, final Integer scale)
    {
        String newPath = baseDirectory + File.separator + THUMBNAILS + File.separator + FilenameUtils.getBaseName(fileName);
        String ext     = FilenameUtils.getExtension(fileName);
        String newFileName = String.format("%s_%d%s%s", newPath, scale, FilenameUtils.EXTENSION_SEPARATOR_STR, ext);
        
      return newFileName;
    }
    
    /**
     * @param attachLocation
     * @param originalLoc
     * @param mimeTypeArg
     * @param scale
     * @return
     */
    private synchronized File getFile(final String  attachLocation,
                                      final String  originalLoc,
                                      final String  mimeTypeArg,
                                      final Integer scale)
    {
        // Check to see what locations were passed in
        boolean hasAttachmentLoc = StringUtils.isNotEmpty(attachLocation);
        boolean hasOrigFileName  = StringUtils.isNotEmpty(originalLoc);
        boolean hasScaleSize     = scale != null;
        
        if (!hasAttachmentLoc && !hasOrigFileName) return getFileForIconName(UNKNOWN);
        
        String  mimeType = mimeTypeArg != null ? mimeTypeArg : AttachmentUtils.getMimeType(hasAttachmentLoc ? attachLocation : originalLoc);
        //boolean isImage  = mimeType.startsWith("image/");
        
        String fileNameToGet = hasAttachmentLoc ? attachLocation : FilenameUtils.getName(originalLoc);
        //////////////////////////////////////////////////////////
        // If scale is not null, then it contains the scale size
        // so change the name to a scale name
        //////////////////////////////////////////////////////////
        if (hasScaleSize)
        {   
            fileNameToGet = getScaledFileName(fileNameToGet, scale);
        }
        
        //////////////////////////////////////////////////////////////////////
        // If we are here then the server side filename is not in the cache
        // for the full file or scaled file and the original file is not
        // not in the cache.
        //
        // If the 'hasAttachmentLoc' is null then it isn't saved on the Server 
        // yet and we need to either get the full file from disk or generate 
        // a thumbnail.
        //////////////////////////////////////////////////////////////////////
        
        // Now let's get the full file, it's a local file
        // hopefully it is still there
        if (!hasAttachmentLoc)
        {
            File fullFile = new File(originalLoc);
            
            // Ok, it isn't on the server yet.
            if (fullFile.exists())
            {
                return hasScaleSize ? getThumnailFromFile(fullFile, fileNameToGet, mimeType, scale) : fullFile;
            }
            return getFileForIconName(UNKNOWN);
        }
        
        String fileName           = hasAttachmentLoc ? attachLocation : FilenameUtils.getName(originalLoc);
        String path               = baseDirectory + File.separator + ORIGINAL + File.separator + FilenameUtils.getName(fileName);
        File   localFileFromCache = new File(path);
        
        if (hasScaleSize)
        {
            if (Thumbnailer.getInstance().hasGeneratorForMimeType(mimeType))
            {
                return getThumnailFromFile(localFileFromCache, fileNameToGet, mimeType, scale);
            }
            return getIconFromFileName(fileName);
        }
        
        return localFileFromCache;
            
        
        //return getFileForIconName(UNKNOWN); // I know, it shouldn't be an icon
    }
    
    /**
     * @param attachmentID
     * @return
     */
    private File getFileFromID(final int attachmentID)
    {
        String sql = "SELECT AttachmentLocation, OrigFilename, MimeType FROM attachment WHERE AttachmentID="+attachmentID;
        log.debug(sql);
        
        Object[] columns = BasicSQLUtils.getRow(sql);
        if (columns != null && columns.length == 3)
        {
            return getFile((String)columns[0], (String)columns[1], (String)columns[2], null);
        }
        return null;
 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getFileEmbddedDate(int)
     */
    @Override
    public Calendar getFileEmbeddedDate(int attachmentID)
    {
        return ImageMetaDataHelper.getEmbeddedDateOrFileDate(getFileFromID(attachmentID)); 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getMetaDataAsJSON(int)
     */
    @Override
    public String getMetaDataAsJSON(int attachmentID)
    {
        String sql = "SELECT AttachmentLocation, OrigFilename,MimeType FROM attachment WHERE AttachmentID="+attachmentID;
        log.debug(sql);
        
        Object[] columns = BasicSQLUtils.getRow(sql);
        if (columns != null && columns.length == 3)
        {
            String mimeType = (String)columns[2];
            File   mdFile   = getFileFromID(attachmentID);
            if (mdFile != null && StringUtils.isNotEmpty(mimeType) && mimeType.startsWith("image"))
            {
                return ImageMetaDataHelper.getJSONMetaData(mdFile); // for file existence
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getThumbnail(edu.ku.brc.specify.datamodel.Attachment)
     */
    public File getThumbnail(final Attachment attachment, final int maxSideInPixels)
    {
        return getOriginalScaled(attachment.getAttachmentLocation(), attachment.getOrigFilename(), attachment.getMimeType(), maxSideInPixels, null);
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#setThumbSize(int)
     */
    @Override
    public void setThumbSize(int sizeInPixels)
    {
        this.thumbSize = sizeInPixels;
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
            
            String prefix  = FilenameUtils.getBaseName(attachment.getAttachmentLocation()) + "_";
            File   baseDir = new File(baseDirectory + File.separator + ORIGINAL);
            for (File file : baseDir.listFiles())
            {
                if (file.getName().startsWith(prefix))
                {
                    file.delete();
                }
            }
            
        } else
        {
            log.error("The AttachmentLocation was null/empty for attachment id: "+attachment.getId());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#isDiskBased()
     */
    @Override
    public boolean isDiskBased()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getImageAttachmentURL()
     */
    @Override
    public String getImageAttachmentURL()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#addListener(edu.ku.brc.util.AttachmentMgrListener)
     */
    @Override
    public void addListener(AttachmentMgrListener listener)
    {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#removeListener(edu.ku.brc.util.AttachmentMgrListener)
     */
    @Override
    public void removeListener(AttachmentMgrListener listener)
    {
        listeners.remove(listener);
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

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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.IconEntry;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.thumbnails.Thumbnailer;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Nov 1, 2011
 *
 */
public class WebStoreAttachmentMgr implements AttachmentManagerIface
{
    private static final Logger  log   = Logger.getLogger(WebStoreAttachmentMgr.class);
    private static final String DEFAULT_URL    = "http://specifyassets.nhm.ku.edu/Informatics/getmetadata.php?dt=<dt>&type=<type>&filename=<fname>&coll=<coll>&disp=<disp>&div=<div>&inst=<inst>";
    private static final String ATTACHMENT_URL = "SELECT AttachmentLocation FROM attachment WHERE AttachmentID = ";
    private static final String UNKNOWN        = "unknown";
    private static MessageDigest sha1 = null;

    
    private Boolean                 isInitialized      = null;
    private byte[]                  bytes              = new byte[100*1024];
    private File                    downloadCacheDir; 
    private File                    shortTermCacheDir; 
    private FileCache               shortTermCache;
    private SimpleDateFormat        dateFormat         = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
    
    // URLs
    private String                  readURLStr    = null;
    private String                  writeURLStr   = null;
    private String                  delURLStr     = null;
    private String                  fileGetURLStr = null;
    private String                  fileGetMetaDataURLStr = null;
    
    private String[]                symbols        = {"<coll>", "<disp>", "<div>", "<inst>"};
    private String[]                values  = new String[symbols.length];
    
    static
    {
        try
        {
            sha1 = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * 
     */
    public WebStoreAttachmentMgr()
    {
        super();

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#isInitialized()
     */
    @Override
    public boolean isInitialized(final String urlStr)
    {
        if (isInitialized == null)
        {
            isInitialized = false;
            
            shortTermCacheDir = new File(UIRegistry.getAppDataDir() + File.separator + "attach_cache");
            if (!shortTermCacheDir.exists())
            {
                if (!shortTermCacheDir.mkdir())
                {
                    shortTermCacheDir = null;
                    return isInitialized;
                }
            }
                
            downloadCacheDir = new File(UIRegistry.getAppDataDir() + File.separator + "download_cache");
            if (!downloadCacheDir.exists())
            {
                if (!downloadCacheDir.mkdir())
                {
                    downloadCacheDir = null;
                    return isInitialized;
                }

            } else
            {
                try
                {
                    FileUtils.cleanDirectory(downloadCacheDir);
                } catch (IOException e) {}
            }
                
            try
            {
                shortTermCache = new FileCache(shortTermCacheDir.getAbsolutePath(), "cache.map");
                
                AppPreferences localPrefs = AppPreferences.getLocalPrefs();
                Integer maxCacheMB = localPrefs.getInt("ATTACH_CACHE_SIZE", null);
                if (maxCacheMB != null)
                {
                    shortTermCache.setMaxCacheSize(maxCacheMB);
                }
                shortTermCache.setSuffix("");
                shortTermCache.setUsingExtensions(true);
                
                return isInitialized = getURLSetupXML(urlStr);
                
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return isInitialized;
    }
    
    /**
     * @return
     */
    /*private File getConfigFile()
    {
        return new File(UIRegistry.getAppDataDir() + File.separator + "web_asset_store.xml");
    }*/
    
    /**
     * @return
     */
    private boolean getURLSetupXML(final String urlStr)
    {
        try
        {
            if (StringUtils.isNotEmpty(urlStr))
            {
                File tmpFile = File.createTempFile("sp6", ".xml", downloadCacheDir.getAbsoluteFile());
                if (fillFileFromWeb(urlStr, tmpFile))
                {
                    if (getURLSFromFile(tmpFile))
                    {
                        tmpFile.delete();
                        return true;
                    }
                    tmpFile.delete();
                }
            } 
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * @param webAssetFile
     * @return
     */
    private boolean getURLSFromFile(final File webAssetFile)
    {
        try
        {
            Element root = XMLHelper.readFileToDOM4J(webAssetFile);
            if (root != null)
            {
                for (Iterator<?> i = root.elementIterator("url"); i.hasNext();) //$NON-NLS-1$
                {
                    Element urlNode = (Element) i.next();
                    String  type    = urlNode.attributeValue("type"); //$NON-NLS-1$
                    String  urlStr  = urlNode.getTextTrim();
                    
                    if (type.equals("read"))
                    {
                        readURLStr = urlStr;
                        
                    } else if (type.equals("write"))
                    {
                        writeURLStr = urlStr;
                        
                    } else if (type.equals("delete"))
                    {
                        delURLStr = urlStr;
                        
                    } else if (type.equals("fileget"))
                    {
                        fileGetURLStr = urlStr;
                        
                    } else if (type.equals("getmetadata"))
                    {
                        fileGetMetaDataURLStr = urlStr;
                    }
                }
            }
            return StringUtils.isNotEmpty(readURLStr) && StringUtils.isNotEmpty(writeURLStr) && StringUtils.isNotEmpty(delURLStr);
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#setStorageLocationIntoAttachment(edu.ku.brc.specify.datamodel.Attachment, boolean)
     */
    @Override
    public boolean setStorageLocationIntoAttachment(Attachment attachment, boolean doDisplayErrors)
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
            File storageFile = File.createTempFile("sp6", suffix, downloadCacheDir.getAbsoluteFile());
            //System.err.println("["+storageFile.getAbsolutePath()+"] "+storageFile.canWrite());
            if (storageFile.exists())
            {
                attachment.setAttachmentLocation(storageFile.getName());
                storageFile.deleteOnExit();
                
                return true;
            }
            errMsg = UIRegistry.getLocalizedMessage("ATTCH_NOT_SAVED_REPOS", storageFile.getAbsolutePath());
            log.error("storageFile doesn't exist["+storageFile.getAbsolutePath()+"]");
        }
        catch (IOException e)
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
    
    /**
     * @param urlStr
     * @return
     */
    public String getURLDataAsString(final String urlStr)
    {
        try
        {
            if (StringUtils.isNotEmpty(urlStr))
            {
                URL         url       = new URL(urlStr);
                InputStream inpStream = url.openStream();
                if (inpStream != null)
                {
                    StringBuilder dataStr = new StringBuilder();
                    BufferedInputStream  in  = new BufferedInputStream(inpStream);
                    do
                    {
                        int numBytes = in.read(bytes);
                        if (numBytes == -1)
                        {
                            break;
                        }
                        if (numBytes > 0)
                        {
                            String data = new String(bytes);
                            dataStr.append(data);
                        }
                        
                    } while(true);
                    in.close();
                
                    //log.debug(dataStr.toString());
                    return dataStr.toString();
                }
            }
            
        } catch (IOException ex)
        {
            log.error(ex.getMessage());
        }

        return null;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getFileEmbddedDate(int)
     */
    @Override
    public Calendar getFileEmbddedDate(final int attachmentID)
    {
        String fileName = BasicSQLUtils.querySingleObj(ATTACHMENT_URL + attachmentID);
        if (StringUtils.isNotEmpty(fileName))
        {
            String metaDataURLStr = StringUtils.isNotEmpty(fileGetMetaDataURLStr) ? fileGetMetaDataURLStr :  DEFAULT_URL;
            
            String urlStr  = subAllExtraData(metaDataURLStr, fileName, false, null, "date");
            String dateStr = getURLDataAsString(urlStr);
            
            if (dateStr != null && dateStr.length() == 10)
            {
                try
                {
                    Date convertedDate = dateFormat.parse(dateStr);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(convertedDate.getTime());
                    return cal;
                    
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
        }
            
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getMetaDataAsJSON(int)
     */
    @Override
    public String getMetaDataAsJSON(final int attachmentID)
    {
        String fileName = BasicSQLUtils.querySingleObj(ATTACHMENT_URL + attachmentID);
        if (StringUtils.isNotEmpty(fileName))
        {
            String metaDataURLStr = StringUtils.isNotEmpty(fileGetMetaDataURLStr) ? fileGetMetaDataURLStr : DEFAULT_URL;
            
            String urlStr  = subAllExtraData(metaDataURLStr, fileName, false, null, "json");
            String jsonStr = getURLDataAsString(urlStr);
        
            //log.debug(jsonStr);
            return jsonStr;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(edu.ku.brc.specify.datamodel.Attachment)
     */
    @Override
    public synchronized File getOriginal(final Attachment attachment)
    {
        return getOriginal(attachment.getAttachmentLocation(), attachment.getOrigFilename(), attachment.getMimeType());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public File getOriginal(final String attachLoc, final String originalLoc, final String mimeType)
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
                                  final int maxSideInPixels)
    {
        return getFile(attachLoc, originalLoc, mimeType, maxSideInPixels);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getThumbnail(edu.ku.brc.specify.datamodel.Attachment, int)
     */
    @Override
    public synchronized File getThumbnail(final Attachment attachment, final int maxSideInPixels)
    {
        return getOriginalScaled(attachment.getAttachmentLocation(), attachment.getOrigFilename(), attachment.getMimeType(), maxSideInPixels);
    }

    /**
     * @param fileName
     * @param doDelOnExit
     * @return a file that is in the cache directory but will be deleted on exit
     * @throws IOException 
     */
    private File createTempFile(final String fileName, final boolean doDelOnExit) throws IOException
    {
        String fileExt = FilenameUtils.getExtension(fileName);
        File file = File.createTempFile("sp6", '.' + fileExt, downloadCacheDir.getAbsoluteFile());
        if (doDelOnExit)
        {
            file.deleteOnExit();
        }
        return file;
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
                return new File(entry.getUrl().toURI());
            } catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
    
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
            //String fileExt   = FilenameUtils.getExtension(srcFile.getName());
            //File   tmpFile = createTempFile(fileExt, false); // gets deleted automatically

            String absPath     = srcFile.getAbsolutePath();
            String newDestPath = FilenameUtils.getPrefix(absPath) + FilenameUtils.getPath(absPath) + FilenameUtils.getName(destFileName);

            // Now generate the thumbnail 
            Thumbnailer thumbnailGen  = AttachmentUtils.getThumbnailer();
            thumbnailGen.generateThumbnail(srcFile.getAbsolutePath(), 
                                           newDestPath,
                                           false);
            
            File destFile = new File(newDestPath);
            if (!destFile.exists())
            {
                return getFileForIconName(UNKNOWN);
            }
            
            // Put thumbail into cache with thumbnail name
            File cachedFileHandle = shortTermCache.cacheFile(destFile);
            if (destFile.exists()) destFile.delete();
            
            return cachedFileHandle;
            
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
        boolean isImage  = mimeType.startsWith("image/");
        
        String fileNameToGet = attachLocation;
        if (hasAttachmentLoc)
        {
            //////////////////////////////////////////////////////////
            // If scale is not null, then it contains the scale size
            // so change the name to a scale name
            //////////////////////////////////////////////////////////
            if (hasScaleSize)
            {   
                fileNameToGet = getScaledFileName(attachLocation, scale);
            }
            
            // Is the filename in the cache?
            File cachedFile = shortTermCache.getCacheFile(fileNameToGet);
            if (cachedFile != null && cachedFile.exists())
            {
                return cachedFile;
            }
            
        } else if (hasOrigFileName && !hasScaleSize)
        {
            //////////////////////////////////////////////////////////////////
            // Check to see if it is cached by original name (Full Sized)
            //////////////////////////////////////////////////////////////////
            File cachedFile = shortTermCache.getCacheFile(originalLoc);
            if (cachedFile != null && cachedFile.exists())
            {
                return cachedFile;
            }
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
        
        try
        {
            // The File is on the server.
            //
            // Now if we need a scaled version of the file we need to make
            // sure we have a thumbnailer that can make the scaled image,
            // if we don't then just get an icon.
            
            String fullPath = shortTermCache.createCachFileName(fileNameToGet);
            File   destFile = new File(fullPath);
            
            if (hasScaleSize && isImage) // Images get scaled on the server
            {
                File tmpFile = getFileFromWeb(attachLocation, mimeType, scale); // ask server for scaled image
                FileUtils.copyFile(tmpFile, destFile);
                return shortTermCache.cacheFile(destFile);
            }  
            
            // Get the Full Image
            // It's not an image, so we need to get the whole file
            File tmpFile = getFileFromWeb(attachLocation, mimeType, null);
            
            // Rename file to cache
            String path  = FilenameUtils.getPrefix(tmpFile.getAbsolutePath()) + FilenameUtils.getPath(tmpFile.getAbsolutePath()) + destFile.getName();
            File   dFile = new File(path);
            
            Path src = Paths.get(tmpFile.getAbsoluteFile().toURI());
            Path dst = Paths.get(dFile.getAbsoluteFile().toURI());
            java.nio.file.Files.move(src, dst, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            File localFileFromCache = shortTermCache.cacheFile(dFile);
            if (dFile.exists()) dFile.delete();
            if (tmpFile.exists()) tmpFile.delete();
            
            if (hasScaleSize)
            {
                if (Thumbnailer.getInstance().hasGeneratorForMimeType(mimeType))
                {
                    return getThumnailFromFile(localFileFromCache, fileNameToGet, mimeType, scale);
                }
                return getIconFromFileName(fileNameToGet);
            }
            return localFileFromCache;
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        return getFileForIconName(UNKNOWN); // I know, it shouldn't be an icon
    }
    
    /**
     * @param urlStr
     * @param tmpFile
     * @return
     * @throws IOException
     */
    private boolean fillFileFromWeb(final String urlStr, final File tmpFile)
    {
        try
        {
            URL url = new URL(urlStr);
            InputStream inpStream = url.openStream();
            if (inpStream != null)
            {
                BufferedInputStream  in  = new BufferedInputStream(inpStream);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
                
                //long totBytes = 0;
                do
                {
                    int numBytes = in.read(bytes);
                    if (numBytes == -1)
                    {
                        break;
                    }
                    //totBytes += numBytes;
                    bos.write(bytes, 0, numBytes);
                    
                } while(true);
                //log.debug(String.format("Total Bytes for file: %d %d", totBytes, totBytes / 1024));
                in.close();
                bos.flush();
                bos.close();
            
                return true;
            }
            
        } catch (IOException ex)
        {
            log.error(ex.getMessage());
            //ex.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * @param fileName
     * @param mimeType
     * @param isThumb
     * @param scale
     * @return
     */
    private synchronized File getFileFromWeb(final String fileName, final String mimeType, final Integer scale)
    {
// For testing
//        boolean NO_INTERNET = false;
//        if (NO_INTERNET)
//        {
//            File tmpFile = null;
//            try
//            {
//                tmpFile = createTempFile(fileName, false);
//                
//                if (fileName.endsWith("pdf"))
//                {
//                    FileUtils.copyFile(new File("/Users/rods/Downloads/PaymentConfirmation.pdf"), tmpFile);
//                    
//                } else if (fileName.endsWith("pdf"))
//                {
//                    FileUtils.copyFile(new File("/Users/rods/Downloads/IA.png"), tmpFile);
//                    
//                } else if (fileName.endsWith("docx"))
//                {
//                    FileUtils.copyFile(new File("/Users/rods/Downloads/Image.docx"), tmpFile);
//                    
//                } else if (fileName.endsWith("txt"))
//                {
//                    FileUtils.copyFile(new File("/Users/rods/Downloads/ich.txt"), tmpFile);
//                }
//            } catch (Exception ex)
//            {
//                ex.printStackTrace();
//            }
//            return tmpFile;
//        }
        
        try
        {
            File tmpFile = createTempFile(fileName, false);
            
            //String urlStr = String.format("http://localhost/cgi-bin/fileget.php?type=%s&filename=%s&mimeType=%s;disp=%s", 
            //                  isThumb ? "thumbs" : "originals", fileName, StringUtils.isNotEmpty(mimeType) ? mimeType : "",
            //                  discipline.getName());
            
            String urlStr = subAllExtraData(readURLStr, fileName, scale != null, scale, null);
            
            //log.debug("["+urlStr+"]");
            return fillFileFromWeb(urlStr, tmpFile) ? tmpFile : null;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#storeAttachmentFile(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     */
    @Override
    public void storeAttachmentFile(final Attachment attachment, final File attachmentFile, final File thumbnail) throws IOException
    {
        
        if (sendFile(attachmentFile, attachment.getAttachmentLocation(), false))
        {
            sendFile(thumbnail, attachment.getAttachmentLocation(), true);
        } else
        {
            throw new IOException(String.format("File [%s] was not saved on the server!", attachmentFile.getName()));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#setThumbSize(int)
     */
    @Override
    public void setThumbSize(int sizeInPixels)
    {
        
    }

    /**
     * @param urlStr
     * @param symbol
     * @param value
     */
    private String doSub(final String urlStr, final String symbol, final String value)
    {
        return StringUtils.replace(urlStr, symbol, value);
    }
    
    /**
     * 
     */
    private void fillValuesArray()
    {
        Collection  coll = AppContextMgr.getInstance().getClassObject(Collection.class);
        Discipline  disp = AppContextMgr.getInstance().getClassObject(Discipline.class);
        Division    div  = AppContextMgr.getInstance().getClassObject(Division.class);
        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
 
        values[0] = coll.getCollectionName();
        values[1] = disp.getName();
        values[2] = div.getName();
        values[3] = inst.getName();
        
        for (int i=0;i<values.length;i++)
        {
            values[i] = StringUtils.replace(values[i], " ", "%20");
        }
    }
    
    /**
     * @param urlStr
     */
    private String subAllExtraData(final String urlStr, 
                                   final String fileName, 
                                   final boolean isThumb,
                                   final Integer scale,
                                   final String datatype)
    {
        fillValuesArray(); // with current values
        
        String newURLStr = urlStr;
        for (int i=0;i<values.length;i++)
        {
            newURLStr = doSub(newURLStr, symbols[i], values[i]);
        }
        
        newURLStr = doSub(newURLStr, "<type>", isThumb ? "T" : "O");
        newURLStr = doSub(newURLStr, "<fname>", fileName);
        newURLStr = doSub(newURLStr, "<dt>", datatype);
        
        if (scale != null)
        {
            if (!newURLStr.endsWith("&"))
            {
                newURLStr += "&";
            }
            newURLStr += "scale=" + scale.toString();
        }
        return newURLStr;
    }
    
    /**
     * @param targetFile
     * @param fileName
     * @param isThumb
     * @return
     */
    private synchronized boolean sendFile(final File targetFile, 
                                          final String fileName, 
                                          final boolean isThumb)/*,
                                          final boolean saveInCache)*/
    {
        String targetURL = writeURLStr;//"http://localhost/cgi-bin/fileupload.php";
        PostMethod filePost = new PostMethod(targetURL);

        fillValuesArray();
        
        try
        {
            log.debug("Uploading " + targetFile.getName() + " to " + targetURL);
            
            String sha1Hash = calculateHash(targetFile);

            Part[] parts = {
                    new FilePart(targetFile.getName(), targetFile),
                    new StringPart("type", isThumb ? "T" : "O"),
                    new StringPart("store", fileName),
                    new StringPart("coll", values[0]),
                    new StringPart("hash", sha1Hash == null ? "" : sha1Hash),
                    //new StringPart("disp", values[1]),
                    //new StringPart("div",  values[2]),
                    //new StringPart("inst", values[3]),
                };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            int status = client.executeMethod(filePost);
            
            //log.debug("---------------------------------------------------");
            log.debug(filePost.getResponseBodyAsString());
            //log.debug("---------------------------------------------------");

            if (status == HttpStatus.SC_OK)
            {
                return true;
            }
            
        } catch (Exception ex)
        {
            log.error("Error:  " + ex.getMessage());
            ex.printStackTrace();
            
        } finally
        {
            filePost.releaseConnection();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#replaceOriginal(edu.ku.brc.specify.datamodel.Attachment, java.io.File, java.io.File)
     */
    @Override
    public void replaceOriginal(Attachment attachment, File newOriginal, File newThumbnail) throws IOException
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#deleteAttachmentFiles(edu.ku.brc.specify.datamodel.Attachment)
     */
    @Override
    public void deleteAttachmentFiles(final Attachment attachment) throws IOException
    {
        String targetFileName = attachment.getAttachmentLocation();
        if (deleteFileFromWeb(targetFileName, false))
        {
            deleteFileFromWeb(targetFileName, true); // ok to fail deleting thumb
        } else
        {
            UIRegistry.showLocalizedError("ATTCH_NOT_DEL_REPOS", targetFileName);
        }
    }
    
    /**
     * @param fileName
     * @param scale
     * @return
     */
    private String getScaledFileName(final String fileName, final Integer scale)
    {
        String newPath = FilenameUtils.removeExtension(fileName);
        String ext     = FilenameUtils.getExtension(fileName);
        return String.format("%s_%d%s%s", newPath, scale, FilenameUtils.EXTENSION_SEPARATOR_STR, ext);
    }
    
    /**
     * @param fileName
     * @param isThumb
     * @return
     */
    private boolean deleteFileFromWeb(final String fileName, final boolean isThumb)
    {
        try
        {
            //String     targetURL  = String.format("http://localhost/cgi-bin/filedelete.php?filename=%s;disp=%s", targetName, discipline.getName());
            String     targetURL  = subAllExtraData(delURLStr, fileName, isThumb, null, null);
            GetMethod  getMethod  = new GetMethod(targetURL);

            //log.debug("Deleting " + fileName + " from " + targetURL );

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            int status = client.executeMethod(getMethod);
            
            //log.debug(getMethod.getResponseBodyAsString());

            return status == HttpStatus.SC_OK;
            
        } catch (Exception ex)
        {
            //log.debug("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#regenerateThumbnail(edu.ku.brc.specify.datamodel.Attachment)
     */
    @Override
    public File regenerateThumbnail(final Attachment attachment) throws IOException
    {
        File thumbFile = null;
        
        boolean doLocalFile = false;
        
        String origFilePath = attachment.getAttachmentLocation();
        if (StringUtils.isEmpty(origFilePath))
        {
            doLocalFile = true;
            origFilePath = attachment.getOrigFilename();
            if (StringUtils.isEmpty(origFilePath))
            {
                return null;
            }
        }
        
        File origFile;
        if (doLocalFile)
        {
            origFile = new File(origFilePath);
            
        } else
        {
            origFile = getOriginal(attachment);
        }
        
        if (origFile != null)
        {
            thumbFile = createTempFile(origFile.getName(), true);
            
            Thumbnailer thumbnailGen   = AttachmentUtils.getThumbnailer();
            thumbnailGen.generateThumbnail(origFile.getAbsolutePath(), 
                                           thumbFile.getAbsolutePath(),
                                           false);
            if (thumbFile.exists())
            {
                if (!doLocalFile)
                {
                    sendFile(thumbFile, thumbFile.getName(), true);
                }
                
                try
                {
                    thumbFile = shortTermCache.cacheFile(thumbFile.getName(), thumbFile);
                    
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return thumbFile;
    }
    
    /**
     * @param algorithm
     * @param fileName
     * @return
     * @throws Exception
     */
    private String calculateHash(final File file) throws Exception
    {
        if (sha1 != null)
        {
            FileInputStream     fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DigestInputStream   dis = new DigestInputStream(bis, sha1);
    
            // read the file and update the hash calculation
            while (dis.read() != -1)
                ;
    
            // get the hash value as byte array
            byte[] hash = sha1.digest();

            dis.close();
            return byteArray2Hex(hash);
        }
        return null;
    }

    /**
     * @param hash
     * @return
     */
    private String byteArray2Hex(byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String s = formatter.toString();
        formatter.close();
        return s;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#setDirectory(java.io.File)
     */
    @Override
    public void setDirectory(final File baseDir) throws IOException
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getDirectory()
     */
    @Override
    public File getDirectory()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#isDiskBased()
     */
    @Override
    public boolean isDiskBased()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getImageAttachmentURL()
     */
    @Override
    public String getImageAttachmentURL()
    {
        return fileGetURLStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#cleanup()
     */
    @Override
    public void cleanup()
    {
        try
        {
            shortTermCache.saveCacheMapping();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * @return the readURLStr
     */
    public String getReadURLStr()
    {
        return readURLStr;
    }

    /**
     * @return the delURLStr
     */
    public String getDelURLStr()
    {
        return delURLStr;
    }

    /**
     * @return the fileGetURLStr
     */
    public String getFileGetURLStr()
    {
        return fileGetURLStr;
    }

    /**
     * @return the fileGetMetaDataURLStr
     */
    public String getFileGetMetaDataURLStr()
    {
        return fileGetMetaDataURLStr;
    }
    
    
}

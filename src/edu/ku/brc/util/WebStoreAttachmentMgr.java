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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
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
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
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
public final class WebStoreAttachmentMgr implements AttachmentManagerIface
{
    private static final Logger  log   = Logger.getLogger(WebStoreAttachmentMgr.class);
    
    private Boolean                 isInitialized      = null;
    private byte[]                  bytes              = new byte[100*1024];
    private File                    cacheDir; 
    private FileCache               shortTermCache;
    private HashMap<String, String> attachNameThumbMap = new HashMap<String, String>();
    private HashMap<String, String> attachNameOrigMap  = new HashMap<String, String>();
    
    // URLs
    private String                  readURLStr  = null;
    private String                  writeURLStr = null;
    private String                  delURLStr   = null;
    
    private String[]                symbols = {"<coll>", "<disp>", "<div>", "<inst>"};
    private String[]                values  = new String[symbols.length];
    
    
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
    public boolean isInitialized()
    {
        if (isInitialized == null)
        {
            isInitialized = false;
            
            cacheDir = new File(UIRegistry.getAppDataDir() + File.separator + "attach_cache");
            if (!cacheDir.exists())
            {
                if (!cacheDir.mkdir())
                {
                    cacheDir = null;
                    return isInitialized;
                }
            }
                
            try
            {
                shortTermCache = new FileCache(cacheDir.getAbsolutePath(), "cache.map");
                shortTermCache.setSuffix("");
                shortTermCache.setUsingExtensions(true);
                
                return isInitialized = getURLSetupXML();
                
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
    private boolean getURLSetupXML()
    {
        try
        {
            String urlStr = AppPreferences.getLocalPrefs().get("attachment.url", null);
            if (StringUtils.isNotEmpty(urlStr))
            {
                File tmpFile = File.createTempFile("sp6", ".xml", cacheDir.getAbsoluteFile());
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
                        
                    } else 
                    {
                        return false;
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
            File storageFile = File.createTempFile("sp6", suffix, cacheDir.getAbsoluteFile());
            System.err.println("["+storageFile.getAbsolutePath()+"] "+storageFile.canWrite());
            if (storageFile.exists())
            {
                attachment.setAttachmentLocation(storageFile.getName());
                storageFile.deleteOnExit();
                
                return true;
            }
            errMsg = UIRegistry.getLocalizedMessage("ATTCH_NOT_SAVED_REPOS", (storageFile != null ? storageFile.getAbsolutePath() : "(missing file name)"));
            log.error("storageFile doesn't exist["+(storageFile != null ? storageFile.getAbsolutePath() : "null")+"]");
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

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getOriginal(edu.ku.brc.specify.datamodel.Attachment)
     */
    @Override
    public synchronized File getOriginal(Attachment attachment)
    {
        return getFile(attachment, attachNameOrigMap, false);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#getThumbnail(edu.ku.brc.specify.datamodel.Attachment)
     */
    @Override
    public synchronized File getThumbnail(final Attachment attachment)
    {
        return getFile(attachment, attachNameThumbMap, true);
    }
    
    /**
     * @param fileName
     * @param doDelOnExit
     * @return
     * @throws IOException 
     */
    private File createTempFile(final String fileName, final boolean doDelOnExit) throws IOException
    {
        String fileExt = FilenameUtils.getExtension(fileName);
        File file = File.createTempFile("sp6", '.' + fileExt, cacheDir.getAbsoluteFile());
        if (doDelOnExit)
        {
            file.deleteOnExit();
        }
        return file;
    }

    /**
     * @param attachment
     * @param nameHash
     * @param isThumb
     * @return
     */
    private synchronized File getFile(final Attachment attachment, 
                                      final HashMap<String, String> nameHash, 
                                      final boolean isThumb)
    {
        String nmExt = isThumb ? ".THB" : "";
        
        boolean isSaved = StringUtils.isNotEmpty(attachment.getAttachmentLocation());
        
        // Check to see if it is cached by original name
        if (!isSaved)
        {
            String localThumbName = nameHash.get(attachment.getOrigFilename()+nmExt);
            if (StringUtils.isNotEmpty(localThumbName))
            {
                File cachedFile = shortTermCache.getCacheFile(localThumbName);
                if (cachedFile != null && cachedFile.exists())
                {
                    return cachedFile;
                }
            }
        }
        
        // Now check to see if it is cached by the saved name.
        String origFilePath = attachment.getAttachmentLocation();
        if (isSaved)
        {
            String fileName = nameHash.get(origFilePath+nmExt);
            if (StringUtils.isNotEmpty(fileName))
            {
                File cachedFile = shortTermCache.getCacheFile(fileName);
                if (cachedFile != null && cachedFile.exists())
                {
                    return cachedFile;
                }
            }
            
            File cachedFile = shortTermCache.getCacheFile(origFilePath);
            if (cachedFile != null && cachedFile.exists())
            {
                return cachedFile;
            }
            
            // Not in the cache by either name, so go get the file form the server
            File thmbFile = getFileFromWeb(attachment.getAttachmentLocation(), attachment.getMimeType(), isThumb);
            if (thmbFile != null && thmbFile.exists())
            {
                try
                {
                    // cache it
                    String nm = thmbFile.getName();
                    shortTermCache.cacheFile(thmbFile);
                    nameHash.put(attachment.getAttachmentLocation()+nmExt, nm);
                    thmbFile.delete();
                    
                    thmbFile = shortTermCache.getCacheFile(nm);
                    
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            return thmbFile;
            
        } 
        
        // ok, it isn't saved yet, make sure original isn't null
        origFilePath = attachment.getOrigFilename();
        if (StringUtils.isEmpty(origFilePath))
        {
            return null;
        }
        
        // Now make a thumb from the original
        File origFile  = new File(origFilePath);
        File thumbFile = null;
        
        try
        {
            String fileExt = FilenameUtils.getExtension(origFilePath);
            thumbFile = createTempFile(fileExt, false);
            
            // Now generate the thumbnail 
            Thumbnailer thumbnailGen   = AttachmentUtils.getThumbnailer();
            thumbnailGen.generateThumbnail(origFile.getAbsolutePath(), 
                                           thumbFile.getAbsolutePath(),
                                           false);
            if (!thumbFile.exists())
            {
                return null;
            }
            
            // Put mapping cache
            nameHash.put(origFilePath+nmExt, thumbFile.getName());
            
            // Put into cache with original name
            shortTermCache.cacheFile(thumbFile);
            
            thumbFile.delete();
            
            return thumbFile;
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        return null;
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
                
                do
                {
                    int numBytes = in.read(bytes);
                    if (numBytes == -1)
                    {
                        break;
                    }
                    bos.write(bytes, 0, numBytes);
                    
                } while(true);
                in.close();
                bos.close();
            
                return true;
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * @param fileName
     * @param mimeType
     * @param isThumb
     * @return
     */
    private synchronized File getFileFromWeb(final String fileName, final String mimeType, final boolean isThumb)
    {
        try
        {
            File tmpFile = createTempFile(fileName, false);
            
            //String urlStr = String.format("http://localhost/cgi-bin/fileget.php?subdir=%s&filename=%s&mimeType=%s;disp=%s", 
            //                  isThumb ? "thumbs" : "originals", fileName, StringUtils.isNotEmpty(mimeType) ? mimeType : "",
            //                  discipline.getName());
            
            String urlStr = subAllExtraData(readURLStr, fileName, isThumb);
            
            log.debug("["+urlStr+"]");
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
        
        sendFile(attachmentFile, attachment.getAttachmentLocation(), false);
        sendFile(thumbnail,      attachment.getAttachmentLocation(), true);
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
            values[i] = "";
        }
    }
    
    /**
     * @param urlStr
     */
    private String subAllExtraData(final String urlStr, final String fileName, final boolean isThumb)
    {
        fillValuesArray(); // with current values
        
        String newURLStr = urlStr;
        for (int i=0;i<values.length;i++)
        {
            newURLStr = doSub(newURLStr, symbols[i], values[i]);
        }
        
        newURLStr = doSub(newURLStr, "<subdir>", isThumb ? "thumbs" : "originals");
        newURLStr = doSub(newURLStr, "<fname>", fileName);
        
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
            System.out.println("Uploading " + targetFile.getName() + " to " + targetURL);

            Part[] parts = {
                    new FilePart(targetFile.getName(), targetFile),
                    new StringPart("subdir", isThumb ? "thumbs" : "originals"),
                    new StringPart("store", fileName),
                    //new StringPart("coll", values[0]),
                    //new StringPart("disp", values[1]),
                    //new StringPart("div",  values[2]),
                    //new StringPart("inst", values[3]),
                };

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            int status = client.executeMethod(filePost);
            
            System.out.println(filePost.getResponseBodyAsString());

            if (status == HttpStatus.SC_OK)
            {
                return true;
            }
            
        } catch (Exception ex)
        {
            System.out.println("Error:  " + ex.getMessage());
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
        if (!deleteFileFromWeb(targetFileName, false))
        {
            throw new IOException("Couldn't delete original file: "+targetFileName);
        }
        deleteFileFromWeb(targetFileName, true); // ok to fail deleting thumb
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
            String     targetURL  = subAllExtraData(delURLStr, fileName, isThumb);
            GetMethod  filePost   = new GetMethod(targetURL);

            System.out.println("Deleting " + fileName + " from " + targetURL );

            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);

            int status = client.executeMethod(filePost);

            return status == HttpStatus.SC_OK;
            
        } catch (Exception ex)
        {
            System.out.println("Error: " + ex.getMessage());
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
                    shortTermCache.cacheFile(thumbFile.getName(), thumbFile);
                    
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return thumbFile;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.util.AttachmentManagerIface#setDirectory(java.io.File)
     */
    @Override
    public void setDirectory(File baseDir) throws IOException
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
     * @see edu.ku.brc.util.AttachmentManagerIface#cleanup()
     */
    @Override
    public void cleanup()
    {
        
    }

}

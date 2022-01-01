/* Copyright (C) 2022, Specify Collections Consortium
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
package edu.ku.brc.helpers;


import java.io.*;
import java.net.ConnectException;

import org.apache.http.client.HttpClient;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 *
 * @code_status Unknown (auto-generated)
 *
 * @author rods
 *
 */
public class HTTPGetter implements Runnable
{
    private static final Logger log = Logger.getLogger(HTTPGetter.class);

    public enum ErrorCode {
        NoError, Error, HttpError, NotDoneError, IOError, URLError
    }

    protected ErrorCode    status     = ErrorCode.NoError;
    protected Thread       thread     = null;
    protected String       urlStr     = null;
    
    protected CloseableHttpClient   httpClient = null;
    protected HttpGet    method     = null;
    protected InputStream  iStream    = null;
    protected File         fileCache  = null;
    
    protected boolean      isThrowingErrors = true;
    
    protected HTTPGetterListener listener = null;


    /**
     * 
     */
    public HTTPGetter()
    {
        this.urlStr = null;
    }

    public HTTPGetter(final String urlStr)
    {
        this.urlStr = urlStr;
    }

    public HTTPGetter(final String urlStr, final File fileCache)
    {
        this.urlStr    = urlStr;
        this.fileCache = fileCache;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(final HTTPGetterListener listener)
    {
        this.listener = listener;
    }

    public String getUrlStr()
    {
        return urlStr;
    }

    public void setUrlStr(String urlStr)
    {
        this.urlStr = urlStr;
    }

    /**
     * @return the isThrowingErrors
     */
    public boolean isThrowingErrors()
    {
        return isThrowingErrors;
    }

    /**
     * @param isThrowingErrors the isThrowingErrors to set
     */
    public void setThrowingErrors(boolean isThrowingErrors)
    {
        this.isThrowingErrors = isThrowingErrors;
    }

    public ErrorCode getStatus()
    {
        return status;
    }

    /**
     * Performs a "generic" HTTP request and fill member variable with results
     * use "getDigirResultsetStr" to get the results as a String
     *
     * @param url URL to be executaed
     * @return returns the input stream for the body
     */
    public InputStream beginHTTPRequest(final String url) throws Exception
    {
        status = ErrorCode.NoError;

        // Create an HttpClient with the MultiThreadedHttpConnectionManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        httpClient = HttpClients.createMinimal(new PoolingHttpClientConnectionManager());

        HttpGet mthod = new HttpGet(url);
        //method.setQueryString("q=xxx");

        log.debug("getting " + mthod.getURI()); //$NON-NLS-1$
        // execute the method
        CloseableHttpResponse response = httpClient.execute(mthod);

        log.debug("Len: "+EntityUtils.toString(response.getEntity()).length()); //$NON-NLS-1$
        return iStream =   new ByteArrayInputStream(EntityUtils.toByteArray(response.getEntity()));
    }
    
    /**
     * @return
     * @throws IOException
     */
    public int getLong() throws IOException
    {
        return iStream.read();
    }

    /**
     * Performs a "generic" HTTP request and fill member variable with results
     * use "getDigirResultsetStr" to get the results as a String
     *
     * @param url URL to be executaed
     * @return returns an error code
     */
    public byte[] doHTTPRequest(final String url)
    {
        return doHTTPRequest(url, null);
    }

    /**
     * Performs a "generic" HTTP request and fill member variable with results
     * use "getDigirResultsetStr" to get the results as a String
     *
     * @param url URL to be executed
     * @param fileCache the file to place the results
     * @return returns an error code
     */
    public byte[] doHTTPRequest(final String url, final File fileCache)
    {
        byte[]    bytes = null;
        Exception excp  = null;
        status          = ErrorCode.NoError;

        // Create an HttpClient with the MultiThreadedHttpConnectionManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        httpClient = HttpClients.createMinimal(new PoolingHttpClientConnectionManager());

        HttpGet method = null;
        try
        {
            method = new HttpGet(url);
            
            //log.debug("getting " + method.getURI()); //$NON-NLS-1$
            CloseableHttpResponse response = httpClient.execute(method);

            // get the response body as an array of bytes
            long bytesRead = 0;
            if (fileCache == null)
            {
                bytes = EntityUtils.toByteArray(response.getEntity());
                bytesRead = bytes.length;
                
            } else
            {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileCache));
                bytes = new byte[4096];
                InputStream ins =  new ByteArrayInputStream(EntityUtils.toByteArray(response.getEntity()));
                BufferedInputStream bis = new BufferedInputStream(ins);
                while (bis.available() > 0)
                {
                    int numBytes = bis.read(bytes);
                    if (numBytes > 0)
                    {
                        bos.write(bytes, 0, numBytes);
                        bytesRead += numBytes;
                    }
                }
                
                bos.flush();
                bos.close();
                
                bytes = null;
            }

            log.debug(bytesRead + " bytes read"); //$NON-NLS-1$

        } catch (ConnectException ce)
        {
            excp = ce;
            log.error(String.format("Could not make HTTP connection. (%s)", ce.toString())); //$NON-NLS-1$
            status = ErrorCode.HttpError;

        } catch (IOException ioe)
        {
            excp = ioe;
            log.error(String.format("IO problem making request.  (%s)", ioe.toString())); //$NON-NLS-1$
            status = ErrorCode.IOError;

        } catch (java.lang.IllegalArgumentException ioe)
        {
            excp = ioe;
            log.error(String.format("IO problem making request.  (%s)", ioe.toString())); //$NON-NLS-1$
            status = ErrorCode.IOError;
            
        } catch (Exception e)
        {
            excp = e;
            log.error("Error: " + e); //$NON-NLS-1$
            status = ErrorCode.Error;

        } finally
        {
            // always release the connection after we're done
            if (isThrowingErrors && status != ErrorCode.NoError)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HTTPGetter.class, excp);
            }
            
            if (method != null) method.releaseConnection();
            //log.debug("Connection released"); //$NON-NLS-1$
        }
        
        if (listener != null)
        {
            if (status == ErrorCode.NoError)
            {
                listener.completed(this);
            } else
            {
                listener.completedWithError(this, status);
            }
        }
        
        return bytes;
    }

    // ----------------------------------------------------------------
    // -- Runnable Interface
    // ----------------------------------------------------------------
    public void start()
    {
        if (thread == null)
        {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    /**
     *
     *
     */
    public synchronized void stop()
    {
        if (thread != null)
        {
            thread.interrupt();
        }
        thread = null;
        notifyAll();
    }

    /**
     *
     */
    public void run()
    {
        //Thread me = Thread.currentThread();

        doHTTPRequest(urlStr, fileCache);

        stop();
    }
    
    //-------------------------------------------------------------------------
    //--
    //-------------------------------------------------------------------------
    public interface HTTPGetterListener
    {
        /**
         * Notifies the consumer that the data has arrived ok
         * @param getter the getter that got the data
         */
        public abstract void completed(HTTPGetter getter);

        /**
         * Notifies the consumer that the data was in error
         * @param getter the getter that got the data
         */
        public abstract void completedWithError(HTTPGetter getter, ErrorCode errCode);

    }
}

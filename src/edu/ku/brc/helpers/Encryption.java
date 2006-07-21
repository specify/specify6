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
package edu.ku.brc.helpers;

import java.io.ByteArrayOutputStream;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.log4j.Logger;

/**
 * This will encrypt and decrypt strings. I added a couple of helper methods for getting to and from an array of bytes to a string.
 * THis is mostly needed so passwords are not stored in clear text.<br><br>
 * This was taken from a news group on www.codecomments.com I changed it a little for our needs.
 *
 * @author chris (some guy on the web)
 * @author rods
 *
 */
public class Encryption
{
    private static final Logger log  = Logger.getLogger(Encryption.class);
    private static String encryptDecryptPassword = "KU BRC Specify";

    /*
     * The "iteration count" for the key generation algorithm. Basically, this means that the
     * processing that is done to generate the key happens 1000 times. You won't even notice the
     * difference while encrypting or decrypting text, but an attacker will notice a *big*
     * difference when brute forcing keys!
     */
    static final int    ITERATION_COUNT = 1000;

    /* Length of the salt (see below for details on what the salt is) */
    static final int    SALT_LENGTH     = 8;

    /* Which encryption algorithm we're using. */
    static final String ALGORITHM       = "PBEWithMD5AndDES";

    /*
     * The name of a provider class to add to the system before running, if using a provider that's
     * not permanently installed.
     */
    static final String EXTRA_PROVIDER  = null;

    /**
     * Encrypts the string from its array of bytes
     * @param input the actual string (in bytes) that is to be encrypted
     * @param password a password, which is really any string, but must be the same string that was used to decrypt it.
     * @return a byte array of the encrypted chars
     * @throws Exception in case something goes wrong
     */
    public static byte[] encrypt(byte[] input, char[] password) throws Exception
    {
        /*
         * Get ourselves a random number generator, needed in a number of places for encrypting.
         */
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");

        /*
         * A "salt" is considered an essential part of password-based encryption. The salt is
         * selected at random for each encryption. It is not considered "sensitive", so it is tacked
         * onto the generated ciphertext without any special processing. It doesn't matter if an
         * attacker actually gets the salt. The salt is used as part of the key, with the very
         * useful result that if you Encryption the same plaintext with the same password twice, you
         * get *different* ciphertexts. There are lots of pages on the 'net with information about
         * salts and password-based encryption, so read them if you want more details. Suffice to
         * say salt=good, no salt=bad.
         */
        byte[] salt = new byte[SALT_LENGTH];
        sr.nextBytes(salt);

        /*
         * We've now got enough information to build the actual key. We do this by encapsulating the
         * variables in a PBEKeySpec and using a SecretKeyFactory to transform the spec into a key.
         */
        PBEKeySpec keyspec = new PBEKeySpec(password, salt, ITERATION_COUNT);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey key = skf.generateSecret(keyspec);

        /*
         * We'll use a ByteArrayOutputStream to conveniently gather up data as it's encrypted.
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        /*
         * We've to a key, but to actually Encryption something, we need a "cipher". The cipher is
         * created, then initialized with the key, salt, and iteration count. We use a
         * PBEParameterSpec to hold the salt and iteration count needed by the Cipher object.
         */
        PBEParameterSpec paramspec = new PBEParameterSpec(salt, ITERATION_COUNT);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, paramspec, sr);

        /*
         * First, in our output, we need to save the salt in plain unencrypted form.
         */
        baos.write(salt);

        /*
         * Next, Encryption our plaintext using the Cipher object, and write it into our output buffer.
         */
        baos.write(cipher.doFinal(input));

        /*
         * We're done. For security reasons, we probably want the PBEKeySpec object to clear its
         * internal copy of the password, so it can't be stolen later.
         */
        keyspec.clearPassword();
        return baos.toByteArray();
    }

    /**
     * Decrypt the string from its array of bytes
     * @param input the actual string (in bytes) that is to be decrypted
     * @param password a password, which is really any string, but must be the same string that was used to encrypt it.
     * @return a byte array of the decrypted chars
     * @throws Exception in case something goes wrong
     */
    public static byte[] decrypt(final byte[] input, final char[] password) throws Exception
    {
        /*
         * The first SALT_LENGTH bytes of the input ciphertext are actually the salt, not the
         * ciphertext.
         */
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(input, 0, salt, 0, SALT_LENGTH);

        /*
         * We can now create a key from our salt (extracted just above), password, and iteration
         * count. Same procedure to create the key as in Encryption().
         */
        PBEKeySpec keyspec = new PBEKeySpec(password, salt, ITERATION_COUNT);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey key = skf.generateSecret(keyspec);

        /*
         * Once again, create a PBEParameterSpec object and a Cipher object.
         */
        PBEParameterSpec paramspec = new PBEParameterSpec(salt, ITERATION_COUNT);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, paramspec);

        /*
         * Decrypt the data. The parameters we pass into doFinal() instruct it to skip the first
         * SALT_LENGTH bytes of input (which are actually the salt), and then to Encryption the next
         * (length - SALT_LENGTH) bytes, which are the real ciphertext.
         */
        byte[] output = cipher.doFinal(input, SALT_LENGTH, input.length - SALT_LENGTH);

        /* Clear the password and return the generated plaintext. */
        keyspec.clearPassword();
        return output;
    }

    /**
     * Makes a string representing the byte array, each byte is two characters
     * @param bytes the byte array to be converted
     * @return the HEX string
     */
    public static String makeHEXStr(final byte[] bytes)
    {
        StringBuffer strBuf = new StringBuffer(bytes.length+50);
        for (int i = 0; i < bytes.length; i++)
        {
            String s = Integer.toHexString(bytes[i] & 0xFF);
            if (s.length() == 1)
                s = "0" + s;
            strBuf.append(s.toUpperCase());
        }
        return strBuf.toString();
    }

    /**
     * Take a string where each two characters represents a HEX byte and convert it back to a byte array
     * @param str the string to be converted
     * @return the byte array
     */
    public static byte[] reverseHEXStr(final String str)
    {
        int len = str.length() / 2;
        byte[] bytes = new byte[len];
        int inx = 0;
        for (int i=0;i<len;i++)
        {
            int iVal = Integer.parseInt(str.substring(inx, inx+2), 16);
            bytes[i] = (byte)(iVal > 127 ? iVal-256 : iVal);
            inx += 2;
        }
        return bytes;
    }

    /**
     * Helper to decrypt a string
     * @param str the string to be decrypted
     * @return the decrypted string
     */
    public static String decrypt(final String str)
    {
        if (str == null || str.length() == 0)
        {
            return "";
        }

        // decrypt the password
        try
        {
            return new String(Encryption.decrypt(Encryption.reverseHEXStr(str), encryptDecryptPassword.toCharArray()));

        } catch (Exception ex)
        {
            log.error("Error decrypting password."); // XXX FIXME Probably want to display a dialog here
            return str;
        }
    }

    /**
     * Encrypts a string and converts it to a string of Hex characaters where each character is two chars
     * @param str the string to be encrypted
     * @return the encrypted string which is now a string of Hex chars
     */
    public static String encrypt(final String str)
    {
        if (str == null || str.length() == 0)
        {
            return "";
        }

        // Encrypt the password before setting it into the pref
        try
        {
            return Encryption.makeHEXStr(Encryption.encrypt(str.getBytes(), encryptDecryptPassword.toCharArray()));

        } catch (Exception ex)
        {
            log.error("Error endcrypting password."); // XXX FIXME Probably want to display a dialog here
            return str;
        }
    }



    /** I am leaving this here for documentation purposes
     * @param args input from the command line
     * @throws Exception some error
     */
    public static void main(final String[] args) throws Exception
    {
        /*
         * If we're configured to use a third-party cryptography provider, where that provider is
         * not permanently installed, then we need to install it first.
         */
        if (EXTRA_PROVIDER != null)
        {
            Provider prov = (Provider) Class.forName(EXTRA_PROVIDER).newInstance();
            Security.addProvider(prov);
        }

        /*
         * The Encryption() function above uses a byte[] as input, so it's more general (it can Encryption
         * anything, not just a String), as well as using a char[] for the password, because it can
         * be overwritten once it's finished. Strings are immutable, so to purge them from RAM you
         * have to hope they get garbage collected and then the RAM gets reused. For char[]s you can
         * simply fill up the array with junk to erase the password from RAM. Anyway, use char[] if
         * you're concerned about security, but for a test case, a String works fine.
         */
        /* Our input text and password. */
        String input           = "Hello World!";
        String password        = "abcd";


        byte[] inputBytes    = input.getBytes();
        char[] passwordChars = password.toCharArray();

        /* Encrypt the data. */
        byte[] ciphertext = encrypt(inputBytes, passwordChars);

        System.out.println("Ciphertext:");

        /*
         * This is just a little loop I made up which displays the encrypted data in hexadecimal, 30
         * bytes to a line. Obviously, the ciphertext won't necessarily be a recognizable String,
         * and it'll probably have control characters and such in it. We don't even want to convert
         * it to a String, let alone display it onscreen. If you need text, investigate some kind of
         * encoding at this point on top of the encryption, like Base64. It's not that hard to
         * implement and it'll give you text to carry from place to place. Just remember to *de*code
         * the text before calling decrypt().
         */
        int i;
        for (i = 0; i < ciphertext.length; i++)
        {
            String s = Integer.toHexString(ciphertext[i] & 0xFF);
            if (s.length() == 1)
                s = "0" + s;
            System.out.print(s);
            if (i % 30 == 29)
                System.out.println();
        }
        if ((ciphertext.length - 1) % 30 != 29)
            System.out.println();

        String hexText = makeHEXStr(ciphertext);
        System.out.println("To:   ["+hexText+"]");
        System.out.println("From: ["+reverseHEXStr(hexText)+"]****");

        /*
         * Now, decrypt the data. Note that all we need is the password and the ciphertext.
         */
        byte[] output = decrypt(ciphertext, passwordChars);

        /* Transform the output into a string. */
        String sOutput = new String(output);

        /* Display it. */
        System.out.println("Plaintext:\n" + sOutput);
    }
}

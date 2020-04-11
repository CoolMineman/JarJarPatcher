package io.github.CoolMineman;

/*
 * Simple MD5 Library Modified From:
 * https://www.rgagnon.com/javadetails/java-0416.html
 * 
 * The Original Code Was Avaliable Under This License:
 * "There is no restriction to use individual How-To in a development (compiled/source) but a mention is appreciated."
 * https://www.rgagnon.com/varia/faq-e.html
*/

import java.io.*;
import java.security.MessageDigest;

public class MD5Checksum {

   public static byte[] createChecksum(String filename) throws
       Exception
   {
     InputStream fis =  new FileInputStream(filename);

     byte[] buffer = new byte[1024];
     MessageDigest complete = MessageDigest.getInstance("MD5");
     int numRead;
     do {
      numRead = fis.read(buffer);
      if (numRead > 0) {
        complete.update(buffer, 0, numRead);
        }
      } while (numRead != -1);
     fis.close();
     return complete.digest();
   }

   // see this How-to for a faster way to convert
   // a byte array to a HEX string
   public static String getMD5Checksum(String filename) throws Exception {
     byte[] b = createChecksum(filename);
     String result = "";
     for (int i=0; i < b.length; i++) {
       result +=
          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
      }
     return result;
   }
}
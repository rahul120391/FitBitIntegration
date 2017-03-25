package weexcel.india.fitbit;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.annotation.SuppressLint;
import android.util.Base64;

public class MethodClass 
{
	

	  /*--------------------------------------------------*/
	  public  static String encode(String input) {
	      StringBuilder resultStr = new StringBuilder();
	      for (char ch : input.toCharArray()) {
	          if (isUnsafe(ch)) {
	              resultStr.append('%');
	              resultStr.append(toHex(ch / 16));
	              resultStr.append(toHex(ch % 16));
	          } else {
	              resultStr.append(ch);
	          }
	      }
	      return resultStr.toString().trim();
	  }
		 private static  char toHex(int ch) {
	      return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
	  }

	  private static boolean isUnsafe(char ch) {
	      if (ch > 128 || ch < 0)
	          return true;
	      return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
	  }
	  
	  /*-----------------------------------------------------*/
	  public static String computeHmac(String baseString, String key)
			    throws NoSuchAlgorithmException, InvalidKeyException, IllegalStateException, UnsupportedEncodingException
			{
			    Mac mac = Mac.getInstance("HmacSHA1");
			    SecretKeySpec secret = new SecretKeySpec(key.getBytes(), mac.getAlgorithm());
			    mac.init(secret);
			    byte[] digest = mac.doFinal(baseString.getBytes());
			    return Base64.encodeToString(digest, Base64.URL_SAFE);
			}
	  
	  /*--------------------------------------------------------*/
	  public static String getoauthsignature()
	  {
		  String result="";
		  try {
			result=encode(computeHmac(Config.CONSUMER_KEY,Config.CONSUMER_SECRET));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		  return result;
	  }
	  
	  /*------------------------------------------------------------*/
	  public static String getunixepochtime()
	  {
		  long unixTime = System.currentTimeMillis() / 1000L;
		  String time=unixTime+"";
		  return time;
	  }
	  @SuppressLint("TrulyRandom") public static String getOAuthNonce()
	  {
		  int millis = (int) System.currentTimeMillis() * -1;
		  return String.valueOf(millis);
	  }
	  public static String randomStringOfLength(int length) {
		    StringBuffer buffer = new StringBuffer();
		    while (buffer.length() < length) {
		        buffer.append(uuidString());
		    }

		    //this part controls the length of the returned string
		    return buffer.substring(0, length);  
		}


		private static String uuidString() 
		{
			
		    return UUID.randomUUID().toString().replaceAll("-", "");
		}

}

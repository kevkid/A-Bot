import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Authenticated {
	public static String generateHMAC(String data, String privKey) throws Exception {
		byte[] hmac_data = null;
        try{
		Mac HMAC = Mac.getInstance("HmacSHA512");

        SecretKeySpec secret = new SecretKeySpec(privKey.getBytes(),"HmacSHA512");
        HMAC.init(secret);

        hmac_data = HMAC.doFinal(data.getBytes("UTF-8"));
        System.out.println(Base64.getEncoder().encodeToString(hmac_data));
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        return hmac_data.toString();//Base64.getEncoder().encodeToString(hmac_data);
    }
}

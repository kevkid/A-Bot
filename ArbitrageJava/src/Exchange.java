import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;

//import com.google.gson.Gson;

import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

public abstract class Exchange {
	protected String Key;
	protected String Secret;
	protected String ExchangeName;
	protected long RequestLimit;
	protected long AuthRequestLimit;
	protected long AuthLastRequest;
	protected int nonce = 0, lastNonce = 0;
	protected ArrayList<Market> markets = new ArrayList<Market>();
	protected static String USER_AGENT = "Mozilla/5.0 (compatible; CRYPTSY-API/1.0; MSIE 6.0 compatible; +https://github.com/abwaters/cryptsy-api)";
	protected static String API_URL;
	protected static long auth_last_request = 0;
	protected static long auth_request_limit = 1000; // request limit in
													// milliseconds
	protected static long last_request = 0;
	protected static long request_limit = 15000; // request limit in milliseconds
												// for non-auth calls...defaults
												// to 15 seconds
	protected boolean initialized = false;
	protected String secret, key;
	protected Mac mac;
	//protected Gson gson;

	public void setAuthKeys(String key, String secret){	
		this.Key = key;
		this.Secret = secret;
	}
	public void preAuth(){	}
	public void preCall(){  }
	public String request(String urlStr){
		return null;
	}
	public String authRequest(String method, Map<String, String> args){
		return null;
	}
	public String toHex(byte[] a) throws UnsupportedEncodingException {//dont know if this is needed for other APIs
		StringBuilder sb = new StringBuilder();
		for(byte b:a)
			sb.append(String.format("%02x", b&0xff));
		return sb.toString();		
	}
	public void setRequestLimit(long reqLim){
		this.RequestLimit = reqLim;
	}
	
	public void setAuthRequestLimit(long reqLim){
		this.AuthRequestLimit = reqLim;
	}
	public ArrayList<Market> getMarkets(){
		//Returns Market
		return null;
	}
	public List<String> getCurrencies(){
		//returns list of currencies (String)
		return null;
	}
	public double getFees(CurrencyPair market){
		//returns double
		return 0.0;
	}
	public void getOrderBook(String market){
		//returns orderBook
	}
	
	public abstract class Market{
		/*Fields*/
		public CurrencyPair currencyPair;
		public int MarketID;
		/*Fields*/
		
		/*Constructors*/
		public Market(String currency1, String currency2){
			this.currencyPair = new CurrencyPair(currency1, currency2);
		}
		public Market(String currencyPair){
			this.currencyPair = new CurrencyPair(currencyPair);
		}
		public Market(CurrencyPair currencyPair){
			this.currencyPair = currencyPair;
		}
		public Market(int marketID){
			this.MarketID = marketID;
		}
		/*Constructors*/
		/*Methods*/
		public double getFee(){
			return 0.0;
		}
		public double getVolume(){
			return 0.0;
		}
		public double getCurrencyVolume(){
			return 0.0;
		}
		/*Methods*/
		
	}	
	public class CurrencyPair{
		public String baseCurrency, counterCurrency;
		public CurrencyPair(String currency1, String currency2){
			this.baseCurrency = currency1;
			this.counterCurrency = currency2;
		}
		public CurrencyPair(String currencyPair){//accepts currencies with '/' and '_'
			String[] strArr; 
			if(currencyPair.contains("/")){
				strArr = currencyPair.split("/");
			}
			else{
				strArr = currencyPair.split("_");
			}
			this.baseCurrency = strArr[0];
			this.counterCurrency = strArr[1];
		}
		public String toString(){
			return this.baseCurrency.toUpperCase() + "/" + this.counterCurrency.toUpperCase();
		}
	}
	public abstract class  OrderBook{
		public Market market;
		public OrderBook(Market market){
			this.market = market;
		}
		public String getOrderBook(){
			return null;
		}
		public String getBuyOrders(){
			return null;
		}
		public String getAskOrders(){
			return null;
		}
	}
}

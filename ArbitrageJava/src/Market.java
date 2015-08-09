import java.sql.SQLException;

import javax.xml.crypto.dsig.spec.HMACParameterSpec;

import org.json.*;
public class Market {
	public String BuyOrderbook, SellOrderbook, fee, publicKey, privateKey;
	public Market(int id){
		//Here we should get the market sell and buy order books from the db
		//Orderbook = Database.getLabelOrderbookURL("BTC_USD", "");
	}
	public Market (String label, String Exchange, String pubKey, String privKey){
		//Here we should get the market sell and buy order books from the db
		int ex_id = Database.getExchangeID(Exchange);
		BuyOrderbook = Database.getLabelOrderbookURL(ex_id, label, "Buy");
		SellOrderbook = Database.getLabelOrderbookURL(ex_id, label, "Sell");
		fee = Database.getFeeURL(ex_id, label);
		publicKey = pubKey;
		privateKey = privKey;
	}
	public double getBuyPrice(){
		String json = CallAPI.GetJson(SellOrderbook);
		JSONObject obj = new JSONObject(json);
		JSONObject data = obj.getJSONObject("data");
		JSONArray sellOrders = data.getJSONArray("sellorders");//least can buy at
		JSONObject topSellOrder =  sellOrders.getJSONObject(0);
		return Double.parseDouble(topSellOrder.get("price").toString());
	}
	public double getSellPrice(){
		String json = CallAPI.GetJson(BuyOrderbook);
		JSONObject obj = new JSONObject(json);
		JSONObject data = obj.getJSONObject("data");
		JSONArray sellOrders = data.getJSONArray("buyorders");
		JSONObject topBuyOrder =  sellOrders.getJSONObject(0);//most can sell at
		return Double.parseDouble(topBuyOrder.get("price").toString());
	}
	public double getBuyFee() throws Exception{//returns percent
		//String sign = Authenticated.generateHMAC("method=markets&id=BTC_USD&action=fees", privateKey);
		String json = CallAPI.Post(fee, publicKey, "");
		JSONObject obj = new JSONObject(json);
		JSONObject data = obj.getJSONObject("data");
		JSONArray fee = data.getJSONArray("buyfeepercent");
		return fee.getDouble(0);
	}
	public double getSellFee() throws Exception{//returns percent
//		String sign = Authenticated.generateHMAC("method=markets&id=BTC_USD&action=fees", privateKey);
		//String sign = Authenticated.generateHMAC(fee, privateKey);
		String json = CallAPI.Post(fee, publicKey, "");
		JSONObject obj = new JSONObject(json);
		JSONObject data = obj.getJSONObject("data");
		JSONArray fee = data.getJSONArray("sellfeepercent");
		return fee.getDouble(0);
	}
}

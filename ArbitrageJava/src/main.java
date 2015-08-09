import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xeiam.xchange.cryptsy.*;
import com.xeiam.xchange.exceptions.ExchangeException;
import com.xeiam.xchange.exceptions.NotAvailableFromExchangeException;
import com.xeiam.xchange.exceptions.NotYetImplementedForExchangeException;
import com.xeiam.xchange.service.polling.account.PollingAccountService;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.cryptsy.CryptsyExchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.MetaData;
public class main {
	public static void main(String [] arg){
		/*String s = CallAPI.GetJson("https://api.cryptsy.com/api/v2/markets/");//market_currency_id":"1"
		JSONObject json = new JSONObject(s);
		JSONArray data = json.getJSONArray("data")
		for(int index = 0; index < data.length(); index++){
			int Market_Currency = data.getJSONObject(index).getInt("market_currency_id");
			String label = data.getJSONObject(index).getString("label").replace('/', '_');
			String Orderbook_Buy_URL = "https://api.cryptsy.com/api/v2/markets/"+ label +"/orderbook";
			String Orderbook_Sell_URL = "https://api.cryptsy.com/api/v2/markets/"+ label +"/orderbook";
			Database.executeSql("INSERT INTO Market ('Exchange_ID', 'Market_Currency_ID', 'Label', 'Orderbook_Buy_URL', 'Orderbook_Sell_URL')" +
			" Values (1, " + Market_Currency + ", '" + label + "', '" + Orderbook_Buy_URL + "', '" + Orderbook_Sell_URL + "');" );	
		}*/
		
		ExchangeSpecification ex_spec = new ExchangeSpecification(CryptsyExchange.class);
		ex_spec.setApiKey("");
		ex_spec.setSecretKey("");//
		Exchange ex = ExchangeFactory.INSTANCE.createExchange(ex_spec);
		PollingAccountService accountService = ex.getPollingAccountService();
	    PollingMarketDataService marketDataService = ex.getPollingMarketDataService();
	    PollingTradeService tradeService = ex.getPollingTradeService();
		
		try {
			
			TradeBot cryptsyXPM_LTC = new TradeBot(ex, new CurrencyPair("XPM", "BTC"),new CurrencyPair("XPM", "LTC"),accountService,marketDataService,tradeService);
			Thread.sleep(2000);
			TradeBot cryptsyUSD_LTC = new TradeBot(ex, new CurrencyPair("BTC", "USD"),new CurrencyPair("LTC", "USD"),accountService,marketDataService,tradeService);
			Thread.sleep(2000);
			TradeBot cryptsyDOGE_LTC = new TradeBot(ex, new CurrencyPair("DOGE", "BTC"),new CurrencyPair("DOGE", "LTC"),accountService,marketDataService,tradeService);
			Thread.sleep(2000);
			TradeBot cryptsyDOGE_BTC = new TradeBot(ex, new CurrencyPair("LTC", "BTC"),new CurrencyPair("DOGE", "BTC"),accountService,marketDataService,tradeService);
			Thread.sleep(2000);
		} catch (ExchangeException | NotAvailableFromExchangeException | NotYetImplementedForExchangeException
				| IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}

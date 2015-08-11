import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xeiam.xchange.bittrex.v1.*;
import com.xeiam.xchange.bittrex.v1.dto.marketdata.BittrexSymbol;
import com.xeiam.xchange.bittrex.v1.dto.marketdata.BittrexTicker;
import com.xeiam.xchange.bittrex.v1.service.polling.BittrexMarketDataServiceRaw;
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
import com.xeiam.xchange.cryptsy.dto.marketdata.CryptsyMarketData;
import com.xeiam.xchange.cryptsy.service.polling.CryptsyMarketDataServiceRaw;
import com.xeiam.xchange.currency.CurrencyPair;
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
		ExchangeSpecification ex_spec_Bittrex = new ExchangeSpecification(BittrexExchange.class);
		ex_spec_Bittrex.setApiKey("");
		ex_spec_Bittrex.setSecretKey("");
		Exchange ex_BittreExchange = ExchangeFactory.INSTANCE.createExchange(ex_spec_Bittrex);
		PollingAccountService accountServiceBittrex = ex_BittreExchange.getPollingAccountService();
	    PollingMarketDataService marketDataServiceBittrex = ex_BittreExchange.getPollingMarketDataService();
	    PollingTradeService tradeServiceBittrex = ex_BittreExchange.getPollingTradeService();

		ex_spec.setApiKey("");
		ex_spec.setSecretKey("");//
		Exchange ex_Cryptsy = ExchangeFactory.INSTANCE.createExchange(ex_spec);
		PollingAccountService accountService_Cryptsy = ex_Cryptsy.getPollingAccountService();
	    PollingMarketDataService marketDataService_Cryptsy = ex_Cryptsy.getPollingMarketDataService();
	    PollingTradeService tradeService_Cryptsy = ex_Cryptsy.getPollingTradeService();

	    
		try {
			
			//TradeBot cryptsyXPM_LTC = new TradeBot(ex, new CurrencyPair("XPM", "BTC"),new CurrencyPair("XPM", "LTC"),accountService,marketDataService,tradeService);
			//Thread.sleep(2000);
			//TradeBot cryptsyUSD_LTC = new TradeBot(ex, new CurrencyPair("BTC", "USD"),new CurrencyPair("LTC", "USD"),accountService,marketDataService,tradeService);
			//Thread.sleep(2000);
			//TradeBot cryptsyDOGE_LTC = new TradeBot(ex, new CurrencyPair("DOGE", "BTC"),new CurrencyPair("DOGE", "LTC"),accountService,marketDataService,tradeService);
			//Thread.sleep(2000);
			//TradeBot cryptsyDOGE_BTC = new TradeBot(ex, new CurrencyPair("CNC", "BTC"),new CurrencyPair("CNC", "LTC"),accountService,marketDataService,tradeService);
			//TradeBot cryptsyDOGE_BTC = new TradeBot(ex_bittreExchange, new CurrencyPair("DOGE", "BTC"),new CurrencyPair("DOGE", "LTC"),accountServiceBittrex,marketDataServiceBittrex,tradeServiceBittrex);
			//TradeBot CryBitBot = new TradeBot(ex_Cryptsy, ex_BittreExchange, new CurrencyPair("STR", "BTC"), accountService_Cryptsy, marketDataService_Cryptsy, tradeService_Cryptsy, accountServiceBittrex, marketDataServiceBittrex, tradeServiceBittrex );
			List<CurrencyPair> BittrexMarkets = ((BittrexMarketDataServiceRaw) marketDataServiceBittrex).getExchangeSymbols();
			List<CurrencyPair> CryptsyMarkets = ((CryptsyMarketDataServiceRaw) marketDataService_Cryptsy).getExchangeSymbols();
			HashSet<CurrencyPair> allMarkets = new HashSet<CurrencyPair>(BittrexMarkets);
			allMarkets.retainAll(CryptsyMarkets);
			for(CurrencyPair market : allMarkets){
				new TradeBot(ex_Cryptsy, ex_BittreExchange, market, accountService_Cryptsy, marketDataService_Cryptsy, tradeService_Cryptsy, accountServiceBittrex, marketDataServiceBittrex, tradeServiceBittrex );
				Thread.sleep(5000);
			}
		} catch (ExchangeException | NotAvailableFromExchangeException | NotYetImplementedForExchangeException
				| IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}

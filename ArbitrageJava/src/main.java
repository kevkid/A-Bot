import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.xeiam.xchange.bittrex.v1.*;
import com.xeiam.xchange.bittrex.v1.service.polling.BittrexMarketDataServiceRaw;
import com.xeiam.xchange.bleutrade.Bleutrade;
import com.xeiam.xchange.btce.v3.BTCE;
import com.xeiam.xchange.btce.v3.BTCEExchange;
import com.xeiam.xchange.btce.v3.service.polling.BTCEMarketDataServiceRaw;
import com.xeiam.xchange.exceptions.ExchangeException;
import com.xeiam.xchange.exceptions.NotAvailableFromExchangeException;
import com.xeiam.xchange.exceptions.NotYetImplementedForExchangeException;
import com.xeiam.xchange.kraken.*;
import com.xeiam.xchange.service.polling.account.PollingAccountService;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.cryptsy.CryptsyExchange;
import com.xeiam.xchange.cryptsy.service.polling.CryptsyMarketDataServiceRaw;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.kraken.KrakenExchange;
import com.xeiam.xchange.kraken.service.polling.KrakenMarketDataServiceRaw;
import com.xeiam.xchange.poloniex.PoloniexExchange;
import com.xeiam.xchange.btce.*;
import com.xeiam.xchange.bleutrade.BleutradeExchange;
import com.xeiam.xchange.BaseExchange;
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
		
		ExchangeSpecification ex_spec_Cryptsy = new ExchangeSpecification(CryptsyExchange.class);
		ExchangeSpecification ex_spec_Bittrex = new ExchangeSpecification(BittrexExchange.class);
		ExchangeSpecification ex_spec_kraken = new ExchangeSpecification(KrakenExchange.class);
		ExchangeSpecification ex_spec_btce = new ExchangeSpecification(BTCEExchange.class);
		ExchangeSpecification ex_spec_bleutrade = new ExchangeSpecification(BleutradeExchange.class);
		ExchangeSpecification ex_spec_poloniex = new ExchangeSpecification(PoloniexExchange.class);

		ex_spec_Bittrex.setApiKey("");
		ex_spec_Bittrex.setSecretKey("");
		Exchange ex_BittreExchange = ExchangeFactory.INSTANCE.createExchange(ex_spec_Bittrex);

		ex_spec_Cryptsy.setApiKey("");
		ex_spec_Cryptsy.setSecretKey("");//
		Exchange ex_Cryptsy = ExchangeFactory.INSTANCE.createExchange(ex_spec_Cryptsy);

	    Exchange krakenExchange = ExchangeFactory.INSTANCE.createExchange(ex_spec_kraken);
	    krakenExchange.getExchangeSpecification().setApiKey("");
	    krakenExchange.getExchangeSpecification().setSecretKey("");
	    krakenExchange.getExchangeSpecification().setUserName("");
	    
	    ex_spec_btce.setApiKey("");
		ex_spec_btce.setSecretKey("");
		ex_spec_btce.setSslUri("https://btc-e.com");
		Exchange ex_btce = ExchangeFactory.INSTANCE.createExchange(ex_spec_btce);
	    
	    ex_spec_bleutrade.setApiKey("");
	    ex_spec_bleutrade.setSecretKey("");
		Exchange ex_bleutrade = ExchangeFactory.INSTANCE.createExchange(ex_spec_bleutrade);
		
		ex_spec_poloniex.setApiKey("");
		ex_spec_poloniex.setSecretKey("");
		Exchange ex_poloniex = ExchangeFactory.INSTANCE.createExchange(ex_spec_poloniex);
	    
	    
		try {
			ArrayList<Exchange> exchangeList = new ArrayList<Exchange>();
			exchangeList.add(ex_Cryptsy);
			exchangeList.add(ex_BittreExchange);
			exchangeList.add(ex_bleutrade);
			exchangeList.add(krakenExchange);
			exchangeList.add(ex_btce);
			exchangeList.add(ex_poloniex);
			
			for(int outer_index = 0; outer_index < exchangeList.size(); outer_index++){
				for(int inner_index = outer_index+1; inner_index < exchangeList.size(); inner_index++){
					List<CurrencyPair> ex1CPList = exchangeList.get(outer_index).getPollingMarketDataService().getExchangeSymbols();
					List<CurrencyPair> ex2CPList = exchangeList.get(inner_index).getPollingMarketDataService().getExchangeSymbols();
					ex1CPList.retainAll(ex2CPList);
					for(CurrencyPair market : ex1CPList){
						if(!market.baseSymbol.equals("USD") && !market.counterSymbol.equals("USD")){//no usd markets
							new TradeBot(exchangeList.get(outer_index), exchangeList.get(inner_index),  
									market,  exchangeList.get(outer_index).getPollingAccountService(), 
									exchangeList.get(outer_index).getPollingMarketDataService(), exchangeList.get(outer_index).getPollingTradeService(), 
									exchangeList.get(inner_index).getPollingAccountService(),exchangeList.get(inner_index).getPollingMarketDataService(),
									exchangeList.get(inner_index).getPollingTradeService());
								new TradeBot(exchangeList.get(inner_index), exchangeList.get(outer_index),  
										market,  exchangeList.get(inner_index).getPollingAccountService(), 
										exchangeList.get(inner_index).getPollingMarketDataService(), exchangeList.get(inner_index).getPollingTradeService(), 
										exchangeList.get(outer_index).getPollingAccountService(),exchangeList.get(outer_index).getPollingMarketDataService(),
										exchangeList.get(outer_index).getPollingTradeService());
						}
					}					
				}
			}
			System.out.println("finished going through all exchanges");
			
			
			//TradeBot BleuCryBot = new TradeBot(ex_bleutrade, ex_Cryptsy,  new CurrencyPair("LTC", "BTC"),  accountService_bleutrade, marketDataService_bleutrade, tradeService_bleutrade, accountService_Cryptsy,marketDataService_Cryptsy,tradeService_Cryptsy);
			//TradeBot cryptsyXPM_LTC = new TradeBot(ex_Cryptsy, new CurrencyPair("CNC", "BTC"),new CurrencyPair("CNC", "LTC"),accountService_Cryptsy,marketDataService_Cryptsy,tradeService_Cryptsy);
			//Thread.sleep(2000);
			//TradeBot cryptsyUSD_LTC = new TradeBot(ex, new CurrencyPair("BTC", "USD"),new CurrencyPair("LTC", "USD"),accountService,marketDataService,tradeService);
			//Thread.sleep(2000);
			//TradeBot cryptsyDOGE_LTC = new TradeBot(ex, new CurrencyPair("DOGE", "BTC"),new CurrencyPair("DOGE", "LTC"),accountService,marketDataService,tradeService);
			//Thread.sleep(2000);
			//TradeBot cryptsyDOGE_BTC = new TradeBot(ex, new CurrencyPair("CNC", "BTC"),new CurrencyPair("CNC", "LTC"),accountService,marketDataService,tradeService);
			//TradeBot cryptsyDOGE_BTC = new TradeBot(ex_bittreExchange, new CurrencyPair("DOGE", "BTC"),new CurrencyPair("DOGE", "LTC"),accountServiceBittrex,marketDataServiceBittrex,tradeServiceBittrex);
			//TradeBot CryBitBot = new TradeBot(ex_Cryptsy, ex_BittreExchange, new CurrencyPair("LTC", "BTC"), accountService_Cryptsy, marketDataService_Cryptsy, tradeService_Cryptsy, accountServiceBittrex, marketDataServiceBittrex, tradeServiceBittrex );
			
			//TradeBot CryKrakBot = new TradeBot(ex_Cryptsy, krakenExchange, new CurrencyPair("LTC", "BTC"), accountService_Cryptsy, marketDataService_Cryptsy, tradeService_Cryptsy, accountService_kraken, marketDataService_kraken, tradeService_kraken );
			//TradeBot KrakCryBot = new TradeBot(krakenExchange, ex_Cryptsy,  new CurrencyPair("LTC", "BTC"),  accountService_kraken, marketDataService_kraken, tradeService_kraken, accountService_Cryptsy, marketDataService_Cryptsy, tradeService_Cryptsy);
			//TradeBot CryBTCEBot = new TradeBot(ex_Cryptsy, ex_btce, new CurrencyPair("BTC", "USD"), accountService_Cryptsy, marketDataService_Cryptsy, tradeService_Cryptsy, accountService_btce, marketDataService_btce, tradeService_btce );
			//TradeBot BTCECryBot = new TradeBot(ex_btce, ex_Cryptsy,  new CurrencyPair("LTC", "BTC"), accountService_btce, marketDataService_btce, tradeService_btce, accountService_Cryptsy, marketDataService_Cryptsy, tradeService_Cryptsy);
			//TradeBot BTCEKrakBot = new TradeBot(ex_btce, krakenExchange,  new CurrencyPair("BTC", "USD"), accountService_btce, marketDataService_btce, tradeService_btce, accountService_kraken, marketDataService_kraken, tradeService_kraken);
			//Thread.sleep(5000);
		} catch (ExchangeException | NotAvailableFromExchangeException | NotYetImplementedForExchangeException
				| IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}

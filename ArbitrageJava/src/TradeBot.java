import com.xeiam.xchange.cryptsy.*;
import com.xeiam.xchange.cryptsy.dto.CryptsyOrder.CryptsyOrderType;
import com.xeiam.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.PublicKey;
import java.util.List;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.dto.marketdata.*;
import com.xeiam.xchange.dto.trade.*;
import com.xeiam.xchange.exceptions.ExchangeException;
import com.xeiam.xchange.exceptions.NotAvailableFromExchangeException;
import com.xeiam.xchange.exceptions.NotYetImplementedForExchangeException;
import com.xeiam.xchange.service.polling.account.PollingAccountService;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import com.xeiam.xchange.cryptsy.CryptsyExchange;
import com.xeiam.xchange.cryptsy.dto.marketdata.CryptsyMarketData;
import com.xeiam.xchange.cryptsy.dto.marketdata.CryptsyPublicMarketData;
import com.xeiam.xchange.cryptsy.dto.marketdata.CryptsyPublicOrderbook;
import com.xeiam.xchange.cryptsy.service.polling.CryptsyAccountServiceRaw;
import com.xeiam.xchange.cryptsy.service.polling.CryptsyMarketDataServiceRaw;
import com.xeiam.xchange.cryptsy.service.polling.CryptsyPublicMarketDataService;
import com.xeiam.xchange.cryptsy.service.polling.CryptsyTradeServiceRaw;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.cryptsy.dto.trade.CryptsyCalculatedFeesReturn;
import com.xeiam.xchange.cryptsy.dto.trade.CryptsyTradeHistory;
import com.xeiam.xchange.cryptsy.dto.trade.CryptsyTradeHistoryReturn;
public class TradeBot {
	public String name;
	public Exchange exchange1;
	public Exchange exchange2;
	public CurrencyPair StartPair, EndPair, DirectPair; 
	public double StartPairBuy, StartPairSell, EndPairBuy, EndPairSell,
					DirectPairBuy, DirectPairSell, StartCoinBalance, EndCoinBalance, DirectBalance,
					BackToStartCurrencyPairBuy, BackToStartCurrencyPairSell;
	private PollingAccountService AccountService;
	private PollingMarketDataService MarketDataService;
	private PollingTradeService TradeService;
	/*The coins really are markets for now.*/
	public TradeBot(Exchange Exchange1, Exchange Exchange2, String coin1, String coin2, String coin3){
		
	/*	exchange1 = Exchange1;
		exchange2 = Exchange2;
		StartCoin = coin1;
		MiddleCoin = coin2;
		EndCoin = coin3;
		Thread t = new Thread(){
			public void run() {
				while(true){
					if(checkMarket()){//while checking the market there is an opportunity to trade send signal
						//Send signal here
						System.out.println("Trade Signal Sent");
					}
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		};
		t.start();*/
	}
	public TradeBot(Exchange Exchange1, CurrencyPair start_pair, CurrencyPair end_pair, PollingAccountService accountService, PollingMarketDataService marketDataService, PollingTradeService tradeService) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException, InterruptedException{//should launch this with an asynchronous thread
		exchange1 = Exchange1;
		exchange2 = null;
		StartPair = start_pair;
		EndPair = end_pair;
		//DirectPair = direct_pair;
		AccountService = accountService;
		MarketDataService = marketDataService;
	    TradeService = tradeService;
	    
		Thread t = new Thread(){
			public void run() {
				while(true){
					try {
						if(checkMarket()){//while checking the market there is an opportunity to trade send signal
							//Send signal here
							System.out.println("Trade Signal Sent");
							Thread.sleep(5000);
						}
					} catch (ExchangeException | NotAvailableFromExchangeException
							| NotYetImplementedForExchangeException | InterruptedException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		};
		t.start();
	}
	public boolean checkMarket() throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException, InterruptedException{
		boolean toTrade = false;
		double StartPairFee, EndPairFee, DirectPairFee, BackToStartCurrencyPairFee;//all in percent
		double StartPurchaseAmount, StartPurchaseTotal, EndPurchaseAmount, DirectPurchaseAmount, BackToStartCurrencyPairAmount;
		String StartCoin, EndCoin;
		/*Set startcoin -> use this to buy or sell depending on the market
		 * Startcoin = Doge
		 * DOGE/BTC -> LTC/BTC -> DOGE/LTC
		 * Sell Doge for BTC => doge is base and btc is counter
		 * Buy LTC with BTC => LTC is base and BTC is counter
		 * Buy Doge with LTC => Doge is base and LTC is counter
		 * The Start pair and endPair have a coin in common - This tells you the startcoin and the direction
		 * The startcoin is the one which only shows up in startpair and not in endpair
		 * The endcoin is the one which only shows up in the endpair 
		 * */
		if(StartPair.baseSymbol.equals(EndPair.baseSymbol) || StartPair.baseSymbol.equals(EndPair.counterSymbol)){
			StartCoin = StartPair.counterSymbol;
			if(StartPair.baseSymbol.equals(EndPair.baseSymbol)){
				EndCoin = EndPair.counterSymbol;
			}
			else{
				EndCoin = EndPair.baseSymbol;
			}
		}
		else{
			StartCoin = StartPair.baseSymbol;
			if(StartPair.counterSymbol.equals(EndPair.baseSymbol)){
				EndCoin = EndPair.baseSymbol;
			}
			else{
				EndCoin = EndPair.counterSymbol;
			}
			
		}
		
		StartCoinBalance = AccountService.getAccountInfo().getBalance(StartCoin).doubleValue();
		OrderBook StartPairOrderBook = MarketDataService.getOrderBook(StartPair);
		StartPairBuy = (StartPairOrderBook.getAsks().get(0).getLimitPrice()).doubleValue();//buys
		StartPairSell = (StartPairOrderBook.getBids().get(0).getLimitPrice()).doubleValue();//sells
		StartPairFee = getFees(StartPair);//getFee
		if(StartCoin.equalsIgnoreCase(StartPair.counterSymbol)){
			StartPurchaseAmount = (StartCoinBalance-(StartCoinBalance*StartPairFee))/StartPairBuy;//how many coins I can buy with the amount of start coins.
		}
		else{
			StartPurchaseAmount = (StartCoinBalance-(StartCoinBalance*StartPairFee))*StartPairSell;//how many coins I can buy with the amount of start coins.
		}

		OrderBook EndPairOrderBook = MarketDataService.getOrderBook(EndPair);
		EndPairBuy = EndPairOrderBook.getAsks().get(0).getLimitPrice().doubleValue();//buys
		EndPairSell = EndPairOrderBook.getBids().get(0).getLimitPrice().doubleValue();//sells
		EndPairFee = getFees(EndPair);
		if(EndCoin.equalsIgnoreCase(EndPair.baseSymbol)){
			EndPurchaseAmount = (StartPurchaseAmount-(StartPurchaseAmount*EndPairFee))/EndPairBuy;

		}
		else{
			EndPurchaseAmount = (StartPurchaseAmount-(StartPurchaseAmount*EndPairFee))*EndPairSell;	
		}
		
		//BackToStart
		CurrencyPair BackToStartCurrencyPair;
		OrderBook BackToStartCurrencyOrderBook = null;
		try{
			BackToStartCurrencyPair = new CurrencyPair(EndCoin, StartCoin);
			BackToStartCurrencyOrderBook = MarketDataService.getOrderBook(BackToStartCurrencyPair);

		}
		catch(Exception e){
			BackToStartCurrencyPair = new CurrencyPair(StartCoin, EndCoin);
			BackToStartCurrencyOrderBook = MarketDataService.getOrderBook(BackToStartCurrencyPair);
		}
		BackToStartCurrencyPairBuy = BackToStartCurrencyOrderBook.getAsks().get(0).getLimitPrice().doubleValue();//buys
		BackToStartCurrencyPairSell = BackToStartCurrencyOrderBook.getBids().get(0).getLimitPrice().doubleValue();//sells
		BackToStartCurrencyPairFee = getFees(BackToStartCurrencyPair);
		if(StartCoin.equalsIgnoreCase(BackToStartCurrencyPair.baseSymbol)){
			BackToStartCurrencyPairAmount = (EndPurchaseAmount-(EndPurchaseAmount*BackToStartCurrencyPairFee))/BackToStartCurrencyPairBuy;	
		}
		else{
			BackToStartCurrencyPairAmount = (EndPurchaseAmount-(EndPurchaseAmount*BackToStartCurrencyPairFee))*BackToStartCurrencyPairSell;	
		}
		
		/*OrderBook DirectPairOrderBook = MarketDataService.getOrderBook(DirectPair);
		DirectPairBuy =  DirectPairOrderBook.getAsks().get(0).getLimitPrice().doubleValue();
		DirectPairSell = DirectPairOrderBook.getBids().get(0).getLimitPrice().doubleValue();
		DirectPairFee = getFees(DirectPair.baseSymbol.toString());
		DirectPurchaseAmount = (StartCoinBalance-(StartCoinBalance*StartPairFee))/DirectPairBuy;//how many coins I can buy with the amount of start coins.
*/		
		if( BackToStartCurrencyPairAmount > StartCoinBalance){
			toTrade = true;
		}
		    Thread.sleep(500);	    Thread.sleep(500);

		return toTrade;
	}
	public double getFees(CurrencyPair pair){
		double PairFee = 0.0;
		double VolumeInBTC = 0.0;
		String baseCoin = "";
		OrderBook PairVolume;
		if(pair.counterSymbol.equalsIgnoreCase("USD")){
			switch (pair.baseSymbol) {
			case "BTC":
				return 0.0025;
			case "LTC":
				return 0.0025;
			case "DASH":
				return 0.0025;
			case "XRP":
				return 0.0029;
			case "FTC":
				return 0.0033;
			case "DOGE":
				return 0.0029;
			case "XYP":
				return 0.0033;
			case "NXT":
				return 0.0029;
			case "EUR":
				return 0.0029;
			case "PPC":
				return 0.0029;
			case "ZRC":
				return 0.0031;
			default:
				System.exit(1);//Something went wrong
				break;
			}
		}
		try {
			if(pair.baseSymbol.equalsIgnoreCase("BTC")){
				baseCoin = pair.counterSymbol;
				PairVolume = MarketDataService.getOrderBook(new CurrencyPair("BTC", baseCoin));
				List<CryptsyMarketData> markets = ((CryptsyMarketDataServiceRaw) MarketDataService).getCryptsyMarkets().getReturnValue();//.get(1).get24hBTCVolume().doubleValue();
				for(CryptsyMarketData market: markets){
					if(market.getPrimaryCurrencyCode().equals(pair.baseSymbol) && market.getSecondaryCurrencyCode().equals(pair.counterSymbol)){
						VolumeInBTC = market.get24hBTCVolume().doubleValue();
						break;
					}
				}
			}
			else{
				baseCoin = pair.baseSymbol;
				PairVolume = MarketDataService.getOrderBook(new CurrencyPair(baseCoin, "BTC"));
				List<CryptsyMarketData> markets = ((CryptsyMarketDataServiceRaw) MarketDataService).getCryptsyMarkets().getReturnValue();//.get(1).get24hBTCVolume().doubleValue();
				//System.out.println(markets);
				for(CryptsyMarketData market: markets){
					if(market.getPrimaryCurrencyCode().equalsIgnoreCase(pair.baseSymbol) && market.getSecondaryCurrencyCode().equalsIgnoreCase(pair.counterSymbol)){
						VolumeInBTC = market.get24hBTCVolume().doubleValue();
						break;
					}
				}
			}
			
		} catch (ExchangeException | NotAvailableFromExchangeException | NotYetImplementedForExchangeException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(VolumeInBTC < 0.5){
			PairFee = 0.0033; //0.33%
		}
		else if(VolumeInBTC >= 0.5 && VolumeInBTC < 1.0){
			PairFee = 0.0031; //0.31%
		}
		else if(VolumeInBTC >= 1.0 && VolumeInBTC < 5.0){
			PairFee = 0.0029; //0.29%
		}
		else if(VolumeInBTC >= 5 && VolumeInBTC < 20){
			PairFee = 0.0027; //0.27%
		}
		else if(VolumeInBTC >= 20){
			PairFee = 0.0025; //0.25%
		}
		else{
			System.exit(1);//something went wrong
		}
		return PairFee;
	}
}

import com.xeiam.xchange.cryptsy.*;
import com.xeiam.xchange.cryptsy.dto.CryptsyOrder.CryptsyOrderType;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.PublicKey;
import java.text.DecimalFormat;
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
	public TradeBot(Exchange Exchange1, Exchange Exchange2, final CurrencyPair pair, final PollingAccountService EX1_accountService, final PollingMarketDataService EX1_marketDataService, final PollingTradeService EX1_tradeService, final PollingAccountService EX2_accountService, final PollingMarketDataService EX2_marketDataService, final PollingTradeService EX2_tradeService) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException, InterruptedException{//should launch this with an asynchronous thread
		
		exchange1 = Exchange1;
		exchange2 = Exchange2;
		Thread t = new Thread(){
			public void run() {
				//while(true){
					try {
						if(checkMarketDifferentExchanges(pair, EX1_accountService, EX1_marketDataService, EX1_tradeService, EX2_accountService, EX2_marketDataService, EX2_tradeService)){//while checking the market there is an opportunity to trade send signal
							//Send signal here
							System.out.println("Trade Signal Sent");
							Thread.sleep(5000);
						}
					} catch (ExchangeException | NotAvailableFromExchangeException
							| NotYetImplementedForExchangeException | InterruptedException | IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				//}
			}
		};
		t.start();
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
				EndCoin = EndPair.counterSymbol;
			}
			else{
				EndCoin = EndPair.baseSymbol;
			}
			
		}
		//AccountService.getAccountInfo().getBalance(StartCoin).doubleValue()
		StartCoinBalance = 1;//AccountService.getAccountInfo().getWallet(StartCoin).getAvailable().doubleValue();
		OrderBook StartPairOrderBook = MarketDataService.getOrderBook(StartPair);
		StartPairBuy = (StartPairOrderBook.getAsks().get(0).getLimitPrice()).doubleValue();//buys
		StartPairSell = (StartPairOrderBook.getBids().get(0).getLimitPrice()).doubleValue();//sells
		
		StartPairFee = checkExchangeFee(exchange1, StartPair);//getFees(StartPair);//getFee
		if(StartCoin.equalsIgnoreCase(StartPair.counterSymbol)){
			StartPurchaseAmount = (StartCoinBalance-(StartCoinBalance*StartPairFee))/StartPairBuy;//how many coins I can buy with the amount of start coins.
		}
		else{
			StartPurchaseAmount = (StartCoinBalance-(StartCoinBalance*StartPairFee))*StartPairSell;//how many coins I can buy with the amount of start coins.
		}

		OrderBook EndPairOrderBook = MarketDataService.getOrderBook(EndPair);
		EndPairBuy = EndPairOrderBook.getAsks().get(0).getLimitPrice().doubleValue();//buys
		EndPairSell = EndPairOrderBook.getBids().get(0).getLimitPrice().doubleValue();//sells
		EndPairFee = checkExchangeFee(exchange1, EndPair);//getFees(EndPair);
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
		BackToStartCurrencyPairFee = checkExchangeFee(exchange1, BackToStartCurrencyPair);//getFees(BackToStartCurrencyPair);
		if(StartCoin.equalsIgnoreCase(BackToStartCurrencyPair.baseSymbol)){
			BackToStartCurrencyPairAmount = (EndPurchaseAmount-(EndPurchaseAmount*BackToStartCurrencyPairFee))/BackToStartCurrencyPairBuy;	
		}
		else{
			BackToStartCurrencyPairAmount = (EndPurchaseAmount-(EndPurchaseAmount*BackToStartCurrencyPairFee))*BackToStartCurrencyPairSell;	
		}
		//System.out.println(BackToStartCurrencyPairAmount);
		/*OrderBook DirectPairOrderBook = MarketDataService.getOrderBook(DirectPair);
		DirectPairBuy =  DirectPairOrderBook.getAsks().get(0).getLimitPrice().doubleValue();
		DirectPairSell = DirectPairOrderBook.getBids().get(0).getLimitPrice().doubleValue();
		DirectPairFee = getFees(DirectPair.baseSymbol.toString());
		DirectPurchaseAmount = (StartCoinBalance-(StartCoinBalance*StartPairFee))/DirectPairBuy;//how many coins I can buy with the amount of start coins.
*/		
		double percentChange = (((BackToStartCurrencyPairAmount-StartCoinBalance)/StartCoinBalance)*100);
		if( BackToStartCurrencyPairAmount > StartCoinBalance){
			if(percentChange >= 1)
				toTrade = true;
			
		}
		System.out.println("Route: " + StartPair + " -> " + EndPair + " -> " + BackToStartCurrencyPair + " -> " +  "Should I trade? " + toTrade + " Percent change: " + new DecimalFormat("##.####").format(percentChange) + "%");
		    Thread.sleep(5000);//5sec

		return toTrade;
	}
	
	public boolean checkMarketDifferentExchanges(CurrencyPair pair, PollingAccountService EX1_accountService, PollingMarketDataService EX1_marketDataService, PollingTradeService EX1_tradeService, PollingAccountService EX2_accountService, PollingMarketDataService EX2_marketDataService, PollingTradeService EX2_tradeService) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException, InterruptedException{
		boolean toTrade = false;
		double StartPairFee, EndPairFee, DirectPairFee, BackToStartCurrencyPairFee;//all in percent
		double EX1_PurchaseAmount, EX2_PurchaseAmount;
		double EX1_PairBuy, EX1_PairSell, EX1_PairFee, EX2_PairBuy, EX2_PairSell, EX2_PairFee;
		String StartCoin = pair.counterSymbol, EndCoin = pair.baseSymbol;
		
		
		Double EX1_CoinBalance =1.0;// EX1_accountService.getAccountInfo().getWallet(StartCoin).getAvailable().doubleValue();
		
		OrderBook EX1_OrderBook = EX1_marketDataService.getOrderBook(checkCurrencyPair(pair,EX1_marketDataService));
		EX1_PairBuy = (EX1_OrderBook.getAsks().get(0).getLimitPrice()).doubleValue();//buys
		EX1_PairSell = (EX1_OrderBook.getBids().get(0).getLimitPrice()).doubleValue();//sells
		
		EX1_PairFee = checkExchangeFee(exchange1, checkCurrencyPair(pair,EX1_marketDataService));//getFees(StartPair);//getFee
		if(exchange1.getExchangeSpecification().getExchangeName().equalsIgnoreCase("Kraken") && pair.baseSymbol.equals("LTC") && pair.counterSymbol.equals("BTC")){
			EX1_PairBuy = 1/EX1_PairBuy;
			EX1_PairSell = 1/EX1_PairSell;
		}
		if(StartCoin.equalsIgnoreCase(pair.counterSymbol)){
			EX1_PurchaseAmount = (EX1_CoinBalance-(EX1_CoinBalance*EX1_PairFee))/EX1_PairBuy;//how many coins I can buy with the amount of start coins.
		}
		else{
			EX1_PurchaseAmount = (StartCoinBalance-(StartCoinBalance*EX1_PairFee))*EX1_PairSell;//how many coins I can buy with the amount of start coins.
		}
		
		OrderBook EX2_PairOrderBook = EX2_marketDataService.getOrderBook(checkCurrencyPair(pair,EX2_marketDataService));
		EX2_PairBuy = EX2_PairOrderBook.getAsks().get(0).getLimitPrice().doubleValue();//buys
		EX2_PairSell = EX2_PairOrderBook.getBids().get(0).getLimitPrice().doubleValue();//sells
		EX2_PairFee = checkExchangeFee(exchange2, checkCurrencyPair(pair,EX1_marketDataService));//getFees(EndPair);
		if(exchange2.getExchangeSpecification().getExchangeName().equalsIgnoreCase("Kraken") && pair.baseSymbol.equals("LTC") && pair.counterSymbol.equals("BTC")){
			EX2_PairBuy = 1/EX2_PairBuy;
			EX2_PairSell = 1/EX2_PairSell;
		}
		else{
			//System.out.println(exchange2.getExchangeSpecification().getExchangeName());
		}
		if(EndCoin.equalsIgnoreCase(pair.counterSymbol)){
			EX2_PurchaseAmount = (EX1_PurchaseAmount-(EX1_PurchaseAmount*EX2_PairFee))/EX2_PairBuy;

		}
		else{
			EX2_PurchaseAmount = (EX1_PurchaseAmount-(EX1_PurchaseAmount*EX2_PairFee))*EX2_PairSell;	
		}
		double percentChange = (((EX2_PurchaseAmount-EX1_CoinBalance)/EX1_CoinBalance)*100);
			if(percentChange >= 1){
				toTrade = true;
			}
			System.out.println("Pair: " + pair + " Route: " + exchange1.getExchangeSpecification().getExchangeName() + " -> " + exchange2.getExchangeSpecification().getExchangeName() + " Should I trade? " + toTrade + " Percent change: " + new DecimalFormat("##.####").format(percentChange) + "%");		
		    Thread.sleep(5000);//5sec

		return toTrade;
	}

	public double getFeesCryptsy(CurrencyPair pair, PollingMarketDataService marketDataService ){
		double PairFee = 0.0;
		double VolumeInBTC = 0.0;
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
			List<CryptsyMarketData> markets = ((CryptsyMarketDataServiceRaw) marketDataService).getCryptsyMarkets().getReturnValue();//.get(1).get24hBTCVolume().doubleValue();
			for(CryptsyMarketData market: markets){
				if(market.getPrimaryCurrencyCode().equals(pair.baseSymbol) && market.getSecondaryCurrencyCode().equals(pair.counterSymbol)){
					VolumeInBTC = market.get24hBTCVolume().doubleValue();
					break;
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
	public double checkExchangeFee(Exchange exchange, CurrencyPair currencyPair){
		String Exchange = exchange.getExchangeSpecification().getExchangeName();
		switch(Exchange){
		case "Cryptsy":
			return getFeesCryptsy(currencyPair, exchange.getPollingMarketDataService());
		case "Bittrex":
			return 0.0025;//
		}
		return 0.0;
		
	}
	
	public CurrencyPair checkCurrencyPair(CurrencyPair pair, PollingMarketDataService marketDataService) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException{
		try{
			OrderBook o = marketDataService.getOrderBook(pair);
			return pair;
		}
		catch(Exception e){
			return new CurrencyPair(pair.counterSymbol, pair.baseSymbol);
		}
	}
}

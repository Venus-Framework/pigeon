/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.demo.typical;

import java.util.concurrent.Future;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.pigeon.container.SpringContainer;
import com.dianping.pigeon.demo.EchoService;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.invoker.concurrent.FutureFactory;
import com.dianping.pigeon.remoting.invoker.concurrent.InvocationCallback;

public class Client {

	private static SpringContainer CLIENT_CONTAINER = new SpringContainer(
			"classpath*:META-INF/spring/typical/invoker.xml");

	static InvocationCallback callback = new InvocationCallback() {

		@Override
		public void onSuccess(Object result) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFailure(Throwable exception) {
			// TODO Auto-generated method stub

		}

	};

	static EchoService echoServiceCallback = ServiceFactory.getService(EchoService.class, callback, 1000);

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CLIENT_CONTAINER.start();

		EchoService echoService = (EchoService) CLIENT_CONTAINER.getBean("echoService");
		EchoService echoServiceWithCallback = (EchoService) CLIENT_CONTAINER.getBean("echoServiceWithCallback");
		EchoService echoServiceWithFuture = (EchoService) CLIENT_CONTAINER.getBean("echoServiceWithFuture");

		while (true) {
			//Transaction t = Cat.newTransaction("test", "");
			//for (int i = 0; i < 500; i++) {
				try {
					// ContextUtils.putRequestContext("key1", "1");
					// // echoService.echo("" + (i++));
					// // echoServiceCallback.asyncEcho("" + (i++));
					// DealBaseDTO b = new DealBaseDTO();
					// b.setCost(new BigDecimal("3.22"));
					// b.setCurrentJoin(22);
					// b.setMarketPrice(new BigDecimal("32.4"));
					// b.setPrice(new BigDecimal("42.2"));
					// b.setReceiptBeginDate(new Date());
					// DealBaseDTO b2 = new DealBaseDTO();
					// b2.setCost(new BigDecimal("32.22"));
					// b2.setCurrentJoin(21);
					// b2.setMarketPrice(new BigDecimal("322.4"));
					// b2.setPrice(new BigDecimal("42.4"));
					// b2.setReceiptBeginDate(new Date());
					// List<DealBaseDTO> deals = new ArrayList<DealBaseDTO>();
					// deals.add(b);
					// deals.add(b2);
					//
					// DealGroupBaseDTO gb = new DealGroupBaseDTO();
					// gb.setAutoRefundSwitch(3);
					// gb.setBeginDate(new Date());
					// gb.setBlockStock(true);
					// gb.setDealGroupId(232);
					// gb.setDealGroupPrice(new BigDecimal("32.22"));
					// gb.setDeals(deals);
					// gb.setCanUseCoupon(false);
					// gb.setEndDate(new Date());
					// gb.setThirdPartVerify(false);
					//
//					 echoServiceWithFuture.echo("scott");
//					 Future future = FutureFactory.getFuture();
//					 System.out.println(future.get());
//					echoServiceWithCallback.echo("scott");
					Thread.sleep(5);
					System.out.println(echoService.echo("scott"));
					// System.out.println(echoService.echo(Gender.FEMALE));
					// System.out.println(echoService.isMale());
					// System.out.println(echoService.findUsers(1));
					// Thread.sleep(10);
					// System.out.println(echoService.asyncEcho("" + (i++)));
					// System.out.println(echoService.now());
					// echoServiceWithFuture.echo("hi " + i++);
					// ServiceFuture future = ServiceFutureFactory.getFuture();
					// Thread.sleep(20);
					// future._get();

					// System.out.println("response:" +
					// ContextUtils.getResponseContext("key1"));
				} catch (Exception e) {
					//e.printStackTrace();
				}
			//}
//			t.setStatus(Message.SUCCESS);
//			t.complete();
		}
	}
}

package bybit.sdk.sample;

import bybit.sdk.rest.ByBitRestClient;
import bybit.sdk.websocket.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class JavaUsageSample {

    public static void main(String[] args) throws InterruptedException {
        String bybitKey = System.getenv("BYBIT_API_KEY");
		String bybitSecret = System.getenv("BYBIT_SECRET");
        if (bybitKey == null || bybitKey.isEmpty() || bybitSecret == null || bybitSecret.isEmpty()) {
            System.err.println("Make sure you set BYBIT_API_KEY & BYBIT_SECRET environment variables!");
            System.exit(1);
        }

        ByBitRestClient client = new ByBitRestClient(bybitKey, bybitSecret, true);

//        System.out.println("Blocking for markets...");
//        final MarketsDTO markets = client.getReferenceClient().getSupportedMarketsBlocking();
//        System.out.println("Got markets synchronously: " + markets.toString());
//
//        final CountDownLatch latch = new CountDownLatch(1);
//        System.out.println("Getting markets asynchronously...");

//        client.getReferenceClient().getSupportedMarkets(new ByBitRestApiCallback<MarketsDTO>() {
//
//            @Override
//            public void onSuccess(MarketsDTO result) {
//                System.out.println("Got markets asynchronously: " + result.toString());
//                latch.countDown();
//            }
//
//            @Override
//            public void onError(Throwable error) {
//                System.out.println("Error getting markets asynchronously");
//                error.printStackTrace();
//                latch.countDown();
//            }
//        });

//        latch.await();
//        System.out.println("Done waiting for async market data\n\n");


        System.out.println("Websocket sample:");
        websocketSample(bybitKey, bybitSecret);
    }

    public static void websocketSample(String bybitKey, String bybitSecret) {

        WSClientConfigurableOptions options =
                new WSClientConfigurableOptions(ByBitEndpoint.Spot,true);

        ByBitWebSocketClient client = new ByBitWebSocketClient(
                options,
                new DefaultByBitWebSocketListener() {
                    @Override
                    public void onReceive(@NotNull ByBitWebSocketClient client, @NotNull ByBitWebSocketMessage message) {
                        if (message instanceof ByBitWebSocketMessage.RawMessage) {
                            System.out.println(new String(((ByBitWebSocketMessage.RawMessage) message).getData()));
                        } else {

                            System.out.println(message.toString());
                        }
                    }

                    @Override
                    public void onError(@NotNull ByBitWebSocketClient client, @NotNull Throwable error) {
                        System.out.println("Error in websocket");
                        error.printStackTrace();
                    }
                });

        client.connectBlocking();

        List<ByBitWebSocketSubscription> subs = Collections.singletonList(
                new ByBitWebSocketSubscription(ByBitWebsocketTopic.Trades.INSTANCE, "BTCUSD"));
        client.subscribeBlocking(subs);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.unsubscribeBlocking(subs);
        client.disconnectBlocking();
    }

}

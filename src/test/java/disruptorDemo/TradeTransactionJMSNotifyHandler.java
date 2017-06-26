package disruptorDemo;

import com.lmax.disruptor.EventHandler;

/**
 * Created by mercop on 2017/6/25.
 */
public class TradeTransactionJMSNotifyHandler implements EventHandler<TradeTransaction> {

    @Override
    public void onEvent(TradeTransaction event, long sequence,
                        boolean endOfBatch) throws Exception {
        //do send jms message
    }
}

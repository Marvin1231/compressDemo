package disruptorDemo;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mercop on 2017/6/25.
 */
public class Demo3 {

    private static final EventFactory<TradeTransaction> RAW_FACTORY = new EventFactory<TradeTransaction>() {
        public TradeTransaction newInstance() {
            return new TradeTransaction();
        }
    };

    public static void main(String[] args) throws InterruptedException {
        long beginTime = System.currentTimeMillis();

        int bufferSize = 1024;
        ExecutorService executor = Executors.newFixedThreadPool(4);



        Disruptor<TradeTransaction> disruptor = new Disruptor<TradeTransaction>(RAW_FACTORY, bufferSize, DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new BlockingWaitStrategy());

        //使用disruptor创建消费者组C1,C2
        EventHandlerGroup<TradeTransaction> handlerGroup = disruptor.handleEventsWith(new TradeTransactionVasConsumer(), new TradeTransactionInDBHandler());

        TradeTransactionJMSNotifyHandler jmsConsumer = new TradeTransactionJMSNotifyHandler();
        //声明在C1,C2完事之后执行JMS消息发送操作 也就是流程走到C3
        handlerGroup.then(jmsConsumer);


        disruptor.start();//启动
        CountDownLatch latch = new CountDownLatch(1);
        //生产者准备
        executor.submit(new TradeTransactionPublisher(latch, disruptor));
        latch.await();//等待生产者完事.
        disruptor.shutdown();
        executor.shutdown();

        System.out.println("总耗时:" + (System.currentTimeMillis() - beginTime));
    }
}

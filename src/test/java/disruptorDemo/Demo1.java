package disruptorDemo;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by mercop on 2017/6/25.
 */
public class Demo1 {
    private static int LOOP=5000000;
    private static ExecutorService executor = Executors.newFixedThreadPool(4);


    static class PublisherService implements Runnable{
        private TradeTransaction tradeTransaction;
        private Random random=new Random();
        int count = 0;

        private CountDownLatch latch;

        private final RingBuffer<TradeTransaction> ringBuffer;

        public PublisherService(RingBuffer<TradeTransaction> ringBuffer,CountDownLatch latch){
            this.latch = latch;
            this.ringBuffer = ringBuffer;
        }
        @Override
        public void run() {

            while(count++ < LOOP){
                long sequence = ringBuffer.next();
                tradeTransaction = ringBuffer.get(sequence);
                tradeTransaction.setPrice(random.nextDouble()*9999);
                ringBuffer.publish(sequence);
            }
            tradeTransaction = new TradeTransaction();
            tradeTransaction.setId("Null");
            latch.countDown();
        }
    }
    static class VasConsumer implements Runnable{

        @Override
        public void run() {

        }
    }
    static class DBService implements Runnable{
        TradeTransaction tradeTransaction;
        CountDownLatch latch;
        RingBuffer<TradeTransaction> ringBuffer;

        public DBService(RingBuffer<TradeTransaction> ringBuffer,CountDownLatch latch) {
            this.latch = latch;
            this.ringBuffer = ringBuffer;
        }

        @Override
        public void run() {
            while (true){

/*                event.setId(UUID.randomUUID().toString());//简单生成下ID
                System.out.println(event.getId());*/
            }

        }
    }

    private static final EventFactory<TradeTransaction> RAW_FACTORY = new EventFactory<TradeTransaction>() {
        @Override
        public TradeTransaction newInstance() {
            return new TradeTransaction();
        }
    };

    public static void main(String[] args) throws InterruptedException {
        long beginTime = System.currentTimeMillis();
        int bufferSize = 1024;
        CountDownLatch countDownLatch = new CountDownLatch(1);

        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        Disruptor<TradeTransaction> disruptor =
                new Disruptor<TradeTransaction>(RAW_FACTORY, bufferSize,
                        DaemonThreadFactory.INSTANCE, ProducerType.MULTI, new BlockingWaitStrategy());
        final RingBuffer<TradeTransaction> ringBuffer = disruptor.start();


        executor.execute(new PublisherService(ringBuffer,countDownLatch));
        executor.execute(new DBService(ringBuffer,countDownLatch2));
        executor.execute(new VasConsumer());
        countDownLatch.await();
        //countDownLatch2.await();
        Thread.sleep(100);
        executor.shutdown();
        System.out.println("总耗时:" + (System.currentTimeMillis() - beginTime));
    }
}

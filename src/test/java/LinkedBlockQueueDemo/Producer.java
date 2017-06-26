package LinkedBlockQueueDemo;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by mercop on 2017/6/25.
 */
public class Producer {
    public static LinkedBlockingDeque<TradeTransaction> tradeTransactions = new LinkedBlockingDeque<TradeTransaction>(1000);
    private static int LOOP=5000000;
    private static ExecutorService executor = Executors.newFixedThreadPool(4);


    static class PublisherService implements Runnable{
        private TradeTransaction tradeTransaction;
        private Random random=new Random();
        int count = 0;

        private CountDownLatch latch;

        public PublisherService(CountDownLatch latch){
            this.latch = latch;
        }
        @Override
        public void run() {

            while(count++ < LOOP){

                try {
                    tradeTransaction = new TradeTransaction();
                    tradeTransaction.setPrice(random.nextDouble()*9999);
                    tradeTransactions.put(tradeTransaction);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

        public DBService(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            while (true){
                try {
                    tradeTransaction = tradeTransactions.take();
                    if("Null".equals(tradeTransaction.getId())){
                        latch.countDown();
                        break;
                    }
                    tradeTransaction.setId(UUID.randomUUID().toString());//简单生成下ID
                    System.out.println(tradeTransaction.getId());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void main(String[] args) throws InterruptedException {
        long beginTime = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        CountDownLatch countDownLatch2 = new CountDownLatch(1);
        executor.execute(new PublisherService(countDownLatch));
        executor.execute(new DBService(countDownLatch2));
        executor.execute(new VasConsumer());
        countDownLatch.await();
        //countDownLatch2.await();
        Thread.sleep(100);
        executor.shutdown();
        System.out.println("总耗时:" + (System.currentTimeMillis() - beginTime));
    }
}

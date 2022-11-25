package com.hzk.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author kee
 * @version 1.0
 * @date 2022/11/17 16:29
 */
public class ThreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main...start");
        //CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        //    System.out.println("当前线程" + Thread.currentThread());
        //    int i = 10 / 2;
        //    System.out.println("i=" + i);
        //}, executor);
        //CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
        //    System.out.println("当前线程" + Thread.currentThread());
        //    int i = 10 / 0;
        //    System.out.println("i=" + i);
        //    return i;
        //}, executor)
        //        .whenComplete((result,exception)->{
        //            System.out.println("结果是：" + result + " 异常：" + exception);
        //        })
        //        .exceptionally(throwable -> {
        //          return 10;
        //        });
        //System.out.println(future1.get());
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("task1..." + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("task1结束" );
            return i;
        },executor);
        CompletableFuture<String> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("task2..." + Thread.currentThread().getId());
            System.out.println("task2结束" );
            return "Hello";
        },executor);
        /**
         * 这个函数不能接受两个任务的返回参数
         */
        //future01.runAfterBothAsync(future02,()->{
        //    System.out.println("任务3.." + Thread.currentThread().getId());
        //},executor);
        /**
         * 能接收两个任务参数但不能改变返回结果
         */
        //future01.thenAcceptBothAsync(future02,(f1,f2) ->{
        //    System.out.println(f1 + f2);
        //},executor);
        /**
         * 能接收任务也能返回新结果
         *
         */
        //CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
        //    return f1 + f2 + "->hello";
        //},executor);
        /**
         * 由于GET这个操作需要获得前两个线程返回结果并且计算才能得到新的结果，所以会变成同步操作
         */
        //System.out.println(future.get());

        System.out.println("main...end");
    }

    public static void testmain(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main... start");
        //ExecutorService service = Executors.newFixedThreadPool(10);
        //service.submit(new Runnable01());
        //service.submit(new Thread01());
        //service.submit(new Callable01());


        /**
         * 七大参数
         corePoolSize – the number of threads to keep in the pool, even if they are idle, unless allowCoreThreadTimeOut is set
         maximumPoolSize – the maximum number of threads to allow in the pool
         keepAliveTime – when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.
         unit – the time unit for the keepAliveTime argument
         workQueue – the queue to use for holding tasks before they are executed. This queue will hold only the Runnable tasks submitted by the execute method.
         threadFactory – the factory to use when the executor creates a new thread
         handler – the handler to use when execution is blocked because the thread bounds and queue capacities are reached

         corePoolSize 核心线程数 用不用一直在那
         maximumPoolSize 线程池最大数    线程池大小 是  min(核心线程数 corePoolSize) - max(maximumPoolSize 最大线程数)
         keepAliveTime  当线程的数量超过核心线程数 这些空闲的线程会在指定时间内释放
         unit  指定释放时间单位
         workQueue 任务队列
         threadFactory 线程工厂 用于创建新的线程
         handler 任务拒绝策略处理器
         */
        /**
         * 1.1 创建核心线程数
         * 1.2 当核心线程数满了 进入队列
         * 1.3 队列满了 再开线程任务
         * 1.4 都满了有任务拒绝策略
         * 1.5 当任务都执行结束后 指定等待时长释放任务
         * 1.6
         *
         *  7个核心数 最大20个线程数 50个队列 100并发进来 7个立即执行 50个进入队列 开13个线程 剩下的30个根据策略进行执行
         *  CallerRunsPolicy 同步执行
         *  DiscardPolicy 丢弃任务 但是没有通知
         *  AbortPolicy 会抛出 RejectedExecutionException异常
         *  DiscardOldestPolicy 这种策略，会把队列中存在时间最久的那个任务给丢弃掉，以便给新来的任务腾位置；
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                5,
                200,
                2,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        /**
         * 创建一个线程池，该线程池根据需要创建新线程，但会在可用时重用以前构造的线程。
         * 这些池通常会提高执行许多短期异步任务的程序的性能。如果可用，对执行的调用将重用以前构造的线程。
         * 如果没有可用的现有线程，将创建一个新线程并将其添加到池中。 60 秒内未使用的线程将被终止并从缓存中删除。
         * 因此，保持空闲时间足够长的池不会消耗任何资源。
         * 请注意，可以使用 ThreadPoolExecutor 构造函数创建具有相似属性但不同细节（例如，超时参数）的池。
         */
        //Executors.newCachedThreadPool();
        /**
         * 创建一个线程池，该线程池重用固定数量的线程，这些线程在共享的无界队列中运行。
         * 在任何时候，最多 nThreads 个线程将是活动的处理任务。
         * 如果在所有线程都处于活动状态时提交了额外的任务，它们将在队列中等待，直到有线程可用。
         * 如果任何线程在关闭之前的执行过程中因故障而终止，则如果需要执行后续任务，将有一个新线程取代它。
         * 池中的线程将一直存在，直到它被显式关闭。
         */
        //Executors.newFixedThreadPool(10);
        /**
         * 创建一个线程池，可以安排命令在给定的延迟后运行，或定期执行。
         */
        //Executors.newScheduledThreadPool(10);
        /**
         *
         创建一个 Executor，它使用单个工作线程在无界队列中运行。
         （但是请注意，如果此单个线程在关闭之前的执行过程中因故障而终止，
         则如果需要执行后续任务，一个新线程将取代它。）
         保证任务按顺序执行，并且不会有多个任务处于活动状态在任何给定时间。与
         其他等效的 newFixedThreadPool(1) 不同，返回的执行程序保证不能重新配置以使用其他线程
         */
        //Executors.newSingleThreadExecutor();


        System.out.println("main... end");
    }


    public static class Runnable01 implements Runnable {

        @Override
        public void run() {
            System.out.println("runnable01"  + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
        }
    }
    public static class Thread01 extends Thread{
        @Override
        public void run() {
            System.out.println("Thread01" + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
        }
    }
    public static class Callable01 implements Callable<Integer>{

        @Override
        public Integer call() throws Exception {
            System.out.println("Callable01"  + Thread.currentThread());
            int i = 10 / 2;
            System.out.println("i=" + i);
            return i;
        }
    }


}

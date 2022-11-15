package cn.threads;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * description  ScheduleThreadPoolExecutor <BR>
 * <p>
 * author: zhao.song
 * date: created in 16:29  2022/11/4
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class ScheduleThreadPoolExecutorTest {

    public static void main(String[] args) throws InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        TimeUnit.SECONDS.sleep(2);

        LocalDateTime nextTime = LocalDateTime.now();
        System.out.println("nextTime:" + nextTime);
        long nextTimestamp = nextTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
        System.out.println("nextTimestamp:" + nextTimestamp);
        System.out.println("nextEpoch:" + LocalDateTime.ofEpochSecond(nextTimestamp / 1000, 0, ZoneOffset.ofHours(8)));
        TimeUnit.SECONDS.sleep(2);


//        ScheduledThreadPoolExecutor scheduledService
//                = new ScheduledThreadPoolExecutor(1) {
//            @Override
//            protected void afterExecute(Runnable r, Throwable t) {
//                System.out.println("任务执行完成！");
//            }
//        };
//
//        ScheduledFuture<?> schedule = scheduledService.scheduleAtFixedRate(() -> {
//            System.out.println("正在执行任务！");
//        }, 2, 2, TimeUnit.SECONDS);
    }
}

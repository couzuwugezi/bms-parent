package com.bms.project.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 有序UUID
 *
 * @author bigsm
 */
public class TinyUUIDGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TinyUUIDGenerator.class);

    /**
     * 最大容器容量
     */
    private static final int MAX_ID_CONTAINER_SIZE = 2000;

    /**
     * 最小容器容量
     */
    private static final int MIN_ID_CONTAINER_SIZE = 500;

    /**
     * 默认生产者的数据
     */
    private static final int DEFAULT_PRODUCER_SIZE = 2;

    /**
     * 自增标识
     */
    private static short counter = (short) 0;

    /**
     * 机器标识码
     */
    private static String JVM_TAG = null;

    /**
     * 主键后缀容器
     */
    private static final ConcurrentSkipListSet<String> ID_CONTAINER = new ConcurrentSkipListSet<>();

    /**
     * 生产者工作状态
     */
    private static AtomicBoolean creating = new AtomicBoolean(false);

    /**
     * 生产者线程容器
     */
    private static List<UUIDGeneratorProducer> producers = new ArrayList<>();

    static {
        try {
            JVM_TAG = formatIp();

            UUIDGeneratorProducer producer;
            for (int i = 0; i < DEFAULT_PRODUCER_SIZE; i++) {
                producer = new UUIDGeneratorProducer(i);
                producers.add(producer);
                new Thread(producer).start();
            }
            creating.set(true);
        } catch (Exception ex) {

            Random random = new Random();

            JVM_TAG = (random.nextInt(100) + 100) +
                    String.valueOf(random.nextInt(100) + 100);

            LOGGER.error("主键生成工具初始化本地IP异常 请检查.. JVM_TAG:" + JVM_TAG, ex);
        }

    }

    private static String getCurrentNanoStr() {
        String nanoTimeStr = String.valueOf(System.nanoTime());

        return nanoTimeStr.substring(nanoTimeStr.length() - 3);
    }

    private static String getCurrentTimeStampStr() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date(System.currentTimeMillis()));
    }

    private static short getCount() {
        synchronized (TinyUUIDGenerator.class) {
            counter++;
            if (counter < 0) {
                counter = 1;
            }
            return counter;
        }
    }

    public static String generate() {
        if (ID_CONTAINER.size() < MIN_ID_CONTAINER_SIZE && !creating.get()) {
            synchronized (ID_CONTAINER) {

                if (!creating.get()) {
                    LOGGER.info("当前容器已接近最小容量 唤醒生产者开始批量生成主键后缀");

                    ID_CONTAINER.notifyAll();

                    creating.set(true);
                }
            }
        }

        String suffix = ID_CONTAINER.pollFirst();

        if (suffix != null) {
            return getCurrentTimeStampStr() + suffix;
        } else {
            return getCurrentTimeStampStr() + createUUIDSuffix(new Random().nextInt(DEFAULT_PRODUCER_SIZE));
        }
    }


    private static String createUUIDSuffix(int index) {
        StringBuilder builder = new StringBuilder(JVM_TAG);

        builder.append(index);

        builder.append(getCurrentNanoStr());

        String counter = String.valueOf(getCount());

        builder.append(new StringBuilder("00000").replace(5 - counter.length(), 5, counter));

        return builder.toString();
    }

    /**
     * 取当前机器IP后两节
     *
     * @return
     * @throws Exception
     */


    private static String formatIp() throws Exception {

        byte[] ipBytes = InetAddress.getLocalHost().getAddress();

        StringBuilder builder = new StringBuilder();

        int data = Math.abs(ipBytes[2]);

        builder.append(new StringBuilder("000").replace(3 - String.valueOf(data).length(), 3,
                String.valueOf(data)));

        data = Math.abs(ipBytes[3]);

        builder.append(new StringBuilder("000").replace(3 - String.valueOf(data).length(), 3,
                String.valueOf(data)));

        return builder.toString();

    }

    private static class UUIDGeneratorProducer implements Runnable {

        private boolean run;

        private int index;

        UUIDGeneratorProducer(int index) {
            this.run = true;

            this.index = index;
        }

        @Override
        public void run() {
            String str;

            while (this.run) {
                try {
                    str = createUUIDSuffix(this.index);

                    synchronized (ID_CONTAINER) {

                        ID_CONTAINER.add(str);

                        //LOGGER.info("生成主键后缀"+str);

                        if (ID_CONTAINER.size() > MAX_ID_CONTAINER_SIZE) {
                            creating.set(false);

                            LOGGER.info("当前容器已达最大容量 生产者暂停" + this.index);

                            ID_CONTAINER.wait();
                        }
                    }
                } catch (Exception ex) {
                    LOGGER.error("统一主键生产者出现未知异常 请检查...", ex);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());

            Random random = new Random();

            String builder = (random.nextInt(100) + 100) +
                    String.valueOf(random.nextInt(100) + 100);
            System.out.println(builder);
            System.out.println(formatIp());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
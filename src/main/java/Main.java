import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Main {
    static int expire_time = 1200;
    static int total = 1000;
    static int totalThread = 5;

    public static void main(String[] args) {
        /*ExecutorService executor = Executors.newFixedThreadPool(5);
        executor.submit(new Insert1Ne());*/

        /*ArrayList<ArrayList<Integer>> arr = defineRangeForEachThread(1000000,100);
        for (int i = 0; i < arr.size(); i++) {
            InsertNe insertNe1 = new InsertNe("insertNe1", arr.get(i).get(0), arr.get(i).get(1));
            insertNe1.start();
        }*/
        selectTest();
    }

    //"1642272383273"
    public static void selectTest() {
        Jedis jedis = connectRedis();
        Set<String> rs = jedis.zrangeByScore("ne_last_update_time", "1642272383273", "+inf");
        for (String r : rs) {
            Transaction transaction = jedis.multi();
        }

    }

    static class InsertNe extends Thread {
        String name;
        int from;
        int to;
        Jedis jedis = connectRedis();

        public InsertNe(String name, int from, int to) {
            this.name = name;
            this.from = from;
            this.to = to;
        }

        @Override
        public void run() {
            System.out.println("Thread " + name + " start!");
            Instant start = Instant.now();
            for (int i = from; i < to; i++) {
                long mltime = System.currentTimeMillis();
                //1. hash map
                Map<String, String> map = new HashMap<>();
                map.put("last_update_time", String.valueOf(System.currentTimeMillis()));
                map.put("type", "pm");
                map.put("alarm_sent", String.valueOf(false));
                map.put("alarm_init_time", String.valueOf(0));
                map.put("alarm_trigger_time", String.valueOf(0));
                map.put("alarm_clear_time", String.valueOf(0));
                map.put("alarm_id", String.valueOf(""));
                map.put("alarm_location", String.valueOf(""));

                Transaction transaction = jedis.multi();
                transaction.hmset("ne:" + i, map);

                //2. list ne_last_update_time
                transaction.zadd("ne_last_update_time", mltime, "ne:" + i);
                //3. list ne_list
                transaction.zadd("ne_list", i, "ne:" + i);

                transaction.exec();
            }
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            System.out.println("Thread " + name + " is done in " + timeElapsed.toMillis());
        }
    }

    public static Jedis connectRedis() {
        try {
            Jedis jedis = new Jedis("localhost");
            return jedis;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Tính ra mỗi khúc mà mỗi thread cần chạy là từ đâu tới đâu
     *
     * @param total:       tổng số cần chia
     * @param totalThread: số thread cần chia
     * @return: mang 2 chieu chua start va end cua moi khuc
     */
    public static ArrayList<ArrayList<Integer>> defineRangeForEachThread(int total, int totalThread) {
        int start = 1;
        int end = 0;
        int last = 0;
        int range = total / totalThread;
        ArrayList<ArrayList<Integer>> arr = new ArrayList<>();

        for (int i = 0; i < totalThread; i++) {
            end = start + range - 1;
            last = total - end;
            ArrayList<Integer> subArr = new ArrayList<>();
            if (last < range) {
                end = total;
            }
            subArr.add(start);
            subArr.add(end);
            arr.add(subArr);
            start = start + range;
        }
        return arr;
    }

    public static int[][] defineRangeForEachThread2(int total, int totalThread) {
        int start = 1;
        int end = 0;
        int last = 0;
        int range = total / totalThread;
        int[][] arr = new int[totalThread][2];
        try {
            for (int i = 0; i < totalThread; i++) {
                end = start + range - 1;
                last = total - end;
                if (last < range) {
                    end = total;
                }
                arr[i][0] = start;
                arr[i][1] = end;
                start = start + range;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }
}

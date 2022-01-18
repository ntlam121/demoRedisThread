import jdk.internal.net.http.frame.DataFrame;
import obj.Ne;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class MainSpark {
    static int total = 7000000;
    static int threads = 2;

    public static void main(String[] args) {
//        multiThreadInsertData();
        readData();
    }

    public static void multiThreadInsertData() {
        List<Ne> arrays = new ArrayList<>();
        //insert data to list
        for (int i = 1; i <= total; i++) {
            arrays.add(new Ne(i, System.currentTimeMillis() / 1000,
                    "pm", false, 0, 0, 0, "", ""
            ));
        }
        /*ArrayList<ArrayList<Integer>> arr = defineRangeForEachThread(total, threads);
        //run thread
        for (int i = 0; i < arr.size(); i++) {
            System.out.println("Thread:" + i + ". From:" + arr.get(i).get(0) + " to:" + arr.get(i).get(1));
            insertBatchToRedis insertNe1 = new insertBatchToRedis(i, arrays.subList(arr.get(i).get(0) - 1, arr.get(i).get(1) - 1));
            insertNe1.start();
        }*/

        /*insertBatchToRedis insertNe1 = new insertBatchToRedis(1, arrays.subList(0,1999999));
        insertNe1.start();
        insertBatchToRedis insertNe2 = new insertBatchToRedis(2, arrays.subList(2000000,3999999));
        insertNe2.start();
        insertBatchToRedis insertNe3 = new insertBatchToRedis(3, arrays.subList(4000000,5999999));
        insertNe3.start();
        insertBatchToRedis insertNe4 = new insertBatchToRedis(4, arrays.subList(6000000,7000000));
        insertNe4.start();*/
    }

    static class insertBatchToRedis extends Thread {
        int threadId;
        List<Ne> arrays;

        public insertBatchToRedis(int threadId, List<Ne> arrays) {
            this.threadId = threadId;
            this.arrays = arrays;
        }

        @Override
        public void run() {
            System.out.println("Thread " + threadId + " started");
            Instant start = Instant.now();
            SparkSession spark = SparkSession
                    .builder()
                    .appName("vht")
                    .master("local[*]")
                    .config("spark.redis.host", "localhost")
                    .config("spark.redis.port", "6379")
                    .getOrCreate();

            Dataset<Row> df1 = spark.createDataFrame(arrays, Ne.class);

            df1.write()
                    .format("org.apache.spark.sql.redis")
                    .option("table", "time")
                    .option("key.column", "neId")
                    .mode(SaveMode.Overwrite)
                    .save();

            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            System.out.println("Thread " + threadId + " is done in " + timeElapsed.toMillis());
        }
    }

    /**
     * Query and update data from redis
     */
    public static void readData() {
        SparkSession spark = SparkSession
                .builder()
                .appName("vht")
                .master("local[*]")
                .config("spark.redis.host", "localhost")
                .config("spark.redis.port", "6379")
                .getOrCreate();

        StructType schema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("neId", DataTypes.IntegerType, false),
                DataTypes.createStructField("last_update_time", DataTypes.LongType, true),
                DataTypes.createStructField("type", DataTypes.StringType, true),
                DataTypes.createStructField("alarm_sent", DataTypes.BooleanType, true),
                DataTypes.createStructField("alarm_init_time", DataTypes.LongType, true),
                DataTypes.createStructField("alarm_trigger_time", DataTypes.LongType, true),
                DataTypes.createStructField("alarm_clear_time", DataTypes.LongType, true),
                DataTypes.createStructField("alarm_id", DataTypes.StringType, true),
                DataTypes.createStructField("alarm_location", DataTypes.StringType, true),
        });
        Dataset<Row> df = spark.read()
                .format("org.apache.spark.sql.redis")
                .schema(schema)
                .option("keys.pattern", "time:*")
                .option("key.column", "neId")
                .load();
        df.createOrReplaceTempView("time");
        //1. lấy ra các bản ghi vi phạm
        Instant time1 = Instant.now();
        //String sql = "SELECT neId FROM time WHERE (unix_timestamp(current_timestamp())-last_update_time)>100";
        String sql = "SELECT neId FROM time where last_update_time > 1642899990";
        Dataset<Row> sqlDF = spark.sql(sql);

        System.out.println("----Total ne need to be updated is: " + sqlDF.count());
        Instant time2 = Instant.now();
        System.out.println("----Time select records: " + Duration.between(time1, time2).toMillis() + " milliseconds");

        //2. send alarm + create dataFrame for step 3
        DataFrame updateNe = spark.createDataFrame();
        sqlDF.foreachPartition(new ForeachPartitionFunction<Row>() {
            public void call(Iterator<Row> t) throws Exception {
                Jedis jedis = new Jedis("localhost");
                Map<String, String> map = new HashMap<>();
                while (t.hasNext()) {
                    Row row = t.next();
                    System.out.println("Send alarm to FM: "+ row.getInt(0));
                    /*map.put("alarm_trigger_time", "1642899999");
                    jedis.hmset("time:" + row.getInt(0), map);*/
                }
            }
        });
        //3. batch update

        Instant time3 = Instant.now();
        System.out.println("----Time update all records: " + Duration.between(time2, time3).toMillis() + " milliseconds");

    }

    public static void insertRedis() {
        SparkSession spark = SparkSession
                .builder()
                .appName("vht")
                .master("local[*]")
                .config("spark.redis.host", "localhost")
                .config("spark.redis.port", "6379")
                .getOrCreate();
        List<Ne> arrays = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            arrays.add(new Ne(
                    i,
                    System.currentTimeMillis(),
                    "pm", false, 0, 0, 0, "", ""
            ));
        }
        Dataset<Row> df1 = spark.createDataFrame(arrays, Ne.class);

        df1.write()
                .format("org.apache.spark.sql.redis")
                .option("table", "time")
                .option("key.column", "neId")
                .mode(SaveMode.Overwrite)
                .save();
    }

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
            if (last < range) end = total;
            subArr.add(start);
            subArr.add(end);
            arr.add(subArr);
            start = start + range;
        }
        return arr;
    }
}

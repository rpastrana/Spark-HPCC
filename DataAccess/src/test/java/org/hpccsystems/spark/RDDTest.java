package org.hpccsystems.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import scala.collection.Seq;
import scala.collection.JavaConverters;
import scala.Tuple2;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;
import org.hpccsystems.spark.HpccFile;
import org.hpccsystems.spark.thor.RemapInfo;
import java.util.Arrays;
import java.io.InputStreamReader;
import java.io.BufferedReader;
//


/**
 * Test from to test RDD by reading the data and running Logistic Regression.
 * @author holtjd
 *
 */
public class RDDTest {
  public static void main(String[] args) throws Exception {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    SparkConf conf = new SparkConf().setAppName("Spark HPCC test");
    conf.setMaster("local[2]");
    System.out.print("Full path name to Spark: ");
    System.out.flush();
    String sparkHome = br.readLine();
    conf.setSparkHome(sparkHome);
    System.out.print("Fiull path to JAPI jar: ");
    System.out.flush();
    String japi_jar = br.readLine();
    System.out.print("Full path to Spark-HPCC jar: ");
    System.out.flush();
    String this_jar = br.readLine();
    // now have Spark inputs
    java.util.List<String> jar_list = Arrays.asList(this_jar, japi_jar);
    Seq<String> jar_seq = JavaConverters.iterableAsScalaIterableConverter(jar_list).asScala().toSeq();;
    conf.setJars(jar_seq);
    System.out.println("Spark configuration set");
    SparkContext sc = new SparkContext(conf);
    System.out.println("Spark context available");
    System.out.println("Now need HPCC file information");
    System.out.print("Enter protocol: ");
    System.out.flush();
    String protocol = br.readLine();
    System.out.print("Enter ip: ");
    System.out.flush();
    String esp_ip = br.readLine();
    System.out.print("Enter port: ");
    System.out.flush();
    String port = br.readLine();
    System.out.print("Enter file name: ");
    System.out.flush();
    String testName = br.readLine();
    System.out.print("User id: ");
    System.out.flush();
    String user = br.readLine();
    System.out.print("pass word: ");
    System.out.flush();
    String pword = br.readLine();
    System.out.print("Number of nodes for remap or empty: ");
    System.out.flush();
    String nodes = br.readLine();
    System.out.print("Base IP or empty: ");
    System.out.flush();
    String base_ip = br.readLine();
    HpccFile hpcc;
    if (nodes.equals("") || base_ip.equals("")) {
      hpcc = new HpccFile(testName, protocol, esp_ip, port, user, pword);
    } else {
      RemapInfo ri = new RemapInfo(Integer.parseInt(nodes), base_ip);
      hpcc = new HpccFile(testName, protocol, esp_ip, port, user, pword, ri);
    }
    System.out.println("Getting file parts");
    FilePart[] parts = hpcc.getFileParts();
    System.out.println("Getting record definition");
    RecordDef rd = hpcc.getRecordDefinition();
    System.out.println(rd.toString());
    System.out.println("Creating RDD");
    HpccRDD myRDD = new HpccRDD(sc, parts, rd);
    System.out.println("Getting local iterator");
    scala.collection.Iterator<Record> rec_iter = myRDD.toLocalIterator();
    while (rec_iter.hasNext()) {
      Record rec = rec_iter.next();
      System.out.println(rec.toString());
    }
    System.out.println("Completed output of Record data");
    System.out.println("Convert to labeled point and run logistic regression");
    String[] names = {"petal_length","petal_width", "sepal_length", "sepal_width"};
    RDD<LabeledPoint> lpRDD = myRDD.makeMLLibLabeledPoint("class", names);
    LogisticRegressionWithLBFGS lr  = new LogisticRegressionWithLBFGS();
    lr.setNumClasses(3);
    LogisticRegressionModel iris_model = lr.run(lpRDD);
    System.out.println(iris_model.toString());
    System.out.println("Generate confusion matrix");
    Function<LabeledPoint, Tuple2<Object, Object>> my_f
      = new Function<LabeledPoint, Tuple2<Object, Object>>() {
      static private final long serialVersionUID = 1L;
      public Tuple2<Object, Object> call(LabeledPoint lp) {
        Double label = new Double(lp.label());
        Double predict = new Double(iris_model.predict(lp.features()));
        return new Tuple2<Object, Object>(predict, label);
      }
    };
    ClassTag<LabeledPoint> typ = ClassTag$.MODULE$.apply(LabeledPoint.class);
    JavaRDD<LabeledPoint> lpJavaRDD = new JavaRDD<LabeledPoint>(lpRDD, typ);
    RDD<Tuple2<Object, Object>> predAndLabelRDD = lpJavaRDD.map(my_f).rdd();
    MulticlassMetrics metrics = new MulticlassMetrics(predAndLabelRDD);
    System.out.println("Confusion matrix:");
    System.out.println(metrics.confusionMatrix());
    System.out.println("End of run");
  }
}

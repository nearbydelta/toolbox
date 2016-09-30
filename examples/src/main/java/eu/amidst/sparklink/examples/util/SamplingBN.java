package eu.amidst.sparklink.examples.util;

import eu.amidst.core.io.BayesianNetworkLoader;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.sparklink.core.data.DataSpark;
import eu.amidst.sparklink.core.util.BayesianNetworkSampler;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;

import java.io.IOException;

/**
 * Created by rcabanas on 27/09/16.
 */
public class SamplingBN {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		//Setting up spark
		SparkConf conf = new SparkConf().setAppName("SparkLink!").setMaster("local");
		JavaSparkContext jsc = new JavaSparkContext(conf);
		SQLContext sqlContext = new SQLContext(jsc);


		// Open a Bayesian network
		String file = "networks/simulated/WasteIncinerator.bn";

		BayesianNetwork bn = BayesianNetworkLoader.loadFromFile(file);
		System.out.println(bn);


		// Sample from the BN
/*		int nSamples = 1000;
		int parallelism = 4;
		BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
		DataSpark data = sampler.sampleToDataSpark(jsc, nSamples, parallelism);


		// Save it as a json file
		data.getDataFrame(sqlContext).write().format("json").save("datasets/simulated/WI_samples.json");
*/
	}



}





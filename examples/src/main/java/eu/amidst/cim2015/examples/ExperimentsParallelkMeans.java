package eu.amidst.cim2015.examples;

import eu.amidst.core.datastream.Attributes;
import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.datastream.DataOnMemoryListContainer;
import eu.amidst.core.datastream.DataStream;
import eu.amidst.core.io.DataStreamLoader;
import eu.amidst.core.io.DataStreamWriter;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.utils.BayesianNetworkGenerator;
import eu.amidst.core.utils.BayesianNetworkSampler;
import eu.amidst.core.utils.OptionParser;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by ana@cs.aau.dk on 01/07/15.
 */
public class ExperimentsParallelkMeans {

    static int k = 2;
    static int numDiscVars = 5;
    static int numGaussVars = 5;
    static int numStates = 2;
    static int sampleSize = 10000;
    static boolean sampleData = true;
    static int batchSize = 100;

    static Attributes atts;
    /*Need to store the centroids*/
    //static double[][] centroids;
    public static DataOnMemoryListContainer centroids;

    public static int getNumStates() {
        return numStates;
    }

    public static void setNumStates(int numStates) {
        ExperimentsParallelML.numStates = numStates;
    }

    public static int getNumDiscVars() {
        return numDiscVars;
    }

    public static void setNumDiscVars(int numDiscVars) {
        ExperimentsParallelML.numDiscVars = numDiscVars;
    }

    public static int getNumGaussVars() {
        return numGaussVars;
    }

    public static void setNumGaussVars(int numGaussVars) {
        ExperimentsParallelML.numGaussVars = numGaussVars;
    }

    public static int getSampleSize() {
        return sampleSize;
    }

    public static void setSampleSize(int sampleSize) {
        ExperimentsParallelML.sampleSize = sampleSize;
    }

    public static boolean isSampleData() {
        return sampleData;
    }

    public static void setSampleData(boolean sampleData) {
        ExperimentsParallelML.sampleData = sampleData;
    }

    public static int getK() {
        return k;
    }

    public static void setK(int k) {
        ExperimentsParallelkMeans.k = k;
    }

    public static void runParallelKMeans() throws IOException {

        DataStream<DataInstance> data;
        if(isSampleData()) {
            BayesianNetworkGenerator.setNumberOfGaussianVars(getNumGaussVars());
            BayesianNetworkGenerator.setNumberOfMultinomialVars(getNumDiscVars(), getNumStates());
            BayesianNetwork bn = BayesianNetworkGenerator.generateBayesianNetwork();
            data = new BayesianNetworkSampler(bn).sampleToDataStream(getSampleSize());
            DataStreamWriter.writeDataToFile(data, "./datasets/tmp.arff");
        }

        data = DataStreamLoader.openFromFile("datasets/tmp.arff");
        atts = data.getAttributes();

        /*Need to store the centroids*/


        //centroids = new double[getK()][atts.getNumberOfAttributes()];
        centroids = new DataOnMemoryListContainer(data.getAttributes());

                AtomicInteger index = new AtomicInteger();
        //data.stream().limit(getK()).forEach(dataInstance -> centroids[index.getAndIncrement()]=dataInstance.toArray());
        data.stream().limit(getK()).forEach(dataInstance -> centroids.add(dataInstance));
        data.restart();

        boolean change = true;
        while(change){

            Map<DataInstance, Averager> oldAndNewCentroids =
                    data.parallelStream(batchSize)
                    .map(instance -> Pair.newPair(centroids, instance))
                    .collect(Collectors.groupingBy(pair -> pair.getCentroid(),
                            Collectors.reducing(new Averager(atts.getNumberOfAttributes()), p -> new Averager(p.getDataInstance()), Averager::combine)));

            //oldAndNewCentroids.values().stream().map(averager -> averager.average());
            Map<double[], double[]> newCentroids = oldAndNewCentroids.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                                    e -> e.getKey().toArray(),
                                    e -> e.getValue().average())
                    );

            //for()

        }

    }

    public static String classNameID(){
        return "eu.amidst.cim2015.examples.batchSizeComparisonsML";
    }

    public static String getOption(String optionName) {
        return OptionParser.parse(classNameID(), listOptions(), optionName);
    }

    public static int getIntOption(String optionName){
        return Integer.parseInt(getOption(optionName));
    }

    public static boolean getBooleanOption(String optionName){
        return getOption(optionName).equalsIgnoreCase("true") || getOption(optionName).equalsIgnoreCase("T");
    }

    public static String listOptions(){

        return  classNameID() +",\\"+
                "-sampleSize, 1000000, Sample size of the dataset\\" +
                "-numStates, 10, Num states of all disc. variables (including the class)\\"+
                "-GV, 5000, Num of gaussian variables\\"+
                "-DV, 5000, Num of discrete variables\\"+
                "-k, 2, Num of clusters\\"+
                "-sampleData, true, Sample arff data (if not read datasets/sampleBatchSize.arff by default)\\";
    }

    public static void loadOptions() {
        setNumGaussVars(getIntOption("-GV"));
        setNumDiscVars(getIntOption("-DV"));
        setNumStates(getIntOption("-numStates"));
        setSampleSize(getIntOption("-sampleSize"));
        setK(getIntOption("-k"));
        setSampleData(getBooleanOption("-sampleData"));
    }




    public static void main(String[] args) throws Exception {
        runParallelKMeans();
    }
}
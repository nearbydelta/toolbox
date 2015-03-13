package eu.amidst.core.learning;

import eu.amidst.core.datastream.DataInstance;
import eu.amidst.core.datastream.DataStream;
import eu.amidst.core.distribution.Multinomial;
import eu.amidst.core.distribution.Multinomial_MultinomialParents;
import eu.amidst.core.distribution.Normal;
import eu.amidst.core.distribution.Normal_NormalParents;
import eu.amidst.core.inference.InferenceEngineForBN;
import eu.amidst.core.inference.VMP;
import eu.amidst.core.io.BayesianNetworkLoader;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.models.DAG;
import eu.amidst.core.utils.BayesianNetworkSampler;
import eu.amidst.core.variables.StaticVariables;
import eu.amidst.core.variables.Variable;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by ana@cs.aau.dk on 04/03/15.
 */
public class BayesianVMPTest extends TestCase {

    public static void testMultinomials1() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varA = variables.newMultionomialVariable("A", 2);

        DAG dag = new DAG(variables);


        BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);

        Multinomial distA = bn.getDistribution(varA);

        distA.setProbabilities(new double[]{0.6, 0.4});

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
        sampler.setSeed(2);
        DataStream<DataInstance> data = sampler.sampleToDataBase(1000);


        BayesianNetwork learntNormalVarBN = LearningEngineForBN.learnParameters(bn.getDAG(), data);

        System.out.println(learntNormalVarBN.toString());

        StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
        svb.setWindowsSize(1000);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMP().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(100);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

        BayesianLearningEngineForBN.setDAG(bn.getDAG());
        BayesianLearningEngineForBN.setDataStream(data);
        BayesianLearningEngineForBN.runLearning();

        BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

        System.out.println(bn.toString());
        System.out.println(learnBN.toString());
        assertTrue(bn.equalBNs(learnBN,0.05));
    }

    public static void testMultinomials2() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varA = variables.newMultionomialVariable("A", 2);
        Variable varB = variables.newMultionomialVariable("B", 2);

        DAG dag = new DAG(variables);

        dag.getParentSet(varB).addParent(varA);

        BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);

        Multinomial distA = bn.getDistribution(varA);
        Multinomial_MultinomialParents distB = bn.getDistribution(varB);

        distA.setProbabilities(new double[]{0.6, 0.4});
        distB.getMultinomial(0).setProbabilities(new double[]{0.75, 0.25});
        distB.getMultinomial(1).setProbabilities(new double[]{0.25, 0.75});

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
        sampler.setSeed(2);
        DataStream<DataInstance> data = sampler.sampleToDataBase(1000);


        System.out.println(LearningEngineForBN.learnParameters(bn.getDAG(), data).toString());

        StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
        svb.setWindowsSize(1000);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMP().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(100);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

        BayesianLearningEngineForBN.setDAG(bn.getDAG());
        BayesianLearningEngineForBN.setDataStream(data);
        BayesianLearningEngineForBN.runLearning();

        BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

        System.out.println(bn.toString());
        System.out.println(learnBN.toString());
        assertTrue(bn.equalBNs(learnBN,0.05));
    }

    public static void testMultinomials3() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varA = variables.newMultionomialVariable("A", 2);
        Variable varB = variables.newMultionomialVariable("B", 2);
        Variable varC = variables.newMultionomialVariable("C", 2);

        DAG dag = new DAG(variables);

        dag.getParentSet(varB).addParent(varA);
        dag.getParentSet(varC).addParent(varA);


        BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);

        bn.randomInitialization(new Random(6));

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
        sampler.setSeed(2);
        sampler.setHiddenVar(varA);
        DataStream<DataInstance> data = sampler.sampleToDataBase(20000);


        StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
        svb.setWindowsSize(1000);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMP().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(100);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

        BayesianLearningEngineForBN.setDAG(bn.getDAG());
        BayesianLearningEngineForBN.setDataStream(data);
        BayesianLearningEngineForBN.runLearning();

        BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

        System.out.println(bn.toString());
        System.out.println(learnBN.toString());
        assertTrue(bn.equalBNs(learnBN,0.1));
    }

    public static void testMultinomials4() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varA = variables.newMultionomialVariable("A", 2);
        Variable varB = variables.newMultionomialVariable("B", 2);
        Variable varC = variables.newMultionomialVariable("C", 2);

        DAG dag = new DAG(variables);

        dag.getParentSet(varC).addParent(varB);
        dag.getParentSet(varB).addParent(varA);



        BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);

        bn.randomInitialization(new Random(5));

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
        sampler.setSeed(5);
        sampler.setHiddenVar(varB);
        DataStream<DataInstance> data = sampler.sampleToDataBase(50000);


        StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
        svb.setWindowsSize(1000);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMP().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(100);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

        BayesianLearningEngineForBN.setDAG(bn.getDAG());
        BayesianLearningEngineForBN.setDataStream(data);
        BayesianLearningEngineForBN.runLearning();

        BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

        System.out.println(bn.toString());
        System.out.println(learnBN.toString());
        assertTrue(bn.equalBNs(learnBN, 0.1));

    }


    public static void testMultinomials5() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varA = variables.newMultionomialVariable("A", 5);
        Variable varB = variables.newMultionomialVariable("B", 5);
        Variable varC = variables.newMultionomialVariable("C", 5);

        DAG dag = new DAG(variables);

        dag.getParentSet(varC).addParent(varB);
        dag.getParentSet(varB).addParent(varA);



        BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);

        bn.randomInitialization(new Random(5));

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
        sampler.setSeed(5);
        sampler.setHiddenVar(varB);
        DataStream<DataInstance> data = sampler.sampleToDataBase(10000);


        StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
        svb.setWindowsSize(1000);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMP().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(100);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

        BayesianLearningEngineForBN.setDAG(bn.getDAG());
        BayesianLearningEngineForBN.setDataStream(data);
        BayesianLearningEngineForBN.runLearning();

        BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

        System.out.println(bn.toString());
        System.out.println(learnBN.toString());
        //assertTrue(bn.equalBNs(learnBN, 0.1));

    }

    public static void testMultinomial6() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varB = variables.newMultionomialVariable("B",4);

        DAG dag = new DAG(variables);


        for (int i = 0; i < 10; i++) {


            BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);
            bn.randomInitialization(new Random(0));


            bn.randomInitialization(new Random(0));

            BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
            sampler.setSeed(i+299);
            sampler.setHiddenVar(varB);
            DataStream<DataInstance> data = sampler.sampleToDataBase(10000);


            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(1000);
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(100);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(bn.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(bn.toString());
            System.out.println(learnBN.toString());
            //assertTrue(bn.equalBNs(learnBN, 0.5));
        }
    }

    public static void testAsia() throws IOException, ClassNotFoundException{

        BayesianNetwork asianet = BayesianNetworkLoader.loadFromFile("networks/asia.bn");
        asianet.randomInitialization(new Random(0));
        System.out.println("\nAsia network \n ");

        BayesianNetworkSampler sampler = new BayesianNetworkSampler(asianet);
        sampler.setSeed(0);
        DataStream<DataInstance> data = sampler.sampleToDataBase(10000);

        StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
        svb.setWindowsSize(1000);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMP().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(100);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

        BayesianLearningEngineForBN.setDAG(asianet.getDAG());
        BayesianLearningEngineForBN.setDataStream(data);
        BayesianLearningEngineForBN.runLearning();

        System.out.println(BayesianLearningEngineForBN.getLogMarginalProbability());

        BayesianNetwork learnAsianet = BayesianLearningEngineForBN.getLearntBayesianNetwork();

        System.out.println(asianet.toString());
        System.out.println(learnAsianet.toString());
        assertTrue(asianet.equalBNs(learnAsianet,0.05));

    }

    public static void testGaussian0() throws IOException, ClassNotFoundException{

        BayesianNetwork oneNormalVarBN = BayesianNetworkLoader.loadFromFile("networks/Normal.bn");

        for (int i = 0; i < 10; i++) {

            Variable varA = oneNormalVarBN.getStaticVariables().getVariableByName("A");
            Normal dist = oneNormalVarBN.getDistribution(varA);

            dist.setMean(2000);
            dist.setVariance(30);

            oneNormalVarBN.randomInitialization(new Random(i));

            System.out.println("\nOne normal variable network \n ");

            BayesianNetworkSampler sampler = new BayesianNetworkSampler(oneNormalVarBN);
            sampler.setSeed(0);

            DataStream<DataInstance> data = sampler.sampleToDataBase(1000);

            System.out.println(LearningEngineForBN.learnParameters(oneNormalVarBN.getDAG(), data).toString());

            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(100);
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(100);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(oneNormalVarBN.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            System.out.println(BayesianLearningEngineForBN.getLogMarginalProbability());

            BayesianNetwork learntOneNormalVarBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(oneNormalVarBN.toString());
            System.out.println(learntOneNormalVarBN.toString());
            assertTrue(oneNormalVarBN.equalBNs(learntOneNormalVarBN, 0.1));
        }
    }

    public static void testWasteIncinerator() throws IOException, ClassNotFoundException{

        BayesianNetwork normalVarBN = BayesianNetworkLoader.loadFromFile("networks/WasteIncinerator.bn");

        //normalVarBN.randomInitialization(new Random(0));
        System.out.println("\nNormal|2Normal variable network \n ");


        BayesianNetworkSampler sampler = new BayesianNetworkSampler(normalVarBN);
        sampler.setSeed(1);
        DataStream<DataInstance> data = sampler.sampleToDataBase(10000);

        BayesianNetwork learntNormalVarBN = LearningEngineForBN.learnParameters(normalVarBN.getDAG(), data);

        System.out.println(normalVarBN.toString());
        System.out.println(learntNormalVarBN.toString());
        assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.2));

        StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
        svb.setWindowsSize(1000);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMP().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(1000);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

        BayesianLearningEngineForBN.setDAG(normalVarBN.getDAG());
        BayesianLearningEngineForBN.setDataStream(data);
        BayesianLearningEngineForBN.runLearning();

        System.out.println(BayesianLearningEngineForBN.getLogMarginalProbability());

        learntNormalVarBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

        System.out.println(normalVarBN.toString());
        System.out.println(learntNormalVarBN.toString());
        assertTrue(normalVarBN.equalBNs(learntNormalVarBN,0.2));


    }

    public static void testGaussian1() throws IOException, ClassNotFoundException{

        BayesianNetwork normalVarBN = BayesianNetworkLoader.loadFromFile("networks/Normal_1NormalParents.bn");

        for (int i = 0; i < 10; i++) {

            System.out.println("\nNormal|Normal variable network \n ");

            normalVarBN.randomInitialization(new Random(i));

            BayesianNetworkSampler sampler = new BayesianNetworkSampler(normalVarBN);
            sampler.setSeed(2);
            DataStream<DataInstance> data = sampler.sampleToDataBase(10000);

            BayesianNetwork learntNormalVarBN = LearningEngineForBN.learnParameters(normalVarBN.getDAG(), data);

            System.out.println(normalVarBN.toString());
            System.out.println(learntNormalVarBN.toString());
            assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.1));

            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(1000);
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(normalVarBN.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            System.out.println(BayesianLearningEngineForBN.getLogMarginalProbability());
            learntNormalVarBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(normalVarBN.toString());
            System.out.println(learntNormalVarBN.toString());
            assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.1));
        }

    }

    public static void testGaussian2() throws IOException, ClassNotFoundException{

        BayesianNetwork normalVarBN = BayesianNetworkLoader.loadFromFile("networks/Normal_NormalParents.bn");

        int cont=0;
        for (int i = 0; i < 10; i++) {

            normalVarBN.randomInitialization(new Random(i));
            System.out.println("\nNormal|2Normal variable network \n ");


            BayesianNetworkSampler sampler = new BayesianNetworkSampler(normalVarBN);
            sampler.setSeed(0);
            DataStream<DataInstance> data = sampler.sampleToDataBase(10000);

            BayesianNetwork learntNormalVarBN = LearningEngineForBN.learnParameters(normalVarBN.getDAG(), data);

            System.out.println(normalVarBN.toString());
            System.out.println(learntNormalVarBN.toString());
            assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.1));


            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(1000);
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(normalVarBN.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            learntNormalVarBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(normalVarBN.toString());
            System.out.println(learntNormalVarBN.toString());
            assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.1));
        }
    }

    public static void testGaussian3() throws IOException, ClassNotFoundException{

        BayesianNetwork normalVarBN = BayesianNetworkLoader.loadFromFile("networks/Normal_MultinomialParents.bn");

        for (int i = 0; i < 10; i++) {


            normalVarBN.randomInitialization(new Random(i));
            System.out.println("\nNormal|2Normal variable network \n ");

            BayesianNetworkSampler sampler = new BayesianNetworkSampler(normalVarBN);
            sampler.setSeed(0);
            DataStream<DataInstance> data = sampler.sampleToDataBase(10000);

            BayesianNetwork learntNormalVarBN = LearningEngineForBN.learnParameters(normalVarBN.getDAG(), data);

            System.out.println(normalVarBN.toString());
            System.out.println(learntNormalVarBN.toString());
            assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.2));

            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(1000);
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(100);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(normalVarBN.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            learntNormalVarBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(normalVarBN.toString());
            System.out.println(learntNormalVarBN.toString());
            assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.2));
        }
    }

    public static void testGaussian4() throws IOException, ClassNotFoundException{

        BayesianNetwork normalVarBN = BayesianNetworkLoader.loadFromFile("networks/Normal_MultinomialNormalParents.bn");
        int contA=0;
        int contB=0;

        for (int i = 1; i < 2; i++) {

            normalVarBN.randomInitialization(new Random(i));
            System.out.println("\nNormal|2Normal variable network \n ");


            BayesianNetworkSampler sampler = new BayesianNetworkSampler(normalVarBN);
            sampler.setSeed(0);
            DataStream<DataInstance> data = sampler.sampleToDataBase(10000);

            BayesianNetwork learntNormalVarBN = LearningEngineForBN.learnParameters(normalVarBN.getDAG(), data);

            System.out.println(normalVarBN.toString());
            System.out.println(learntNormalVarBN.toString());
            assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.3));
            if (normalVarBN.equalBNs(learntNormalVarBN, 0.3)) contA++;

            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(1000);
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(normalVarBN.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            learntNormalVarBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(normalVarBN.toString());
            System.out.println(learntNormalVarBN.toString());
            assertTrue(normalVarBN.equalBNs(learntNormalVarBN, 0.3));
            if (normalVarBN.equalBNs(learntNormalVarBN, 0.3)) contB++;
        }
        System.out.println(contA);
        System.out.println(contB);

    }



    public static void testGaussian5() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varA = variables.newGaussianVariable("A");
        Variable varB = variables.newGaussianVariable("B");
        Variable varC = variables.newGaussianVariable("C");

        DAG dag = new DAG(variables);

        dag.getParentSet(varB).addParent(varA);
        dag.getParentSet(varC).addParent(varA);


        BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);
        bn.randomInitialization(new Random(0));

        System.out.println(bn.toString());
        BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
        sampler.setSeed(2);
        sampler.setHiddenVar(varA);
        DataStream<DataInstance> data = sampler.sampleToDataBase(50);


        StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
        svb.setWindowsSize(5); //Set to 2 and an exception is raised. Numerical instability.
        svb.setSeed(1);
        VMP vmp = svb.getPlateuVMP().getVMP();
        vmp.setTestELBO(true);
        vmp.setMaxIter(1000);
        vmp.setThreshold(0.001);
        BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

        BayesianLearningEngineForBN.setDAG(bn.getDAG());
        BayesianLearningEngineForBN.setDataStream(data);
        BayesianLearningEngineForBN.runLearning();

        BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

        System.out.println(bn.toString());
        System.out.println(learnBN.toString());
        //assertTrue(bn.equalBNs(learnBN,0.1));
    }

    public static void testGaussian6() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varA = variables.newGaussianVariable("A");
        Variable varB = variables.newGaussianVariable("B");
        Variable varC = variables.newGaussianVariable("C");

        DAG dag = new DAG(variables);

        dag.getParentSet(varB).addParent(varA);
        dag.getParentSet(varC).addParent(varB);

        for (int i = 0; i < 1; i++) {


            BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);

            bn.randomInitialization(new Random(i));

            System.out.println(bn.toString());
            BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
            sampler.setSeed(i);
            sampler.setMARVar(varB, 0.9);
            DataStream<DataInstance> data = sampler.sampleToDataBase(10000);


            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(1000);
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(bn.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            System.out.println("Data Prob: " + BayesianLearningEngineForBN.getLogMarginalProbability());

            BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(bn.toString());
            System.out.println(learnBN.toString());
            assertTrue(bn.equalBNs(learnBN, 0.2));
        }
    }

    public static void testGaussian7() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varB = variables.newGaussianVariable("B");

        DAG dag = new DAG(variables);


        for (int i = 0; i < 10; i++) {


            BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);
            bn.randomInitialization(new Random(0));


            bn.randomInitialization(new Random(0));

            BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
            sampler.setSeed(i);
            sampler.setHiddenVar(varB);
            DataStream<DataInstance> data = sampler.sampleToDataBase(10);


            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(10); //Set to 2 and an exception is raised. Numerical instability.
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(bn.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            System.out.println("Data Prob: " + BayesianLearningEngineForBN.getLogMarginalProbability());


            BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(bn.toString());
            System.out.println(learnBN.toString());
            //assertTrue(bn.equalBNs(learnBN, 0.5));
        }
    }

    public static void testGaussian8() throws IOException, ClassNotFoundException {
        StaticVariables variables = new StaticVariables();
        Variable varA = variables.newGaussianVariable("A");
        Variable varB = variables.newGaussianVariable("B");
        Variable varC = variables.newGaussianVariable("C");

        DAG dag = new DAG(variables);

        dag.getParentSet(varB).addParent(varA);
        dag.getParentSet(varC).addParent(varB);

        for (int i = 1; i < 2; i++) {


            BayesianNetwork bn = BayesianNetwork.newBayesianNetwork(dag);
            bn.randomInitialization(new Random(0));

            bn.randomInitialization(new Random(i));

            //System.out.println(bn.toString());
            BayesianNetworkSampler sampler = new BayesianNetworkSampler(bn);
            sampler.setSeed(i+299);
            sampler.setHiddenVar(varB);
            DataStream<DataInstance> data = sampler.sampleToDataBase(10000);


            StreamingVariationalBayesVMP svb = new StreamingVariationalBayesVMP();
            svb.setWindowsSize(10);
            svb.setSeed(i);
            VMP vmp = svb.getPlateuVMP().getVMP();
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.001);
            BayesianLearningEngineForBN.setBayesianLearningAlgorithmForBN(svb);

            BayesianLearningEngineForBN.setDAG(bn.getDAG());
            BayesianLearningEngineForBN.setDataStream(data);
            BayesianLearningEngineForBN.runLearning();

            System.out.println("Data Prob: " + BayesianLearningEngineForBN.getLogMarginalProbability());


            BayesianNetwork learnBN = BayesianLearningEngineForBN.getLearntBayesianNetwork();

            System.out.println(bn.toString());
            System.out.println(learnBN.toString());
            Normal_NormalParents distCP = bn.getDistribution(varC);
            Normal_NormalParents distCQ = learnBN.getDistribution(varC);

            assertEquals(distCP.getSd(), distCQ.getSd(), 0.05);
        }
    }
}

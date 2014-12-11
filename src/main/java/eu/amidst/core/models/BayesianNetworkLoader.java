package eu.amidst.core.models;

import COM.hugin.HAPI.DefaultClassParseListener;
import COM.hugin.HAPI.Domain;
import COM.hugin.HAPI.ExceptionHugin;
import COM.hugin.HAPI.ParseListener;
import eu.amidst.core.huginlink.ConverterToAMIDST;

import java.io.*;

/**
 * Created by afa on 11/12/14.
 */
public class BayesianNetworkLoader {

    public static BayesianNetwork loadFromHugin(String file) throws ExceptionHugin {

        ParseListener parseListener = new DefaultClassParseListener();
        Domain huginBN = new Domain (file, parseListener);
        BayesianNetwork amidstBN = ConverterToAMIDST.convertToAmidst(huginBN);
        return amidstBN;
    }

    public static BayesianNetwork loadFromSerializableObject(String file) throws IOException, ClassNotFoundException {
        ObjectInputStream objectIn = new ObjectInputStream(new FileInputStream(file));
        return (BayesianNetwork)objectIn.readObject();

    }
}
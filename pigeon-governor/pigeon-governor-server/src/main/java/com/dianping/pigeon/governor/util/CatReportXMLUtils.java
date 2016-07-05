package com.dianping.pigeon.governor.util;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created by shihuashen on 16/6/29.
 */
public class CatReportXMLUtils {
    //Since the TransactionReport model differ from the real Cat XML, we need to convert to fit.
    //The java model is : <transaction-report></transaction-report>.
    //The XML format is : <transaction><report></report></transaction>.
    public static String XMLFitFormat(String origin){
        StringBuilder sb = new StringBuilder(origin);
        String prefix = "<transaction><report";
        String postfix = "report></transaction>";
        sb.replace(0,prefix.length(),"<transaction-report");
        int length = sb.length();
        sb.replace(length-postfix.length(),length,"transaction-report>");
        return sb.toString();
    }
    public static TransactionReport convertXMLToModel(String xml){
        TransactionReport tr = null;
        try {
            tr = DefaultSaxParser.parse(xml);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tr;
    }
}

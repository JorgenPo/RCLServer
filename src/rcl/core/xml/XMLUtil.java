package rcl.core.xml;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by jorgen on 23.12.16.
 */
public class XMLUtil {
    public static Document fromXML(String xml) {
        Document document = null;
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = bf.newDocumentBuilder();
            document = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        } catch (Exception e) {
            System.err.println("XML parse error: " + e.getMessage());
            e.printStackTrace();
        }

        return document;
    }

    public static String getFunctionName(Document doc) {
        Node methodName = doc.getElementsByTagName("methodName").item(0);

        return methodName.getTextContent();
    }

    public static ArrayList<String> getParams(Document doc) {
        ArrayList<String> params = new ArrayList<>();

        NodeList paramsNode = doc.getElementsByTagName("param");

        for ( int i = 0; i < paramsNode.getLength(); ++i ) {
            params.add(paramsNode.item(i).getFirstChild().getTextContent());
        }

        return params;
    }

    public static String makeResponse(String result) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = builder.newDocument();

        Element body = document.createElement("methodResponse");
        document.appendChild(body);

        Element params = document.createElement("params");
        body.appendChild(params);
        Element param = document.createElement("param");
        params.appendChild(param);
        Element value = document.createElement("value");
        param.appendChild(value);
        Element string = document.createElement("string");
        value.appendChild(string);

        string.setTextContent(result);

        return XMLUtil.documentToText(document);
    }

    static String documentToText(Document doc) {
        Writer out = new StringWriter();
        XMLSerializer serializer = new XMLSerializer(out, null);

        try {
            serializer.serialize(doc);
        } catch (IOException e) {
            System.err.println("Error serializing document");
            e.printStackTrace();
        }

        return out.toString();
    }
}

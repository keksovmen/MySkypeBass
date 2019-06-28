package Bin.Util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Simple xml worker
 * Just to get sound notification names
 */

public class XMLWorker {

    /**
     * Tries to get a document from the given name of resource
     *
     * @param resource name absolute reference
     * @return loaded document
     * @throws ParserConfigurationException if can't create new document builder
     * @throws IOException                  can't read file
     * @throws SAXException                 format is broken
     */

    static Document getDocument(String resource) throws ParserConfigurationException, IOException, SAXException {
        InputStream stream = XMLWorker.class.getClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            throw new NoSuchElementException("File can't be found");
        }
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return documentBuilder.parse(stream);
    }

    /**
     * Method to get names
     * Document should look like <root><file><name>here goes file name.format</name></file></root>
     * But you can put it in more deeper state cause
     * method tries to find <name> attribute and gets its text info
     *
     * @param resourceName name of recurse to load
     * @return empty if exception or filled list with names
     */

    public static List<String> retrieveNames(String resourceName) {
        Document document;
        List<String> result;
        try {
            document = getDocument(resourceName);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
        result = new ArrayList<>();
        Element root = document.getDocumentElement();//<list>
        /*How it looks like but in details*/
//        NodeList rootChildNodes = root.getChildNodes();//<file>
//        for (int i = 0; i < rootChildNodes.getLength(); i++) {
//            Node item = rootChildNodes.item(i);//<file>
//            if (item instanceof Element){
//                NodeList childNodes = item.getChildNodes();//<name> and others
//                for (int j = 0; j < childNodes.getLength(); j++) {
//                    Node k = childNodes.item(j);//<name> and others
//                    if (k instanceof Element){
//                        if (((Element) k).getTagName().equals("name")){//only <name>
//                            result.add(k.getTextContent().trim());//remove whitespaces
//                        }
//                    }
//                }
//            }
//        }
        iterate(root, "name", result);
        return result;
    }

    /**
     * Recursive
     * Search each element and its children for specific attribute name
     * and gets its text info, also trims it
     *
     * @param element       root or whatever
     * @param attributeName to look for
     * @param toAppendTo    where to put treasures
     */

    private static void iterate(Element element, String attributeName, List<String> toAppendTo) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element) {
                iterate((Element) item, attributeName, toAppendTo);
                if (((Element) item).getTagName().equals(attributeName)) {
                    toAppendTo.add(item.getTextContent().trim());
                }
            }
        }
    }
}

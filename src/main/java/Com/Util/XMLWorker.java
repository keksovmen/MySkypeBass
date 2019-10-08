package Com.Util;

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

/**
 * Simple xml worker
 * Just to get sound notification names
 */

public class XMLWorker {

    /**
     * Tries to get a document from the given name of resource
     * Also if a document has DTD will check for it
     *
     * @param resource name absolute reference
     * @return loaded document
     * @throws ParserConfigurationException if can't create new document builder
     * @throws IOException                  can't read file
     * @throws SAXException                 format is broken
     */

    private static Document getDocument(String resource) throws ParserConfigurationException, IOException, SAXException {
        InputStream stream = Checker.getCheckedInput(resource);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setValidating(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
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

    public static List<Pair<String, String>> retrieveNames(String resourceName) {
        Document document;
        try {
            document = getDocument(resourceName);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return new ArrayList<>(0);
        }
        Element root = document.getDocumentElement();//<list>

        return getFromProperDocument(root);
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

    /**
     * Gets file names from a XML file with DTD
     * More clear version of iterate()
     * Also check if file is enabled
     *
     * @param root should be root element like <list></list>
     * @return List filled with sound names
     */

    private static List<Pair<String, String>> getFromProperDocument(Element root) {
        NodeList childNodes = root.getChildNodes();
        List<Pair<String, String>> result = new ArrayList<>(childNodes.getLength());
        for (int i = 0; i < childNodes.getLength(); i++) {
            Element file = (Element) childNodes.item(i);
            if (file.getAttribute("enabled").equals("false")) {
                continue;
            }
            Element name = (Element) file.getFirstChild();
            Element description = (Element) file.getLastChild();
            result.add(new Pair<>(name.getTextContent(), description.getTextContent()));
        }
        return result;
    }
}

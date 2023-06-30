package org.assimbly.docconverter;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xml.serialize.OutputFormat;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.thoughtworks.xstream.XStream;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class DocConverter {

	private static String xml;
	private static String yaml;
	private static String json;
	private static String csv;
	
	// String conversions

	/**
	* @param inputstream InputStream 
    * @return String
	*/	
	public static String convertStreamToString(InputStream inputstream) {
		java.util.Scanner scanner = new java.util.Scanner(inputstream).useDelimiter("\\A");
		return scanner.hasNext() ? scanner.next() : "";
	}

	/**
	* @param string String
    * @return InputStream
    * @throws Exception (generic exception)
	*/	
	public static InputStream convertStringToStream(String string) throws Exception {
		return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
	}

	
	/**
	* @param doc (org.w3c.dom.Document XML document)
    * @return String
    * @throws Exception (generic exception)
	*/
	public static String convertDocToString(Document doc) throws Exception {

		OutputFormat format = new OutputFormat(doc);
		StringWriter stringOut = new StringWriter();
		XMLSerializer serial = new XMLSerializer(stringOut, format);
		serial.serialize(doc);
		return stringOut.toString();
	}

	/**
	* String conversion
	* 
	* @param string String
    * @return Document (org.w3c.dom.Document XML document)
    * @throws Exception (generic exception)
	*/
	public static Document convertStringToDoc(String string) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		return builder.parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * String conversion
	 *
	 * @param node Node (org.w3c.dom.Node)
	 * @return String
	 */
	public static String convertNodeToString(Node node) {

		DOMImplementationLS lsImpl = (DOMImplementationLS)node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
		LSSerializer lsSerializer = lsImpl.createLSSerializer();

		DOMConfiguration config = lsSerializer.getDomConfig();
		config.setParameter("xml-declaration", Boolean.FALSE);

		return lsSerializer.writeToString(node);

	}

	/**
	 * String conversion
	 *
	 * @param string String (xml node as string value. For example: &lt;node&gt;value&lt;/node&gt; )
	 * @return node Node (org.w3c.dom.Node)
	 * @throws Exception (generic exception)
	 */
	public Node convertStringToNode(String string) throws Exception {

		Element node = DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)))
				.getDocumentElement();

		return node;

	}

	/**
	* @param url (java.net.URL)
    * @return String
    * @throws Exception (generic exception)
	*/
	public static String convertURLToString(URL url) throws Exception {

		InputStream stream = url.openStream();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(stream);
		stream.close();

		String docAsString = convertDocToString(doc);

		return docAsString;
	}

	
	/**
	* @param uri (java.net.URI)
    * @return String
    * @throws Exception (generic exception)
	*/
	public static String convertUriToString(URI uri) throws Exception {

		URL url = uri.toURL(); // get URL from your uri object
		InputStream stream = url.openStream();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(stream);
		stream.close();

		String docAsString = convertDocToString(doc); 
		
		return docAsString;
	}

	/**
	* @param uri (java.net.URI)
    * @return Document (org.w3c.dom.Document XML document) 
    * @throws Exception (generic exception)
	*/	
	public static Document convertUriToDoc(URI uri) throws Exception {

		URL url = uri.toURL(); // get URL from your uri object
		InputStream stream = url.openStream();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(stream);
		stream.close();

		return doc;
	}

	/**
	* @param path as string (uses UTF-8 for encoding)
    * @return String
    * @throws Exception (generic exception)
	*/
   public static String convertFileToString(String path) throws Exception {
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  String fileAsString = new String(encoded, StandardCharsets.UTF_8);
	  return fileAsString;
	}


	/**
	* @param file File object
	* @return URL
    * @throws Exception (generic exception)
	*/
	  public static URI convertFileToURI(File file) throws Exception {
	      URI fileAsURI = file.toURI();   
		  return fileAsURI;
	  }

   
	/**
	* @param file File object
    * @return URL
    * @throws Exception (generic exception)
	*/
   public static URL convertFileToURL(File file) throws Exception {
      URL fileAsURL = file.toURI().toURL();   
	  return fileAsURL;
   }

   
	/**
	* @param string String object
	* @return Source as string
    * @throws Exception (generic exception)
	*/
	public static Source convertStringToSource(String string) throws Exception {
		Source stringAsSource = new StreamSource(new StringReader(string));
		return stringAsSource;
	}
	
	/**
	* @param path as String
	* @param encoding Charset
    * @return String
    * @throws Exception (generic exception)
	*/
	public static String convertFileToString(String path, Charset encoding) throws Exception {
		if(encoding==null) {
			encoding = StandardCharsets.UTF_8;
		}
		  byte[] encoded = Files.readAllBytes(Paths.get(path));

		  return new String(encoded, encoding);
	}

    /**
    * @param path as String
    * @param content as String
    * @throws Exception (generic exception)
    */
	public static void convertStringToFile(String path, String content) throws Exception {
		Files.write( Paths.get(path), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
	}

	/**
	* @param list List of strings
    * @return String
	*/
	public static String convertListToString(List<String> list)  {
		String listAsString = String.join(",", list);
		return listAsString;
	}

	/**
	* @param commaSeparatedString List of strings separated by a comma
    * @return List
	*/	
	public static List<String> convertStringToList(String commaSeparatedString)  {
		List<String> stringAsList = new ArrayList<String>(Arrays.asList(commaSeparatedString.split(",")));
		return stringAsList;
	}	

	/**
	* @param string String object
    * @return Reader
	*/	public static Reader convertStringToReader(String string)  {
		Reader reader = new StringReader(string);
		return reader;
	}

	/**
	* @param reader (java.io.Reader)
    * @return String
    * @throws Exception (generic exception)
	*/		
	public static String convertReaderToString(Reader reader) throws Exception  {
		String readerAsString = IOUtils.toString(reader);
		return readerAsString;
	}

	//data format conversions
	
	/**
	* @param xml as string
    * @return json as string
	* @throws Exception (generic exception)
	*/
	public static String convertXmlToJson(String xml) throws Exception {

		XmlMapper xmlMapper = new XmlMapper();

		xml = "<ObjectNode>" + xml + "</ObjectNode>";

		JsonNode node = xmlMapper.readTree(xml.getBytes(StandardCharsets.UTF_8));

		ObjectMapper objectMapper = new ObjectMapper();

		String json = objectMapper.writeValueAsString(node);

		return json;
	}

	/**
	* @param xml as string
    * @return yaml as string 
    * @throws Exception (generic exception)
	*/
	public static String convertXmlToYaml(String xml) throws Exception {

		JSONObject xmlJSONObj = XML.toJSONObject(xml);
		json = xmlJSONObj.toString();

		JsonNode jsonNode = new ObjectMapper().readTree(json);
		yaml = new YAMLMapper().writeValueAsString(jsonNode);

		return yaml;
	}

	/**
	* @param xml as String
    * @return csv as String
    * @throws Exception (generic exception)
	*/
	public static String convertXmlToCsv(String xml) throws Exception {

		String[] xmlLines = xml.split(System.lineSeparator());
		String stylesheet = "src/main/resources/xmltocsv.generic.xsl"; 
		if(xmlLines.length > 2) {
			if(xmlLines[0].equals("<rows>")||xmlLines[1].equals("<rows>")) {
				stylesheet = "src/main/resources/xmltocsv.xsl"; 
			}
		}
		
		Document document = convertStringToDoc(xml);
	    StringWriter writer = new StringWriter();
	    
        StreamSource stylesource = new StreamSource(new File(stylesheet));
        Transformer transformer = TransformerFactory.newInstance().newTransformer(stylesource);
        Source source = new DOMSource(document);
        Result result = new javax.xml.transform.stream.StreamResult(writer);
        transformer.transform(source, result);
        
		csv = writer.toString();;

		return csv;
	}

	/**
	* @param json as String
    * @return xml as String
    * @throws Exception (generic exception)
 
	*/
	public static String convertJsonToXml(String json) throws Exception {

		String xml = "{}";
		if(json.startsWith("[")){
			JSONArray jsonArray = new JSONArray(json);

			// Convert the JSONArray to a List of Maps
			List<Object> list = jsonArray.toList();

			// Convert the List of Maps to XML
			XmlMapper xmlMapper = new XmlMapper();
			xml = xmlMapper.writeValueAsString(list);
		}else{
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNode = objectMapper.readTree(json);

			XmlMapper xmlMapper = new XmlMapper();
			xml = xmlMapper.writeValueAsString(jsonNode);

			//if xml already has a root node then ObjectNode element can be stripped
			String newXml = StringUtils.substringBetween(xml,"<ObjectNode>","</ObjectNode>");
			if(isXML(newXml)){
				xml = newXml;
			}

		}		

		return xml;

	}

	/**
	* @param json as string
    * @return yaml as string 
    * @throws Exception (generic exception)
	*/
	public static String convertJsonToYaml(String json) throws Exception {

		JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
		yaml = new YAMLMapper().writeValueAsString(jsonNodeTree);

		return yaml;
	}

	/**
	* @param json as string
    * @return csv as string 
    * @throws Exception (generic exception)
	*/
	public static String convertJsonToCsv(String json) throws Exception{

		xml = convertJsonToXml(json);
		csv = convertXmlToCsv(xml);

		return csv;
	}

	/**
	* @param csv as string
    * @return xml as string 
    * @throws Exception (generic exception)
	*/
	public static String convertCsvToXml(String csv) throws Exception {

		CsvParserSettings settings = new CsvParserSettings();
		settings.detectFormatAutomatically();
		settings.setEmptyValue("");
		settings.setNullValue("");
		CsvParser parser = new CsvParser(settings);
		
		InputStream input = convertStringToStream(csv);
		
		List<String[]> rows = parser.parseAll(input);

		XStream xstream = new XStream();
		xstream.alias("rows", List.class);
		xstream.alias("row", String[].class);
		xstream.alias("item", String.class);
	
		xml = xstream.toXML(rows);

		input.close();

		return xml;
	}

	/**
	* @param csv as string
    * @return json as string 
    * @throws Exception (generic exception)
	*/
	public static String convertCsvToJson(String csv) throws Exception{

        xml = convertCsvToXml(csv);
        json = convertXmlToJson(xml);
        	    
		return json;
	}

	/**
	* @param csv as string
    * @return yaml as string 
    * @throws Exception (generic exception)
	*/
	public static String convertCsvToYaml(String csv) throws Exception{

        xml = convertCsvToXml(csv);
        json = convertXmlToYaml(xml);
        	    
		return json;
	}

	/**
	* @param yaml as string
    * @return xml as string 
    * @throws Exception (generic exception)
	*/
	public static String convertYamlToXml(String yaml) throws Exception {

		json = convertYamlToJson(yaml);
		xml = convertJsonToXml(json);

		return xml;
	}

	/**
	* @param yaml as string
    * @return json as string 
    * @throws Exception (generic exception)
	*/	
	public static String convertYamlToJson(String yaml) throws Exception {

		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = yamlReader.readValue(yaml, Object.class);

		ObjectMapper jsonWriter = new ObjectMapper();
		json = jsonWriter.writeValueAsString(obj);

		return json;
	}	

	/**
	* @param yaml as string
    * @return csv as string 
    * @throws Exception (generic exception)
	*/
	public static String convertYamlToCsv(String yaml) throws Exception {

		xml = convertYamlToXml(yaml);
		csv = convertXmlToCsv(xml);

		return csv;
	}

	private static boolean isXML(String xml) {
		try {
			SAXParserFactory.newInstance().newSAXParser().getXMLReader().parse(new InputSource(new StringReader(xml)));
			return true;
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			return false;
		}
	}

}
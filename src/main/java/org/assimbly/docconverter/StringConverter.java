package org.assimbly.docconverter;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import tools.jackson.databind.json.JsonMapper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Static utility for converting between strings and common Java types.
 * This class is not instantiable.
 */
public final class StringConverter {

	private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

	private static final int INITIAL_BUFFER_SIZE = 8192;

	private StringConverter() {}

	// ─────────────────────────────────────────────
	// Stream / String
	// ─────────────────────────────────────────────

	/**
	 * Reads an InputStream into a UTF-8 String. The caller remains responsible for closing the stream.
	 * @param inputStream InputStream
	 * @return String
	 */
	public static String streamToString(InputStream inputStream) {
		try {
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw wrap("Failed to convert InputStream to String", e);
		}
	}

	/**
	 * Converts a String to a Stream
	 * @param string String
	 * @return InputStream
	 */
	public static InputStream stringToStream(String string) {
		return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
	}

	// ─────────────────────────────────────────────
	// Document / String
	// ─────────────────────────────────────────────

	/**
	 * Converts an org.w3c.dom.Document (XML) to a String
	 * @param document org.w3c.dom.Document XML document
	 * @return String
	 * @throws DocConversionException if the document cannot be serialized
	 */
	public static String docToString(Document document) {
		try {
			TransformerFactory factory = TransformerFactory.newDefaultInstance();
			Transformer transformer = factory.newTransformer();
			StringWriter stringWriter = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
			return stringWriter.toString();
		} catch (Exception e) {
			throw wrap("Failed to convert Document to String", e);
		}
	}

	/**
	 * Converts a String to an org.w3c.dom.Document (XML)
	 * @param xmlString String
	 * @return Document org.w3c.dom.Document XML document
	 * @throws DocConversionException if the string is not well-formed XML
	 */
	public static Document stringToDoc(String xmlString) {
		try {
			return newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
		} catch (Exception e) {
			throw wrap("Failed to convert String to Document", e);
		}
	}

	// ─────────────────────────────────────────────
	// Node / String
	// ─────────────────────────────────────────────

	/**
	 * Converts an org.w3c.dom.Node to a String
	 * @param node Node org.w3c.dom.Node
	 * @return String
	 */
	public static String nodeToString(Node node) {
		try {
			DOMImplementationLS domImplementation = (DOMImplementationLS)
					node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
			LSSerializer serializer = domImplementation.createLSSerializer();
			DOMConfiguration config = serializer.getDomConfig();
			config.setParameter("xml-declaration", Boolean.FALSE);
			return serializer.writeToString(node);
		} catch (Exception e) {
			throw wrap("Failed to convert Node to String", e);
		}
	}

	/**
	 * Converts a String to an org.w3c.dom.Node
	 * @param string XML node as string, e.g. {@code <node>value</node>}
	 * @return Node org.w3c.dom.Node
	 * @throws DocConversionException if the string is not well-formed XML
	 */
	public static Node stringToNode(String string) {
		try {
			return newDocumentBuilder().parse(new InputSource(new StringReader(string))).getDocumentElement();
		} catch (Exception e) {
			throw wrap("Failed to convert String to Node", e);
		}
	}

	// ─────────────────────────────────────────────
	// URL / URI / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a URL to a String
	 * @param url java.net.URL
	 * @return String
	 * @throws DocConversionException if the URL cannot be read
	 */
	public static String urlToString(URL url) {
		try (InputStream stream = url.openStream()) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw wrap("Failed to convert URL to String", e);
		}
	}

	/**
	 * Converts a URI to a String
	 * @param uri java.net.URI
	 * @return String
	 * @throws DocConversionException if the URI cannot be read
	 */
	public static String uriToString(URI uri) {
		try {
			return urlToString(uri.toURL());
		} catch (Exception e) {
			throw wrap("Failed to convert URI to String", e);
		}
	}

	/**
	 * Converts a URI to an org.w3c.dom.Document (XML)
	 * @param uri java.net.URI
	 * @return Document org.w3c.dom.Document XML document
	 * @throws DocConversionException if the URI cannot be read or is not well-formed XML
	 */
	public static Document uriToDoc(URI uri) {
		try {
			URL url = uri.toURL();
			try (InputStream stream = url.openStream()) {
				return newDocumentBuilder().parse(stream);
			}
		} catch (Exception e) {
			throw wrap("Failed to convert URI to Document", e);
		}
	}

	// ─────────────────────────────────────────────
	// File / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a file to String (UTF-8)
	 * @param path path as String
	 * @return String
	 * @throws DocConversionException if the file cannot be read
	 */
	public static String fileToString(String path) {
		try {
			return Files.readString(Paths.get(path));
		} catch (Exception e) {
			throw wrap("Failed to convert file to String", e);
		}
	}

	/**
	 * Converts a File to String with explicit encoding
	 * @param path path as String
	 * @param encoding Charset (defaults to UTF-8 when null)
	 * @return String
	 * @throws DocConversionException if the file cannot be read
	 */
	public static String fileToString(String path, Charset encoding) {
		try {
			Charset charset = encoding != null ? encoding : StandardCharsets.UTF_8;
			return Files.readString(Paths.get(path), charset);
		} catch (Exception e) {
			throw wrap("Failed to convert file to String", e);
		}
	}

	/**
	 * Converts a String to a File
	 * @param path path as String
	 * @param content content as String
	 * @throws DocConversionException if the file cannot be written
	 */
	public static void stringToFile(String path, String content) {
		try {
			Files.writeString(Paths.get(path), content,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
		} catch (Exception e) {
			throw wrap("Failed to convert String to file", e);
		}
	}

	/**
	 * Converts a File to a URI
	 * @param file File object
	 * @return URI
	 */
	public static URI fileToUri(File file) {
		return file.toURI();
	}

	/**
	 * Converts a File to a URL
	 * @param file File object
	 * @return URL
	 * @throws DocConversionException if the file cannot be converted to a URL
	 */
	public static URL fileToUrl(File file) {
		try {
			return file.toURI().toURL();
		} catch (Exception e) {
			throw wrap("Failed to convert File to URL", e);
		}
	}

	// ─────────────────────────────────────────────
	// Source / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a String to a Source
	 * @param string String object
	 * @return Source
	 */
	public static Source stringToSource(String string) {
		return new StreamSource(new StringReader(string));
	}

	// ─────────────────────────────────────────────
	// List / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a List to a comma-separated String
	 * @param list List of strings
	 * @return String
	 */
	public static String listToString(List<String> list) {
		return String.join(",", list);
	}

	/**
	 * Converts a comma-separated String to a List
	 * @param commaSeparatedString comma-separated String
	 * @return List
	 */
	public static List<String> stringToList(String commaSeparatedString) {
		return new ArrayList<>(Arrays.asList(commaSeparatedString.split(",")));
	}

	// ─────────────────────────────────────────────
	// Reader / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a String to a Reader
	 * @param string String object
	 * @return Reader
	 */
	public static Reader stringToReader(String string) {
		return new StringReader(string);
	}

	/**
	 * Converts a Reader to a String
	 * @param reader java.io.Reader
	 * @return String
	 * @throws DocConversionException if the reader cannot be read
	 */
	public static String readerToString(Reader reader) {
		try {
			StringWriter writer = new StringWriter(INITIAL_BUFFER_SIZE);
			reader.transferTo(writer);
			return writer.toString();
		} catch (Exception e) {
			throw wrap("Failed to convert Reader to String", e);
		}
	}

	// ─────────────────────────────────────────────
	// Object conversions
	// ─────────────────────────────────────────────

	/**
	 * Converts an Object to a String
	 * @param object Generic Object
	 * @return String
	 */
	public static String objectToString(Object object) {
		return String.valueOf(object);
	}

	/**
	 * Converts an Object to a JSON String
	 * @param object Generic Object
	 * @return String
	 */
	public static String objectToJsonString(Object object) {
		try {
			return JSON_MAPPER.writeValueAsString(object);
		} catch (Exception e) {
			throw wrap("Failed to convert Object to JSON String", e);
		}
	}

	private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return createSecureDocumentBuilderFactory().newDocumentBuilder();
	}

	private static DocumentBuilderFactory createSecureDocumentBuilderFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException e) {
			factory.setValidating(false);
			factory.setNamespaceAware(true);
		}
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);
		return factory;
	}

	private static DocConversionException wrap(String message, Exception e) {
		if (e instanceof DocConversionException conversionException) {
			return conversionException;
		}
		return new DocConversionException(message, e);
	}

}

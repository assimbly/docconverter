package org.assimbly.docconverter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assimbly.docconverter.StringConverter.docToString;
import static org.assimbly.docconverter.StringConverter.fileToString;
import static org.assimbly.docconverter.StringConverter.fileToUri;
import static org.assimbly.docconverter.StringConverter.fileToUrl;
import static org.assimbly.docconverter.StringConverter.listToString;
import static org.assimbly.docconverter.StringConverter.nodeToString;
import static org.assimbly.docconverter.StringConverter.objectToJsonString;
import static org.assimbly.docconverter.StringConverter.objectToString;
import static org.assimbly.docconverter.StringConverter.readerToString;
import static org.assimbly.docconverter.StringConverter.streamToString;
import static org.assimbly.docconverter.StringConverter.stringToDoc;
import static org.assimbly.docconverter.StringConverter.stringToFile;
import static org.assimbly.docconverter.StringConverter.stringToList;
import static org.assimbly.docconverter.StringConverter.stringToNode;
import static org.assimbly.docconverter.StringConverter.stringToReader;
import static org.assimbly.docconverter.StringConverter.stringToSource;
import static org.assimbly.docconverter.StringConverter.stringToStream;

class StringConverterTest {

	private static final String ROOT_XML = "<root><child>value</child></root>";
	private static final String NODE_XML = "<child>value</child>";

	@Test
	void streamToStringReturnsOriginalContent() {
		InputStream stream = new ByteArrayInputStream("hello world".getBytes(StandardCharsets.UTF_8));
		assertThat(streamToString(stream)).isEqualTo("hello world");
	}

	@Test
	void stringToStreamReturnsReadableStream() throws Exception {
		InputStream stream = stringToStream("hello world");
		assertThat(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("hello world");
	}

	@Test
	void docToStringContainsRootElement() {
		Document doc = stringToDoc(ROOT_XML);
		assertThat(docToString(doc)).contains("<root>", "<child>value</child>");
	}

	@Test
	void stringToDocParsesRootElement() {
		Document doc = stringToDoc(ROOT_XML);
		assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
	}

	@Test
	void stringToDocThrowsOnMalformedXml() {
		assertThatThrownBy(() -> stringToDoc("<unclosed>"))
				.isInstanceOf(DocConversionException.class)
				.hasMessage("Failed to convert String to Document")
				.hasCauseInstanceOf(Exception.class);
	}

	@Test
	void nodeToStringReturnsNodeMarkup() {
		Document doc = stringToDoc(ROOT_XML);
		Node child = doc.getDocumentElement().getFirstChild();
		assertThat(nodeToString(child)).contains("child", "value");
	}

	@Test
	void stringToNodeParsesTagName() {
		Node node = stringToNode(NODE_XML);
		assertThat(node.getNodeName()).isEqualTo("child");
	}

	@Test
	void fileToStringDefaultEncodingReturnsContent(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("test.txt");
		java.nio.file.Files.writeString(file, "file content");
		assertThat(fileToString(file.toString())).isEqualTo("file content");
	}

	@Test
	void fileToStringExplicitEncodingReturnsContent(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("test.txt");
		java.nio.file.Files.writeString(file, "file content");
		assertThat(fileToString(file.toString(), StandardCharsets.UTF_8)).isEqualTo("file content");
	}

	@Test
	void stringToFileWritesReadableFile(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("output.txt");
		stringToFile(file.toString(), "written content");
		assertThat(java.nio.file.Files.readString(file)).isEqualTo("written content");
	}

	@Test
	void stringToFileOverwritesExistingContent(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("output.txt");
		stringToFile(file.toString(), "original content");
		stringToFile(file.toString(), "new");
		assertThat(java.nio.file.Files.readString(file)).isEqualTo("new");
	}

	@Test
	void stringToSourceReturnsNonNullSource() {
		assertThat(stringToSource("<root/>")).isNotNull();
	}

	@Test
	void listToStringJoinsWithComma() {
		assertThat(listToString(List.of("a", "b", "c"))).isEqualTo("a,b,c");
	}

	@Test
	void stringToListSplitsOnComma() {
		assertThat(stringToList("a,b,c")).containsExactly("a", "b", "c");
	}

	@Test
	void stringToReaderReturnsReadableContent() throws Exception {
		Reader reader = stringToReader("hello");
		assertThat(new BufferedReader(reader).readLine()).isEqualTo("hello");
	}

	@Test
	void readerToStringReturnsOriginalContent() {
		assertThat(readerToString(new StringReader("hello"))).isEqualTo("hello");
	}

	@Test
	void objectToStringReturnsStringRepresentation() {
		assertThat(objectToString(42)).isEqualTo("42");
	}

	@Test
	void objectToJsonStringSerializesFields() {
		record Person(String name, int age) {}
		assertThat(objectToJsonString(new Person("Alice", 30)))
				.contains("\"name\"", "Alice", "\"age\"", "30");
	}

	@Test
	void fileToUriReturnsFileSchemeURI(@TempDir Path tempDir) throws Exception {
		File file = tempDir.resolve("test.txt").toFile();
		boolean created = file.createNewFile();
		assertThat(created).isTrue();
		URI uri = fileToUri(file);
		assertThat(uri.getScheme()).isEqualTo("file");
	}

	@Test
	void fileToUrlReturnsFileSchemeURL(@TempDir Path tempDir) throws Exception {
		File file = tempDir.resolve("test.txt").toFile();
		boolean created = file.createNewFile();
		assertThat(created).isTrue();
		URL url = fileToUrl(file);
		assertThat(url.getProtocol()).isEqualTo("file");
	}
}

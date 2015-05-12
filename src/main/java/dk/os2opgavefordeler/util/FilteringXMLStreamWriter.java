package dk.os2opgavefordeler.util;

import com.google.common.collect.ImmutableSet;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;
import java.util.Set;
import java.util.Stack;

/**
 * Filtering XMLStreamWriter which supports:
 * - trimming namespace definitions
 * - trimming whitespace in writeCharacters()
 * - ignoring certain XML elements
 *
 */
public class FilteringXMLStreamWriter
	extends DelegatingXMLStreamWriter
	implements AutoCloseable
{
	private final boolean trimNamespace;
	private final boolean trimWhitespace;
	private final Set<String> ignoredElements;

	// Used for tracking write{Start,End}Element when doing ignored-element filtering.
	private final Stack<String> elemStack = new Stack<>();

	public FilteringXMLStreamWriter(XMLStreamWriter delegate, boolean trimNamespace, boolean trimWhitespace, String... ignoredElements) {
		super(delegate);

		this.trimNamespace = trimNamespace;
		this.trimWhitespace = trimWhitespace;
		this.ignoredElements = ImmutableSet.copyOf(ignoredElements);
	}

	/**
	 * Creates a FilteringXMLStreamWriter which outputs to the supplied writer.
	 * @param writer output @Writer
	 * @param trimNamespace remove namespace definitions?
	 * @param trimWhitespace remove whitespaces from element text?
	 * @param ignoredElements XML elements to suppress in output
	 * @return
	 * @throws XMLStreamException
	 */
	public static FilteringXMLStreamWriter wrap(Writer writer, boolean trimNamespace, boolean trimWhitespace, String... ignoredElements) throws XMLStreamException {
		// A note about implementation: there's no guarantees that a JAXB implementation won't wrap our Writer, so we
		// can't do anything creative that way (e.g. filtering out tags with a custom SuppressingWriter instead of
		// avoiding writeStart/EndElement calls and requiring the ignoredElements stack).
		final XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
		return new FilteringXMLStreamWriter(xmlWriter, trimNamespace, trimWhitespace, ignoredElements);
	}

	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		if(!trimNamespace) {
			super.writeDefaultNamespace(namespaceURI);
		}
	}

	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		if(!trimNamespace) {
			super.writeNamespace(prefix, namespaceURI);
		}
	}

	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		elemStack.push(localName);
		if(!ignoredElements.contains(localName)) {
			super.writeStartElement(localName);
		}
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		elemStack.push(localName);
		if(!ignoredElements.contains(localName)) {
			super.writeStartElement(namespaceURI, localName);
		}
	}

	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		elemStack.push(localName);
		if(!ignoredElements.contains(localName)) {
			super.writeStartElement(prefix, localName, namespaceURI);
		}
	}

	@Override
	public void writeEndElement() throws XMLStreamException {
		// writeStartElement(...) and writeEndElement() calls must be balanced, have to do that with a stack since
		// there aren't any arguments to writeEndElement() we can use.
		final String element = elemStack.pop();
		if(!ignoredElements.contains(element)) {
			super.writeEndElement();
		}
	}

	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		super.writeCharacters(trimWhitespace ? text.trim() : text);
	}

	@Override
	public void close() throws XMLStreamException {
		super.close();
	}
}

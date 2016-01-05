package dk.os2opgavefordeler.rest;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class KleXmlUploadData {
	@FormParam("xsd")
	@PartType(MediaType.APPLICATION_XML)
	private InputStream xsd;

	@FormParam("xml")
	@PartType(MediaType.APPLICATION_XML)
	private InputStream xml;

	public InputStream getXsd() {
		return xsd;
	}

	public InputStream getXml() {
		return xml;
	}
}

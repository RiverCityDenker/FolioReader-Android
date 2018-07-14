package com.folioreader.ui.custom;

import org.readium.r2_streamer.model.container.Container;
import org.readium.r2_streamer.model.publication.EpubPublication;
import org.readium.r2_streamer.parser.CBZParser;
import org.readium.r2_streamer.parser.EncryptionParser;
import org.readium.r2_streamer.parser.EpubParserException;
import org.readium.r2_streamer.parser.MediaOverlayParser;
import org.readium.r2_streamer.parser.OPFParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EpubParser {
    private final String TAG = "EpubParser";

    private Container container;        //can be either EpubContainer or DirectoryContainer
    private EpubPublicationCustom publication;

    public EpubParser(Container container) {
        this.container = container;
        this.publication = new EpubPublicationCustom();
    }

    public EpubPublicationCustom parseEpubFile(String filePath) {
        String rootFile;
        try {
            if (filePath.contains(".cbz")) {
                CBZParserCustom.parseCBZ(container, publication);
                return publication;
            }
            rootFile = "/content.opf";

            publication.internalData.put("type", "epub");
            publication.internalData.put("rootfile", rootFile);
            //Parse OPF file
            this.publication = OPFParserCustom.parseOpfFile(rootFile, this.publication, container);
            // Parse Encryption
            this.publication.encryptions = EncryptionParser.parseEncryption(container);
            // Parse Media Overlay
            MediaOverlayParserCustom.parseMediaOverlay(this.publication, container);
            return publication;
        } catch (EpubParserException e) {
            System.out.println(TAG + " parserEpubFile() error " + e.toString());
        }
        return null;
    }

    private boolean isMimeTypeValid() throws EpubParserException {
        String mimeTypeData = container.rawData("mimetype");

        if (mimeTypeData.equals("application/epub+zip")) {
            return true;
        } else {
            System.out.println(TAG + "Invalid MIME type: " + mimeTypeData);
            throw new EpubParserException("Invalid MIME type");
        }
    }

    private String parseContainer() throws EpubParserException {
        String containerPath = "META-INF/container.xml";
        String containerData = container.rawData(containerPath);

        if (containerData == null) {
            System.out.println(TAG + " File is missing: " + containerPath);
            throw new EpubParserException("File is missing");
        }

        String opfFile = containerXmlParser(containerData);
        if (opfFile == null) {
            throw new EpubParserException("Error while parsing");
        }
        return opfFile;
    }

    //@Nullable
    private String containerXmlParser(String containerData) throws EpubParserException {           //parsing container.xml
        try {
            String xml = containerData.replaceAll("[^\\x20-\\x7e]", "").trim();         //in case encoding problem

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));
            document.getDocumentElement().normalize();
            if (document == null) {
                throw new EpubParserException("Error while parsing container.xml");
            }

            Element rootElement = (Element) ((Element) document.getDocumentElement().getElementsByTagName("rootfiles").item(0)).getElementsByTagName("rootfile").item(0);
            if (rootElement != null) {
                String opfFile = rootElement.getAttribute("full-path");
                if (opfFile == null) {
                    throw new EpubParserException("Missing root file element in container.xml");
                }

                return opfFile;                    //returns opf file
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    //@Nullable
    public static Document xmlParser(String xmlData) throws EpubParserException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlData)));
            document.getDocumentElement().normalize();
            if (document == null) {
                throw new EpubParserException("Error while parsing xml file");
            }

            return document;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

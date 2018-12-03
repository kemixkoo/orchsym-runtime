package org.apache.nifi.processors.edireader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Marks;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.edireader.json.JSONAdapter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.berryworks.edireader.DefaultXMLTags;
import com.berryworks.edireader.EDIReader;
import com.berryworks.edireader.EDIReaderFactory;
import com.berryworks.edireader.XMLTags;

@Marks(categories={"转换控制/数据转换"}, createdDate="2018-11-14")
@Tags({"EDI", "X12", "XML"})
@CapabilityDescription("Transform EDI X12 into XML.")
public class EdiToJSON extends AbstractProcessor {

    private List<PropertyDescriptor> properties;
    private Set<Relationship> relationships;

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("success")
            .build();

    private static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("failure")
            .build();

    public static final PropertyDescriptor PRETTY_PRINT = new PropertyDescriptor.Builder()
            .name("pretty-print")
            .displayName("Pretty print results")
            .description("Whether to get query result data in pretty-printed json or an optimized unformatted single line")
            .required(true)
            .allowableValues("true", "false")
            .defaultValue("false")
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .build();

    @Override
    public void init(final ProcessorInitializationContext context) {

        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(PRETTY_PRINT);
        this.properties = properties;

        Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return this.properties;
    }

    @Override
    public void onTrigger(ProcessContext processContext, ProcessSession session) throws ProcessException {
        final FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        boolean prettyPrint = processContext.getProperty(PRETTY_PRINT).asBoolean();
        final FlowFile flowFileClone = session.clone(flowFile);
        session.read(flowFile, inputStream -> session.write(flowFileClone, outputStream -> {
            InputSource inputSource = new InputSource(inputStream);

            XMLTags xmlTags = new DefaultXMLTags();
            ContentHandler handler = new JSONAdapter(xmlTags);
            EDIReader ediReader;
            char[] leftOver = null;

            try {
                while ((ediReader = EDIReaderFactory.createEDIReader(inputSource, leftOver)) != null) {
                    ediReader.setContentHandler(handler);
                    ediReader.parse(inputSource);
                    leftOver = ediReader.getTokenizer().getBuffered();
                }
                if (prettyPrint) {
                    outputStream.write(((JSONAdapter)handler).toPrettyJsonBytes());
                } else {
                    outputStream.write(((JSONAdapter) handler).toJsonBytes());
                }
            } catch (SAXException e) {
                getLogger().error("SAXException: " + e.getMessage());
                session.transfer(flowFile, REL_FAILURE);
            }
        }));
        session.transfer(flowFileClone, REL_SUCCESS);
        session.remove(flowFile);
        session.commit();
    }


}
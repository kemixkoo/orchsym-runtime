package org.apache.nifi.processors.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.DynamicProperty;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.SideEffectFree;
import org.apache.nifi.annotation.behavior.SupportsBatching;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.avro.AvroTypeUtil;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.mapper.exp.MapperExpField;
import org.apache.nifi.processors.mapper.exp.MapperTable;
import org.apache.nifi.schema.access.SchemaNotFoundException;
import org.apache.nifi.serialization.MalformedRecordException;
import org.apache.nifi.serialization.RecordReader;
import org.apache.nifi.serialization.RecordReaderFactory;
import org.apache.nifi.serialization.RecordSetWriter;
import org.apache.nifi.serialization.RecordSetWriterFactory;
import org.apache.nifi.serialization.WriteResult;
import org.apache.nifi.serialization.record.ListRecordSet;
import org.apache.nifi.serialization.record.MapRecord;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.RecordSchema;
import org.apache.nifi.serialization.record.RecordSet;

import com.fasterxml.jackson.databind.JsonMappingException;

@SideEffectFree
@SupportsBatching
@Tags({ "map", "avro", "json", "xml", "flow" })
@InputRequirement(Requirement.INPUT_REQUIRED)
@CapabilityDescription("Enable to map the flows to another one.")
@DynamicProperty(name = "The name of a flow for output.", value = "the value is json format, which contained the settings for expression fields, avro schema, writer controller, etc.", expressionLanguageScope = ExpressionLanguageScope.FLOWFILE_ATTRIBUTES, description = "enable to customize multi-output flows and do mapping with expression for each fields.")
public class RecordMapper extends AbstractProcessor {
    static final String DEFAULT_MAIN = "main";
    static final String PRE_INPUT = "input.";
    static final String PRE_OUTPUT = "output.";

    static final PropertyDescriptor RECORD_READER = new PropertyDescriptor.Builder().name("record-reader").displayName("Record Reader")
            .description("Specifies the Controller Service to use for reading incoming data").identifiesControllerService(RecordReaderFactory.class).required(true).build();
    static final PropertyDescriptor RECORD_WRITER = new PropertyDescriptor.Builder().name("record-writer").displayName("Record Writer")
            .description("Specifies the Controller Service to use for writing out the records").identifiesControllerService(RecordSetWriterFactory.class).required(true).build();

    static final Relationship REL_ORIGINAL = new Relationship.Builder().name("original")
            .description("The original FlowFile that was mapped. If the FlowFile fails processing, nothing will be sent to " + "this relationship").build();

    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure").description("A FlowFile is routed to this relationship when do map with some reasons").build();

    private List<PropertyDescriptor> descriptors;
    private Map<String, MapperTable> outputTables;
    private Map<String, Relationship> relationships;

    /**
     * Cache of dynamic collections set during {@link #onScheduled(ProcessContext)} for quick access in {@link #onTrigger(ProcessContext, ProcessSession)}
     */
    private volatile Map<String, Map<String, String>> outputFlowAttributesMap;
    private volatile Map<String, RecordSchema> outputRecordSchemasMap;
    private volatile Map<String, AtomicLong> writeCountsMap;

    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);

        // properties
        final List<PropertyDescriptor> properties = new ArrayList<>();
        properties.add(RECORD_READER);
        properties.add(RECORD_WRITER);
        this.descriptors = Collections.unmodifiableList(properties);

        // output tables
        this.outputTables = Collections.unmodifiableMap(Collections.emptyMap());

        // relationships
        final Map<String, Relationship> outputRelationships = new HashMap<>();
        outputRelationships.put(REL_ORIGINAL.getName(), REL_ORIGINAL);
        outputRelationships.put(REL_FAILURE.getName(), REL_FAILURE);
        this.relationships = Collections.unmodifiableMap(outputRelationships);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @Override
    protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(final String propertyDescriptorName) {
        return new PropertyDescriptor.Builder().name(propertyDescriptorName).description("The name of output flow: '" + propertyDescriptorName + "', which will try to create new relationship")
                .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).expressionLanguageSupported(ExpressionLanguageScope.NONE).dynamic(true).build();
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships.values().stream().collect(Collectors.toSet());
    }

    @Override
    public void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) {
        super.onPropertyModified(descriptor, oldValue, newValue);

        final String descriptorName = descriptor.getName();

        final Map<String, MapperTable> newDynamicOutputTables = new HashMap<>(this.outputTables);
        final Map<String, Relationship> newDynamicRelationships = new HashMap<>(this.relationships);

        try {

            if (newValue == null) { // remove?
                if (descriptor.isDynamic()) {
                    if (descriptorName.startsWith(PRE_OUTPUT)) { // output table
                        String outputTableName = descriptorName.substring(PRE_OUTPUT.length());
                        newDynamicOutputTables.remove(outputTableName);
                        newDynamicRelationships.remove(outputTableName);
                    }
                }

            } else if (oldValue == null) { // new property
                //
            }

            // if change the output schema, need update the relationships
            if (descriptor.isDynamic() && StringUtils.isNotEmpty(newValue) && !newValue.equals(oldValue)) {
                if (descriptorName.startsWith(PRE_OUTPUT)) { // output table
                    final MapperTable mappingTable = new MapperTable.Parser().parseTable(newValue);
                    final Schema schema = mappingTable.getSchema();
                    if (schema == null) {
                        throw new IllegalArgumentException("The schema is missing for table " + descriptorName);
                    }
                    final String tableName = mappingTable.getName();
                    final String schemaName = schema.getName();

                    String outputTableName = descriptorName.substring(PRE_OUTPUT.length());
                    if (!outputTableName.equals(tableName)) {
                        throw new JsonMappingException(null, MessageFormat.format("The mapping table name {0} is different with property setting name {1}", tableName, outputTableName));
                    }
                    if (!tableName.equals(schemaName)) {
                        throw new JsonMappingException(null, MessageFormat.format("The mapping table name {0} is different with schema name {1}", tableName, schemaName));
                    }

                    newDynamicOutputTables.put(schemaName, mappingTable);

                    // the relationship is output.xxx
                    final Relationship relationship = new Relationship.Builder().name(descriptorName).description(MessageFormat.format("A FlowFile is mapped to this \"{0}\" relationship", schemaName))
                            .build();
                    newDynamicRelationships.put(schemaName, relationship);

                }
            }

        } catch (IOException e) {
            getLogger().error("Cannot create the map flow via new settings {} - {}", new Object[] { newValue, descriptorName, e });
        }

        //
        this.outputTables = Collections.unmodifiableMap(newDynamicOutputTables);
        this.relationships = Collections.unmodifiableMap(newDynamicRelationships);

    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        final Set<String> outputTablesKeyset = outputTables.keySet();
        final Collection<MapperTable> outputTablesValues = outputTables.values();

        outputFlowAttributesMap = outputTablesKeyset.stream().collect(Collectors.toMap(Function.identity(), n -> new HashMap<String, String>()));
        outputRecordSchemasMap = outputTablesValues.stream().collect(Collectors.toMap(MapperTable::getName, t -> AvroTypeUtil.createSchema(t.getSchema())));
        writeCountsMap = outputTablesKeyset.stream().collect(Collectors.toMap(Function.identity(), n -> new AtomicLong()));
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        final RecordReaderFactory readerFactory = context.getProperty(RECORD_READER).asControllerService(RecordReaderFactory.class);
        final RecordSetWriterFactory writerFactory = context.getProperty(RECORD_WRITER).asControllerService(RecordSetWriterFactory.class);

        final ComponentLog logger = getLogger();

        // init some maps
        final Set<String> outputTablesKeyset = outputTables.keySet();
        final Map<String, FlowFile> outputFlowsMap = outputTables.keySet().stream().collect(Collectors.toMap(Function.identity(), n -> session.create()));

        final FlowFile original = flowFile;
        final Map<String, String> originalAttributes = flowFile.getAttributes();
        // read
        InputStreamCallback readerCallback = new InputStreamCallback() {
            @Override
            public void process(final InputStream rawIn) throws IOException {
                try (final RecordReader reader = readerFactory.createRecordReader(originalAttributes, rawIn, getLogger())) {
                    final Map<String, List<Record>> outputRecordsMap = outputTablesKeyset.stream().collect(Collectors.toMap(Function.identity(), n -> new ArrayList<>()));
                    final Set<Entry<String, MapperTable>> entrySet = outputTables.entrySet();

                    // TODO: I think we should refactor this part to be more 'streaming' oriented, not to hold too many record in the memory
                    // but process one incoming record one at a time, let framework to handle batching
                    // read record
                    Record curRecord = null;
                    while ((curRecord = reader.nextRecord()) != null) {
                        for (final Map.Entry<String, MapperTable> entry : entrySet) {
                            final Record writeRecord = processRecord(context, entry.getValue(), outputRecordSchemasMap.get(entry.getKey()), original, outputFlowsMap.get(entry.getKey()), curRecord);
                            if (writeRecord != null) {
                                outputRecordsMap.get(entry.getKey()).add(writeRecord);
                            }
                        }
                    }

                    for (final Map.Entry<String, MapperTable> entry : entrySet) {
                        final List<Record> records = outputRecordsMap.get(entry.getKey());
                        if (records != null && !records.isEmpty()) {
                            RecordSet recordSet = new ListRecordSet(outputRecordSchemasMap.get(entry.getKey()), records);
                            writeRecordSet(context, session, original, recordSet, entry.getKey(), outputFlowsMap, writerFactory);
                            records.clear();
                        }
                    }
                } catch (final SchemaNotFoundException e) {
                    throw new ProcessException(e.getLocalizedMessage(), e);
                } catch (final MalformedRecordException e) {
                    throw new ProcessException("Could not parse incoming data", e);
                }
            }
        };

        try {
            session.read(flowFile, readerCallback);

            for (Map.Entry<String, MapperTable> entry : outputTables.entrySet()) {
                final String outputName = entry.getKey();

                FlowFile outputFlow = outputFlowsMap.get(outputName);
                final Relationship relationship = this.relationships.get(outputName);

                final Map<String, String> attributes = outputFlowAttributesMap.get(outputName);
                attributes.put("map.count", String.valueOf(writeCountsMap.get(outputName).get()));
                attributes.put("output.name", relationship.getName());

                outputFlow = session.putAllAttributes(outputFlow, attributes);
                outputFlow = session.putAllAttributes(outputFlow, originalAttributes);

                session.getProvenanceReporter().route(outputFlow, relationship);
                session.transfer(outputFlow, relationship);
            }

            // original
            session.getProvenanceReporter().route(original, REL_ORIGINAL);
            session.transfer(original, REL_ORIGINAL);
        } catch (final ProcessException e) {
            logger.error("Cannot do map {} - ", new Object[] { flowFile, e });
            session.transfer(flowFile, REL_FAILURE);
        }

    }

    void writeRecordSet(final ProcessContext context, final ProcessSession session, final FlowFile original, final RecordSet recordSet, final String outputName,
            final Map<String, FlowFile> outputFlowsMap, final RecordSetWriterFactory writerFactory) {
        FlowFile outputFlow = outputFlowsMap.get(outputName);

        final Map<String, String> attributes = new HashMap<>();
        outputFlow = session.write(outputFlow, new OutputStreamCallback() {
            @Override
            public void process(OutputStream rawOut) throws IOException {
                try {
                    try (final RecordSetWriter writer = writerFactory.createWriter(getLogger(), outputRecordSchemasMap.get(outputName), rawOut)) {
                        final WriteResult writeResult = writer.write(recordSet);

                        // attributes
                        final Map<String, String> attributes = outputFlowAttributesMap.get(outputName);
                        attributes.put(CoreAttributes.MIME_TYPE.key(), writer.getMimeType());
                        attributes.putAll(writeResult.getAttributes());

                        writeCountsMap.get(outputName).getAndAdd(writeResult.getRecordCount());
                    }
                } catch (SchemaNotFoundException e) {
                    throw new ProcessException(e);
                }
            }
        });

        outputFlow = session.putAllAttributes(outputFlow, attributes);
        outputFlowsMap.put(outputName, outputFlow);
    }

    Record processRecord(final ProcessContext context, final MapperTable outputTable, RecordSchema writeRecordSchema, final FlowFile inputFlowFile, final FlowFile outputFlowFile,
            final Record readerRecord) {
        // FIXME, only support fixed "main" input flow
        final String mainPrefix = PRE_INPUT + DEFAULT_MAIN;

        final Map<String, String> inputRecordValuesMap = readerRecord.getSchema().getFields().stream()
                .collect(Collectors.toMap(f -> (mainPrefix + '.' + f.getFieldName()), f -> readerRecord.getAsString(f.getFieldName())));

        // process for expressions
        final Record writeRecord = new MapRecord(writeRecordSchema, new HashMap<>());
        List<String> processedFields = new ArrayList<String>();
        for (MapperExpField f : outputTable.getExpressions()) {
            final String expression = f.getExp();
            if (StringUtils.isNotEmpty(expression)) {
                PropertyValue expPropValue = context.newPropertyValue(expression);
                expPropValue = expPropValue.evaluateAttributeExpressions(inputFlowFile, inputRecordValuesMap);
                String name = f.getPath();
                if (name.startsWith("/")) { // TODO, currently, don't support record path yet.
                    name = name.substring(1);
                }
                // TODO investigate if we need to (try) do type conversion here, if input is a csv, output is json and has non-string field
                // the output field is not automatically converted
                writeRecord.setValue(name, expPropValue);
                processedFields.add(name);
            }
        }

        // process left
        writeRecordSchema.getFields().stream().filter(f -> !processedFields.contains(f.getFieldName())).forEach(f -> {
            final String fieldName = f.getFieldName();
            final Object value = readerRecord.getValue(fieldName);
            writeRecord.setValue(fieldName, value);
        });

        // process filter
        final String filter = outputTable.getFilter();
        if (StringUtils.isNotEmpty(filter)) {
            final String outputPrefix = PRE_OUTPUT + outputTable.getName();
            // add all output data, must be mapped via expression
            final Map<String, String> recordValuesMap = new HashMap<>(inputRecordValuesMap);
            writeRecordSchema.getFields().forEach(f -> {
                final String value = writeRecord.getAsString(f.getFieldName());
                recordValuesMap.put(outputPrefix + '.' + f.getFieldName(), value);
                // support default key without prefix for output, means the key is field of current output table.
                recordValuesMap.put(f.getFieldName(), value);
            });

            PropertyValue filterPropValue = context.newPropertyValue(filter);
            filterPropValue = filterPropValue.evaluateAttributeExpressions(inputFlowFile, recordValuesMap);
            if (filterPropValue.isExpressionLanguagePresent()) { // still some vars not eval
                // TODO
            } else if (!filterPropValue.asBoolean()) {
                return null; // if filtered, will be null
            }
        }
        return writeRecord;
    }

}

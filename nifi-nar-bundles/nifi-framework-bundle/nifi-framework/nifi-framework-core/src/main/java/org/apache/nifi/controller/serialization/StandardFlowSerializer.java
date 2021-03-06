/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.controller.serialization;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.nifi.bundle.BundleCoordinate;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.connectable.ConnectableType;
import org.apache.nifi.connectable.Connection;
import org.apache.nifi.connectable.Funnel;
import org.apache.nifi.connectable.Port;
import org.apache.nifi.connectable.Position;
import org.apache.nifi.connectable.Size;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.controller.ReportingTaskNode;
import org.apache.nifi.controller.Template;
import org.apache.nifi.controller.label.Label;
import org.apache.nifi.controller.service.ControllerServiceNode;
import org.apache.nifi.controller.service.ControllerServiceState;
import org.apache.nifi.encrypt.StringEncryptor;
import org.apache.nifi.flowfile.FlowFilePrioritizer;
import org.apache.nifi.groups.ProcessGroup;
import org.apache.nifi.groups.RemoteProcessGroup;
import org.apache.nifi.persistence.TemplateSerializer;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.registry.VariableDescriptor;
import org.apache.nifi.registry.VariableRegistry;
import org.apache.nifi.registry.flow.FlowRegistry;
import org.apache.nifi.registry.flow.FlowRegistryClient;
import org.apache.nifi.registry.flow.VersionControlInformation;
import org.apache.nifi.remote.RemoteGroupPort;
import org.apache.nifi.remote.RootGroupPort;
import org.apache.nifi.util.CharacterFilterUtils;
import org.apache.nifi.util.ProcessUtil;
import org.apache.nifi.util.StringUtils;
import org.apache.nifi.web.api.dto.ProcessGroupDTO;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Serializes a Flow Controller as XML to an output stream.
 *
 * NOT THREAD-SAFE.
 */
public class StandardFlowSerializer implements FlowSerializer<Document> {

    private static final String MAX_ENCODING_VERSION = "1.3";

    private final StringEncryptor encryptor;

    public StandardFlowSerializer(final StringEncryptor encryptor) {
        this.encryptor = encryptor;
    }


    @SuppressWarnings("unchecked")
    @Override
    public Document transform(final FlowController controller, final ScheduledStateLookup scheduledStateLookup) throws FlowSerializationException {
        try {
            // create a new, empty document
            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);

            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();

            // populate document with controller state
            final Element rootNode = doc.createElement("flowController");
            rootNode.setAttribute("encoding-version", MAX_ENCODING_VERSION);
            doc.appendChild(rootNode);
            addTextElement(rootNode, "maxTimerDrivenThreadCount", controller.getMaxTimerDrivenThreadCount());
            addTextElement(rootNode, "maxEventDrivenThreadCount", controller.getMaxEventDrivenThreadCount());

            final Element registriesElement = doc.createElement("registries");
            rootNode.appendChild(registriesElement);

            addFlowRegistries(registriesElement, controller.getFlowRegistryClient());
            addProcessGroup(rootNode, controller.getGroup(controller.getRootGroupId()), "rootGroup", scheduledStateLookup);

            // Add root-level controller services
            final Element controllerServicesNode = doc.createElement("controllerServices");
            rootNode.appendChild(controllerServicesNode);
            for (final ControllerServiceNode serviceNode : (List<ControllerServiceNode>)ElementsSorter.COMP_SORTER.sort(controller.getRootControllerServices())) {
                addControllerService(controllerServicesNode, serviceNode);
            }

            final Element reportingTasksNode = doc.createElement("reportingTasks");
            rootNode.appendChild(reportingTasksNode);
            for (final ReportingTaskNode taskNode : ElementsSorter.sortTaskNode(controller.getAllReportingTasks())) {
                addReportingTask(reportingTasksNode, taskNode, encryptor);
            }

            return doc;
        } catch (final ParserConfigurationException | DOMException | TransformerFactoryConfigurationError | IllegalArgumentException e) {
            throw new FlowSerializationException(e);
        }
    }

    @Override
    public void serialize(final Document flowConfiguration, final OutputStream os) throws FlowSerializationException {
        try {
            final DOMSource domSource = new DOMSource(flowConfiguration);
            final StreamResult streamResult = new StreamResult(new BufferedOutputStream(os));

            // configure the transformer and convert the DOM
            final TransformerFactory transformFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformFactory.newTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // transform the document to byte stream
            transformer.transform(domSource, streamResult);

        } catch (final DOMException | TransformerFactoryConfigurationError | IllegalArgumentException | TransformerException e) {
            throw new FlowSerializationException(e);
        }
    }

    private void addFlowRegistries(final Element parentElement, final FlowRegistryClient registryClient) {
        for (final String registryId : registryClient.getRegistryIdentifiers()) {
            final FlowRegistry flowRegistry = registryClient.getFlowRegistry(registryId);

            final Element registryElement = parentElement.getOwnerDocument().createElement("flowRegistry");
            parentElement.appendChild(registryElement);

            addStringElement(registryElement, "id", flowRegistry.getIdentifier());
            addStringElement(registryElement, "name", flowRegistry.getName());
            addStringElement(registryElement, "url", flowRegistry.getURL());
            addStringElement(registryElement, "description", flowRegistry.getDescription());
        }
    }

    private void addStringElement(final Element parentElement, final String elementName, final String value) {
        final Element childElement = parentElement.getOwnerDocument().createElement(elementName);
        childElement.setTextContent(value);
        parentElement.appendChild(childElement);
    }

    private void addSize(final Element parentElement, final Size size) {
        final Element element = parentElement.getOwnerDocument().createElement("size");
        element.setAttribute("width", String.valueOf(size.getWidth()));
        element.setAttribute("height", String.valueOf(size.getHeight()));
        parentElement.appendChild(element);
    }

    private void addPosition(final Element parentElement, final Position position) {
        addPosition(parentElement, position, "position");
    }

    private void addPosition(final Element parentElement, final Position position, final String elementName) {
        final Element element = parentElement.getOwnerDocument().createElement(elementName);
        element.setAttribute("x", String.valueOf(position.getX()));
        element.setAttribute("y", String.valueOf(position.getY()));
        parentElement.appendChild(element);
    }

    @SuppressWarnings("unchecked")
    private void addProcessGroup(final Element parentElement, final ProcessGroup group, final String elementName, final ScheduledStateLookup scheduledStateLookup) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement(elementName);
        parentElement.appendChild(element);
        addTextElement(element, "id", group.getIdentifier());
        addTextElement(element, "versionedComponentId", group.getVersionedComponentId());
        addTextElement(element, "name", group.getName());
        addPosition(element, group.getPosition());
        addTextElement(element, "comment", group.getComments());

        final VersionControlInformation versionControlInfo = group.getVersionControlInformation();
        if (versionControlInfo != null) {
            final Element versionControlInfoElement = doc.createElement("versionControlInformation");
            addTextElement(versionControlInfoElement, "registryId", versionControlInfo.getRegistryIdentifier());
            addTextElement(versionControlInfoElement, "bucketId", versionControlInfo.getBucketIdentifier());
            addTextElement(versionControlInfoElement, "bucketName", versionControlInfo.getBucketName());
            addTextElement(versionControlInfoElement, "flowId", versionControlInfo.getFlowIdentifier());
            addTextElement(versionControlInfoElement, "flowName", versionControlInfo.getFlowName());
            addTextElement(versionControlInfoElement, "flowDescription", versionControlInfo.getFlowDescription());
            addTextElement(versionControlInfoElement, "version", versionControlInfo.getVersion());
            element.appendChild(versionControlInfoElement);
        }

        for (final ProcessorNode processor : ElementsSorter.sortNode(group.getProcessors())) {
            addProcessor(element, processor, scheduledStateLookup);
        }

        if (group.isRootGroup()) {
            for (final Port port : (List<Port>)ElementsSorter.COMP_SORTER.sort(group.getInputPorts())) {
                addRootGroupPort(element, (RootGroupPort) port, "inputPort", scheduledStateLookup);
            }

            for (final Port port : (List<Port>)ElementsSorter.COMP_SORTER.sort(group.getOutputPorts())) {
                addRootGroupPort(element, (RootGroupPort) port, "outputPort", scheduledStateLookup);
            }
        } else {
            for (final Port port : (List<Port>)ElementsSorter.COMP_SORTER.sort(group.getInputPorts())) {
                addPort(element, port, "inputPort", scheduledStateLookup);
            }

            for (final Port port : (List<Port>)ElementsSorter.COMP_SORTER.sort(group.getOutputPorts())) {
                addPort(element, port, "outputPort", scheduledStateLookup);
            }
        }

        for (final Label label : (List<Label>)ElementsSorter.COMP_SORTER.sort(group.getLabels())) {
            addLabel(element, label);
        }

        for (final Funnel funnel : (List<Funnel>)ElementsSorter.COMP_SORTER.sort(group.getFunnels())) {
            addFunnel(element, funnel);
        }

        for (final ProcessGroup childGroup : (List<ProcessGroup>)ElementsSorter.COMP_SORTER.sort(group.getProcessGroups())) {
            addProcessGroup(element, childGroup, "processGroup", scheduledStateLookup);
        }

        for (final RemoteProcessGroup remoteRef : (List<RemoteProcessGroup>)ElementsSorter.COMP_SORTER.sort(group.getRemoteProcessGroups())) {
            addRemoteProcessGroup(element, remoteRef, scheduledStateLookup);
        }

        for (final Connection connection : ElementsSorter.sortConnection(group.getConnections())) {
            addConnection(element, connection);
        }

        for (final ControllerServiceNode service : (List<ControllerServiceNode>)ElementsSorter.COMP_SORTER.sort(group.getControllerServices(false))) {
            addControllerService(element, service);
        }

        for (final Template template : (List<Template>)ElementsSorter.COMP_SORTER.sort(group.getTemplates())) {
            addTemplate(element, template);
        }

        final VariableRegistry variableRegistry = group.getVariableRegistry();
        for (final Map.Entry<VariableDescriptor, String> entry : ElementsSorter.VAR_SORTER.sort(variableRegistry.getVariableMap().entrySet())) {
            addVariable(element, entry.getKey().getName(), entry.getValue());
        }

        ProcessUtil.addTags(element, group.getTags());

        ProcessUtil.addAddtions(element, group.getAdditions().values());
    }

    private static void addVariable(final Element parentElement, final String variableName, final String variableValue) {
        final Element variableElement = parentElement.getOwnerDocument().createElement("variable");
        variableElement.setAttribute("name", variableName);
        variableElement.setAttribute("value", variableValue);
        parentElement.appendChild(variableElement);
    }

    private static void addBundle(final Element parentElement, final BundleCoordinate coordinate) {
        // group
        final Element groupElement = parentElement.getOwnerDocument().createElement("group");
        groupElement.setTextContent(coordinate.getGroup());

        // artifact
        final Element artifactElement = parentElement.getOwnerDocument().createElement("artifact");
        artifactElement.setTextContent(coordinate.getId());

        // version
        final Element versionElement = parentElement.getOwnerDocument().createElement("version");
        versionElement.setTextContent(coordinate.getVersion());

        // bundle
        final Element bundleElement = parentElement.getOwnerDocument().createElement("bundle");
        bundleElement.appendChild(groupElement);
        bundleElement.appendChild(artifactElement);
        bundleElement.appendChild(versionElement);

        parentElement.appendChild(bundleElement);
    }

    private void addStyle(final Element parentElement, final Map<String, String> style) {
        final Element element = parentElement.getOwnerDocument().createElement("styles");

        for (final Map.Entry<String, String> entry : ElementsSorter.STR_SORTER.sort(style.entrySet())) {
            final Element styleElement = parentElement.getOwnerDocument().createElement("style");
            styleElement.setAttribute("name", entry.getKey());
            styleElement.setTextContent(entry.getValue());
            element.appendChild(styleElement);
        }

        parentElement.appendChild(element);
    }

    private void addLabel(final Element parentElement, final Label label) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement("label");
        parentElement.appendChild(element);
        addTextElement(element, "id", label.getIdentifier());
        addTextElement(element, "versionedComponentId", label.getVersionedComponentId());

        addPosition(element, label.getPosition());
        addSize(element, label.getSize());
        addStyle(element, label.getStyle());

        addTextElement(element, "value", label.getValue());
        parentElement.appendChild(element);
    }

    private void addFunnel(final Element parentElement, final Funnel funnel) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement("funnel");
        parentElement.appendChild(element);
        addTextElement(element, "id", funnel.getIdentifier());
        addTextElement(element, "versionedComponentId", funnel.getVersionedComponentId());
        addPosition(element, funnel.getPosition());
    }

    @SuppressWarnings("unchecked")
    private void addRemoteProcessGroup(final Element parentElement, final RemoteProcessGroup remoteRef, final ScheduledStateLookup scheduledStateLookup) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement("remoteProcessGroup");
        parentElement.appendChild(element);
        addTextElement(element, "id", remoteRef.getIdentifier());
        addTextElement(element, "versionedComponentId", remoteRef.getVersionedComponentId());
        addTextElement(element, "name", remoteRef.getName());
        addPosition(element, remoteRef.getPosition());
        addTextElement(element, "comment", remoteRef.getComments());
        addTextElement(element, "url", remoteRef.getTargetUri());
        addTextElement(element, "urls", remoteRef.getTargetUris());
        addTextElement(element, "timeout", remoteRef.getCommunicationsTimeout());
        addTextElement(element, "yieldPeriod", remoteRef.getYieldDuration());
        addTextElement(element, "transmitting", String.valueOf(remoteRef.isTransmitting()));
        addTextElement(element, "transportProtocol", remoteRef.getTransportProtocol().name());
        addTextElement(element, "proxyHost", remoteRef.getProxyHost());
        if (remoteRef.getProxyPort() != null) {
            addTextElement(element, "proxyPort", remoteRef.getProxyPort());
        }
        addTextElement(element, "proxyUser", remoteRef.getProxyUser());
        if (!StringUtils.isEmpty(remoteRef.getProxyPassword())) {
            final String value = ENC_PREFIX + encryptor.encrypt(remoteRef.getProxyPassword()) + ENC_SUFFIX;
            addTextElement(element, "proxyPassword", value);
        }
        if (remoteRef.getNetworkInterface() != null) {
            addTextElement(element, "networkInterface", remoteRef.getNetworkInterface());
        }

        for (final RemoteGroupPort port : (List<RemoteGroupPort>)ElementsSorter.COMP_SORTER.sort(remoteRef.getInputPorts())) {
            if (port.hasIncomingConnection()) {
                addRemoteGroupPort(element, port, "inputPort", scheduledStateLookup);
            }
        }

        for (final RemoteGroupPort port : (List<RemoteGroupPort>)ElementsSorter.COMP_SORTER.sort(remoteRef.getOutputPorts())) {
            if (!port.getConnections().isEmpty()) {
                addRemoteGroupPort(element, port, "outputPort", scheduledStateLookup);
            }
        }

        parentElement.appendChild(element);
    }

    private void addRemoteGroupPort(final Element parentElement, final RemoteGroupPort port, final String elementName, final ScheduledStateLookup scheduledStateLookup) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement(elementName);
        parentElement.appendChild(element);
        addTextElement(element, "id", port.getIdentifier());
        addTextElement(element, "versionedComponentId", port.getVersionedComponentId());
        addTextElement(element, "name", port.getName());
        addPosition(element, port.getPosition());
        addTextElement(element, "comments", port.getComments());
        addTextElement(element, "scheduledState", scheduledStateLookup.getScheduledState(port).name());
        addTextElement(element, "targetId", port.getTargetIdentifier());
        addTextElement(element, "maxConcurrentTasks", port.getMaxConcurrentTasks());
        addTextElement(element, "useCompression", String.valueOf(port.isUseCompression()));
        final Integer batchCount = port.getBatchCount();
        if (batchCount != null && batchCount > 0) {
            addTextElement(element, "batchCount", batchCount);
        }
        final String batchSize = port.getBatchSize();
        if (batchSize != null && batchSize.length() > 0) {
            addTextElement(element, "batchSize", batchSize);
        }
        final String batchDuration = port.getBatchDuration();
        if (batchDuration != null && batchDuration.length() > 0) {
            addTextElement(element, "batchDuration", batchDuration);
        }

        parentElement.appendChild(element);
    }

    private void addPort(final Element parentElement, final Port port, final String elementName, final ScheduledStateLookup scheduledStateLookup) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement(elementName);
        parentElement.appendChild(element);
        addTextElement(element, "id", port.getIdentifier());
        addTextElement(element, "versionedComponentId", port.getVersionedComponentId());
        addTextElement(element, "name", port.getName());
        addPosition(element, port.getPosition());
        addTextElement(element, "comments", port.getComments());
        addTextElement(element, "scheduledState", scheduledStateLookup.getScheduledState(port).name());

        parentElement.appendChild(element);
    }

    private void addRootGroupPort(final Element parentElement, final RootGroupPort port, final String elementName, final ScheduledStateLookup scheduledStateLookup) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement(elementName);
        parentElement.appendChild(element);
        addTextElement(element, "id", port.getIdentifier());
        addTextElement(element, "versionedComponentId", port.getVersionedComponentId());
        addTextElement(element, "name", port.getName());
        addPosition(element, port.getPosition());
        addTextElement(element, "comments", port.getComments());
        addTextElement(element, "scheduledState", scheduledStateLookup.getScheduledState(port).name());
        addTextElement(element, "maxConcurrentTasks", String.valueOf(port.getMaxConcurrentTasks()));
        for (final String user : port.getUserAccessControl()) {
            addTextElement(element, "userAccessControl", user);
        }
        for (final String group : port.getGroupAccessControl()) {
            addTextElement(element, "groupAccessControl", group);
        }

        parentElement.appendChild(element);
    }

    private void addProcessor(final Element parentElement, final ProcessorNode processor, final ScheduledStateLookup scheduledStateLookup) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement("processor");
        parentElement.appendChild(element);
        addTextElement(element, "id", processor.getIdentifier());
        addTextElement(element, "versionedComponentId", processor.getVersionedComponentId());
        addTextElement(element, "name", processor.getName());

        addPosition(element, processor.getPosition());
        addStyle(element, processor.getStyle());

        addTextElement(element, "comment", processor.getComments());
        addTextElement(element, "class", processor.getCanonicalClassName());

        addBundle(element, processor.getBundleCoordinate());

        addTextElement(element, "maxConcurrentTasks", processor.getMaxConcurrentTasks());
        addTextElement(element, "schedulingPeriod", processor.getSchedulingPeriod());
        addTextElement(element, "penalizationPeriod", processor.getPenalizationPeriod());
        addTextElement(element, "yieldPeriod", processor.getYieldPeriod());
        addTextElement(element, "bulletinLevel", processor.getBulletinLevel().toString());
        addTextElement(element, "lossTolerant", String.valueOf(processor.isLossTolerant()));
        addTextElement(element, "scheduledState", scheduledStateLookup.getScheduledState(processor).name());
        addTextElement(element, "schedulingStrategy", processor.getSchedulingStrategy().name());
        addTextElement(element, "executionNode", processor.getExecutionNode().name());
        addTextElement(element, "runDurationNanos", processor.getRunDuration(TimeUnit.NANOSECONDS));

        addConfiguration(element, processor.getProperties(), processor.getAnnotationData(), encryptor);

        for (final Relationship rel : ElementsSorter.sortRelationship(processor.getAutoTerminatedRelationships())) {
            addTextElement(element, "autoTerminatedRelationship", rel.getName());
        }
    }

    private static void addConfiguration(final Element element, final Map<PropertyDescriptor, String> properties, final String annotationData, final StringEncryptor encryptor) {
        final Document doc = element.getOwnerDocument();
        for (final Map.Entry<PropertyDescriptor, String> entry : ElementsSorter.PROP_SORTER.sort(properties.entrySet())) {
            final PropertyDescriptor descriptor = entry.getKey();
            String value = entry.getValue();

            if (value != null && descriptor.isSensitive()) {
                value = ENC_PREFIX + encryptor.encrypt(value) + ENC_SUFFIX;
            }

            if (value == null) {
                value = descriptor.getDefaultValue();
            }

            final Element propElement = doc.createElement("property");
            addTextElement(propElement, "name", descriptor.getName());
            if (value != null) {
                addTextElement(propElement, "value", value);
            }

            element.appendChild(propElement);
        }

        if (annotationData != null) {
            addTextElement(element, "annotationData", annotationData);
        }
    }

    private void addConnection(final Element parentElement, final Connection connection) {
        final Document doc = parentElement.getOwnerDocument();
        final Element element = doc.createElement("connection");
        parentElement.appendChild(element);
        addTextElement(element, "id", connection.getIdentifier());
        addTextElement(element, "versionedComponentId", connection.getVersionedComponentId());
        addTextElement(element, "name", connection.getName());

        final Element bendPointsElement = doc.createElement("bendPoints");
        element.appendChild(bendPointsElement);
        for (final Position bendPoint : ElementsSorter.sortPositions(connection.getBendPoints())) {
            addPosition(bendPointsElement, bendPoint, "bendPoint");
        }

        addTextElement(element, "labelIndex", connection.getLabelIndex());
        addTextElement(element, "zIndex", connection.getZIndex());

        final String sourceId = connection.getSource().getIdentifier();
        final ConnectableType sourceType = connection.getSource().getConnectableType();
        final String sourceGroupId;
        if (sourceType == ConnectableType.REMOTE_OUTPUT_PORT) {
            sourceGroupId = ((RemoteGroupPort) connection.getSource()).getRemoteProcessGroup().getIdentifier();
        } else {
            sourceGroupId = connection.getSource().getProcessGroup().getIdentifier();
        }

        final ConnectableType destinationType = connection.getDestination().getConnectableType();
        final String destinationId = connection.getDestination().getIdentifier();
        final String destinationGroupId;
        if (destinationType == ConnectableType.REMOTE_INPUT_PORT) {
            destinationGroupId = ((RemoteGroupPort) connection.getDestination()).getRemoteProcessGroup().getIdentifier();
        } else {
            destinationGroupId = connection.getDestination().getProcessGroup().getIdentifier();
        }

        addTextElement(element, "sourceId", sourceId);
        addTextElement(element, "sourceGroupId", sourceGroupId);
        addTextElement(element, "sourceType", sourceType.toString());

        addTextElement(element, "destinationId", destinationId);
        addTextElement(element, "destinationGroupId", destinationGroupId);
        addTextElement(element, "destinationType", destinationType.toString());

        for (final Relationship relationship : ElementsSorter.sortRelationship(connection.getRelationships())) {
            addTextElement(element, "relationship", relationship.getName());
        }

        addTextElement(element, "maxWorkQueueSize", connection.getFlowFileQueue().getBackPressureObjectThreshold());
        addTextElement(element, "maxWorkQueueDataSize", connection.getFlowFileQueue().getBackPressureDataSizeThreshold());

        addTextElement(element, "flowFileExpiration", connection.getFlowFileQueue().getFlowFileExpiration());
        for (final FlowFilePrioritizer comparator : ElementsSorter.sortPriorities(connection.getFlowFileQueue().getPriorities())) {
            final String className = comparator.getClass().getCanonicalName();
            addTextElement(element, "queuePrioritizerClass", className);
        }

        parentElement.appendChild(element);
    }

    public void addControllerService(final Element element, final ControllerServiceNode serviceNode) {
        final Element serviceElement = element.getOwnerDocument().createElement("controllerService");
        addTextElement(serviceElement, "id", serviceNode.getIdentifier());
        addTextElement(serviceElement, "versionedComponentId", serviceNode.getVersionedComponentId());
        addTextElement(serviceElement, "name", serviceNode.getName());
        addTextElement(serviceElement, "comment", serviceNode.getComments());
        addTextElement(serviceElement, "class", serviceNode.getCanonicalClassName());

        addBundle(serviceElement, serviceNode.getBundleCoordinate());

        final ControllerServiceState state = serviceNode.getState();
        final boolean enabled = (state == ControllerServiceState.ENABLED || state == ControllerServiceState.ENABLING);
        addTextElement(serviceElement, "enabled", String.valueOf(enabled));

        addConfiguration(serviceElement, serviceNode.getProperties(), serviceNode.getAnnotationData(), encryptor);

        ProcessUtil.addAddtions(serviceElement, serviceNode.getAdditions().values());

        element.appendChild(serviceElement);
    }

    public static void addReportingTask(final Element element, final ReportingTaskNode taskNode, final StringEncryptor encryptor) {
        final Element taskElement = element.getOwnerDocument().createElement("reportingTask");
        addTextElement(taskElement, "id", taskNode.getIdentifier());
        addTextElement(taskElement, "name", taskNode.getName());
        addTextElement(taskElement, "comment", taskNode.getComments());
        addTextElement(taskElement, "class", taskNode.getCanonicalClassName());

        addBundle(taskElement, taskNode.getBundleCoordinate());

        addTextElement(taskElement, "schedulingPeriod", taskNode.getSchedulingPeriod());
        addTextElement(taskElement, "scheduledState", taskNode.getScheduledState().name());
        addTextElement(taskElement, "schedulingStrategy", taskNode.getSchedulingStrategy().name());

        addConfiguration(taskElement, taskNode.getProperties(), taskNode.getAnnotationData(), encryptor);

        element.appendChild(taskElement);
    }

    private static void addTextElement(final Element element, final String name, final long value) {
        addTextElement(element, name, String.valueOf(value));
    }

    private static void addTextElement(final Element element, final String name, final String value) {
        final Document doc = element.getOwnerDocument();
        final Element toAdd = doc.createElement(name);
        toAdd.setTextContent(CharacterFilterUtils.filterInvalidXmlCharacters(value)); // value should already be filtered, but just in case ensure there are no invalid xml characters
        element.appendChild(toAdd);
    }

    private static void addTextElement(final Element element, final String name, final Optional<String> value) {
        if (!value.isPresent()) {
            return;
        }

        final Document doc = element.getOwnerDocument();
        final Element toAdd = doc.createElement(name);
        toAdd.setTextContent(CharacterFilterUtils.filterInvalidXmlCharacters(value.get())); // value should already be filtered, but just in case ensure there are no invalid xml characters
        element.appendChild(toAdd);
    }

    public static void addTemplate(final Element element, final Template template) {
        try {
            Map<String,String> additionsMap = template.getDetails().getAdditions();
            final Set<String> tagsSet = template.getDetails().getTags();
            // 防止字节序列化时默认将Map序列化为Entry，需将additions置为null
            template.getDetails().setAdditions(null);
            template.getDetails().setTags(null);
            for(ProcessGroupDTO groupDTO  : template.getDetails().getSnippet().getProcessGroups()){
                // 序列化模板时，不需要序列化Snippet中GroupDTO的Additions相关信息
                groupDTO.setAdditions(null);
            }
            final byte[] serialized = TemplateSerializer.serialize(template.getDetails());

            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            final Document document;
            try (final InputStream in = new ByteArrayInputStream(serialized)) {
                document = docBuilder.parse(in);
            }

            final Node templateNode = element.getOwnerDocument().importNode(document.getDocumentElement(), true);

            // serialize additions of template
            if (additionsMap!= null && !additionsMap.isEmpty()){
                ProcessUtil.addAddtions((Element) templateNode, additionsMap);
            }

            if (tagsSet!= null && !tagsSet.isEmpty()){
                ProcessUtil.addTags((Element) templateNode, tagsSet);
            }

            element.appendChild(templateNode);
            // finally set additions and set to template
            template.getDetails().setAdditions(additionsMap);
            template.getDetails().setTags(tagsSet);
        } catch (final Exception e) {
            throw new FlowSerializationException(e);
        }
    }
}

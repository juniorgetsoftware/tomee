/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.jee;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.Map;

/**
 * The webservice-description element defines a WSDL document file
 * and the set of Port components associated with the WSDL ports
 * defined in the WSDL document.  There may be multiple
 * webservice-descriptions defined within a module.
 * <p/>
 * All WSDL file ports must have a corresponding port-component element
 * defined.
 * <p/>
 * Used in: webservices
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "webservice-descriptionType", propOrder = {
    "description",
    "displayName",
    "icon",
    "webserviceDescriptionName",
    "wsdlFile",
    "jaxrpcMappingFile",
    "portComponent"
})
public class WebserviceDescription implements Keyable<String> {
    protected String description;
    @XmlElement(name = "display-name")
    protected String displayName;
    protected Icon icon;
    @XmlElement(name = "webservice-description-name", required = true)
    protected String webserviceDescriptionName;
    @XmlElement(name = "wsdl-file")
    protected String wsdlFile;
    @XmlElement(name = "jaxrpc-mapping-file")
    protected String jaxrpcMappingFile;
    @XmlTransient
    protected JavaWsdlMapping jaxrpcMapping;
    @XmlElement(name = "port-component", required = true)
    protected KeyedCollection<String, PortComponent> portComponent;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;


    public String getKey() {
        return webserviceDescriptionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String value) {
        this.displayName = value;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon value) {
        this.icon = value;
    }

    public String getWebserviceDescriptionName() {
        return webserviceDescriptionName;
    }

    public void setWebserviceDescriptionName(String value) {
        this.webserviceDescriptionName = value;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String value) {
        this.wsdlFile = value;
    }

    public String getJaxrpcMappingFile() {
        return jaxrpcMappingFile;
    }

    public void setJaxrpcMappingFile(String value) {
        this.jaxrpcMappingFile = value;
    }

    public JavaWsdlMapping getJaxrpcMapping() {
        return jaxrpcMapping;
    }

    public void setJaxrpcMapping(JavaWsdlMapping jaxrpcMapping) {
        this.jaxrpcMapping = jaxrpcMapping;
    }

    public Collection<PortComponent> getPortComponent() {
        if (portComponent == null) {
            portComponent = new KeyedCollection<String,PortComponent>();
        }
        return this.portComponent;
    }

    public Map<String,PortComponent> getPortComponentMap() {
        if (portComponent == null) {
            portComponent = new KeyedCollection<String,PortComponent>();
        }
        return this.portComponent.toMap();
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}

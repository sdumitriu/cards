/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ca.sickkids.ccm.lfs.dataentry.internal.serialize;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.osgi.service.component.annotations.Component;

import ca.sickkids.ccm.lfs.serialize.spi.ResourceJsonProcessor;

/**
 * Gets the question answer for vocabulary questions.
 *
 * @version $Id$
 */
@Component(immediate = true)
public class VocabularyLabelProcessor extends SimpleAnswerLabelProcessor implements ResourceJsonProcessor
{
    @Override
    public String getName()
    {
        return "labels";
    }

    @Override
    public int getPriority()
    {
        return 75;
    }

    @Override
    public boolean isEnabledByDefault(Resource resource)
    {
        return true;
    }

    @Override
    public boolean canProcess(Resource resource)
    {
        return resource.isResourceType("lfs/Form");
    }

    @Override
    public JsonValue processChild(final Node node, final Node child, final JsonValue input,
        final Function<Node, JsonValue> serializeNode)
    {
        try {
            if (child.isNodeType("lfs:VocabularyAnswer")) {
                return serializeNode.apply(child);
            }
        } catch (RepositoryException e) {
            // Really shouldn't happen
        }
        return input;
    }

    @Override
    public void leave(Node node, JsonObjectBuilder json, Function<Node, JsonValue> serializeNode)
    {
        try {
            if (node.isNodeType("lfs:VocabularyAnswer")) {
                addProperty(node, json, serializeNode);
            }
        } catch (RepositoryException e) {
            // Really shouldn't happen
        }
    }

    @Override
    public String getAnswerLabel(final Node node, final Node question)
    {
        try {
            List<String> propsSet = new ArrayList<>();
            List<String> labelSet = new ArrayList<>();

            Property nodeProp = node.getProperty(PROP_VALUE);
            if (nodeProp.isMultiple()) {
                for (Value value : nodeProp.getValues()) {
                    propsSet.add(value.getString());
                }
            } else {
                propsSet.add(nodeProp.getString());
            }

            for (String value : propsSet) {
                if (value.startsWith("/Vocabularies/")) {
                    Node term = node.getSession().getNode(value);
                    String label = term.getProperty("label").getValue().toString();
                    if (label != null) {
                        labelSet.add(label);
                    }
                }
            }

            if (!labelSet.isEmpty()) {
                return StringUtils.join(labelSet, ", ");
            }
            return StringUtils.join(propsSet, ", ");
        } catch (final RepositoryException ex) {
            // Really shouldn't happen
        }
        return null;
    }
}

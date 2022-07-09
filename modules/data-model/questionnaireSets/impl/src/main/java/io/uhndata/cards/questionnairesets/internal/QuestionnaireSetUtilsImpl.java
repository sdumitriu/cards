/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.uhndata.cards.questionnairesets.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.FieldOption;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.uhndata.cards.questionnairesets.api.QuestionnaireReference;
import io.uhndata.cards.questionnairesets.api.QuestionnaireSet;
import io.uhndata.cards.questionnairesets.api.QuestionnaireSetUtils;
import io.uhndata.cards.spi.AbstractNodeUtils;

@Component
public final class QuestionnaireSetUtilsImpl extends AbstractNodeUtils implements QuestionnaireSetUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionnaireSetUtilsImpl.class);

    private static final class QuestionnaireReferenceImpl implements QuestionnaireReference
    {
        private final Node node;

        private QuestionnaireReferenceImpl(final Node node)
        {
            this.node = node;
        }

        @Override
        public Node getNode()
        {
            return this.node;
        }

        @Override
        public String getPath()
        {
            try {
                return this.node.getPath();
            } catch (RepositoryException e) {
                LOGGER.error("Unexpected repository exception while reading qref path: {}", e.getMessage(), e);
            }
            return null;
        }

        @Override
        public String getQuestionnairePath()
        {
            try {
                return this.node.getProperty("questionnaire").getNode().getPath();
            } catch (RepositoryException e) {
                LOGGER.error("Unexpected repository exception while reading qref referenced path: {}", e.getMessage(),
                    e);
            }
            return null;
        }

        @Override
        public Long getOrder()
        {
            return getProperty("order");
        }

        @Override
        public Long getEstimate()
        {
            return getProperty("estimate");
        }

        @Override
        public Long getFrequency()
        {
            return getProperty("frequency");
        }

        private Long getProperty(final String propertyName)
        {
            try {
                if (this.node.hasProperty(propertyName)) {
                    return this.node.getProperty(propertyName).getLong();
                }
            } catch (RepositoryException e) {
                LOGGER.error("Unexpected repository exception while reading property {} of qset {}: {}", propertyName,
                    this.node, e.getMessage(), e);
            }
            return null;
        }
    }

    private static final class QuestionnaireSetImpl implements QuestionnaireSet
    {
        private final Node node;

        private QuestionnaireSetImpl(final Node node)
        {
            this.node = node;
        }

        @Override
        public Node getNode()
        {
            return this.node;
        }

        @Override
        public String getPath()
        {
            try {
                return this.node.getPath();
            } catch (RepositoryException e) {
                LOGGER.error("Unexpected repository exception while reading qset path: {}", e.getMessage(), e);
            }
            return null;
        }

        @Override
        public String getName()
        {
            try {
                return this.node.getProperty("name").getString();
            } catch (RepositoryException e) {
                LOGGER.error("Unexpected repository exception while reading qset name: {}", e.getMessage(), e);
                try {
                    return this.node.getName();
                } catch (RepositoryException e1) {
                    return "";
                }
            }
        }

        @Override
        public String getDescription()
        {
            try {
                return this.node.hasProperty("description") ? this.node.getProperty("description").getString() : "";
            } catch (RepositoryException e) {
                LOGGER.error("Unexpected repository exception while qset description: {}", e.getMessage(), e);
            }
            return "";
        }

        @Override
        public List<QuestionnaireReference> getQuestionnaires()
        {
            try {
                final NodeIterator children = this.node.getNodes();
                final List<QuestionnaireReference> result = new ArrayList<>();
                while (children.hasNext()) {
                    final Node child = children.nextNode();
                    if (child.isNodeType(QUESTIONNAIRE_REFERENCE_NODETYPE)) {
                        result.add(new QuestionnaireReferenceImpl(child));
                    }
                }
                return result;
            } catch (RepositoryException e) {
                LOGGER.error("Unexpected repository exception while retrieving questionnaires in qset {}: {}",
                    this.node, e.getMessage(), e);
            }
            return Collections.emptyList();
        }
    }

    @Reference(fieldOption = FieldOption.REPLACE, cardinality = ReferenceCardinality.OPTIONAL,
        policyOption = ReferencePolicyOption.GREEDY)
    private ResourceResolverFactory rrf;

    @Override
    public boolean isQuestionnaireSet(Node node)
    {
        return isNodeType(node, QUESTIONNAIRE_SET_NODETYPE);
    }

    @Override
    public QuestionnaireSet getQuestionnaireSet(String identifier)
    {
        return getQuestionnaireSet(identifier, getSession(this.rrf));
    }

    @Override
    public QuestionnaireSet getQuestionnaireSet(String identifier, Session session)
    {
        final Node result = getNodeByIdentifier(identifier, session);
        return isQuestionnaireSet(result) ? new QuestionnaireSetImpl(result) : null;
    }

    @Override
    public boolean isQuestionnaireReference(Node node)
    {
        return isNodeType(node, QUESTIONNAIRE_REFERENCE_NODETYPE);
    }

    @Override
    public QuestionnaireReference getQuestionnaireReference(String identifier)
    {
        return getQuestionnaireReference(identifier, getSession(this.rrf));
    }

    @Override
    public QuestionnaireReference getQuestionnaireReference(String identifier, Session session)
    {
        final Node result = getNodeByIdentifier(identifier, session);
        return isQuestionnaireReference(result) ? new QuestionnaireReferenceImpl(result) : null;
    }
}

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
package io.uhndata.cards.questionnairesets.api;

import javax.jcr.Node;
import javax.jcr.Session;

/**
 * Basic utilities for working with QuestionnaireSets.
 *
 * @version $Id$
 */
public interface QuestionnaireSetUtils
{
    /** The primary node type for a QuestionnaireSet, a set of questionnaires to be filled together. */
    String QUESTIONNAIRE_SET_NODETYPE = "cards:QuestionnaireSet";

    /** The Sling resource type for a QuestionnaireSet. */
    String QUESTIONNAIRE_SET_RESOURCE = "cards/QuestionnaireSet";

    /** The primary node type for a QuestionnaireRef, a reference to a Questionnaire. */
    String QUESTIONNAIRE_REFERENCE_NODETYPE = "cards:QuestionnaireRef";

    /** The Sling resource type for a QuestionnaireRef. */
    String QUESTIONNAIRE_REFERENCE_RESOURCE = "cards/QuestionnaireRef";

    /**
     * Check if the given node is a QuestionnaireSet node.
     *
     * @param node the node to check, a JCR Node, may be {@code null}
     * @return {@code true} if the node is not {@code null} and is of type {@code cards:QuestionnaireSet}, {@code false}
     *         otherwise
     */
    boolean isQuestionnaireSet(Node node);

    /**
     * Retrieve the QuestionnaireSet with the given UUID, loaded using the current request session.
     *
     * @param identifier an UUID that references a questionnaire set
     * @return a QuestionnaireSet object, or {@code null} if the provided identifier does not identify a questionnaire
     *         set node, or if the session does not have access to it
     */
    QuestionnaireSet getQuestionnaireSet(String identifier);

    /**
     * Retrieve the QuestionnaireSet with the given UUID.
     *
     * @param identifier an UUID that references a questionnaire set
     * @param session a valid JCR session to use for loading nodes
     * @return a QuestionnaireSet object, or {@code null} if the provided identifier does not identify a questionnaire
     *         set node, or if the session does not have access to it
     */
    QuestionnaireSet getQuestionnaireSet(String identifier, Session session);

    /**
     * Check if the given node is a QuestionnaireRef node.
     *
     * @param node the node to check, a JCR Node, may be {@code null}
     * @return {@code true} if the node is not {@code null} and is of type {@code cards:QuestionnaireRef}, {@code false}
     *         otherwise
     */
    boolean isQuestionnaireReference(Node node);

    /**
     * Retrieve the QuestionnaireRef with the given UUID, loaded using the current request session.
     *
     * @param identifier an UUID that references a QuestionnaireRef
     * @return a QuestionnaireReference object, or {@code null} if the provided identifier does not identify a
     *         questionnaire reference set node, or if the session does not have access to it
     */
    QuestionnaireReference getQuestionnaireReference(String identifier);

    /**
     * Retrieve the QuestionnaireRef with the given UUID.
     *
     * @param identifier an UUID that references a QuestionnaireRef
     * @param session a valid JCR session to use for loading nodes
     * @return a QuestionnaireReference object, or {@code null} if the provided identifier does not identify a
     *         questionnaire reference set node, or if the session does not have access to it
     */
    QuestionnaireReference getQuestionnaireReference(String identifier, Session session);
}

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

/**
 * A Domain Model Object representing a {@code cards:QuestionnaireRef} node, a reference to a questionnaire used in a
 * questionnaire set. Instances can be built using {@link QuestionnaireReferenceBuilder}.
 *
 * @version $Id$
 */
public interface QuestionnaireReference
{
    /**
     * The reference node itself.
     *
     * @return a JCR node
     */
    Node getNode();

    /**
     * The path of the reference node itself.
     *
     * @return a JCR path
     */
    String getPath();

    // TODO Should we have a Questionnaire DMO?
    // Questionnaire getQuestionnaire();

    /**
     * The path to the questionnaire being referenced.
     *
     * @return a valid JCR path to an existing questionnaire node
     */
    String getQuestionnairePath();

    /**
     * An order number indicating the order of this questionnaire in the sequence.
     *
     * @return a positive number
     */
    Long getOrder();

    /**
     * An optional estimate for the time it would take to fill out the questionnaire, in minutes.
     *
     * @return a positive number, or {@code null} if an estimate is not specified
     */
    Long getEstimate();

    /**
     * An optional frequency for re-taking the questionnaire, in weeks.
     *
     * @return a positive number, or {@code null} if a frequency is not specified
     */
    Long getFrequency();
}

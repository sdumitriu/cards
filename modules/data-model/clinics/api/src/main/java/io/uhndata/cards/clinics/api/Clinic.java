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
package io.uhndata.cards.clinics.api;

import javax.jcr.Node;

import io.uhndata.cards.questionnairesets.api.QuestionnaireSet;

public interface Clinic
{
    Node getNode();

    String getPath();

    /**
     * Retrieve the internal name of the Clinic.
     *
     * @return a name, or {@code null} if the clinic does not have one
     */
    String getName();

    /**
     * Retrieve the user-facing display name of a Clinic.
     *
     * @return a name, or {@code null} if the clinic does not have one
     */
    String getDisplayName();

    String getDescription();

    String getShortName();

    String getContact();

    String getEmergencyContact();

    QuestionnaireSet getQuestionnaireSet();
}

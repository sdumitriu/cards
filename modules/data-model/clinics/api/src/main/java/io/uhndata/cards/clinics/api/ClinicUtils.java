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
import javax.jcr.Session;

/**
 * Basic utilities for working with Clinic instances.
 *
 * @version $Id$
 */
public interface ClinicUtils
{
    // Constants for the Clinic data types

    /** The primary node type for a Clinic. */
    String CLINIC_NODETYPE = "cards:Clinic";

    /** The Sling resource type of a Clinic. */
    String CLINIC_RESOURCE = "cards/Clinic";

    /** The name of the property of a Clinic node that links to the QuestionnaireSet being answered. */
    String QUESTIONNAIRE_SET_PROPERTY = "survey";

    /** The name of the property of a Clinic node that specifies its internal name. */
    String NAME_PROPERTY = "clinicName";

    /** The name of the property of a Clinic node that specifies its user-facing display name. */
    String DISPLAY_NAME_PROPERTY = "displayName";

    // Clinic management methods

    /**
     * Check if the given node is a Clinic node.
     *
     * @param node the node to check, a JCR Node, may be {@code null}
     * @return {@code true} if the node is not {@code null} and is of type {@code cards:Clinic}, {@code false} otherwise
     */
    boolean isClinic(Node node);

    /**
     * Retrieve the Clinic with the given UUID, loaded using the current request session.
     *
     * @param identifier an UUID that references a clinic
     * @return a QuestionnaireSet object, or {@code null} if the provided identifier does not identify a questionnaire
     *         set node, or if the session does not have access to it
     */
    Clinic getClinic(String identifier);

    /**
     * Retrieve the Clinic with the given UUID.
     *
     * @param identifier an UUID that references a clinic
     * @param session a valid JCR session to use for loading nodes
     * @return a Clinic object, or {@code null} if the provided identifier does not identify a clinic node, or if the
     *         session does not have access to it
     */
    Clinic getClinic(String identifier, Session session);

    /**
     * Create a new clinic, along with all the related entities: a group, a sidebar entry.
     *
     * @param name the new clinic's internal name
     * @param displayName the new clinic's display name
     * @param description a description for the clinic
     * @param questionnaireSet the questionnaire set to use in the new clinic
     * @param emergencyContact an optional email address for sending emergency notification
     * @param sidebarLabel the label for the sidebar entry linking to the clinic-specific dashboard
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean createClinic(String name, String displayName, String description, String questionnaireSet,
        String emergencyContact, String sidebarLabel);

    /**
     * Delete an existing clinic, along with all its related entities.
     *
     * @param node the clinic to delete
     * @return {@code true} if the operation was successful, {@code false} otherwise
     */
    boolean deleteClinic(Node node);
}

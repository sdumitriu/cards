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

public final class ClinicInformationBuilder
{
    private static final class ClinicInformation implements Clinic
    {
        private String name;

        private String displayName;

        private String description;

        private String shortName;

        private String contact;

        private String emergencyContact;

        private QuestionnaireSet questionnaireSet;

        @Override
        public Node getNode()
        {
            // Not backed by a real node
            return null;
        }

        @Override
        public String getPath()
        {
            // Not backed by a real node
            return null;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        @Override
        public String getDisplayName()
        {
            return this.displayName;
        }

        @Override
        public String getShortName()
        {
            return this.shortName;
        }

        @Override
        public String getDescription()
        {
            return this.description;
        }

        @Override
        public String getContact()
        {
            return this.contact;
        }

        @Override
        public String getEmergencyContact()
        {
            return this.emergencyContact;
        }

        @Override
        public QuestionnaireSet getQuestionnaireSet()
        {
            return this.questionnaireSet;
        }
    }

    private final ClinicInformation clinic;

    private ClinicInformationBuilder()
    {
        // Nothing to do,
        this.clinic = new ClinicInformation();
    }

    public static ClinicInformationBuilder newBuilder()
    {
        return new ClinicInformationBuilder();
    }

    public ClinicInformationBuilder withName(final String name)
    {
        this.clinic.name = name;
        return this;
    }

    public ClinicInformationBuilder withDisplayName(final String displayName)
    {
        this.clinic.displayName = displayName;
        return this;
    }

    public ClinicInformationBuilder withDescription(final String description)
    {
        this.clinic.description = description;
        return this;
    }

    public ClinicInformationBuilder withShortName(final String shortName)
    {
        this.clinic.shortName = shortName;
        return this;
    }
}

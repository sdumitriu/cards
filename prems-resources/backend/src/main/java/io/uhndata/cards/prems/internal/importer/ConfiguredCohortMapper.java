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

package io.uhndata.cards.prems.internal.importer;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.uhndata.cards.clarity.importer.spi.ClarityDataProcessor;

/**
 * Clarity import processor that assigns a cohort to an imported visit based on OSGi configured conditions.
 *
 * @version $Id$
 */
@Designate(ocd = ConfiguredCohortMapper.Config.class, factory = true)
@Component
public class ConfiguredCohortMapper extends AbstractConditionalClarityDataProcessor implements ClarityDataProcessor
{
    @ObjectClassDefinition(name = "Clarity import filter - Cohort mapping conditions",
    description = "Configuration for the Clarity importer to map visits matching these conditions to a specified"
        + " cohort")
    public static @interface Config
    {
        @AttributeDefinition(name = "Priority", description = "Clarity Data Processor priority."
            + " Processors are run in ascending priority order")
        int priority();

        @AttributeDefinition(name = "Clinic",
            description = "Clinic mapping path that should be assigned if all conditions are met"
                + " (eg. /Survey/ClinicMapping/123456789)")
        String clinic();

        @AttributeDefinition(name = "Conditions",
            description = "Conditions for this cohort to be assigned."
                + " Included operators are:"
                + "\n - Case insensitive string comparisons '<>' and '='"
                + "\n - Case insensitive list comparisons 'in' and 'not in'. Split values by ';' eg. COLUMN in a; b; c"
                + "\n - Regex comparisons 'matches' and 'not matches'"
                + "\n - Double comparisons '<=', '<', '>=' and '>'"
                + "\n - Unary operators 'is empty' and 'is not empty'"
                + "\nFor example \"COLUMN_NAME is empty\".")
        String[] conditions();
    }

    private final String cohort;

    @Activate
    public ConfiguredCohortMapper(Config configuration)
    {
        super(configuration.priority(), configuration.conditions());
        this.cohort = configuration.clinic();
    }

    @Override
    protected Map<String, String> handleAllConditionsMatched(Map<String, String> input)
    {
        input.put("CLINIC", this.cohort);
        return input;
    }

    @Override
    protected Map<String, String> handleUnmatchedCondition(Map<String, String> input)
    {
        return input;
    }
}

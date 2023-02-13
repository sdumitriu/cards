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
 * Clarity import processor that discards any visits meeting all conditions configured via OSGI configurations.
 *
 * @version $Id$
 */
@Designate(ocd = ConfiguredDiscardFilter.Config.class, factory = true)
@Component
public class ConfiguredDiscardFilter extends AbstractConditionalClarityDataProcessor implements ClarityDataProcessor
{
    @ObjectClassDefinition(name = "Clarity import filter - Discard Conditions",
    description = "Configuration for the Clarity importer filter to discard any questionnaires matching these"
        + " conditions")
    public static @interface Config
    {
        @AttributeDefinition(name = "Priority", description = "Clarity Data Processor priority."
            + " Processors are run in ascending priority order")
        int priority();

        @AttributeDefinition(name = "Conditions",
            description = "Conditions for this cohort to be assigned."
                + " Included operators are:"
                + "\n - Case insensitive string comparisons '<>' and '='"
                + "\n - Regex comparisons 'matches' and 'not matches'"
                + "\n - Double comparisons '<=', '<', '>=' and '>'"
                + "\n - Unary operators 'is empty' and 'is not empty'"
                + "\nFor example \"COLUMN_NAME is empty\".")
        String[] conditions();
    }

    @Activate
    public ConfiguredDiscardFilter(Config configuration)
    {
        super(configuration.priority(), configuration.conditions());
    }

    @Override
    protected Map<String, String> handleAllConditionsMatched(Map<String, String> input)
    {
        return null;
    }

    @Override
    protected Map<String, String> handleUnmatchedCondition(Map<String, String> input)
    {
        return input;
    }
}

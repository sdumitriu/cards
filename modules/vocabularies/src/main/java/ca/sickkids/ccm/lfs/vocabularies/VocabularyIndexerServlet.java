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
package ca.sickkids.ccm.lfs.vocabularies;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.sickkids.ccm.lfs.vocabularies.internal.NCITFlatParser;
import ca.sickkids.ccm.lfs.vocabularies.spi.VocabularyIndexException;

/**
 * Servlet which handles parsing and JCR node creation for vocabularies.
 *
 * @version $Id$
 */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(resourceTypes = { "lfs/VocabulariesHomepage" }, methods = { "POST" })
public class VocabularyIndexerServlet extends SlingAllMethodsServlet
{
    private static final long serialVersionUID = -2156160697967947088L;

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularyIndexerServlet.class);

    @Override
    public void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws IOException
    {
        String source = request.getParameter("source");
        if (source != null && "ncit".equalsIgnoreCase(source)) {
            NCITFlatParser parser = new NCITFlatParser();
            try {
                parser.parse(source, request, response);
            } catch (VocabularyIndexException e) {
                LOGGER.warn("Failed to parse vocabulary from [{}] using parser [{}]: {}", source,
                    parser.getClass().getCanonicalName(), e.getMessage());
            }
        }
    }
}

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
package io.uhndata.cards.s3export;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import io.uhndata.cards.resolverProvider.ThreadResourceResolverProvider;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(
    resourceTypes = { "cards/SubjectsHomepage" },
    selectors = { "s3push" })
public class ExportEndpoint extends SlingSafeMethodsServlet
{
    private static final long serialVersionUID = -1615592669184694095L;

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private ThreadResourceResolverProvider rrp;

    @Reference
    private volatile List<ExportConfig> configs;

    @Override
    public void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException
    {
        final Writer out = response.getWriter();

        // Ensure that this can only be run when logged in as admin
        final String remoteUser = request.getRemoteUser();
        if (remoteUser == null || !"admin".equals(remoteUser)) {
            writeError(403, "Only admin can perform this operation.", response);
            return;
        }

        final String configName = request.getParameter("config");
        ExportConfigDefinition config;
        if (StringUtils.isBlank(configName)) {
            if (this.configs.size() != 1) {
                writeError(400, this.configs.size() > 1 ? "Configuration name must be specified"
                    : "No S3 export is configured", response);
                return;
            }
            config = this.configs.get(0).getConfig();
        } else {
            config = this.configs.stream()
                .filter(c -> configName.equals(c.getConfig().name()))
                .map(ExportConfig::getConfig)
                .findFirst().orElse(null);
            if (config == null) {
                response.setStatus(404);
                writeError(400, "Unknown S3 export configuration", response);
                return;
            }

        }

        final LocalDate dateLowerBound = this.strToDate(request.getParameter("dateLowerBound"));
        final LocalDate dateUpperBound = this.strToDate(request.getParameter("dateUpperBound"));
        final String exportRunMode = (dateLowerBound != null && dateUpperBound != null)
            ? "manualBetween"
            : (dateLowerBound != null && dateUpperBound == null) ? "manualAfter" : "manualToday";

        final Runnable exportJob = ("manualToday".equals(exportRunMode))
            ? new ExportTask(this.resolverFactory, this.rrp, config, exportRunMode)
            : new ExportTask(this.resolverFactory, this.rrp, config, exportRunMode, dateLowerBound, dateUpperBound);
        final Thread thread = new Thread(exportJob);
        thread.start();
        out.write("S3 export started");
    }

    private LocalDate strToDate(final String date)
    {
        if (date == null) {
            return null;
        }
        try {
            return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void writeError(final int status, final String message, final SlingHttpServletResponse response)
        throws IOException
    {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("status", "error");
        json.add("error", message);
        writeResponse(status, json.build().toString(), response);
    }

    private void writeSuccess(final SlingHttpServletResponse response)
        throws IOException
    {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        json.add("status", "success");
        json.add("message", "Data import started");
        writeResponse(200, json.build().toString(), response);
    }

    private void writeResponse(final int status, final String body, final SlingHttpServletResponse response)
        throws IOException
    {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body);
    }
}

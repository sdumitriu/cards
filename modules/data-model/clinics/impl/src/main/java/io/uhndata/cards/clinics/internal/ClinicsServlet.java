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
package io.uhndata.cards.clinics.internal;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.servlet.Servlet;

import org.apache.jackrabbit.api.security.user.Group;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.uhndata.cards.permissions.spi.PermissionsManager;

@Component(service = { Servlet.class })
@SlingServletResourceTypes(resourceTypes = { "cards/ClinicHomepage" }, methods = { "POST" })
public class ClinicsServlet extends SlingAllMethodsServlet
{
    private static final long serialVersionUID = -5555906093850253193L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClinicsServlet.class);

    private static final String IMPORT_FACTORY_PID = "io.uhndata.cards.proms.internal.importer.ImportConfig";

    private static final String DESCRIPTION_FIELD = "description";

    private static final String PRIMARY_TYPE_FIELD = "jcr:primaryType";

    private final ThreadLocal<String> clinicName = new ThreadLocal<>();

    private final ThreadLocal<String> displayName = new ThreadLocal<>();

    private final ThreadLocal<String> sidebarLabel = new ThreadLocal<>();

    private final ThreadLocal<String> surveyID = new ThreadLocal<>();

    private final ThreadLocal<String> emergencyContact = new ThreadLocal<>();

    private final ThreadLocal<String> description = new ThreadLocal<>();

    private final ThreadLocal<String> idHash = new ThreadLocal<>();

    @Reference
    private ConfigurationAdmin configAdmin;

    @Reference
    private PermissionsManager permissionsManager;

    @Override
    public void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
        throws IOException
    {
        try {
            // Create all of the necessary nodes
            final ResourceResolver resolver = request.getResourceResolver();
            this.parseArguments(request);
            try {
                final Session session = resolver.adaptTo(Session.class);
                this.displayName.set(getUniqueDisplayName(resolver, this.displayName.get()));
                this.createClinicMapping(resolver);
                final Group clinicGroup = this.createGroup(resolver);
                this.createSidebar(resolver, clinicGroup);
                this.createDashboardExtension(resolver);
                session.save();
            } catch (final RepositoryException e) {
                this.returnError(response, e.getMessage());
            } catch (final NullPointerException e) {
                this.returnError(response, e.getMessage());
            }

            // Grab the configuration to edit
            final Configuration[] configs = this.configAdmin.listConfigurations(
                "(service.factoryPid=" + IMPORT_FACTORY_PID + ")");

            if (configs != null) {
                for (final Configuration config : configs) {
                    this.insertNewClinic(config, this.clinicName.get());
                }
            }
        } catch (final InvalidSyntaxException e) {
            // This can happen when the filter given to the listConfigurations call above is wrong
            // This shouldn't happen unless a typo was made in the value of IMPORT_FACTORY_PID
            this.returnError(response, "Invalid syntax in config search.");
        } catch (final IOException e) {
            // This can happen while updating the properties of a configuration
            // Unknown how to handle this
            this.returnError(response, "Updating clinic configurations failed.");
        } finally {
            cleanup();
        }
    }

    /**
     * Parse out the arguments from the request into threadlocal variables for later usage.
     *
     * @param request servlet request whose arguments we need to parse
     */
    private boolean parseArguments(final SlingHttpServletRequest request)
    {
        this.clinicName.set(request.getParameter("clinicName"));
        this.displayName.set(request.getParameter("displayName"));
        this.sidebarLabel.set(request.getParameter("sidebarLabel"));
        this.surveyID.set(request.getParameter("survey"));
        this.emergencyContact.set(request.getParameter("emergencyContact"));
        this.description.set(request.getParameter("description"));
        this.idHash.set(Integer.toString(this.clinicName.get().hashCode()));
        return true;
    }

    /**
     * Return an error code to the POST request, letting the user know that something is wrong.
     *
     * @param response object to send response through
     * @param reason reason to give to user
     */
    private void returnError(final SlingHttpServletResponse response, String reason)
    {
        LOGGER.error(reason);
        try {
            response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
            Writer out = response.getWriter();
            JsonGenerator generator = Json.createGenerator(out);
            generator.writeStartObject();
            generator.write("error", reason);
            generator.writeEnd();
            generator.flush();
        } catch (IOException e) {
            LOGGER.error("Furthermore, IOException occurred while returning response to user: {}", e.getMessage(), e);
        }
    }

    /**
     * Remove any data from the ThreadLocal state.
     */
    private void cleanup()
    {
        this.clinicName.remove();
        this.description.remove();
        this.displayName.remove();
        this.emergencyContact.remove();
        this.idHash.remove();
        this.sidebarLabel.remove();
        this.surveyID.remove();
    }
}

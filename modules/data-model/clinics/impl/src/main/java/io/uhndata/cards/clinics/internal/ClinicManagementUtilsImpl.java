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
package io.uhndata.cards.clinics.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Group;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.oak.spi.security.principal.EveryonePrincipal;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.cm.Configuration;
import org.osgi.service.component.annotations.Component;

import io.uhndata.cards.clinics.api.ClinicManagementUtils;
import io.uhndata.cards.spi.AbstractNodeUtils;

/**
 * Implementation of {@link ClinicManagementUtils}.
 *
 * @version $Id$
 */
@Component
public final class ClinicManagementUtilsImpl extends AbstractNodeUtils implements ClinicManagementUtils
{
    @Override
    public boolean createClinic(final String name, final String displayName, final String description,
        final String questionnaireSet, final String emergencyContact, final String sidebarLabel)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteClinic(Node node)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * Create a cards:ClinicMapping node.
     *
     * @param resolver Resource resolver to use
     */
    private void createClinicNode(final ResourceResolver resolver)
        throws RepositoryException, PersistenceException
    {
        final Resource parentResource = resolver.getResource("/Clinics");

        resolver.create(parentResource, this.idHash.get(), Map.of(
            "clinicName", this.clinicName.get(),
            "displayName", this.displayName.get(),
            "sidebarLabel", this.sidebarLabel.get(),
            "survey", this.surveyID.get(),
            "emergencyContact", this.emergencyContact.get(),
            ClinicsServlet.DESCRIPTION_FIELD, this.description.get(),
            ClinicsServlet.PRIMARY_TYPE_FIELD, "cards:ClinicMapping"));
    }

    /**
     * Create a Group with access to the clinic.
     *
     * @param resolver Resource resolver to use
     * @return the created Group
     * @throws RepositoryException if accessing the repository fails
     */
    private Group createGroup(final ResourceResolver resolver) throws RepositoryException
    {
        final Session session = resolver.adaptTo(Session.class);
        if (!(session instanceof JackrabbitSession)) {
            return null;
        }
        final JackrabbitSession jsession = (JackrabbitSession) session;
        final UserManager um = jsession.getUserManager();
        return um.createGroup(this.clinicName.get());
    }

    /**
     * Create a cards:Extension node for the sidebar.
     *
     * @param resolver Resource resolver to use
     * @param clinicGroup the Group corresponding to this clinic
     */
    private void createSidebar(final ResourceResolver resolver, final Group clinicGroup)
        throws RepositoryException, PersistenceException
    {
        final Resource parentResource = resolver.getResource("/Extensions/Sidebar");
        final Resource sidebarEntry = resolver.create(parentResource, this.idHash.get(), Map.of(
            "cards:extensionPointId", "cards/coreUI/sidebar/entry",
            "cards:extensionName", this.sidebarLabel.get(),
            "cards:targetURL", "/content.html/Dashboard/" + this.idHash.get(),
            "cards:icon", "asset:proms-homepage.clinicIcon.js",
            "cards:defaultOrder", 10,
            ClinicsServlet.PRIMARY_TYPE_FIELD, "cards:Extension"));
        if (clinicGroup != null) {
            Session session = resolver.adaptTo(Session.class);
            this.permissionsManager.addAccessControlEntry(sidebarEntry.getPath(), false,
                EveryonePrincipal.getInstance(), new String[] { Privilege.JCR_ALL }, null, session);
            this.permissionsManager.addAccessControlEntry(sidebarEntry.getPath(), true, clinicGroup.getPrincipal(),
                new String[] { Privilege.JCR_READ }, null, session);
        }
    }

    /**
     * Create a dashboard extension for the new clinic.
     *
     * @param resolver Resource resolver to use
     */
    private void createDashboardExtension(final ResourceResolver resolver)
        throws RepositoryException, PersistenceException
    {
        final Resource parentResource = resolver.getResource("/apps/cards/ExtensionPoints");
        resolver.create(parentResource, "DashboardViews" + this.idHash.get(), Map.of(
            ClinicsServlet.PRIMARY_TYPE_FIELD, "cards:ExtensionPoint",
            "cards:extensionPointId", "proms/dashboard/" + this.idHash.get(),
            "cards:extensionPointName", this.displayName.get() + " questionnaires dashboard"));
    }

    /**
     * Update the clinic.names field of the given configuration with a new clinic name.
     *
     * @param config An OSGI config object for an instance of a proms ImportConfig
     * @param newClinicName An new clinic's name to add
     */
    public void insertNewClinic(Configuration config, String newClinicName) throws IOException
    {
        String[] clinicNames = (String[]) config.getProperties().get("clinic.names");
        String[] updatedClinicNames = Arrays.copyOf(clinicNames, clinicNames.length + 1);
        updatedClinicNames[clinicNames.length] = newClinicName;

        // Create a dictionary to contain the update request
        Dictionary<String, Object> updateDictionary = new Hashtable<>();
        updateDictionary.put("clinic.names", updatedClinicNames);
        config.update(updateDictionary);
    }

    /**
     * Get a unique display name for a cards:Clinic node, appending numbers to the end if one already exists.
     *
     * @param resolver Resource resolver to use
     * @param displayName Given display name to check for duplicates for
     * @return Unique display name
     */
    private String getUniqueDisplayName(final ResourceResolver resolver, String displayName)
        throws RepositoryException
    {
        // Pre-sanitize the name
        String sanitizedName = displayName.replaceAll("'", "''");

        // Query for similar names
        String query = "SELECT * FROM [cards:ClinicMapping] as c WHERE c.'displayName' LIKE '"
            + sanitizedName + "%'";
        Iterator<Resource> results = resolver.findResources(query, "JCR-SQL2");

        // Determine what names currently exist
        Set<Integer> foundNames = new HashSet<>();
        boolean noNumberValid = true;
        Pattern numberRegex = Pattern.compile(displayName + " ([\\d]+)");
        while (results.hasNext()) {
            String name = results.next().adaptTo(Node.class).getProperty("displayName").getString();

            Matcher match = numberRegex.matcher(name);
            if (match.find()) {
                foundNames.add(Integer.parseInt(match.group(1)));
            } else if (displayName.equals(name)) {
                noNumberValid = false;
            }
        }

        // Determine if we can use the display name as-is
        if (noNumberValid) {
            return displayName;
        }

        // Find the first number i that works if we stick it after the display name
        for (int i = 1; i < foundNames.size(); i++) {
            if (!foundNames.contains(i)) {
                return displayName + " " + String.valueOf(i);
            }
        }
        return displayName + " " + String.valueOf(foundNames.size() + 1);
    }
}

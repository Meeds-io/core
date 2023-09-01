/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.services.organization.api;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreService;
import org.exoplatform.services.organization.externalstore.model.IDMEntityType;
import org.exoplatform.services.organization.impl.UserProfileImpl;

@RunWith(MockitoJUnitRunner.class)
public class IDMExternalStoreImportServiceTest {

  @Mock
  private InitParams                            initParams;

  @Mock
  private IDMExternalStoreService       idmExternalStoreService;

  @Mock
  private ListenerService               listenerService;

  private IDMExternalStoreImportService idmExternalStoreImportService;

  private OrganizationService           organizationService;

  private UserProfileHandler            profileHandler;

  @Before
  public void setUp() throws Exception {
    URL containerConfURL = TestUserHandler.class.getResource("/conf/standalone/test-configuration.xml");
    assertNotNull(containerConfURL);

    String containerConf = containerConfURL.toString();
    StandaloneContainer.addConfigurationURL(containerConf);
    StandaloneContainer container = StandaloneContainer.getInstance();

    organizationService =
                        (BaseOrganizationService) container.getComponentInstance(org.exoplatform.services.organization.OrganizationService.class);
    assertNotNull(organizationService);

    profileHandler = organizationService.getUserProfileHandler();
    this.idmExternalStoreImportService = new IDMExternalStoreImportService(container,
                                                                           organizationService,
                                                                           listenerService,
                                                                           idmExternalStoreService,
                                                                           null,
                                                                           null,
                                                                           initParams);
  }

  @Test
  public void importUserProfileTest() throws Exception {
    String userName = "john";
    profileHandler.createUserProfileInstance();
    UserProfile externalUserProfile = mock(UserProfile.class);
    when(idmExternalStoreService.getEntity(IDMEntityType.USER_PROFILE, userName)).thenReturn(externalUserProfile);
    when(externalUserProfile.getUserInfoMap()).thenReturn(new HashMap<>());
    when(idmExternalStoreService.isEntityPresent(IDMEntityType.USER_PROFILE, userName)).thenReturn(true);
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, times(0)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                anyObject(),
                                                argThat(param -> param instanceof HashMap<?, ?>));

    when(externalUserProfile.getUserInfoMap()).thenReturn(new HashMap<>());
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, times(0)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                anyObject(),
                                                argThat(param -> param instanceof HashMap<?, ?>));

    Map<String, String> propertiesMap = new HashMap<>();
    propertiesMap.put("propertyKey", "propertyValue");
    when(externalUserProfile.getUserInfoMap()).thenReturn(propertiesMap);
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, times(1)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                anyObject(),
                                                argThat(param -> param instanceof HashMap<?, ?>));
    UserProfile userProfile = new UserProfileImpl();
    userProfile.setUserName(userName);
    userProfile.setUserInfoMap(propertiesMap);
    profileHandler.saveUserProfile(userProfile, false);
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, atLeast(0)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                  anyObject(),
                                                  argThat(param -> param instanceof HashMap<?, ?>));

    Map<String, String> updatedPropertyMap = new HashMap<>();
    updatedPropertyMap.put("propertyKey", "updatedPropertyValue");
    when(externalUserProfile.getUserInfoMap()).thenReturn(updatedPropertyMap);
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, atLeast(1)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                  anyObject(),
                                                  argThat(param -> param instanceof HashMap<?, ?>));
  }

}

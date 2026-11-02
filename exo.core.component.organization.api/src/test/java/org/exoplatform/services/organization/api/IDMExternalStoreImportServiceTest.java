/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.services.organization.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import org.exoplatform.services.organization.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreService;
import org.exoplatform.services.organization.externalstore.model.IDMEntityType;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.services.organization.impl.MembershipImpl;
import org.exoplatform.services.organization.impl.MembershipTypeImpl;
import org.exoplatform.services.organization.impl.UserImpl;
import org.exoplatform.services.organization.impl.UserProfileImpl;

@RunWith(MockitoJUnitRunner.class)
public class IDMExternalStoreImportServiceTest {

  private static final String           GROUP_ID = "/ldap-groups/G1";

  @Mock
  private InitParams                            initParams;

  @Mock
  private IDMExternalStoreService       idmExternalStoreService;

  @Mock
  private ListenerService               listenerService;

  private IDMExternalStoreImportService idmExternalStoreImportService;

  private OrganizationService           organizationService;

  private UserProfileHandler            profileHandler;

  private UserHandler                   userHandler;

  private MembershipHandler             membershipHandler;

  private Group                         group;

  private MembershipType                membershipType;

  private Map<String, User>             internalUsers;

  @Before
  public void setUp() throws Exception {
    URL containerConfURL = TestUserHandler.class.getResource("/conf/standalone/test-configuration.xml");
    assertNotNull(containerConfURL);

    String containerConf = containerConfURL.toString();
    StandaloneContainer.addConfigurationURL(containerConf);
    StandaloneContainer container = StandaloneContainer.getInstance();

    organizationService = mock(OrganizationService.class);
    userHandler = mock(UserHandler.class);
    when(organizationService.getUserHandler()).thenReturn(userHandler);

    profileHandler = mock(UserProfileHandler.class);
    when(organizationService.getUserProfileHandler()).thenReturn(profileHandler);

    this.idmExternalStoreImportService = new IDMExternalStoreImportService(null,
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
                                                any(),
                                                argThat(param -> param instanceof HashMap<?, ?>));

    when(externalUserProfile.getUserInfoMap()).thenReturn(new HashMap<>());
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, times(0)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                any(),
                                                argThat(param -> param instanceof HashMap<?, ?>));

    Map<String, String> propertiesMap = new HashMap<>();
    propertiesMap.put("propertyKey", "propertyValue");
    when(externalUserProfile.getUserInfoMap()).thenReturn(propertiesMap);
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, times(1)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                any(),
                                                argThat(param -> param instanceof HashMap<?, ?>));
    UserProfile userProfile = new UserProfileImpl();
    userProfile.setUserName(userName);
    userProfile.setUserInfoMap(propertiesMap);
    profileHandler.saveUserProfile(userProfile, false);
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, atLeast(0)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                  any(),
                                                  argThat(param -> param instanceof HashMap<?, ?>));

    Map<String, String> updatedPropertyMap = new HashMap<>();
    updatedPropertyMap.put("propertyKey", "updatedPropertyValue");
    when(externalUserProfile.getUserInfoMap()).thenReturn(updatedPropertyMap);
    //
    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_PROFILE, userName, false, false);
    verify(listenerService, atLeast(1)).broadcast(eq(IDMExternalStoreService.USER_PROFILE_ADDED_FROM_EXTERNAL_STORE),
                                                  any(),
                                                  argThat(param -> param instanceof HashMap<?, ?>));
  }

  @Test
  public void skipUserCreationWhenUsernameContainsWhitespace() throws Exception {
    String usernameWithSpace = "john doe";

    Method importUserMethod = IDMExternalStoreImportService.class.getDeclaredMethod("importUser",
                                                                                    String.class,
                                                                                    boolean.class,
                                                                                    boolean.class,
                                                                                    boolean.class);
    importUserMethod.setAccessible(true);

    Object result = importUserMethod.invoke(idmExternalStoreImportService, usernameWithSpace, false, true, true);

    assertNull(result);
    verify(organizationService.getUserHandler(), never()).createUser(any(User.class), anyBoolean());
  }

  @Test
  public void skipUserCreationWhenUsernameContainsApostrophe() throws Exception {
    String usernameWithApostrophe = "john's doe";

    Method importUserMethod = IDMExternalStoreImportService.class.getDeclaredMethod("importUser",
            String.class,
            boolean.class,
            boolean.class,
            boolean.class);
    importUserMethod.setAccessible(true);

    Object result = importUserMethod.invoke(idmExternalStoreImportService, usernameWithApostrophe, false, true, true);

    assertNull(result);
    verify(organizationService.getUserHandler(), never()).createUser(any(User.class), anyBoolean());
  }

  @Test
  public void skipUserCreationWhenUsernameIsEmpty() throws Exception {
    String usernameEmpty = "";

    Method importUserMethod = IDMExternalStoreImportService.class.getDeclaredMethod("importUser",
            String.class,
            boolean.class,
            boolean.class,
            boolean.class);
    importUserMethod.setAccessible(true);

    Object result = importUserMethod.invoke(idmExternalStoreImportService, usernameEmpty, false, true, true);

    assertNull(result);
    verify(organizationService.getUserHandler(), never()).createUser(any(User.class), anyBoolean());
  }

  @Test
  public void importGroupMembershipsKeepsUserWhoseDirectoryNameDiffersInCase() throws Exception {
    mockMembershipStores("testuser");
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of(membership("testuser")));
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID)).thenReturn(List.of(membership("TESTUSER")));

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(membershipHandler, never()).removeMembership(anyString(), anyBoolean());
    verify(membershipHandler, never()).linkMembership(any(), any(), any(), anyBoolean());
    // each spelling is resolved once per import
    verify(userHandler).findUserByName("testuser", UserStatus.ANY);
    verify(userHandler).findUserByName("TESTUSER", UserStatus.ANY);
  }

  @Test
  public void importGroupMembershipsAddsMissingMembershipOfUserWhoseDirectoryNameDiffersInCase() throws Exception {
    mockMembershipStores("testuser");
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of());
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID)).thenReturn(List.of(membership("TESTUSER")));

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(membershipHandler).linkMembership(argThat(user -> "testuser".equals(user.getUserName())),
                                             eq(group),
                                             eq(membershipType),
                                             eq(true));
    verify(membershipHandler, never()).removeMembership(anyString(), anyBoolean());
  }

  @Test
  public void importGroupMembershipsRemovesMembershipOfUserWhoseDirectoryNameDiffersInCase() throws Exception {
    mockMembershipStores("testuser", "mary");
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of(membership("testuser"), membership("mary")));
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID)).thenReturn(List.of(membership("MARY")));

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(membershipHandler).removeMembership(membership("testuser").getId(), true);
    verify(membershipHandler, never()).removeMembership(membership("mary").getId(), true);
    verify(membershipHandler, never()).linkMembership(any(), any(), any(), anyBoolean());
  }

  @Test
  public void importGroupMembershipsAppliesRoleChangedInDirectoryForUserWhoseNameDiffersInCase() throws Exception {
    mockMembershipStores("testuser");
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of(membership("testuser")));
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS,
                                           GROUP_ID)).thenReturn(List.of(new MembershipImpl("manager", "TESTUSER", GROUP_ID, false)));
    MembershipType managerType = new MembershipTypeImpl("manager", null, null);
    when(organizationService.getMembershipTypeHandler().findMembershipType("manager")).thenReturn(managerType);

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(membershipHandler).linkMembership(argThat(user -> "testuser".equals(user.getUserName())),
                                             eq(group),
                                             eq(managerType),
                                             eq(true));
    verify(membershipHandler).removeMembership(membership("testuser").getId(), true);
  }

  @Test
  public void importUserMembershipsRemovesGroupDroppedFromDirectoryForUserWhoseNameDiffersInCase() throws Exception {
    mockMembershipStores("testuser");
    String otherGroupId = "/ldap-groups/G2";
    GroupImpl otherGroup = new GroupImpl("G2");
    otherGroup.setId(otherGroupId);
    otherGroup.setOriginatingStore(OrganizationService.EXTERNAL_STORE);
    when(organizationService.getGroupHandler().findGroupById(otherGroupId)).thenReturn(otherGroup);
    Membership otherGroupMembership = new MembershipImpl("member", "testuser", otherGroupId, false);
    when(idmExternalStoreService.isEntityPresent(IDMEntityType.USER_MEMBERSHIPS, "TESTUSER")).thenReturn(true);
    when(membershipHandler.findMembershipsByUser("TESTUSER")).thenReturn(List.of(membership("testuser"), otherGroupMembership));
    when(idmExternalStoreService.getEntity(IDMEntityType.USER_MEMBERSHIPS, "TESTUSER")).thenReturn(Set.of(membership("TESTUSER")));

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_MEMBERSHIPS, "TESTUSER", true, true);

    verify(membershipHandler).removeMembership(otherGroupMembership.getId(), true);
    verify(membershipHandler, never()).removeMembership(membership("testuser").getId(), true);
    verify(membershipHandler, never()).linkMembership(any(), any(), any(), anyBoolean());
  }

  @Test
  public void importUserMembershipsKeepsUserWhoseDirectoryNameDiffersInCase() throws Exception {
    mockMembershipStores("testuser");
    when(idmExternalStoreService.isEntityPresent(IDMEntityType.USER_MEMBERSHIPS, "TESTUSER")).thenReturn(true);
    when(membershipHandler.findMembershipsByUser("TESTUSER")).thenReturn(List.of(membership("testuser")));
    when(idmExternalStoreService.getEntity(IDMEntityType.USER_MEMBERSHIPS, "TESTUSER")).thenReturn(Set.of(membership("TESTUSER")));

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.USER_MEMBERSHIPS, "TESTUSER", true, true);

    verify(membershipHandler, never()).removeMembership(anyString(), anyBoolean());
    verify(membershipHandler, never()).linkMembership(any(), any(), any(), anyBoolean());
  }

  @Test
  public void importGroupMembershipsKeepsUserWhoseDirectoryNameMatches() throws Exception {
    mockMembershipStores("john");
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of(membership("john")));
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID)).thenReturn(List.of(membership("john")));

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(membershipHandler, never()).removeMembership(anyString(), anyBoolean());
    verify(membershipHandler, never()).linkMembership(any(), any(), any(), anyBoolean());
  }

  @Test
  public void importGroupMembershipsRemovesMembershipDeletedFromDirectory() throws Exception {
    mockMembershipStores("testuser", "mary");
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of(membership("testuser"), membership("mary")));
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID)).thenReturn(List.of(membership("TESTUSER")));

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(membershipHandler).removeMembership(membership("mary").getId(), true);
    verify(membershipHandler, never()).removeMembership(membership("testuser").getId(), true);
    verify(membershipHandler, never()).linkMembership(any(), any(), any(), anyBoolean());
  }

  @Test
  public void importGroupMembershipsAddsMembershipAddedInDirectory() throws Exception {
    mockMembershipStores("testuser", "mary");
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of(membership("testuser")));
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS,
                                           GROUP_ID)).thenReturn(List.of(membership("TESTUSER"), membership("MARY")));

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(membershipHandler).linkMembership(argThat(user -> "mary".equals(user.getUserName())),
                                             eq(group),
                                             eq(membershipType),
                                             eq(true));
    verify(membershipHandler, never()).linkMembership(argThat(user -> "testuser".equals(user.getUserName())),
                                                      any(),
                                                      any(),
                                                      anyBoolean());
    verify(membershipHandler, never()).removeMembership(anyString(), anyBoolean());
    // the added membership's user is resolved once per import
    verify(userHandler).findUserByName("MARY", UserStatus.ANY);
  }

  @Test
  public void importGroupMembershipsImportsUserNotYetInInternalStore() throws Exception {
    mockMembershipStores("testuser");
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of(membership("testuser")));
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS,
                                           GROUP_ID)).thenReturn(List.of(membership("TESTUSER"), membership("newuser")));
    when(idmExternalStoreService.isEntityPresent(IDMEntityType.USER, "newuser")).thenReturn(false);

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(idmExternalStoreService).isEntityPresent(IDMEntityType.USER, "newuser");
    verify(idmExternalStoreService, never()).isEntityPresent(IDMEntityType.USER, "TESTUSER");
    verify(membershipHandler, never()).removeMembership(anyString(), anyBoolean());
    // a lookup that found no user is not repeated
    verify(userHandler).findUserByName("newuser", UserStatus.ANY);
  }

  /**
   * An internal membership whose user the internal store does not resolve (it
   * answers no user, as its case-insensitive search does when two accounts'
   * usernames differ only in letter case) is compared on its raw username.
   */
  @Test
  public void importGroupMembershipsImportsDirectoryMemberBesideUnresolvedInternalMembership() throws Exception {
    mockMembershipStores();
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of(membership("ghost")));
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID)).thenReturn(List.of(membership("newuser")));
    when(idmExternalStoreService.isEntityPresent(IDMEntityType.USER, "newuser")).thenReturn(false);

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    verify(idmExternalStoreService).isEntityPresent(IDMEntityType.USER, "newuser");
    verify(membershipHandler).removeMembership(membership("ghost").getId(), true);
  }

  @Test
  public void importGroupMembershipsImportsNewUserOnceForAllTheirMemberships() throws Exception {
    mockMembershipStores();
    when(membershipHandler.findMembershipsByGroup(group)).thenReturn(List.of());
    when(idmExternalStoreService.getEntity(IDMEntityType.GROUP_MEMBERSHIPS,
                                           GROUP_ID)).thenReturn(List.of(membership("newuser"),
                                                                         new MembershipImpl("manager", "newuser", GROUP_ID, false)));
    when(idmExternalStoreService.isEntityPresent(IDMEntityType.USER, "newuser")).thenReturn(true);
    when(idmExternalStoreService.getEntity(IDMEntityType.USER, "newuser")).thenReturn(new UserImpl("newuser"));
    doAnswer(invocation -> internalUsers.put("newuser", invocation.getArgument(0))).when(userHandler)
                                                                                   .createUser(any(User.class), eq(true));
    MembershipType managerType = new MembershipTypeImpl("manager", null, null);
    when(organizationService.getMembershipTypeHandler().findMembershipType("manager")).thenReturn(managerType);

    idmExternalStoreImportService.importEntityToInternalStore(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID, true, true);

    // the user imported for its first membership is reused for the second one
    verify(idmExternalStoreService).isEntityPresent(IDMEntityType.USER, "newuser");
    verify(userHandler).createUser(argThat(user -> "newuser".equals(user.getUserName())), eq(true));
    verify(membershipHandler).linkMembership(argThat(user -> "newuser".equals(user.getUserName())),
                                             eq(group),
                                             eq(membershipType),
                                             eq(true));
    verify(membershipHandler).linkMembership(argThat(user -> "newuser".equals(user.getUserName())),
                                             eq(group),
                                             eq(managerType),
                                             eq(true));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void checkDeletedUsersIgnoresUserWhoseDirectoryNameDiffersInCase() throws Exception {
    when(idmExternalStoreService.getManagedEntityTypes()).thenReturn(Set.of(IDMEntityType.USER));
    ListAccess<User> storedUsers = mock(ListAccess.class);
    when(storedUsers.getSize()).thenReturn(4);
    when(storedUsers.load(0, 4)).thenReturn(new User[] { new UserImpl("john"), new UserImpl("testuser"), new UserImpl("gone"),
        new UserImpl("root") });
    when(idmExternalStoreService.getAllInternalUsers()).thenReturn(storedUsers);
    ListAccess<String> externalUsernames = mock(ListAccess.class);
    when(externalUsernames.load(0, Integer.MAX_VALUE)).thenReturn(new String[] { "john", "TESTUSER" });
    when(idmExternalStoreService.getAllOfType(IDMEntityType.USER, null)).thenReturn(externalUsernames);
    when(idmExternalStoreService.isEntityPresent(IDMEntityType.USER, "testuser")).thenReturn(true);
    when(idmExternalStoreService.isEntityPresent(IDMEntityType.USER, "gone")).thenReturn(false);
    when(userHandler.findUserByName("gone")).thenReturn(externalUser("gone"));
    when(userHandler.findUserByName("testuser")).thenReturn(externalUser("testuser"));
    when(userHandler.findUserByName("root")).thenReturn(new UserImpl("root"));

    List<String> deletedUsernames = new ArrayList<>();
    idmExternalStoreImportService.checkDeletedEntitiesOfType(IDMEntityType.USER, deletedUsernames::add);

    assertEquals(List.of("gone"), deletedUsernames);
    verify(idmExternalStoreService, never()).isEntityPresent(IDMEntityType.USER, "john");
    // a user created in eXo is never searched in the directory
    verify(idmExternalStoreService, never()).isEntityPresent(IDMEntityType.USER, "root");
  }

  /**
   * Mocks the internal store with its default case-insensitive user search
   * (exo.idm.search.notCaseSensitive=true): a username lookup ignores the
   * letter case and returns the user with the username it was stored with.
   */
  private void mockMembershipStores(String... internalUsernames) throws Exception {
    group = new GroupImpl("G1");
    ((GroupImpl) group).setId(GROUP_ID);
    group.setOriginatingStore(OrganizationService.EXTERNAL_STORE);
    GroupHandler groupHandler = mock(GroupHandler.class);
    when(organizationService.getGroupHandler()).thenReturn(groupHandler);
    when(groupHandler.findGroupById(GROUP_ID)).thenReturn(group);
    membershipHandler = mock(MembershipHandler.class);
    when(organizationService.getMembershipHandler()).thenReturn(membershipHandler);
    lenient().when(idmExternalStoreService.isEntityPresent(IDMEntityType.GROUP_MEMBERSHIPS, GROUP_ID)).thenReturn(true);
    MembershipTypeHandler membershipTypeHandler = mock(MembershipTypeHandler.class);
    lenient().when(organizationService.getMembershipTypeHandler()).thenReturn(membershipTypeHandler);
    membershipType = new MembershipTypeImpl("member", null, null);
    lenient().when(membershipTypeHandler.findMembershipType("member")).thenReturn(membershipType);

    internalUsers = new HashMap<>();
    for (String internalUsername : internalUsernames) {
      internalUsers.put(internalUsername.toLowerCase(), externalUser(internalUsername));
    }
    when(userHandler.findUserByName(anyString(),
                                    eq(UserStatus.ANY))).thenAnswer(invocation -> internalUsers.get(invocation.getArgument(0,
                                                                                                                   String.class)
                                                                                                    .toLowerCase()));
  }

  private User externalUser(String username) {
    User user = new UserImpl(username);
    user.setOriginatingStore(OrganizationService.EXTERNAL_STORE);
    return user;
  }

  private Membership membership(String username) {
    return new MembershipImpl("member", username, GROUP_ID, false);
  }

}

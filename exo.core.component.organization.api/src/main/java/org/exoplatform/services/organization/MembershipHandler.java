/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.services.organization;

import org.exoplatform.commons.utils.ListAccess;

import java.util.Collection;
import java.util.List;

/**
 * Created by The eXo Platform SAS<br>
 * This interface is a sub part of the organization service.
 * It is used to manage the memberships -
 * the relation of user , group, and membership type - and broadcast the
 * membership events to all the registered listeners in the organization service.
 * The membership event can be: new linked membership and delete the membership
 * type event. Each event should have 2 phases: pre event and post event. The
 * method linkMembership(..) and removeMembership(..) broadcast the event at
 * each phase so the listeners can handle the event properly
 * @author <a href="mailto:tuan08@users.sourceforge.net">Tuan Nguyen</a>
 * @LevelAPI Platform
 */
public interface MembershipHandler
{
   /**
    * @deprecated This method should not be called, use the linkMembership
    *             instead.
    */
   Membership createMembershipInstance();

   /**
    * @deprecated This method should no be called, use the linkMembership(..)
    *             instead
    */
   void createMembership(Membership m, boolean broadcast) throws Exception;

   /**
    * Use this method to create a membership record, a relation of the user,
    * group and membership type. Doesn't throw an Exception if membership record with the 
    * same user, group and membership type exists.
    * 
    * @param user The user of the membership
    * @param group The group of the membership
    * @param m The MembershipType of the membership
    * @param broadcast Broadcast the event if the value of the broadcast is
    *          'true'
    * @throws Exception An exception is thrown if the method is fail to access
    *           the database, membership type not existed or any listener fail to handle the event.
    */
   void linkMembership(User user, Group group, MembershipType m, boolean broadcast) throws Exception;

   /**
    * Use this method to remove a membership. Usually you need to call the method
    * findMembershipByUserGroupAndType(..) to find the membership and remove.
    * 
    * @param id The id of the membership
    * @param broadcast Broadcast the event to the registered listeners if the
    *          broadcast event is 'true'
    * @return The membership object which has been removed from the database
    * @throws Exception An exception is thrown if the method cannot access the
    *           database or any listener fail to handle the event.
    */
   Membership removeMembership(String id, boolean broadcast) throws Exception;

   /**
    * Use this method to remove all user's membership.
    * 
    * @param username The username which user object need remove memberships
    * @param broadcast Broadcast the event to the registered listeners if the
    *          broadcast event is 'true'
    * @return The membership object which has been removed from the database
    * @throws Exception An exception is thrown if the method cannot access the
    *           database or any listener fail to handle the event.
    */
   Collection<Membership> removeMembershipByUser(String username, boolean broadcast) throws Exception;

   /**
    * Use this method to search for an membership record with the given id
    * 
    * @param id The id of the membership
    * @return Return The membership object that matched the id
    * @throws Exception An exception is thrown if the method fail to access the
    *           database or no membership is found.
    */
   Membership findMembership(String id) throws Exception;

   /**
    * Use this method to search for a specific membership type of an user in a
    * group.
    * 
    * @param userName The username of the user.
    * @param groupId The group identifier
    * @param type The membership type
    * @return Null if no such membership record or a membership object.
    * @throws Exception Usually an exception is thrown if the method cannot
    *           access the database
    */
   Membership findMembershipByUserGroupAndType(String userName, String groupId, String type) throws Exception;

   /**
    * Use this method to find all the memberships of an user in a group
    * 
    * @param userName
    * @param groupId
    * @return A collection of the membership of an user in a group. The
    *         collection cannot be null and the collection should be empty is no
    *         membership is found
    * @throws Exception Usually an exception is thrown if the method cannot
    *           access the database.
    */
   Collection<Membership> findMembershipsByUserAndGroup(String userName, String groupId) throws Exception;

   /**
    * Use this method to find all the memberships of an user in any group.
    * 
    * @param userName
    * @return A collection of the membership. The collection cannot be null and
    *         if no membership is found , the collection should be empty
    * @throws Exception Usually an exception is thrown if the method cannot
    *           access the database.
    */
   Collection<Membership> findMembershipsByUser(String userName) throws Exception;

   /**
    * Use this method to find all the membership in a group. Note that an user
    * can have more than one membership in a group. For example , user admin can
    * have membership 'member' and 'admin' in the group '/users'
    * 
    * @param group
    * @return A collection of the memberships. The collection cannot be none and
    *         empty if no membership is found.
    * @throws Exception
    * @deprecated This method should no be called, use {@link MembershipHandler#findAllMembershipsByGroup(Group)}
    *             instead
    */
   Collection<Membership> findMembershipsByGroup(Group group) throws Exception;

   /**
    * Use this method to find all the membership in a group. Note that an user
    * can have more than one membership in a group. For example , user admin can
    * have membership 'member' and 'admin' in the group '/users'
    * 
    * @param group
    * @return the list of the memberships
    * @throws Exception
    */
   ListAccess<Membership> findAllMembershipsByGroup(Group group) throws Exception;

   /**
    * Use this method to find all the membership in a group. Note that an user
    * can have more than one membership in a group. For example , user admin can
    * have membership 'member' and 'admin' in the group '/users'
    * 
    * @param user
    * @return the list of the memberships
    * @throws Exception
    */
   default ListAccess<Membership> findAllMembershipsByUser(User user) throws Exception {
     throw new UnsupportedOperationException();
   }
   /**
    * Use this method to find all the membershipTypes in a group.
    *
    * @param groupId ID of the group
    * @return the list of the membershipTypes
    * @throws Exception
    */
   default List<MembershipType> findMembershipTypesByGroup(String groupId) throws Exception {
     throw new UnsupportedOperationException();
   }

   /**
    * Use this method to register a membership event listener.
    * 
    * @param listener the listener instance.
    */
   void addMembershipEventListener(MembershipEventListener listener);

   /**
    * Use this method to unregister a membership event listener.
    * 
    * @param listener the listener instance.
    */
   void removeMembershipEventListener(MembershipEventListener listener);
}

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
package org.exoplatform.services.security;

import org.exoplatform.container.component.ThreadContext;
import org.exoplatform.container.component.ThreadContextHolder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * An object which stores all information about the state of the current user.
 */
public class ConversationState implements ThreadContextHolder
{

   /**
    * "subject".
    */
   public static final String SUBJECT = "subject";

   /**
    * ThreadLocal keeper for ConversationState.
    */
   private static ThreadLocal<ConversationState> current = new ThreadLocal<>();

   /**
    * See {@link Identity}.
    */
   private Identity identity;

   /**
    * Additions attributes of ConversationState.
    */
   private HashMap<String, Object> attributes;

   public ConversationState(Identity identity)
   {
      this.identity = identity;
      this.attributes = new HashMap<>();
   }

   /**
    * @return current ConversationState or null if it was not preset
    */
   public static ConversationState getCurrent()
   {
      return current.get();
   }

   /**
    * Preset current ConversationState.
    *
    * @param state ConversationState
    */
   public static void setCurrent(ConversationState state)
   {
      if (state == null) {
        current.remove();
      } else {
        current.set(state);
      }
   }

   /**
    * @return Identity  the user identity object
    */
   public Identity getIdentity()
   {
      return identity;
   }

   /**
    * sets attribute.
    *
    * @param name the name of the attribute to set
    * @param value the value of the attribute to set
    */
   public void setAttribute(String name, Object value)
   {
      this.attributes.put(name, value);
   }

   /**
    * @param name the name of the attribute to retrieve
    * @return the value of the attribute to retrieve
    */
   public Object getAttribute(String name)
   {
      return this.attributes.get(name);
   }

   /**
    * Returns unmodifiable set of attribute names.
    * 
    * @return all attribute names
    */
   public Set<String> getAttributeNames()
   {
      return Collections.unmodifiableSet(attributes.keySet());
   }

   /**
    * removes attribute.
    *
    * @param name the name of the attribute to remove
    */
   public void removeAttribute(String name)
   {
      this.attributes.remove(name);
   }

   /**
    * {@inheritDoc}
    */
   public ThreadContext getThreadContext()
   {
      return new ThreadContext(current);
   }

  @Override
  public int hashCode() {
    return Objects.hash(attributes, identity);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (obj == null) {
      return false;
    } else if (getClass() != obj.getClass()) {
      return false;
    } else {
      ConversationState other = (ConversationState) obj;
      return Objects.equals(attributes, other.attributes) && Objects.equals(identity, other.identity);
    }
  }

  @Override
  public String toString() {
    return "ConversationState [identity=" + identity + ", attributes=" + attributes + "]";
  }

}

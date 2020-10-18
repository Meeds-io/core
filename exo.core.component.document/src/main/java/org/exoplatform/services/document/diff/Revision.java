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
package org.exoplatform.services.document.diff;

import java.util.List;

public interface Revision extends ToString
{

   /**
    * Adds a delta to this revision.
    * 
    * @param delta the {@link Delta Delta} to add.
    */
   public void addDelta(Delta delta);

   /**
    * Adds a delta to the start of this revision.
    * 
    * @param delta the {@link Delta Delta} to add.
    */
   public void insertDelta(Delta delta);

   /**
    * Retrieves a delta from this revision by position.
    * 
    * @param i the position of the delta to retrieve.
    * @return the specified delta
    */
   public Delta getDelta(int i);

   /**
    * Returns the number of deltas in this revision.
    * 
    * @return the number of deltas.
    */
   public int size();

   /**
    * Applies the series of deltas in this revision as patches to the given text.
    * 
    * @param src the text to patch, which the method doesn't change.
    * @return the resulting text after the patches have been applied.
    * @throws Exception if any of the patches cannot be applied.
    */
   public Object[] patch(Object[] src) throws Exception;

   /**
    * Applies the series of deltas in this revision as patches to the given text.
    * 
    * @param target the text to patch.
    * @throws Exception if any of the patches cannot be applied.
    */
   public void applyTo(List target) throws Exception;

   /**
    * Converts this revision into its Unix diff style string representation.
    * 
    * @param s a {@link StringBuffer StringBuffer} to which the string
    *          representation will be appended.
    */
   public void toString(StringBuffer s);

   /**
    * Converts this revision into its RCS style string representation.
    * 
    * @param s a {@link StringBuffer StringBuffer} to which the string
    *          representation will be appended.
    * @param EOL the string to use as line separator.
    */
   public void toRCSString(StringBuffer s, String EOL);

   /**
    * Converts this revision into its RCS style string representation.
    * 
    * @param s a {@link StringBuffer StringBuffer} to which the string
    *          representation will be appended.
    */
   public void toRCSString(StringBuffer s);

   /**
    * Converts this delta into its RCS style string representation.
    * 
    * @param EOL the string to use as line separator.
    */
   public String toRCSString(String EOL);

   /**
    * Converts this delta into its RCS style string representation using the
    * default line separator.
    */
   public String toRCSString();

   /**
    * Accepts a visitor.
    * 
    * @param visitor the {@link RevisionVisitor} visiting this instance
    */
   public void accept(RevisionVisitor visitor);

}

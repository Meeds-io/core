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
package org.exoplatform.services.document.impl.diff;

import org.exoplatform.services.document.diff.ChangeDelta;
import org.exoplatform.services.document.diff.RevisionVisitor;

import java.util.List;

/**
 * Holds an change-delta between to revisions of a text.
 * 
 * @version $Id: ChangeDeltaImpl.java 5799 2006-05-28 17:55:42Z geaz $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @see DeltaImpl
 * @see DiffServiceImpl
 * @see ChunkImpl
 */
public class ChangeDeltaImpl extends DeltaImpl implements ChangeDelta
{

   ChangeDeltaImpl()
   {
      super();
   }

   public ChangeDeltaImpl(ChunkImpl orig, ChunkImpl rev)
   {
      init(orig, rev);
   }

   public void verify(List target) throws Exception
   {
      if (!original.verify(target))
      {
         throw new IllegalStateException("target isn't correct");
      }
      if (original.first() > target.size())
      {
         throw new IllegalStateException("original.first() > target.size()");
      }
   }

   public void applyTo(List target)
   {
      original.applyDelete(target);
      revised.applyAdd(original.first(), target);
   }

   public void toString(StringBuffer s)
   {
      original.rangeString(s);
      s.append("c");
      revised.rangeString(s);
      s.append(DiffServiceImpl.NL);
      original.toString(s, "< ", "\n");
      s.append("---");
      s.append(DiffServiceImpl.NL);
      revised.toString(s, "> ", "\n");
   }

   public void toRCSString(StringBuffer s, String EOL)
   {
      s.append("d");
      s.append(original.rcsfrom());
      s.append(" ");
      s.append(original.size());
      s.append(EOL);
      s.append("a");
      s.append(original.rcsto());
      s.append(" ");
      s.append(revised.size());
      s.append(EOL);
      revised.toString(s, "", EOL);
   }

   public void accept(RevisionVisitor visitor)
   {
      visitor.visit(this);
   }
}

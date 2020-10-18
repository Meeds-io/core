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

import org.exoplatform.services.document.diff.Delta;
import org.exoplatform.services.document.diff.DiffService;
import org.exoplatform.services.document.diff.Revision;
import org.exoplatform.services.document.diff.RevisionVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A Revision holds the series of deltas that describe the differences between
 * two sequences.
 * 
 * @version $Revision: 1.8 $ $Date: 2003/10/13 08:00:24 $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @author <a href="mailto:bwm@hplb.hpl.hp.com">Brian McBride</a>
 * @see DeltaImpl
 * @see DiffServiceImpl
 * @see ChunkImpl
 * @see RevisionImpl modifications 27 Apr 2003 bwm Added visitor pattern Visitor
 *      interface and accept() method.
 */

public class RevisionImpl extends ToStringImpl implements Revision
{

   List deltas_ = new LinkedList();

   /**
    * Creates an empty Revision.
    */
   public RevisionImpl()
   {
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.diff.Rev#addDelta(org.exoplatform.services.diff
    * .Delta)
    */
   public synchronized void addDelta(Delta delta)
   {
      if (delta == null)
      {
         throw new IllegalArgumentException("new delta is null");
      }
      deltas_.add(delta);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.diff.Rev#insertDelta(org.exoplatform.services.
    * diff.Delta)
    */
   public synchronized void insertDelta(Delta delta)
   {
      if (delta == null)
      {
         throw new IllegalArgumentException("new delta is null");
      }
      deltas_.add(0, delta);
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#getDelta(int)
    */
   public DeltaImpl getDelta(int i)
   {
      return (DeltaImpl)deltas_.get(i);
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#size()
    */
   public int size()
   {
      return deltas_.size();
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#patch(java.lang.Object[])
    */
   public Object[] patch(Object[] src) throws Exception
   {
      List target = new ArrayList(Arrays.asList(src));
      applyTo(target);
      return target.toArray();
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#applyTo(java.util.List)
    */
   public synchronized void applyTo(List target) throws Exception
   {
      ListIterator i = deltas_.listIterator(deltas_.size());
      while (i.hasPrevious())
      {
         Delta delta = (Delta)i.previous();
         delta.patch(target);
      }
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#toString(java.lang.StringBuffer)
    */
   public synchronized void toString(StringBuffer s)
   {
      Iterator i = deltas_.iterator();
      while (i.hasNext())
      {
         ((Delta)i.next()).toString(s);
      }
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#toRCSString(java.lang.StringBuffer,
    * java.lang.String)
    */
   public synchronized void toRCSString(StringBuffer s, String EOL)
   {
      Iterator i = deltas_.iterator();
      while (i.hasNext())
      {
         ((Delta)i.next()).toRCSString(s, EOL);
      }
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#toRCSString(java.lang.StringBuffer)
    */
   public void toRCSString(StringBuffer s)
   {
      toRCSString(s, DiffService.NL);
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#toRCSString(java.lang.String)
    */
   public String toRCSString(String EOL)
   {
      StringBuffer s = new StringBuffer();
      toRCSString(s, EOL);
      return s.toString();
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.diff.Rev#toRCSString()
    */
   public String toRCSString()
   {
      return toRCSString(DiffService.NL);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.diff.Rev#accept(org.exoplatform.services.diff.
    * RevisionVisitor)
    */
   public void accept(RevisionVisitor visitor)
   {
      visitor.visit(this);
      Iterator iter = deltas_.iterator();
      while (iter.hasNext())
      {
         ((Delta)iter.next()).accept(visitor);
      }
   }

}

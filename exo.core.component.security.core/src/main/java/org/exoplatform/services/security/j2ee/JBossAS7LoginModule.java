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
package org.exoplatform.services.security.j2ee;

import javax.security.auth.login.LoginException;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 20.09.2012
 * 
 * @author <a href="mailto:dvishinskiy@exoplatform.com">Dmitriy Vishinskiy</a>
 * @version $Id: JBossAS7LoginModule.java 76870 2012-09-20 10:38:54Z dkuleshov $
 */

public class JBossAS7LoginModule extends JbossLoginModule
{
   public boolean logout() throws LoginException
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("In logout of JBossAS7LoginModule.");
      }
      return true;
   }
}

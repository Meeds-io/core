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
package org.exoplatform.services.security;

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.security.jaas.BasicCallbackHandler;
import org.exoplatform.services.security.jaas.DigestCallbackHandler;

import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginContext;

/**
 * Created y the eXo platform team User: Benjamin Mestrallet Date: 28 avr. 2004
 */
public class TestLoginModule extends TestCase
{

   protected ConversationRegistry conversationRegistry;

   protected IdentityRegistry identityRegistry;

   protected Authenticator authenticator;

   public TestLoginModule(String name)
   {
      super(name);
   }

   protected void setUp() throws Exception
   {

      if (conversationRegistry == null)
      {
         URL containerConfURL = TestLoginModule.class.getResource("/conf/standalone/test-configuration.xml");
         assertNotNull(containerConfURL);
         String containerConf = containerConfURL.toString();
         URL loginConfURL = TestLoginModule.class.getResource("/login.conf");
         assertNotNull(loginConfURL);
         String loginConf = loginConfURL.toString();
         StandaloneContainer.addConfigurationURL(containerConf);
         if (System.getProperty("java.security.auth.login.config") == null)
            System.setProperty("java.security.auth.login.config", loginConf);

         StandaloneContainer manager = StandaloneContainer.getInstance();

         authenticator = (DummyAuthenticatorImpl)manager.getComponentInstanceOfType(DummyAuthenticatorImpl.class);
         assertNotNull(authenticator);
         conversationRegistry = (ConversationRegistry)manager.getComponentInstanceOfType(ConversationRegistry.class);
         assertNotNull(conversationRegistry);
         identityRegistry = (IdentityRegistry)manager.getComponentInstanceOfType(IdentityRegistry.class);
         assertNotNull(identityRegistry);

      }
      identityRegistry.clear();
      conversationRegistry.clear();
   }

   public void testBasicLogin() throws Exception
   {
      BasicCallbackHandler handler = new BasicCallbackHandler("exo", "exo".toCharArray());
      LoginContext loginContext = new LoginContext("exo", handler);
      loginContext.login();

      assertNotNull(identityRegistry.getIdentity("exo"));
      assertEquals("exo", identityRegistry.getIdentity("exo").getUserId());

      assertEquals(1, identityRegistry.getIdentity("exo").getGroups().size());

      StateKey key = new SimpleStateKey("exo");
      conversationRegistry.register(key, new ConversationState(identityRegistry.getIdentity("exo")));
      assertNotNull(conversationRegistry.getState(key));

   }

   /**
    * Here we test Digest Authorization. We artificially create a password context, to emulate 
    * Authorize request environment. Than we login and expect to have "exo" identity registered 
    * and corresponding group created. More information about Digest Authorization is settled 
    * <a href=http://www.apps.ietf.org/rfc/rfc2617.html>here</a>.  
    * @throws Exception
    */
   public void testDigestLogin() throws Exception
   {
      /**
       * Number of hex digits.
       * Hex digits are needed to encode A2, A1 elements as defined in RFC-2617.
       */
      int HASH_HEX_LENGTH = 32;

      /**
       * Here we are going to keep all password context information
       */
      Map<String, String> passwordContext = new HashMap<String, String>();

      passwordContext.put("realmName", "eXo REST services");
      passwordContext.put("nonce", "2c613333aa4cc017d358c09f61977718");
      passwordContext.put("cnonce", "bFaGgjcb+QP47nzPpxtonQ28Kgbz22WsBqmKjHU49q9=");
      passwordContext.put("qop", "auth");
      passwordContext.put("nc", "00000001");
      passwordContext.put("response", "303c5080ac28ed876ea138d207fdf2cd");

      // encrypt A2 string using MD5
      String md5a2 = "Method:" + "/rest/jcr/repository/production";
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(md5a2.getBytes());

      // encode encrypted A2 string using HEX digits
      byte[] bin = md.digest();
      StringBuffer tmpStr = new StringBuffer(HASH_HEX_LENGTH);
      int digit;
      for (int i = 0; i < HASH_HEX_LENGTH / 2; i++)
      {
         digit = (bin[i] >> 4) & 0xf;
         tmpStr.append(Integer.toHexString(digit));
         digit = bin[i] & 0xf;
         tmpStr.append(Integer.toHexString(digit));

      };

      passwordContext.put("md5a2", tmpStr.toString());

      DigestCallbackHandler handler = new DigestCallbackHandler("exo", "exo".toCharArray(), passwordContext);
      LoginContext loginContext = new LoginContext("exo", handler);
      loginContext.login();

      assertNotNull(identityRegistry.getIdentity("exo"));
      assertEquals("exo", identityRegistry.getIdentity("exo").getUserId());

      assertEquals(1, identityRegistry.getIdentity("exo").getGroups().size());

      StateKey key = new SimpleStateKey("exo");
      conversationRegistry.register(key, new ConversationState(identityRegistry.getIdentity("exo")));
      assertNotNull(conversationRegistry.getState(key));

   }

}

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
package org.exoplatform.services.script.groovy.jarjar;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.exoplatform.commons.utils.SecurityHelper;

import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A groovy class loader that performs jar jar operations.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JarJarClassLoader extends GroovyClassLoader
{

   private Package root = new Package();

   public JarJarClassLoader()
   {
   }

   public JarJarClassLoader(ClassLoader classLoader)
   {
      super(classLoader);
   }

   public JarJarClassLoader(GroovyClassLoader groovyClassLoader)
   {
      super(groovyClassLoader);
   }

   public JarJarClassLoader(ClassLoader classLoader, CompilerConfiguration compilerConfiguration, boolean b)
   {
      super(classLoader, compilerConfiguration, b);
   }

   public JarJarClassLoader(ClassLoader classLoader, CompilerConfiguration compilerConfiguration)
   {
      super(classLoader, compilerConfiguration);
   }

   public void addMapping(List<String> source, List<String> destination)
   {
      root.add(source, destination);
   }

   public void addMapping(Map<String, String> mapping)
   {
      for (Map.Entry<String, String> entry : mapping.entrySet())
      {
         addMapping(entry.getKey(), entry.getValue());
      }
   }

   public void addMapping(String source, String destination)
   {
      List<String> sourcePackage = Arrays.asList(source.split("\\."));
      List<String> destinationPackage = Arrays.asList(destination.split("\\."));
      root.add(sourcePackage, destinationPackage);
   }

   @Override
   protected CompilationUnit createCompilationUnit(final CompilerConfiguration compilerConfiguration,
      final CodeSource codeSource)
   {
      //
      final CompilationUnit unit = SecurityHelper.doPrivilegedAction(new PrivilegedAction<CompilationUnit>()
      {
         public CompilationUnit run()
         {
            return JarJarClassLoader.super.createCompilationUnit(compilerConfiguration, codeSource);
         }
      });

      //
      unit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation()
      {
         @Override
         public void call(SourceUnit sourceUnit, GeneratorContext generatorContext, ClassNode classNode)
            throws CompilationFailedException
         {

            //
            ModuleNode module = classNode.getModule();

            //
            for (Iterator<ImportNode> i = module.getImports().iterator(); i.hasNext();)
            {
               ImportNode importNode = i.next();
               ClassNode cn = importNode.getType();
               String s = cn.getPackageName();
               List<String> ss = root.map2(s);
               if (ss != null)
               {
                  StringBuilder sb = new StringBuilder();
                  for (String n : ss)
                  {
                     sb.append(n).append('.');
                  }
                  sb.append(cn.getNameWithoutPackage());
                  String name = sb.toString();
                  cn.setName(name);
               }
            }

            //
            JarJarExpressionTransformer visitor = new JarJarExpressionTransformer(sourceUnit, root);
            visitor.visitClass(classNode);
         }
      }, Phases.CONVERSION);

      return unit;
   }

   static protected JarJarClassLoader createJarJarClassLoaderInPrivilegedMode(final ClassLoader classLoader)
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<JarJarClassLoader>()
      {
         public JarJarClassLoader run()
         {
            return new JarJarClassLoader(classLoader);
         }
      });
   }
}

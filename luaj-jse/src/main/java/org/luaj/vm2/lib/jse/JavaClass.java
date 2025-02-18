/*----------------------------------------------------------------------------
* Copyright (c) 2011 Luaj.org. All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*----------------------------------------------------------------------------*/
package org.luaj.vm2.lib.jse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.luaj.vm2.LuaValue;

/**
 * LuaValue that represents a Java class.
 * <p>
 * Will respond to get() and set() by returning field values, or java methods.
 * <p>
 * This class is not used directly. It is returned by calls to
 * {@link CoerceJavaToLua#coerce(Object)} when a Class is supplied.
 *
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
class JavaClass extends JavaInstance implements CoerceJavaToLua.Coercion {

  static final Map<Class<?>, JavaClass> classes = Collections.synchronizedMap(new HashMap<>());

  static final LuaValue NEW = valueOf("new");

  Map<LuaValue, Field> fields;
	Map<LuaValue, LuaValue> methods;
	Map<LuaValue, Class<?>> innerclasses;

  static JavaClass forClass(Class<?> c) {
    JavaClass j = classes.get(c);
    if (j == null)
      classes.put(c, j = new JavaClass(c));
    return j;
  }

  JavaClass(Class<?> c) {
    super(c);
    this.jclass = this;
  }

  @Override
  public LuaValue coerce(Object javaValue) {
    return this;
  }

  Field getField(LuaValue key) {
    if (fields == null) {
      Map<LuaValue, Field> m = new HashMap<>();
      Field[] f = ((Class<?>) m_instance).getFields();
      for (Field fi : f) {
        if (Modifier.isPublic(fi.getModifiers())) {
          m.put(LuaValue.valueOf(fi.getName()), fi);
          try {
            if (!fi.isAccessible())
              fi.setAccessible(true);
          } catch (SecurityException ignored) {
          }
        }
      }
      fields = m;
    }
    return fields.get(key);
  }

  LuaValue getMethod(LuaValue key) {
    if (methods == null) {
      Map<String, List<JavaMethod>> namedlists = new HashMap<>();
      Method[] m = ((Class<?>) m_instance).getMethods();
      for (Method mi : m) {
        if (Modifier.isPublic(mi.getModifiers())) {
          String name = mi.getName();
          List<JavaMethod> list = namedlists.computeIfAbsent(name, k -> new ArrayList<>());
          list.add(JavaMethod.forMethod(mi));
        }
      }
      Map<LuaValue, LuaValue> map = new HashMap<>();
      Constructor<?>[] c = ((Class<?>) m_instance).getConstructors();
      List<JavaConstructor> list = new ArrayList<>();
      for (Constructor<?> element : c)
        if (Modifier.isPublic(element.getModifiers()))
          list.add(JavaConstructor.forConstructor(element));
      switch (list.size()) {
      case 0:
        break;
      case 1:
        map.put(NEW, list.get(0));
        break;
      default:
        map.put(NEW, JavaConstructor
          .forConstructors(list.toArray(new JavaConstructor[0])));
        break;
      }

      for (Entry<String, List<JavaMethod>> stringListEntry : namedlists.entrySet()) {
        String name = stringListEntry.getKey();
        List<JavaMethod> methods = stringListEntry.getValue();
        map.put(LuaValue.valueOf(name), methods.size() == 1 ? methods.get(0)
            : JavaMethod.forMethods(
                methods.toArray(new JavaMethod[0])));
      }
      methods = map;
    }
    return methods.get(key);
  }

  Class<?> getInnerClass(LuaValue key) {
    if (innerclasses == null) {
      Map<LuaValue, Class<?>> m = new HashMap<>();
      Class<?>[] c = ((Class<?>) m_instance).getClasses();
      for (Class<?> ci : c) {
        String name = ci.getName();
        String stub = name.substring(Math.max(name.lastIndexOf('$'), name.lastIndexOf('.'))+1);
        m.put(LuaValue.valueOf(stub), ci);
      }
      innerclasses = m;
    }
    return innerclasses.get(key);
  }

  public LuaValue getConstructor() { return getMethod(NEW); }
}

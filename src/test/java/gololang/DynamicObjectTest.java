/*
 * Copyright 2012-2013 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gololang;

import org.testng.annotations.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;

import static java.lang.invoke.MethodType.genericMethodType;
import static java.lang.invoke.MethodType.methodType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.fail;

public class DynamicObjectTest {

  static Object same(Object receiver, Object a, Object b) {
    return a == b;
  }

  static Object fallback(MutableCallSite callsite, String name, Object[] args) throws Throwable {
    DynamicObject object = (DynamicObject) args[0];
    MethodType type = callsite.type();
    MethodHandle fallback = FALLBACK
        .bindTo(callsite)
        .bindTo(name)
        .asCollector(Object[].class, type.parameterCount())
        .asType(type);
    MethodHandle newTarget = object.plug(name, type, fallback);
    callsite.setTarget(newTarget);
    return newTarget.invokeWithArguments(args);
  }

  static final MethodHandle FALLBACK;

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    try {
      FALLBACK = lookup.findStatic(
          DynamicObjectTest.class,
          "fallback",
          methodType(Object.class, MutableCallSite.class, String.class, Object[].class));
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new Error(e);
    }
  }

  @Test
  public void define_undefine_value() {
    DynamicObject dynamicObject = new DynamicObject();
    assertThat(dynamicObject.get("name"), nullValue());

    dynamicObject.define("name", "Mr Bean");
    assertThat((String) dynamicObject.get("name"), is("Mr Bean"));

    dynamicObject.undefine("name");
    assertThat(dynamicObject.get("name"), nullValue());
  }

  @Test
  public void plug_value() throws Throwable {
    DynamicObject dynamicObject = new DynamicObject();
    dynamicObject.define("name", "Mr Bean");

    MethodType type = genericMethodType(1);
    MutableCallSite callSite = new MutableCallSite(type);
    MethodHandle fallback = FALLBACK
        .bindTo(callSite)
        .bindTo("name")
        .asCollector(Object[].class, 1)
        .asType(type);
    callSite.setTarget(fallback);

    MethodHandle invoker = callSite.dynamicInvoker();
    Object result = invoker.invoke(dynamicObject);
    assertThat(result, instanceOf(String.class));
    assertThat((String) result, is("Mr Bean"));

    dynamicObject.undefine("name");
    assertThat(dynamicObject.get("name"), nullValue());
    try {
      invoker.invoke(dynamicObject);
      fail("Expected NoSuchMethodException");
    } catch (NoSuchMethodException expected) {
    }

    dynamicObject.define("name", "John B Root");
    assertThat((String) invoker.invoke(dynamicObject), is("John B Root"));

    dynamicObject.define("name", "John B Rootz");
    assertThat((String) invoker.invoke(dynamicObject), is("John B Rootz"));
  }

  @Test
  public void plug_value_setter() throws Throwable {
    DynamicObject dynamicObject = new DynamicObject();
    dynamicObject.define("name", "Mr Bean");

    MethodType type = genericMethodType(2);
    MutableCallSite callSite = new MutableCallSite(type);
    MethodHandle fallback = FALLBACK
        .bindTo(callSite)
        .bindTo("name")
        .asCollector(Object[].class, 2)
        .asType(type);
    callSite.setTarget(fallback);

    MethodHandle invoker = callSite.dynamicInvoker();
    Object result = invoker.invoke(dynamicObject, "John B Root");
    assertThat(result, instanceOf(DynamicObject.class));
    assertThat((String) dynamicObject.get("name"), is("John B Root"));

    result = invoker.invoke(dynamicObject, "John B Rootz");
    assertThat(result, instanceOf(DynamicObject.class));
    assertThat((String) dynamicObject.get("name"), is("John B Rootz"));
  }

  @Test
  public void plug_function() throws Throwable {
    MethodType type = genericMethodType(3);
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodHandle same = lookup.findStatic(DynamicObjectTest.class, "same", type);

    DynamicObject dynamicObject = new DynamicObject();
    dynamicObject.define("same", same);

    MutableCallSite callSite = new MutableCallSite(type);
    MethodHandle fallback = FALLBACK
        .bindTo(callSite)
        .bindTo("same")
        .asCollector(Object[].class, 3)
        .asType(type);
    callSite.setTarget(fallback);

    MethodHandle invoker = callSite.dynamicInvoker();
    assertThat((Boolean) invoker.invokeWithArguments(dynamicObject, "a", "a"), is(true));
    assertThat((Boolean) invoker.invokeWithArguments(dynamicObject, "a", "b"), is(false));

    dynamicObject.undefine("same");
    try {
      invoker.invokeWithArguments(dynamicObject, "a", "a");
      fail("Expected NoSuchMethodException");
    } catch (NoSuchMethodException expected) {
    }

    dynamicObject.define("same", same);
    assertThat((Boolean) invoker.invokeWithArguments(dynamicObject, "a", "a"), is(true));
    assertThat((Boolean) invoker.invokeWithArguments(dynamicObject, "a", "b"), is(false));
  }
}

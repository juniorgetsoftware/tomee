/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.bval;

import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.apache.xbean.asm7.AnnotationVisitor;
import org.apache.xbean.asm7.ClassWriter;
import org.apache.xbean.asm7.MethodVisitor;
import org.apache.xbean.asm7.Opcodes;
import org.apache.xbean.asm7.Type;

import javax.validation.Constraint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * We allow CDI and EJB beans to use BeanValidation to validate a JsonWebToken
 * instance by simply creating contstraints and putting them on that method.
 *
 * BeanValidation doesn't "see" them there so we have to generate a class
 * that has the annotations in a place BeanValidation can see.
 *
 * To accomplish this, for every method that has BeanValidation constraints
 * we generate an equivalent method that has those same annotations and
 * returns JsonWebToken.
 *
 * We can then pass the generated method to BeanValidation's
 * ExecutableValidator.validateReturnValue and pass in the JsonWebToken instance
 *
 * The only purpose of this generated class and these generated methods is to
 * make BeanValidation happy.  If BeanValidation added something like this:
 *
 *   getValidator().validate(Object instance, Annotation[] annotations);
 *
 * Then all the code here could be deleted.
 *
 * A short example of the kind of code it generates.
 *
 * This class:
 *
 *    public class Colors {
 *      @Issuer("http://foo.bar.com")
 *      public void red(String foo) {
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public boolean blue(boolean b) {
 *          return b;
 *      }
 *
 *      public void green() {
 *      }
 *    }
 *
 * Would result in this generated class:
 *
 *    public class Colors$$JwtConstraints {
 *
 *      private Colors$$JwtConstraints() {
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public JsonWebToken red$$0() {
 *          return null;
 *      }
 *
 *      @Issuer("http://foo.bar.com")
 *      public JsonWebToken blue$$1() {
 *          return null;
 *      }
 *    }
 *
 */
public class ValidationGenerator implements Opcodes {

    public static byte[] generateFor(final Class<?> target) throws ProxyGenerationException {
        final List<Method> constrainedMethods = getConstrainedMethods(target);

        if (constrainedMethods.size() == 0) return null;

        final Map<String, MethodVisitor> visitors = new HashMap<>();

        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        final String generatedClassName = getName(target).replace('.', '/');

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, generatedClassName, null, "java/lang/Object", null);

        { // public constructor
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }

        int id = 0;
        for (final Method method : constrainedMethods) {
            final String name = method.getName() + "$$" + (id++);

            // Declare a method of return type JsonWebToken for use with
            // a call to BeanValidation's ExecutableValidator.validateReturnValue
            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, name, "()Lorg/eclipse/microprofile/jwt/JsonWebToken;", null, null);

            // Put the method name on the
            final AnnotationVisitor av = mv.visitAnnotation(Type.getDescriptor(Name.class), true);
            av.visit("value", method.toString());
            av.visitEnd();

            // track the MethodVisitor
            // We will later copy over the annotations
            visitors.put(method.getName() + Type.getMethodDescriptor(method), mv);

            // The method will simply return null
            mv.visitCode();
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
        }

        DynamicSubclass.copyMethodAnnotations(target, visitors);

        // This should never be reached, but just in case
        for (final MethodVisitor visitor : visitors.values()) {
            visitor.visitEnd();
        }

        return cw.toByteArray();
    }

    public static String getName(final Class<?> target) {
        return target.getName() + "$$JwtConstraints";
    }

    public static List<Method> getConstrainedMethods(final Class<?> clazz) {
        final List<Method> constrained = new ArrayList<Method>();

        // we could have been doing this long before Streams
        for (Method method : clazz.getMethods())
            for (Annotation annotation : method.getAnnotations())
                if (isConstraint(annotation))
                    constrained.add(method);

        return constrained;
    }

    private static boolean isConstraint(final Annotation annotation) {
        return annotation.annotationType().isAnnotationPresent(Constraint.class);
    }
}

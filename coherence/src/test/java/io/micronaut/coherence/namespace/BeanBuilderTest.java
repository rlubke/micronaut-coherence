/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.coherence.namespace;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.oracle.coherence.common.base.Classes;

import com.tangosol.coherence.config.ParameterList;
import com.tangosol.coherence.config.SimpleParameterList;
import com.tangosol.config.ConfigurationException;
import com.tangosol.config.expression.ParameterResolver;
import com.tangosol.config.expression.SystemPropertyParameterResolver;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Jonathan Knight
 * @since 1.0
 */
@MicronautTest(startApplication = false)
class BeanBuilderTest {

    @Inject ApplicationContext ctx;

    @Inject FooBean fooBean;

    @Test
    void shouldRealizeNamedBean() {
        ParameterResolver resolver = new SystemPropertyParameterResolver();
        ClassLoader loader = Classes.getContextClassLoader();

        BeanBuilder builder = new BeanBuilder(ctx, "Foo");
        Boolean result = builder.realizes(FooBean.class, resolver, loader);
        assertThat(result, is(true));
    }

    @Test
    void shouldNotRealizeUnknowBean() {
        ParameterResolver resolver = new SystemPropertyParameterResolver();
        ClassLoader loader = Classes.getContextClassLoader();

        BeanBuilder builder = new BeanBuilder(ctx, "Bar");
        Boolean result = builder.realizes(FooBean.class, resolver, loader);
        assertThat(result, is(false));
    }

    @Test
    void shouldBuildBean() {
        ParameterResolver resolver = new SystemPropertyParameterResolver();
        ClassLoader loader = Classes.getContextClassLoader();
        ParameterList parameters = new SimpleParameterList();

        BeanBuilder builder = new BeanBuilder(ctx, "Foo");
        Object result = builder.realize(resolver, loader, parameters);
        assertThat(result, is(sameInstance(fooBean)));
    }

    @Test
    void shouldNotBuildUnknownBean() {
        ParameterResolver resolver = new SystemPropertyParameterResolver();
        ClassLoader loader = Classes.getContextClassLoader();
        ParameterList parameters = new SimpleParameterList();

        BeanBuilder builder = new BeanBuilder(ctx, "Bar");
        assertThrows(ConfigurationException.class, () -> builder.realize(resolver, loader, parameters));
    }

    @Singleton
    @Named("Foo")
    static class FooBean {
    }
}

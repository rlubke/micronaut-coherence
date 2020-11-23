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
package io.micronaut.coherence.events;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import com.tangosol.net.events.internal.NamedEventInterceptor;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;

/**
 * A {@link ExecutableMethodProcessor} that processes methods annotated with
 * {@literal @}{@link CoherenceEventListener}.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Singleton
@Context
public class CoherenceEventListenerProcessor implements ExecutableMethodProcessor<CoherenceEventListener> {

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
    }

    /**
     * Return the {@link NamedEventInterceptor} instances created from executable methods
     * that have been annotated with {@literal @}{@link CoherenceEventListener}.
     *
     * @return  the discovered {@link NamedEventInterceptor} instances
     */
    public List<NamedEventInterceptor<?>> getInterceptors() {
        return Collections.emptyList();
    }
}
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
package io.micronaut.coherence;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Context;

/**
 * A utility class to capture the {@link ApplicationContext}
 * so that it is available to Coherence classes that are not
 * managed by Micronaut.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Context
@Singleton
public class CoherenceContext {

    /**
     * The {@link io.micronaut.context.ApplicationContext}.
     */
    private static ApplicationContext ctx;

    /**
     * Create a {@link CoherenceContext}.
     *
     * @param ctx the {@link io.micronaut.context.ApplicationContext}
     */
    @Inject
    CoherenceContext(ApplicationContext ctx) {
        CoherenceContext.ctx = ctx;
    }

    /**
     * Returns the {@link io.micronaut.context.ApplicationContext}.
     *
     * @return the {@link io.micronaut.context.ApplicationContext}
     */
    public static ApplicationContext getApplicationContext() {
        return ctx;
    }
}

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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.event.AnnotatedMapListener;
import com.oracle.coherence.inject.AlwaysFilter;
import com.oracle.coherence.inject.FilterBinding;
import com.oracle.coherence.inject.FilterFactory;
import com.oracle.coherence.inject.WhereFilter;

import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.QueryHelper;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.InjectionPoint;

/**
 * A Micronaut factory for producing {@link com.tangosol.util.Filter} instances.
 *
 * @author Jonathan Knight
 * @since 1.0
 */
@Factory
class FilterFactories implements AnnotatedMapListener.FilterProducer {

    /**
     * The Micronaut bean context.
     */
    private final ApplicationContext ctx;

    /**
     * Create a {@link FilterFactories} that will use the specified bean context.
     *
     * @param ctx the bean context to use
     */
    @Inject
    FilterFactories(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    /**
     * Produce a {@link FilterFactory} that produces an instance of an
     * {@link com.tangosol.util.filter.AlwaysFilter}.
     *
     * @return a {@link FilterFactory} that produces an instance of an
     *         {@link com.tangosol.util.filter.AlwaysFilter}
     */
    @Singleton
    @AlwaysFilter
    FilterFactory<AlwaysFilter, ?> alwaysFactory() {
        return annotation -> Filters.always();
    }

    /**
     * Produce a {@link FilterFactory} that produces an instance of a
     * {@link com.tangosol.util.Filter} created from a CohQL where clause.
     *
     * @return a {@link FilterFactory} that produces an instance of an
     *         {@link com.tangosol.util.Filter} created from a CohQL
     *         where clause
     */
    @Singleton
    @WhereFilter("")
    @SuppressWarnings("unchecked")
    FilterFactory<WhereFilter, ?> whereFactory() {
        return annotation -> {
            String sWhere = annotation.value();
            return sWhere.trim().isEmpty() ? Filters.always() : QueryHelper.createFilter(annotation.value());
        };
    }

    @Prototype
    @SuppressWarnings({"rawtypes", "unchecked"})
    Filter<?> filter(InjectionPoint<?> injectionPoint) {
        AnnotationMetadata metadata = injectionPoint.getAnnotationMetadata();
        List<Class<? extends Annotation>> bindings = metadata.getAnnotationTypesByStereotype(FilterBinding.class);

        for (Class<? extends Annotation> type : bindings) {
            FilterFactory filterFactory = ctx.findBean(FilterFactory.class, new FilterFactoryQualifier(type))
                    .orElse(null);
            if (filterFactory != null) {
                return filterFactory.create(injectionPoint.synthesize(type));
            }
        }

        return Filters.always();
    }

    /**
     * Resolve a {@link Filter} implementation from the specified qualifiers.
     *
     * @param annotations  the qualifiers to use to create the {@link Filter}
     * @param <T>          the type that the {@link Filter} can filter
     *
     * @return a {@link Filter} implementation created from the specified qualifiers.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> Filter<T> resolve(Set<Annotation> annotations) {
        List<Filter<?>> list = new ArrayList<>();

        for (Annotation annotation : annotations) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type.isAnnotationPresent(FilterBinding.class)) {
                FilterFactory factory = ctx.findBean(FilterFactory.class, new FilterFactoryQualifier(type)).orElse(null);
                if (factory != null) {
                    list.add(factory.create(annotation));
                } else {
                    throw new IllegalStateException("Unsatisfied dependency - no FilterFactory bean found annotated with " + annotation);
                }
            }
        }

        Filter[] aFilters = list.toArray(new Filter[0]);

        if (aFilters.length == 0) {
            return Filters.always();
        } else if (aFilters.length == 1) {
            return aFilters[0];
        } else {
            return Filters.all(aFilters);
        }
    }

    /**
     * A {@link Qualifier} that qualifies on {@link FilterFactory} types.
     */
    @SuppressWarnings("rawtypes")
    static class FilterFactoryQualifier implements Qualifier<FilterFactory> {
        private final Class<? extends Annotation> type;

        /**
         * Create a qualifier that matches the specific {@link com.oracle.coherence.inject.FilterFactory} type.
         *
         * @param cls the {@link com.oracle.coherence.inject.FilterFactory} to match
         */
        FilterFactoryQualifier(Class<? extends Annotation> cls) {
            type = cls;
        }

        @Override
        public <BT extends BeanType<FilterFactory>> Stream<BT> reduce(Class<FilterFactory> beanType,
                                                                      Stream<BT> candidates) {
            return candidates.filter(bt -> bt.isAnnotationPresent(type));
        }
    }
}
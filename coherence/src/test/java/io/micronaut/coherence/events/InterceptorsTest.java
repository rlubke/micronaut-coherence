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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.oracle.coherence.common.collections.ConcurrentHashMap;
import com.oracle.coherence.event.CacheName;
import com.oracle.coherence.event.Created;
import com.oracle.coherence.event.Destroyed;
import com.oracle.coherence.event.Executed;
import com.oracle.coherence.event.Executing;
import com.oracle.coherence.event.Inserted;
import com.oracle.coherence.event.MapName;
import com.oracle.coherence.event.Processor;
import com.oracle.coherence.event.Removed;
import com.oracle.coherence.event.ScopeName;
import com.oracle.coherence.event.ServiceName;
import com.oracle.coherence.event.Updated;
import com.oracle.coherence.inject.Name;

import com.tangosol.net.Coherence;
import com.tangosol.net.NamedCache;
import com.tangosol.net.Session;
import com.tangosol.net.events.CoherenceLifecycleEvent;
import com.tangosol.net.events.Event;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.net.events.partition.TransactionEvent;
import com.tangosol.net.events.partition.TransferEvent;
import com.tangosol.net.events.partition.cache.CacheLifecycleEvent;
import com.tangosol.net.events.partition.cache.EntryEvent;
import com.tangosol.net.events.partition.cache.EntryProcessorEvent;
import com.tangosol.util.InvocableMap;

import data.Person;
import data.PhoneNumber;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@MicronautTest(propertySources = "classpath:sessions.yaml")
class InterceptorsTest {

    @Inject
    @Name("test")
    private Session session;

    @Inject
    private TestObservers observers;

    @Test
    void testEventInterceptorMetthods() {
        NamedCache<String, Person> people = session.getCache("people");
        people.put("homer", new Person("Homer", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
        people.put("marge", new Person("Marge", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
        people.put("bart", new Person("Bart", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
        people.put("lisa", new Person("Lisa", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));
        people.put("maggie", new Person("Maggie", "Simpson", LocalDate.now(), new PhoneNumber(1, "555-123-9999")));

        people.invokeAll(new Uppercase());

        people.clear();
        people.truncate();
        people.destroy();

        Coherence.closeAll();

        Set<Enum<?>> events = observers.getEvents();

        events.forEach(System.err::println);

        assertThat(events, hasItem(LifecycleEvent.Type.ACTIVATING));
        assertThat(events, hasItem(LifecycleEvent.Type.ACTIVATED));
        assertThat(events, hasItem(LifecycleEvent.Type.DISPOSING));
        assertThat(events, hasItem(CacheLifecycleEvent.Type.CREATED));
        assertThat(events, hasItem(CacheLifecycleEvent.Type.DESTROYED));
        assertThat(events, hasItem(CacheLifecycleEvent.Type.TRUNCATED));
        assertThat(events, hasItem(TransferEvent.Type.ASSIGNED));
        assertThat(events, hasItem(TransactionEvent.Type.COMMITTING));
        assertThat(events, hasItem(TransactionEvent.Type.COMMITTED));
        assertThat(events, hasItem(EntryProcessorEvent.Type.EXECUTING));
        assertThat(events, hasItem(EntryProcessorEvent.Type.EXECUTED));
        assertThat(events, hasItem(EntryEvent.Type.INSERTING));
        assertThat(events, hasItem(EntryEvent.Type.INSERTED));
        assertThat(events, hasItem(EntryEvent.Type.UPDATING));
        assertThat(events, hasItem(EntryEvent.Type.UPDATED));
        assertThat(events, hasItem(EntryEvent.Type.REMOVING));
        assertThat(events, hasItem(EntryEvent.Type.REMOVED));
        assertThat(events, hasItem(CoherenceLifecycleEvent.Type.STARTED));
        assertThat(events, hasItem(CoherenceLifecycleEvent.Type.STARTING));
        assertThat(events, hasItem(CoherenceLifecycleEvent.Type.STOPPED));
        assertThat(events, hasItem(CoherenceLifecycleEvent.Type.STOPPING));
    }

    /**
     * A simple entry processor to convert a {@link Person} last name to upper case.
     */
    public static class Uppercase implements InvocableMap.EntryProcessor<String, Person, Object> {
        @Override
        public Object process(InvocableMap.Entry<String, Person> entry) {
            Person p = entry.getValue();
            p.setLastName(p.getLastName().toUpperCase());
            entry.setValue(p);
            return null;
        }
    }

    @Singleton
    public static class TestObservers {
        private final Map<Enum<?>, Boolean> events = new ConcurrentHashMap<>();

        Set<Enum<?>> getEvents() {
            Set<Enum<?>> set = new TreeSet<>(Comparator.comparing(Enum::name));
            set.addAll(events.keySet());
            return set;
        }

        // cache lifecycle events
        @CoherenceEventListener
        void onCacheLifecycleEvent(@ServiceName("StorageService") CacheLifecycleEvent event) {
            record(event);
        }

        // Coherence lifecycle events
        @CoherenceEventListener
        void onCoherenceLifecycleEvent(CoherenceLifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onCreatedPeople(@Created @MapName("people") CacheLifecycleEvent event) {
            record(event);
            assertThat(event.getCacheName(), is("people"));
        }

        @CoherenceEventListener
        void onDestroyedPeople(@Destroyed @CacheName("people") CacheLifecycleEvent event) {
            record(event);
            assertThat(event.getCacheName(), is("people"));
        }

        // entry events
        @CoherenceEventListener
        void onEntryEvent(@MapName("people") EntryEvent<String, Person> event) {
            record(event);
        }

        @CoherenceEventListener
        void onExecuted(@Executed @CacheName("people") @Processor(Uppercase.class) EntryProcessorEvent event) {
            record(event);
            assertThat(event.getProcessor(), is(instanceOf(Uppercase.class)));
            assertThat(event.getEntrySet().size(), is(0));
        }

        @CoherenceEventListener
        void onExecuting(@Executing @CacheName("people") @Processor(Uppercase.class) EntryProcessorEvent event) {
            record(event);
            assertThat(event.getProcessor(), is(instanceOf(Uppercase.class)));
            assertThat(event.getEntrySet().size(), is(5));
        }

        // lifecycle events
        @CoherenceEventListener
        void onLifecycleEvent(LifecycleEvent event) {
            record(event);
        }

        @CoherenceEventListener
        void onPersonInserted(@Inserted @CacheName("people") EntryEvent<String, Person> event) {
            record(event);
            assertThat(event.getValue().getLastName(), is("Simpson"));
        }

        @CoherenceEventListener
        void onPersonRemoved(@Removed @CacheName("people") EntryEvent<String, Person> event) {
            record(event);
            assertThat(event.getOriginalValue().getLastName(), is("SIMPSON"));
        }

        @CoherenceEventListener
        void onPersonUpdated(@Updated @CacheName("people") EntryEvent<String, Person> event) {
            record(event);
            assertThat(event.getValue().getLastName(), is("SIMPSON"));
        }

        // transaction events
        @CoherenceEventListener
        void onTransactionEvent(TransactionEvent event) {
            record(event);
        }

        // transfer events
        @CoherenceEventListener
        void onTransferEvent(@ScopeName("Test") @ServiceName("StorageService") TransferEvent event) {
            record(event);
        }

        void record(Event<?> event) {
            events.put(event.getType(), true);
        }
    }
}
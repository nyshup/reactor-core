/*
 * Copyright (c) 2019-Present Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.core;

import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;
import reactor.core.scheduler.NonBlocking;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.ScheduledThreadPoolExecutor;

class ReactorBlockHoundIntegration implements BlockHoundIntegration {

    @Override
    public void applyTo(BlockHound.Builder builder) {
        builder.nonBlockingThreadPredicate(current -> current.or(NonBlocking.class::isInstance));

        // `ScheduledThreadPoolExecutor$DelayedWorkQueue.offer` parks the Thread with Unsafe#park.
        builder.allowBlockingCallsInside(ScheduledThreadPoolExecutor.class.getName(), "scheduleAtFixedRate");

        Schedulers.onScheduleHook("BlockHound", Wrapper::new);
        builder.disallowBlockingCallsInside(Wrapper.class.getName(), "run");
    }

    private static final class Wrapper implements Runnable {

        private final Runnable delegate;

        private Wrapper(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            delegate.run();
        }
    }
}

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

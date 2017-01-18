/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.lightbend.training.coffeehouse;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.FromConfig;
import scala.Serializable;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CoffeeHouse extends AbstractLoggingActor {

    private final FiniteDuration baristaPrepareCoffeeDuration =
            Duration.create(
                    context().system().settings().config().getDuration(
                            "coffee-house.barista.prepare-coffee-duration", MILLISECONDS), MILLISECONDS);

    private final int baristaAccuracy =
            context().system().settings().config().getInt("coffee-house.barista.accuracy");

    private final int waiterMaxComplaintCount =
            context().system().settings().config().getInt("coffee-house.waiter.max-complaint-count");

    private final ActorRef barista =
            createBarista();

    private final ActorRef waiter =
            createWaiter();

    private final Map<ActorRef, Integer> guestCaffeineBookkeeper = new ConcurrentHashMap<>();

    private final int caffeineLimit;

    private SupervisorStrategy strategy = new OneForOneStrategy(false, DeciderBuilder.
            match(Guest.CaffeineException.class, e ->
                    SupervisorStrategy.stop()
            ).
            match(Waiter.FrustratedException.class, (Waiter.FrustratedException e) -> {
                barista.tell(new Barista.PrepareCoffee(e.coffee, e.guest), sender());
                return SupervisorStrategy.restart();
            }).
            matchAny(e -> SupervisorStrategy.restart()).build()
    );

    public CoffeeHouse(int caffeineLimit) {
        log().debug("CoffeeHouse Open");
        this.caffeineLimit = caffeineLimit;

        receive(ReceiveBuilder.
                match(GuestCreated.class, guestCreated -> {
                    // Connect the guest with the waiter
                    guestCreated.guestActorRef.tell(new WaiterServingGuest(waiter), self());
                    // Add guest to book keeping
                    addGuestToBookkeeper(guestCreated.guestActorRef);
                    // Also make sure to watch the guest (now being done remotely)
                    context().watch(guestCreated.guestActorRef);
                }).
                match(ApproveCoffee.class, this::coffeeApproved, approveCoffee ->
                        barista.forward(new Barista.PrepareCoffee(approveCoffee.coffee, approveCoffee.guest), context())
                ).
                match(ApproveCoffee.class, approveCoffee -> {
                    log().info("Sorry, {}, but you have reached your limit.", approveCoffee.guest.path().name());
                    context().stop(approveCoffee.guest);
                }).
                match(Terminated.class, terminated -> {
                    log().info("Thanks, {}, for being our guest!", terminated.getActor());
                    removeGuestFromBookkeeper(terminated.getActor());
                }).
                matchAny(this::unhandled).build()
        );
    }

    public static Props props(int caffeineLimit) {
        return Props.create(CoffeeHouse.class, () -> new CoffeeHouse(caffeineLimit));
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    private boolean coffeeApproved(ApproveCoffee approveCoffee) {
        final int guestCaffeineCount = guestCaffeineBookkeeper.get(approveCoffee.guest);
        if (guestCaffeineCount < caffeineLimit) {
            guestCaffeineBookkeeper.put(approveCoffee.guest, guestCaffeineCount + 1);
            return true;
        }
        return false;
    }

    private void addGuestToBookkeeper(ActorRef guest) {
        guestCaffeineBookkeeper.put(guest, 0);
        log().debug("Guest {} added to bookkeeper", guest);
    }

    private void removeGuestFromBookkeeper(ActorRef guest) {
        guestCaffeineBookkeeper.remove(guest);
        log().debug("Removed guest {} from bookkeeper", guest);
    }

    protected ActorRef createBarista() {
        return context().actorOf(FromConfig.getInstance().props(
                Barista.props(baristaPrepareCoffeeDuration, baristaAccuracy)), "barista");
    }

    protected ActorRef createWaiter() {
        return context().actorOf(Waiter.props(self(), barista, waiterMaxComplaintCount), "waiter");
    }

    public static final class WaiterServingGuest implements Serializable {
        public final ActorRef waiter;

        public WaiterServingGuest(ActorRef waiter) {
            this.waiter = waiter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WaiterServingGuest waiter1 = (WaiterServingGuest) o;

            return waiter != null ? waiter.equals(waiter1.waiter) : waiter1.waiter == null;
        }

        @Override
        public int hashCode() {
            return waiter != null ? waiter.hashCode() : 0;
        }
    }

    public static final class ApproveCoffee implements Serializable {

        public final Coffee coffee;

        public final ActorRef guest;

        public ApproveCoffee(final Coffee coffee, final ActorRef guest) {
            checkNotNull(coffee, "Coffee cannot be null");
            checkNotNull(guest, "Guest cannot be null");
            this.coffee = coffee;
            this.guest = guest;
        }

        @Override
        public String toString() {
            return "ApproveCoffee{"
                    + "coffee=" + coffee + ", "
                    + "guest=" + guest + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof ApproveCoffee) {
                ApproveCoffee that = (ApproveCoffee) o;
                return (this.coffee.equals(that.coffee))
                        && (this.guest.equals(that.guest));
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= coffee.hashCode();
            h *= 1000003;
            h ^= guest.hashCode();
            return h;
        }
    }

    public static final class GuestCreated implements Serializable {
        public final ActorRef guestActorRef;

        public GuestCreated(ActorRef guestActorRef) {
            this.guestActorRef = guestActorRef;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GuestCreated that = (GuestCreated) o;

            return guestActorRef != null ? guestActorRef.equals(that.guestActorRef) : that.guestActorRef == null;
        }

        @Override
        public int hashCode() {
            return guestActorRef != null ? guestActorRef.hashCode() : 0;
        }
    }
}

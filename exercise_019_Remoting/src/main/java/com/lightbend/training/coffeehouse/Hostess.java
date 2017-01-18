/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */
package com.lightbend.training.coffeehouse;

import akka.actor.*;
import akka.japi.pf.ReceiveBuilder;
import scala.Serializable;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Hostess extends AbstractLoggingActor {
    private ActorRef coffeeHouse;

    private final FiniteDuration guestFinishCoffeeDuration =
            Duration.create(
                    context().system().settings().config().getDuration(
                            "coffee-house.guest.finish-coffee-duration", MILLISECONDS), MILLISECONDS);

    public Hostess() {
        // Do not use hard coded values in production.
        context().actorSelection("akka.tcp://coffee-house-system@127.0.0.1:2552/user/coffee-house").tell(new Identify("SomeUniqueCode"), self());

        receive(ReceiveBuilder.
                match(ActorIdentity.class, actorIdentity -> {
                    this.coffeeHouse = actorIdentity.getRef();
                }).
                match(CreateGuest.class, createGuest -> {
                    final ActorRef guest = createGuest(createGuest.favoriteCoffee, createGuest.caffeineLimit);
                    coffeeHouse.tell(new CoffeeHouse.GuestCreated(guest), self());
                }).
                matchAny(this::unhandled).build());
    }

    protected ActorRef createGuest(Coffee favoriteCoffee, int caffeineLimit) {
        return context().actorOf(Guest.props(favoriteCoffee, guestFinishCoffeeDuration, caffeineLimit));
    }

    public static Props props() {
        return Props.create(Hostess.class, () -> new Hostess());
    }

    public static final class CreateGuest implements Serializable {

        public final Coffee favoriteCoffee;

        public final int caffeineLimit;

        public CreateGuest(final Coffee favoriteCoffee, final int caffeineLimit) {
            checkNotNull(favoriteCoffee, "Favorite coffee cannot be null");
            this.favoriteCoffee = favoriteCoffee;
            this.caffeineLimit = caffeineLimit;
        }

        @Override
        public String toString() {
            return "CreateGuest{"
                    + "favoriteCoffee=" + favoriteCoffee + ", "
                    + "caffeineLimit=" + caffeineLimit + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof CreateGuest) {
                CreateGuest that = (CreateGuest) o;
                return (this.favoriteCoffee.equals(that.favoriteCoffee))
                        && (this.caffeineLimit == that.caffeineLimit);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= favoriteCoffee.hashCode();
            h *= 1000003;
            h ^= caffeineLimit;
            return h;
        }
    }
}

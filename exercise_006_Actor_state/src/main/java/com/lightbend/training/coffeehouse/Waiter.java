/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.lightbend.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public class Waiter extends AbstractLoggingActor {

    public Waiter() {

        receive(ReceiveBuilder.
                match(ServeCoffee.class, serveCoffee ->
                        sender().tell(new CoffeeServed(serveCoffee.coffee), self())
                ).
                matchAny(this::unhandled).build()
        );
    }

    public static Props props() {
        return Props.create(Waiter.class, Waiter::new);
    }

    public static final class ServeCoffee {

        public final Coffee coffee;

        public ServeCoffee(final Coffee coffee) {
            checkNotNull(coffee, "Coffee cannot be null");
            this.coffee = coffee;
        }

        @Override
        public String toString() {
            return "ServeCoffee{coffee=" + coffee + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof ServeCoffee) {
                ServeCoffee that = (ServeCoffee) o;
                return this.coffee.equals(that.coffee);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= coffee.hashCode();
            return h;
        }
    }

    public static final class CoffeeServed {

        public final Coffee coffee;

        public CoffeeServed(final Coffee coffee) {
            checkNotNull(coffee, "Coffee cannot be null");
            this.coffee = coffee;
        }

        @Override
        public String toString() {
            return "CoffeeServed{coffee=" + coffee + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof CoffeeServed) {
                CoffeeServed that = (CoffeeServed) o;
                return this.coffee.equals(that.coffee);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int h = 1;
            h *= 1000003;
            h ^= coffee.hashCode();
            return h;
        }
    }
}

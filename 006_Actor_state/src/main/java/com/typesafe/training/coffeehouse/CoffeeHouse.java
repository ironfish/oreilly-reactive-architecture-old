/**
 * Copyright © 2014, 2015 Typesafe, Inc. All rights reserved. [http://www.typesafe.com]
 */

package com.typesafe.training.coffeehouse;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

public class CoffeeHouse extends AbstractLoggingActor{

    private final ActorRef waiter =
        context().actorOf(Waiter.props(), "waiter");

    public CoffeeHouse(){
        log().debug("CoffeeHouse Open");

        receive(ReceiveBuilder.
                match(CreateGuest.class, createGuest ->
                        createGuest(createGuest.favoriteCoffee)
                ).
                matchAny(this::unhandled).build()
        );
    }

    public static Props props(){
        return Props.create(CoffeeHouse.class, CoffeeHouse::new);
    }

    protected void createGuest(Coffee favoriteCoffee){
        context().actorOf(Guest.props(waiter, favoriteCoffee));
    }

    public static final class CreateGuest{

        public final Coffee favoriteCoffee;

        public CreateGuest(final Coffee favoriteCoffee){
            checkNotNull(favoriteCoffee, "Favorite coffee cannot be null");
            this.favoriteCoffee = favoriteCoffee;
        }

        @Override
        public String toString(){
            return "CreateGuest{favoriteCoffee=" + favoriteCoffee + "}";
        }

        @Override
        public boolean equals(Object o){
            if (o == this) return true;
            if (o instanceof CreateGuest) {
                CreateGuest that = (CreateGuest) o;
                return this.favoriteCoffee.equals(that.favoriteCoffee);
            }
            return false;
        }

        @Override
        public int hashCode(){
            int h = 1;
            h *= 1000003;
            h ^= favoriteCoffee.hashCode();
            return h;
        }
    }
}

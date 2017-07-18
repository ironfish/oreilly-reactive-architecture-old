/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.lightbend.training.coffeehouse;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;

public class Guest extends AbstractLoggingActor {

    public Guest() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(this::unhandled).build();
    }

    public static Props props() {
        return Props.create(Guest.class, Guest::new);
    }
}

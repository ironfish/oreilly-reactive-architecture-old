remoting

# Exercise 19 Remoting

In this exercise, we will demonstrate remoting by breaking the application into two different main classes that will be run separately.

_Note: all message classes must implement `Serializable` since we will be sending messages remotely._

- Create a `Hostess` actor that is responsible for creating/getting customers
    - move class `CoffeeHouse.CreateGuest` into `Hostess`
    - `Hostess` should read info from config about `guestFinishCoffeeDuration`
    - the constructor should add a lookup of the coffee house actor (use `ActorSelection` and send an `Identity` message)
    - the `receive` block should handle `ActorIdentity` and `CreateGuest` - make sure to notiofy `CoffeeHouse` about created guests (create a `GuestCreated` message)

- Create a `GuestApp` with the same functionality as `CoffeeHouseApp`
    - remove the functionality to create guests from `CoffeeHouseApp` terminal loop
    - start a `Hostess` actor in `GuestApp` instead of a `CoffeeHouse` actor
    - when the create guest command is issued there should be a message sent to `Hostess` with instructions (`Hostess.CreateGuess`)

- Update `CoffeeHouse`:
    - expect a `Hostess.GuestCreated` message with guest information
    - adding a new message type `WaiterServingGuest` (used to connect the guest with the waiter)

- Update `Guest`:
    - expect a `CoffeeHouse.WaiterServingGuest` with information about the waiter
    - order a coffee when the guest knows its waiter

- Split configuration file into two separate files:
    - `guest.conf` used in `GuestApp` when creating the actor system
    - `coffeehouse.conf` used by `CoffeeHouseApp` when creating the actor system
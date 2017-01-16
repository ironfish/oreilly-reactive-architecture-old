modify-behavior

# Exercise 18 > Modify Behavior

In this exercise, we will demonstrate through the use of `become` and `stash` to modify actor

- Re-implement the `Barista` actors behavior as a finite state machine:
    - Do not use `Thread.sleep` anymore.
    - Use `become`, `stash` and the `scheduler`.
- Use the `run` command to boot the `CoffeeHouseApp` and verify everything works as expected.
- Use the `test` command to verify the solution works as expected.
- Use the `koan next` command to move to the next exercise.

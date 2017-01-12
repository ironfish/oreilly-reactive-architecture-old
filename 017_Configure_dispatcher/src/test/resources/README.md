configure-dispatcher

# Exercise 17 > Configure Dispatcher

In this exercise, we will optimize parallelism through configuring a dispatcher.

- Configure the `default-dispatcher`.
- Use the default `fork-join-executor`.
- Use the `run` command to boot the `CoffeeHouseApp` with different values for `parallelism-max` as follows:
    - Less than the number of cores available.
    - Equal to the number of cores available.
    - More than the number of cores available.
    - Watch the throughput for each scenario.
- Use the `test` command to verify the solution works as expected.
- Use the `koan next` command to move to the next exercise.

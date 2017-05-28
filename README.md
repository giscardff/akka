# Discovering the Actor Model and the Enterprise, All Over Again
* The main **goal** of reactive programming is making _enterprise integration patterns_ easier
* **Actor** shall be the first thing to think about, they receive commands and _generate_ events and that is it
* The **developer** shall care about _actors_ input/output, its state and how to supervise the actor
* Your actor shall _react_ to users and other components
* Your actor shall _react_ to failtures
* Your actor shall _react_ to load peaks
* Your actor shall _react_ to messages

## Reactive Manifesto
* **Reponsive:** Send the response back as earlier as you can
* **Resilient:** Be prepared to handle the failures, having an strategy is most important
* **Elastic:** Your system shall scale up and down based on incoming needs
* **Message Driven:** Be prepated to send/receive _commands_, _documents_ and _events_

## Actor Characteristics
* Conceptual Ones
  * Communicating using asynchronous messaging
  * It has a _finite state machine_
  * Share no data
  * Has no lock
  * Work in parallel with other actors
  * Together they become a system
* Technical Ones
  * Actors can be located on same machine or remotly
  * Actors shall supervise its childs or be supervised by its parents
  * Receive responses assynchronously
  * Stash messages so they can be used later

# Akka Framework
## Imperative programming
* objects concurrency are low performance
* call stack aren't multi threads
* error handler is a pain

##  Main Libs
* supports the implementation of **actors**
* supports **remote** communication
* supports **cluster** and **cluster sharding**
* supports **singleton** pattern
* supports **persistence** of actors and its states
* supports **distributed data** to share it among actors
* supports **streams** of data incoming from a network
* supports **http** communication

### Actors
* Actor have parents, and childs so they can communicate each other



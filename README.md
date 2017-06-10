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

## Location Transparency
* Akka shall be properly configured to work behind NAT or dokers

## Akka and Java Memory Model
* Folows _at most once_ patterns for message delivery
* if you need a guarantee mechanism you can implement somthing on top of it (like SIP over UDP)

## Message Delivery Reliability
* event sourcing
* auto ack
* dead letters

## Configuration
* read configuration from a file and supports callback
* it allows a configuration file _includes_ another one
* logging configuration
* very flexibile, reasone support for unit test and developing mode
* configure specific actors (ex: remote, dispatcher, mailbox, instances, cluster, testkit, persistence, distributed data ...)
* configure actors support wildcards

## Actors
* Actor have a lifecycle
* Actor can be selected by using an ActorSelection
* Actor are created by using an ActorRef
* Actor can monitor or be monitored by other actos about its getting stopped (it is also possible to unwatch an actor)
* Actor can are supervisioned by its parents
* Supports send message assynchronously - **tell/fire-and-forget** - or **ask** for a future or **forward** a message
* It is possible to create timeouts for handling messages
* Stopping an actor gives him the change to process the current message, so use _watch()_ to supervise it and receive the **Terminated** message
* _PoisonPill_ put a message at the end of the queue so the _Actor_ will stop when it times comes by
* Kilggin an actor is put him on his knees right way
* Usage of _Become/Unbecome_ allows to define actor states, so supported message will be different from one state to another
* Actos reincarnation can be performed by _constructors_ or _pre-start_, the former gurantee immutability while the latter gives flexibility to change inner content
* Initialization via _message passing_ isn't recommended

## Supervision Strategy
* Allows to define how errors shall be handled
* It allows OneForOne and AllForOne strategy
* It has maximum failtures accepts per time
* It decides what to do whena failure occurs, including escalate it
* It has default behaviors (see akka documentation)

## Message Dispatchers
* It handles the thread pool which handle the actors and its messages
* Default dispatcher uses a fork-join-executor or a thread-pool-executor
* PinnedDispatcher to have one thread per actor
* CallingThreadDispatcher the thread whom call the actor is the which will handle it (use it for test only)

## Mailboxes
* Each actor has its own mailbox
* It is possible to customize the mailbox for each actor
* SingleConsumerOnlyUnboundedMailbox is an unbounded with a single consumer
* NonBlockingBoundedMailbox used for Multiple-Producer / Single-Consumer queue
* (Un)boundedMailbox is the default one
* (Un)boundedControlAwareMailbox
* (Un)boundedPriorityMailbox
* (Un)boundedStablePriorityMailbox in order to guarantee order for messages with same priority (FIFO)
* It is possible to customize mailboxes

## Routing
* Routes can be used as load balancer for actors
* It is possible to create the routes when getting actor references
* Routes can reference remote actors
* You can choose routing by pool of actors or by group of actors based on its _path_
* Routes can be Broadcast, RoundRobin, Random, Balancing, ScatterGatherFirstCompleted, TailChoppingPool or ConsistentHashingPool
* It is possible to resize the routing pool during routing process itself
* It is possible to create a custom router

## FSM
* Akka allows to create actors as Finite State Machines
* Every state can have a timeout setted
* it allows to monitor the fsm about all state transitions

## Persistence
* Uses Event Source to recovery current state and allows take snapshots of the state to reduce recovery time
* It has pluings for file system and leveldb
* It is possible to _stash_ some data before persist it itself
* Prevnt to use PoisionPill so stash messages are journeled
* It supports replay for corrupted messages
* It supports Actors and FSMs
* Do not rely on java serialization which is the default one used by akka

## Testing Actor Systems
* It has a testkit
* It supports built-in assertions
* It supports to check expected log messages
* It supports timing assertions
* It supports watching actors
* It supports an Auto Pilot
* IT supports parent child tests
* It supports to trace parent invocations
* Supports to use CallingThreadDispatcher and prevent from multi-parallel-stack

## Networking
* Akka supports clustering actors through distinct nodes
* It has a library to support TCP and UDP protocols
* It has a library to support Apache Camel

## Streams
* Reusable Streams, Timed-based processing
* Srouces are things with one output stream
* Sinks are things with one output stream
* Flow are this with exactly one input and one output stream
* BidiFlow: something with exactly two input and two output stream
* Graph: Packages stream
* Difference between error and failure
* _Stream_ is about moving data
* _Element_ is someone that process the stream
* _Back-pressure_ is a method for consumer notify the producer it is ready
* _Non Blocking_ in order to not hinder a thread
* _Graph_ is the stream processing topology (pipe and filter)
* _Processing Stage_ is all the building blocks together
* Supports buffers and flow ratio
* It supports dynamic _graph_ mutation

## Future and Agents
* Uses future to handle answers (like _g7 requests_), it supports ordering, callbaks, onfailures, onsuccess, etc.
* See about Future, CompletionStage and CompletableFuture

## Utilities
* Defines a pattern for an EventBus (publish/subscribe)
* Allows to perform publish/subscribers based on classifiers
* Allows to perform publish/subscribers on event streams
* Loggers and loggings
* Logging level
* Schedulers
* Durations
* Circuit Breakers

## HTTP
* Full server and client http agents
* It has a core and a high level api architecture
* It supports http routes based on paths
* It uses _jackson_ as json serializer
* It supports web socket and https
* Supports gzip and deflate encodings
* Supports timeout
* Supports file upload
* It allows to create _Directives_ which are reusable routes, akka itself comes with a dozen ones
* It works on top of streams

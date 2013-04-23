# Bark

## Overview
Bark is a small RPC-like stack build on Akka IO through [Sentinel](http://github.com/gideondk/sentinel). It's positioned as a light alternative (both in code as performance) to the more featured stacks like [Finagle](https://github.com/twitter/finagle).

Its protocol makes it easy to implement in other programming languages and is loosely modelled to the [BERT-RPC](http://bert-rpc.org/) spec. It uses Erlang's [External Term Format](http://erlang.org/doc/apps/erts/erl_ext_dist.html) to serialise and deserialize content to be send over the line.

The inner workings a build to be as type safe as possible, while the external API makes it easy to be used in other (dynamic) languages. In the future, **Bark** will focus on the creation of higher level reactive services with a small footprint.

## Status
Bark is currently being used in services with low overhead in terms of message sizes. Therefor, Bark currently isn't tested (and not yet optimised) for sending layer payloads through the stack (though theoretically more then possible). For these specific use cases, a higher level Play Iteratee powered API will be developed to react on chunked input. 

In the future the (de)serialisation stack will be extended with more complex types, just as longer running (async) messages will be handled in a less (socket) blocking way (using callbacks or better…)

In overall, treat Bark as alpha software which might change rapidly in the nearby future.

**Currently available in Bark:**

* Easy to use DSL to implement services using multiple modules and functions;
* Full actor based stack through Akka IO and Sentinel;
* Type class based (de)serialisation to Erlang Term Format;
* Separate functionality to create `call` and `cast` messages (request -> response or fire and forget) 
* Supervision (and restart / reconnection functionality) on both server and client for a defined number of workers;

**The following is currently missing in Bark, but will be added soon:**

* Better handling of async messages (the current implementation doesn't block processing, but does block socket communication);
* Better error handling and recovery on client side (by implementing specific `Throwable` types for the different cases);
* Better performance (noack based strategies aren't suited for the protocol, the current ack based implementation in Sentinel is sub-optimal).

## Installation
You can install Bark through source (by publishing it into your local Ivy repository):

```bash
./sbt publish-local
```

Or by adding the repo:
<notextile><pre><code>"gideondk-repo" at "https://raw.github.com/gideondk/gideondk-mvn-repo/master"</code></pre></notextile>

to your SBT configuration and adding the `SNAPSHOT` to your library dependencies:

<notextile><pre><code>libraryDependencies ++= Seq(
	"nl.spotdog" %% "bark" % "0.2.3"
)
</code></pre></notextile>


## Usage
### Server
The implementation of the server is done through the creation of modules and functions. The current best usage is through the extension of the `BarkRouting` trait:

```scala
object CacheServer extends BarkRouting {
	val modules = module("cache") {
		cast("set")((key: String, value: String) ⇒ actor ! SetCache(key, value)) ~
		call("get")((key: String) ⇒ (actor ? GetCache(key)).mapTo[CacheResult].map(_.v.getOrElse("")))
  }
}
```

Within each module, it's possible to create both `cast` as `call` methods, which return nothing or an actual result respective on the function type used. All functions should currently return `Future[T]`, in which `T` is a type for which the `ETFWriter` / `ETFReader` type classes are available. 

Within a module, the `~` function can be used on a single function, to add a other function the the module. The same function can be used to implement multiple modules within the service.

After the creation of the modules, the server can be initialised by passing the module to a BarkServer together with the number of workers and the name of the service:

```scala
val server = BarkServer(24, "Cache Service")(CacheServer.modules)(serverSystem)
```

After initialisation, the server can be be started by using the `run(port: Int)` command.

### Client
A client can be initialised and connected, based on the server's hostname, port and the number of workers which should be available within the client: 

```scala
val client = BarkClient("localhost", 8888, 4, "Cache client")(clientSystem)
```

The `<<?` and `<<!` functions can be used to call a function with the specified arguments: 

```scala
(client |?| "cache" |/| "get") <<? "A"
```

The `|?|` function is used to enter a specific module on the server, while the `|/|` function is used to specify the to-be-called function on the server. Calling a function returns a `Task[T]`. Task has (co)monadic behavior which wraps a `Future[Try[A]]`. 


Use `run` to expose the `Future[Try[A]]`, or use `start(d: Duration)` to perform IO and wait (blocking) on the future.

The Task returned by the BarkClient contains a `BarkClientResult` which contains the `ByteString` representation of the received frame from the server combined with the ability `as[T]` to deserialize the `ByteString` to the expected type through the type classes (passing a `Option` of the type, depending on the success of the deserialization).

Importing `nl.spotdog.bark.client._` results in a loaded implicit which makes it possible to directly call `.as[T]` on a Task. Wrapping the `Option` returned from `as[T]` in the Try used by the Task. This makes it possible to make the following (blocking) call which directly retuns the expected String:

```scala
callTask.as[String].copoint
```

The current DSL isn't optimal for plain usage (and is rather verbose and fuzzy), but is designed to be used within environment where the actual usage of the `BarkClient` is abstracted on a higher level.

# License
Copyright © 2013 Gideon de Kok, SpotDog

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

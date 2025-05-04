This is about the assignment of apiwiz

the assisgnment problem statement goes like this, we need to build an internal orchestration API which handles high volume(1000+ concurrent requests) of incoming http traffic, so each http request triggers a downstream 
http call to external service so these all should be truly async and non-blocking I/O 

For example the thread should dispatch the request and continue with other work instead of waiting for downstream service to response
this makes the api truly async and non-blocking I/O

so i approached this problem statement with an implementation of webclient which is provided by the spring webflux dependency (Reactor Project--Netty)

the service which i have developed uses the webclient or sslWebclient(for security) to executeTarget method which is basically accept the given parameters specified in assignment
such as 
->APIMethod(enum of http method types)
->RequestDTO(dto class for request attributes)
->timeout(in milliseconds)
->SSL flag(boolean to use SSL or not)

here execute target basically with the help of requestspec we are invocating a api call to downstream service and retrieve it in asynchronous manner and return to the method as Mono<String>

so for further extensibility i added logging and for tracing correlation-ID

the supporting dto classes, controller and MockExternalAPIController are included in project.

i have used k6(performance testing tool) test.js file to load test to achieve desirable benchmarks

 
----------------------------------------------------------

for the additional assignment i.e script engine service utility program

Honestly i tried using chatgpt, even chatgpt also guided me wrong because of so many updates in graalVM

so to give a brief headsUP why i choosed graalvm is because of it's polyglot nature
and i can use java nashorn script engine but in recent JDK's they have removed it, so i can write only python supported code with the help of jython
to avoid these issues.

i choosed graalVM which is a better for this usecase and polyglot supports so many languages like python, ruby, r etc..
so graal was inspired by the christianity concept of Holy Grail which means a engine which supports all languages

so in the process of finding this solution, i encountered gu (graal updater) issue which caused so much trouble to resolve it and found out that,
it is removed in latest versions.

so finally ith the  help of graalVM original website support docs i found the solution to embed other languages scripts in our java code.
and used the polyglot dependencies and solved the problem

there are certain limitations with the solution heavy 3rd party lib like numpy and pandas can't run because they heavily depend on nativeC/C++ libraries
but with standard libraries, this solution works fine.


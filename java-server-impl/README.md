# Concurrency and Non blocking IO

Demonstrates different approaches for servers implementations.
Shows evolution form simple one threaded blocking server to nonblocking Netty server implementation.  

## Blocking 

* SingleThreadedServer - server with one thread 
* MultipleThreadedServer - creates new tread per connection 
* ThreadPoolServer - uses fixed thread pool for handling connections 
* NIOBlockingServer - uses New IO and fixed thread pool

## Non blocking 

* NonblockingSingleThreadedPollingServer - single threaded server using loop polling 
* NonblockingSelectorServer - single threaded server using NIO selectors 
* NonblockingMultiThreadedSelectorServer - single threaded server using NIO selectors and fixed pool for processing
* NettyServer - Netty server with NIO socket channels 
* NettyEpollServer - Netty server with epoll socket channels   

### References 
 
* [JDK IO 2014 - Java Concurrency and Non blocking IO - Dr Heinz M. Kabutz](https://www.youtube.com/watch?v=vkjNjZiMt4w&t=0s)
* [Building a chat client/server system with Netty in under 15 minutes](https://www.youtube.com/watch?v=tsz-assb1X8)

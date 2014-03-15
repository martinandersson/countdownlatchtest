CountDownLatch Test
===================

Will prove the current JavaDoc of CountDownLatch to be flawed (Java 1.7, date read: 2014-03-15). Synchronization of worker threads keep them from starting too early, not too late. It's simply put it not right to think that the start is synchronized. The problem has been described a bit here:

http://blog.martinandersson.com/javas-countdownlatch-javadoc-is-flawed/

The JavaDoc in the provided source code of this repository is really expressive and should have all the information you need. Begin your reading in the Judge class.

The project is built using Maven.

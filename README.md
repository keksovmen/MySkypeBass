# MySkypeBass
Simple VoIP program, made without any dependencies except for intellij idea swing extension, just plain java 1.8 SE.

Contain 2 major packages Abstraction and Implementation:
Abstraction is independent from platform (desktop, android) and consists of application logic
and some factories that you will have to implement.
Implementation basically is GUI and overridden factories.

POM file instructed to produce 2 artifacts, and javadoc:
1 - Full application
2 - And only Abstract package (for sending to android and implementing it)


When starting can specify -logD flag to disable logging.

When writing a message can specify a particular sound through <$n> or <$n-m>
where: n - index of specific notification, m - delay in millis.

All you need for server side is port forwarding if your internet connection made through a router.

Has some good sound notification, 18+ I guess.

P.S:
Some times trying to port on pure WIN32 api, but you know it's tough one, maybe some day I finally do it.
Reason just to make something bigger than console application on C++, to better understand it.

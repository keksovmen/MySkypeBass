# Ветка для САФУ
**Основные изменения:**
- заменены звуковые уведомления на приличные
- изменено название приложения

# NARFU VOIP
Simple VoIP program, made without any dependencies except for intellij idea swing extension, just plain java 1.8 SE.

Contain 2 major packages Abstraction and Implementation:
Abstraction is independent from platform (desktop, android) and consists of application logic
and some factories that you will have to implement.
Implementation basically is GUI and overridden factories.

POM file instructed to produce 2 artifacts, and javadoc:
1 - Full application
2 - And only Abstract package (for sending to android and implementing it)


When starting can specify -logD flag to disable logging.

All you need for server side is port forwarding if your internet connection made through a router.
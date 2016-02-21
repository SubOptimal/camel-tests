If a unit test extends from `CamelTestSupport` and you want to change the route definition with `adviceWith` you might step into a trap if you don't follow all points of the documentation.

citing: [http://camel.apache.org/advicewith.html](http://camel.apache.org/advicewith.html)

```
Telling Camel you are using adviceWith
From Camel 2.9 onwards its recommended to override the isUseAdviceWith method and return true to tell Camel you are using advice with in your unit tests. Then after you have done the adviceWith, then you must start CamelContext manually.
```

Important parts here are `its recommended to override ... isUseAdviceWith` and `you must start CamelContext manually`. The highlighted points should be taken seriously. If you don't do, your test most probably run not as you expect.

The examples will demonstrate possible errors you could discover. For this demonstration the test case uses a route definition defined in a Spring configuration file (check the files in `META-INF/spring/`) and in the unit test it overrides the `From` clause to use a mocked `Producer`. For simplicity the implemented `AdviceWithRouteBuilder` only override the `From` clause. No other possible route alteration is used.

#### SpringWithAdviceRunFailTest.java

If the method `isUseAdviceWith` is overriden and return `true` but the Camel context is not started, the test fail with an

```
java.lang.IllegalStateException: ProducerTemplate has not been started
```

To solve this the Camel context needs to be started with `context().start()` for example in the `@Before` annotated method.


#### SpringWithAdviceAssertFailTest.java

If the method `isUseAdviceWith` is not overriden or return `false` and the Camel context is not started manually the test assertion will fail with an

```
java.lang.AssertionError: mock://result Received message count. Expected: <1> but was: <0>
```

If you investigate the reason you will notice in the debug output that the route definition is modified as expected.

```
routeDefinition = Route(routeTwo)[[From[direct:start]] -> [To[mock:result]]]
```

The routes `From` defined in the Spring configuration file `test-routes-assertfail` is changed from `direct:startTwo` to `direct:start`.

And during the test the message actually was sent correctly.

```
DEBUG o.a.c.i.ProducerCache$2(398) - >>>> Endpoint[direct://start] Exchange[][Message: Test message1]
DEBUG o.a.c.p.SendProcessor(137) - >>>> Endpoint[mock://result] Exchange[ID-host-45462-1456071566607-0-2][Message: Test message1]
DEBUG o.a.c.c.m.MockEndpoint(1330) - mock://result >>>> 0 : Exchange[ID-host-45462-1456071566607-0-2][Message: Test message1] with body: Test message1 and headers:{breadcrumbI
d=ID-host-45462-1456071566607-0-1}
```
As the Camel context was already started and the `adviceWith` is performed on an already started route the test gets executed on a route which is not bound to the defined endpoint `MockEndpoint mockResult`. This fact is also mentioned on the Camel documentation.

```
Recommendation
It's recommended to only advice routes which are not started already.
If you advice already started routes, then it may not work as expected.
```
As stated there `it may not work as expected`.

This test case could be even worst. If another route is defined to the same endpoint `mock:result` (uncomment the route definition `routeOne` in `test-routes-assertfail`) the test actually will run successful, as long the unused route is not deleted.

##### SpringWithAdviceTest.java

Is a complete test proprly using Spring XML configuration and an implementaion of `AdviceWithRouteBuilder` which overrides the `From` in the defined route.

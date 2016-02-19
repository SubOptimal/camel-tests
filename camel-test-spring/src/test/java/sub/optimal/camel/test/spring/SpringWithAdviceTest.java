package sub.optimal.camel.test.spring;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.spring.CamelSpringTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringWithAdviceTest extends CamelSpringTestSupport {

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("META-INF/spring/all-routes.xml");
    }

    @Produce(uri = "direct:start")
    private ProducerTemplate start;

    @EndpointInject(uri = "mock:result")
    private MockEndpoint mockResult;

    @Override
    public boolean isUseAdviceWith() {
        return true;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        RouteDefinition routeDefinition = context.getRouteDefinition("routeTwo");
        routeDefinition.adviceWith(context, new AdviceWithRouteBuilderImpl());
        System.out.println("routeDefinition = " + routeDefinition);
        System.out.println("endpoints = " + context.getEndpoints());
        context().start();
    }

    @Test
    public void testRoute() throws Exception {
        mockResult.expectedBodiesReceived("Test message1");
        start.sendBody("Test message1");
        mockResult.assertIsSatisfied();
    }

    private static class AdviceWithRouteBuilderImpl
            extends AdviceWithRouteBuilder {

        @Override
        public void configure() throws Exception {
            replaceFromWith("direct:start");
        }
    }
}

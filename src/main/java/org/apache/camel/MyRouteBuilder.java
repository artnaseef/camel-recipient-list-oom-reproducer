package org.apache.camel;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A Camel Java DSL Router
 */
public class MyRouteBuilder extends RouteBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(MyRouteBuilder.class);

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        /*
							.setProperty("LOGGER_ENDPOINT", () -> "log:logger#" + atomicLongSeq.incrementAndGet())

         */

        AtomicLong atomicLongSeq = new AtomicLong(0);

        from("direct:test-route-entrypoint")
                .setProperty("LOGGER_ENDPOINT", simple("log:logger#${header.CUR}"))
                .recipientList(exchangeProperty("LOGGER_ENDPOINT")).cacheSize(-1)
        ;

        from("jetty:http://0.0.0.0:9000/start-test")
                .to("direct:start-test")
        ;

        from("direct:start-test")
                .log("STARTING TEST...")
                .log("HEADERS = ${headers}")
                .process(this::initStartAndCount)
                .loop(header("count")).to("direct:loop-internals")
                    .process(this::setCurStep)
                    .log("hi inside loop ${exchangeProperty.CamelLoopIndex} ${header.CUR}")
                .end()
        ;

        from("direct:loop-internals")
                .process(this::setCurStep)
                .to("direct:test-route-entrypoint")
                ;


        //
        // USING toD()
        //

        from("jetty:http://0.0.0.0:9000/start-test-tod")
                .to("direct:start-test-toD")
        ;

        from("direct:test-route-entrypoint-toD")
                .setProperty("LOGGER_ENDPOINT", simple("log:logger#${header.CUR}"))
                .toD("${exchangeProperty.LOGGER_ENDPOINT}")
        ;

        from("direct:start-test-toD")
                .log("STARTING TEST...")
                .log("HEADERS = ${headers}")
                .process(this::initStartAndCount)
                .loop(header("count")).to("direct:loop-internals-toD")
                    .process(this::setCurStep)
                    .log("hi inside loop ${exchangeProperty.CamelLoopIndex} ${header.CUR}")
                .end()
        ;

        from("direct:loop-internals-toD")
                .process(this::setCurStep)
                .to("direct:test-route-entrypoint-toD")
                ;
    }

//========================================
// Internals
//----------------------------------------

    private void initStartAndCount(Exchange exchange) {
        Message in = exchange.getIn();
        Long start = in.getHeader("start", Long.class);
        if (start == null) {
            LOG.info("start is null");
            in.setHeader("start", 1L);
        }

        Long count = in.getHeader("count", Long.class);
        if (count == null) {
            in.setHeader("count", 10L);
        }
    }

    private void setCurStep(Exchange exchange) {
        Message in = exchange.getIn();
        long start = in.getHeader("start", Long.class);
        long index = exchange.getProperty("CamelLoopIndex", Long.class);
        long cur = start + index;
        in.setHeader("CUR", cur);
    }

}

package org.rosuda.rengine.rserve;

import static junit.framework.TestCase.*;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.rosuda.rengine.REXP;
import org.rosuda.rengine.REXPMismatchException;
import org.rosuda.rengine.REngineException;
import org.rosuda.rengine.rserve.protocol.RConnectionException;
import org.rosuda.rengine.rserve.protocol.RTalk;

import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;
import pl.domzal.junit.docker.rule.DockerRule;
import pl.domzal.junit.docker.rule.WaitFor;

public class RserveConnectionIT {
    private static final String RSERVE_PORT = "6311";

    private final DockerRule rserveContainer = DockerRule.builder()
            .imageName("usgs/rserve:0.0.1")
            .name("RserveConnectionIT_rserve")
            .expose(RSERVE_PORT)
            .waitFor(WaitFor.logMessage("running Rserve in this R session"))
            .build();

    private final DockerRule toxicContainer = DockerRule.builder()
            .imageName("shopify/toxiproxy")
            .link("RserveConnectionIT_rserve")
            .expose("8474")
            .expose(RSERVE_PORT)
            .waitFor(WaitFor.logMessage("API HTTP server starting"))
            .build();

    @Rule
    public RuleChain containers = RuleChain.outerRule(rserveContainer).around(toxicContainer);

    @Test
    public void shouldConnectToServer() throws RserveException, IOException {
        // when
        ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxicContainer.getDockerHost(), Integer.parseInt(toxicContainer.getExposedContainerPort("8474")));
        String rserveChannel = "broker_channel";
        Proxy proxy = toxiproxyClient.createProxy(rserveChannel, "0.0.0.0:" + RSERVE_PORT, rserveContainer.getContainerIp() + ":" + RSERVE_PORT);
        RConnection connection = new RConnection(toxicContainer.getDockerHost(), Integer.parseInt(toxicContainer.getExposedContainerPort(RSERVE_PORT)));

        // then
        assertTrue(connection.isConnected());
    }

    @Test
    public void shouldDetectConnectionClosed() throws IOException, REngineException, REXPMismatchException {
        // given
        ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxicContainer.getDockerHost(), Integer.parseInt(toxicContainer.getExposedContainerPort("8474")));
        String rserveChannel = "broker_channel";
        Proxy proxy = toxiproxyClient.createProxy(rserveChannel, "0.0.0.0:" + RSERVE_PORT, rserveContainer.getContainerIp() + ":" + RSERVE_PORT);
        RConnection connection = new RConnection(toxicContainer.getDockerHost(), Integer.parseInt(toxicContainer.getExposedContainerPort(RSERVE_PORT)));
        assertTrue(connection.isConnected());

        // when
        proxy.disable();

        try {
            REXP rexp = connection.parseAndEval("1");
        } catch (REngineException e) {
            // then
            assertTrue(e instanceof RserveException);
            assertEquals(RTalk.ERR_conn_broken, ((RserveException) e).getRequestReturnCode());
        }
    }

    @Test
    public void shouldDetectBadAuth() throws IOException, REngineException, REXPMismatchException {
        // given
        ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxicContainer.getDockerHost(), Integer.parseInt(toxicContainer.getExposedContainerPort("8474")));
        String rserveChannel = "broker_channel";
        Proxy proxy = toxiproxyClient.createProxy(rserveChannel, "0.0.0.0:" + RSERVE_PORT, rserveContainer.getContainerIp() + ":" + RSERVE_PORT);
        RConnection connection = new RConnection(toxicContainer.getDockerHost(), Integer.parseInt(toxicContainer.getExposedContainerPort(RSERVE_PORT)));
        assertTrue(connection.isConnected());

        // when
        try {
            REXP rexp = connection.parseAndEval("1");
        } catch (REngineException e) {
            // then
            assertTrue(e instanceof RserveException);
            assertEquals(RTalk.ERR_auth_failed, ((RserveException) e).getRequestReturnCode());
        }
    }

    @Test
    public void shouldDetectEvalFailed() throws IOException, REngineException, REXPMismatchException, RConnectionException {
        // given
        ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxicContainer.getDockerHost(), Integer.parseInt(toxicContainer.getExposedContainerPort("8474")));
        String rserveChannel = "broker_channel";
        Proxy proxy = toxiproxyClient.createProxy(rserveChannel, "0.0.0.0:" + RSERVE_PORT, rserveContainer.getContainerIp() + ":" + RSERVE_PORT);
        RConnection connection = new RConnection(toxicContainer.getDockerHost(), Integer.parseInt(toxicContainer.getExposedContainerPort(RSERVE_PORT)));
        assertTrue(connection.isConnected());
        connection.login("rserve", "rserve");

        // when
        try {
            REXP rexp = connection.parseAndEval("notExistant()");
        } catch (REngineException e) {
            // then
            assertTrue(e instanceof RserveException);
        }
    }
}

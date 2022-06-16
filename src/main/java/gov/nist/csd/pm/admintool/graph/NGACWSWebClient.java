package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class NGACWSWebClient {
    public static final String LOCALHOST_URL = "http://localhost:8081";


    private final String ngac_ws_url;
    private final WebClient client;


    public NGACWSWebClient(String ngac_ws_url) {
        this.ngac_ws_url = ngac_ws_url;
        this.client = WebClient.create(ngac_ws_url);
    }

    public Node getNode(String name) {
        Mono<Node> result = client.get()
                .uri("/node?name=" + name).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Node.class);

        return result.block(Duration.ofSeconds(1));
    }

    public Set<Node> getNodes() {
        Flux<Node> result = client.get()
                .uri("/nodes").accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Node.class);

        return new HashSet<>(result.collectList().block(Duration.ofSeconds(1)));
    }
}

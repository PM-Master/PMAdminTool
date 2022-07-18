package gov.nist.csd.pm.admintool.graph;

import gov.nist.csd.pm.admintool.app.MainView;
import gov.nist.csd.pm.epp.events.EventContext;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.operations.OperationSet;
import gov.nist.csd.pm.pip.graph.model.nodes.Node;
import gov.nist.csd.pm.pip.graph.model.nodes.NodeType;
import gov.nist.csd.pm.pip.obligations.model.Obligation;
import gov.nist.csd.pm.pip.prohibitions.model.Prohibition;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

public class NGACWSWebClient {

    public static final String LOCALHOST_URL = "http://localhost:8081";

    private static final ParameterizedTypeReference<Map<String, OperationSet>> MAP_TYPE_REF = new ParameterizedTypeReference<Map<String, OperationSet>>() {};
    public static final ParameterizedTypeReference<Boolean> MAP_TYPE_BOOLEAN = new ParameterizedTypeReference<Boolean>() {};

    private final String ngac_ws_url;
    private final WebClient client;

    public NGACWSWebClient(String ngac_ws_url) {
        this.ngac_ws_url = ngac_ws_url;
        this.client = WebClient.create(ngac_ws_url);
    }

    public Node getNode(String name) {
        Mono<Node> result = client.get()
                .uri("/nodes/" + name).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(Node.class);

        return result.block(Duration.ofSeconds(1));
    }

    public Set<Node> getNodes() {
        Flux<Node> result = client.get()
                .uri("/nodes").accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(Node.class);

        return new HashSet<>(result.collectList().block(Duration.ofSeconds(1)));
    }

    public Set<String> getChildren(String name) {
        Flux<String> result = client.get()
                .uri("/nodes/children/" + name).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(String.class);

        return new HashSet<>(result.collectList().block(Duration.ofSeconds(1)));
    }

    public Set<String> getParents(String name) {
        Flux<String> result = client.get()
                .uri("/nodes/parents/" + name).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(String.class);

        return new HashSet<>(result.collectList().block(Duration.ofSeconds(1)));
    }

    public Node createNode(Node node, String parent) {
        Mono<Node> result = client.post()
                    .uri("/nodes?parent=" + parent).accept(MediaType.APPLICATION_JSON)
                    .body(Mono.just(node), Node.class)
                    .retrieve()
                    .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                        Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                        return errorMessage.flatMap(msg -> {
                            System.out.println(msg);
                            throw new RuntimeException(msg);
                        });
                    })
                    .bodyToMono(Node.class);
            return result.block();
    }

    public void deleteNode(String name) {
        String responseMessage = client.delete()
                .uri("/nodes/" + name).accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();

        MainView.notify(responseMessage, MainView.NotificationType.SUCCESS);
    }
    public void updateNode(String nodeName, Node node) {
        String responseMessage = client.put()
                .uri("/nodes?nodeName="+nodeName)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(node), Node.class)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
//        MainView.notify(responseMessage, MainView.NotificationType.SUCCESS);
    }

    public Node createPolicyClass(String name, Map<String, String> properties)  {
        Mono<Node> result = client.post()
                .uri("/nodes/pc?name="+name).accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(properties), Map.class)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(Node.class);

        return result.block(Duration.ofSeconds(1));
    }

    // *************** Assignment ********************************

    public void assign(String childNode, String parentNode) {
        client.post()
                .uri("/assignments?childNode="+childNode+"&parentNode="+parentNode)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public void deassign(String childNode, String parentNode) {
        client.delete()
                .uri("/assignments?childNode="+childNode+"&parentNode="+parentNode)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }
    // *************** Association ********************************

    public void associate(String ua, String target, OperationSet operations) {
        client.post()
                .uri("/associations?ua="+ua+"&target="+target)
                .accept(MediaType.ALL)
                .body(Mono.just(operations), OperationSet.class)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public void dissociate(String ua, String target) {
        client.delete()
                .uri("/associations?ua="+ua+"&target="+target)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public Map<String, OperationSet> getSourceAssociations(String target) {
        return client.get()
                .uri("/associations/source/" + target)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(MAP_TYPE_REF).block(Duration.ofSeconds(1));
    }
    public Map<String, OperationSet> getTargetAssociations(String source) {
        return client.get()
                .uri("/associations/target/" + source)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(MAP_TYPE_REF).block(Duration.ofSeconds(1));

    }

    // *************** Policy methods ********************************

    public void reset(){
        client.post()
                    .uri("/policies/reset")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                        Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                        return errorMessage.flatMap(msg -> {
                            System.out.println(msg);
                            throw new RuntimeException(msg);
                        });
                    })
                    .bodyToMono(String.class).block();
//                    .retrieve()
//                    .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
//                        Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
//                        return errorMessage.flatMap(msg -> {
//                            System.out.println(msg);
//                            return Mono.error(new PMException(msg));
//                        });
//                    })
//                    .bodyToMono(String.class).block();
    }

    public String getPolicyClassDefault(String pc, NodeType type) {
        return client.get()
                .uri("/policies/defaultclass?pc="+pc+"&type="+type)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public Set<String> getPolicies() {
        Flux<String> result =  client.get()
                .uri("/policies")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(String.class);
        return new HashSet<>(result.collectList().block());
    }

    public Set<String> getChildrenNoSuperPolicy(String name) {
        Flux<String> result = client.get()
                .uri("/policies/children/nosuperpolicy?name="+name)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(String.class);
        return new HashSet<>(result.collectList().block());
    }

    public void fromJson(String s) {
        client.get()
                .uri("/policies/fromJson?s="+s)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public String toJson() {
        Mono<String> result = client.get()
                .uri("/policies/toJson")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class);
        return result.block();
    }

    // *************** Prohibitions ********************************

    public List<Prohibition> getAllProhibitions() {

        Flux<Prohibition> result = client.get()
                .uri("/prohibitions")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(Prohibition.class);
        return new ArrayList<>(result.collectList().block(Duration.ofSeconds(1)));
    }

    public Prohibition getProhibition(String prohibitionname) {
        Mono<Prohibition> result = client.get()
                .uri("/prohibitions/"+prohibitionname)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(Prohibition.class);
        return result.block();
    }

    public static class ProhibitionInfo {
        public Map<String, Boolean> containers;
        public OperationSet ops;

        public void ProhibitionInfo() {
        }
    }

    public void addProhibition(String prohibitionName, String subject, ProhibitionInfo containersAndOperations, boolean intersection) {
        client.post()
                .uri("/prohibitions/"+prohibitionName+"?subject="+subject+"&intersection="+intersection)
                .accept(MediaType.ALL)
                .body(Mono.just(containersAndOperations), ProhibitionInfo.class) // (Mono.just(containers), Map.class))
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public List<Prohibition> getProhibitionsFor(String subject) {
        Flux<Prohibition> result = client.get()
                .uri("/prohibitions/for/"+subject)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(Prohibition.class);
        return new ArrayList<>(result.collectList().block());
    }

    public List<Prohibition> getProhibitionsFrom(String target) {
        Flux<Prohibition> result = client.get()
                .uri("/prohibitions/from/" + target)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(Prohibition.class);
        return new ArrayList<>(result.collectList().block());
    }

    public void updateProhibition(String prohibitionName, String subject, ProhibitionInfo containersAndOperations, boolean intersection) {
        client.put()
                .uri("/prohibitions/"+prohibitionName+"?subject="+subject+"&intersection="+intersection)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(containersAndOperations), ProhibitionInfo.class) // (Mono.just(containers), Map.class))
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public void deleteProhibition(String prohibitionName) {
        client.delete()
                .uri("/prohibitions/"+prohibitionName)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    // *************** Operations ********************************

    public Set<String> getAdminOps() throws PMException {
        Flux<String> result = client.get()
                .uri("/operations/adminOps")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(String.class);
        return new HashSet<>(result.collectList().block());
    }
    public Set<String> getAdminOpsWithStars() throws PMException {
        Flux<String> result = client.get()
                .uri("/operations/adminOpsWithStars")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(String.class);
        return new HashSet<>(result.collectList().block());
    }

    public Set<String> getResourceOps() throws PMException {
        Flux<String> result = client.get()
                .uri("/operations/resourceOps")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(String.class);
        return new HashSet<>(result.collectList().block());
    }

    public Set<String> getResourceOpsWithStars() throws PMException {
        Flux<String> result = client.get()
                .uri("/operations/resourceOpsWithStars")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(String.class);
        return new HashSet<>(result.collectList().block());
    }

    public void addResourceOps(String... ops) throws PMException {
        client.post()
                .uri("/operations/resourceOps")
                .accept(MediaType.ALL)
                .body(Flux.just(ops), String.class)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public void deleteResourceOps(String... ops) throws PMException {
                client
                .method(HttpMethod.DELETE)
                .uri("/operations/resourceOps")
                .accept(MediaType.ALL)
                .body(BodyInserters.fromObject(ops))
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public String getExplanation(String target) {
        return client.get()
                .uri("/explain/"+target)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public boolean checkPermissions (String target, String[] ops) throws PMException {
        return Boolean.TRUE.equals(client.post()
                .uri("/permissions/" + target)
                .syncBody(ops)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(Boolean.class).block());
    }

    // *************** Obligation methods ********************************

    public void processEvent (EventContext eventCtx) throws PMException {
        client.post()
                .uri("/obligations/processEvent")
                .accept(MediaType.ALL)
                .body(Mono.just(eventCtx), EventContext.class)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public Obligation getObl(String label) throws PMException {
        return client.get()
                .uri("/obligations/"+label)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(Obligation.class).block();
    }

    public List<Obligation> getAllObls() throws PMException {
        return client.get()
                .uri("/obligations")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(Obligation.class).collectList().block();
    }

    public void addObl(Obligation obligation) throws PMException {
        client.post()
                .uri("/obligations")
                .accept(MediaType.ALL)
                .body(Mono.just(obligation), Obligation.class)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public void updateObl(String label, Obligation obligation) throws PMException {
        client.put()
                .uri("/obligations/"+label)
                .accept(MediaType.ALL)
                .body(Mono.just(obligation), Obligation.class)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public void deleteObl(String label) throws PMException {
        client.delete()
                .uri("/obligations/"+label)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public void enableObl(String label) throws PMException {
        client.put()
                .uri("/obligations/enable/"+label)
                .accept(MediaType.ALL)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToMono(String.class).block();
    }

    public List<Obligation> getEnabledObls() throws PMException {
        return client.get()
                .uri("/obligations/enabled")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    Mono<String> errorMessage = clientResponse.bodyToMono(String.class);
                    return errorMessage.flatMap(msg->{
                        System.out.println(msg);
                        throw new RuntimeException(msg);
                    });
                })
                .bodyToFlux(Obligation.class).collectList().block();
    }
}

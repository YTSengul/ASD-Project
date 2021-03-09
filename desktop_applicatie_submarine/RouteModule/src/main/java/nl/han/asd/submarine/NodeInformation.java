package nl.han.asd.submarine;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import nl.han.asd.submarine.models.NodesDTO;
import nl.han.asd.submarine.models.routing.Node;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class NodeInformation {

    HttpTransport transport;

    @Inject
    @Named("nodeDirectoryServerIp")
    String nodeDirectoryIp;

    public NodeInformation() {
        this(new NetHttpTransport.Builder().build());
    }

    public HttpTransport getTransport() {
        return transport;
    }

    public void setTransport(HttpTransport transport) {
        this.transport = transport;
    }

    public String getNodeDirectoryIp() {
        return nodeDirectoryIp;
    }

    public void setNodeDirectoryIp(String nodeDirectoryIp) {
        this.nodeDirectoryIp = nodeDirectoryIp;
    }

    public NodeInformation(HttpTransport transport) {
        this.transport = transport;
    }

    public List<Node> get() {
        try {
            HttpRequest request = transport.createRequestFactory().buildGetRequest(new GenericUrl("http://" + nodeDirectoryIp + "/node"));
            request.setParser(new JsonObjectParser(new GsonFactory()));
            HttpResponse response = request.execute();

            NodesDTO nodes = new Gson().fromJson(response.parseAsString(), NodesDTO.class);
            return nodes.getNodes().stream().map(rn -> new Node(rn.getDestination(), rn.getKey())).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            return List.of();
        }
        return List.of();
    }
}

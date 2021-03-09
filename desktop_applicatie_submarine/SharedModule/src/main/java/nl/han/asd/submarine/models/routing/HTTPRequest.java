package nl.han.asd.submarine.models.routing;

public class HTTPRequest<T> {
    private String endpoint;
    // Request type is used this way in the nodes.
    private String request_type; // NOSONAR
    private T body;

    public HTTPRequest(String endpoint, String request_type, T body){
        this.endpoint = endpoint;
        this.request_type = request_type;
        this.body = body;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}

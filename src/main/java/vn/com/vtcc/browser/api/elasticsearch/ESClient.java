package vn.com.vtcc.browser.api.elasticsearch;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.stereotype.Component;
import vn.com.vtcc.browser.api.Application;
import vn.com.vtcc.browser.api.config.ProductionConfig;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by giang on 15/08/2017.
 */
@Component
public class ESClient {
    TransportClient client;
    public String[] host;
    public int port;
    public String clusterName;

    public ESClient() throws UnknownHostException {
        if (Application.PRODUCTION_ENV == true) {
            this.host = ProductionConfig.ES_HOST_PRODUCTION;
        } else {
            this.host = ProductionConfig.ES_HOST_STAGING;
        }
        this.port = ProductionConfig.ES_TRANSPORT_PORT;
        this.clusterName = Application.ES_CLUSTER_NAME;
    }

    public ESClient(String[] host,int port,String clusterName) {
        this.host = host;
        this.clusterName = clusterName;
        this.port = port;
    }

    @PostConstruct
    public void connect() throws UnknownHostException {
        Settings settings = Settings.builder().put("cluster.name", clusterName).build();
        client = new PreBuiltTransportClient(settings);
        for (String esHost : host) {
            client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(esHost),
                    ProductionConfig.ES_TRANSPORT_PORT));

        }
    }

    public void setHost(String[] host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public TransportClient getClient() {
        //System.out.println(this.client);
        return this.client;
    }

    public void close() {
        if (client != null) {
            client.close();
        }
    }

    @Override
    public String toString() {
        return String.format("%s, Host: %s, Port: %s, Cluster: %s", super.toString(), host, port, clusterName);
    }

}

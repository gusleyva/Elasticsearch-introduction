package com.elasticsearch.client;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        String indexName = "users";
        ElasticSearchClient client = new ElasticSearchClient();
        try {
            boolean exist = client.existIndex(indexName);
            System.out.printf("Index %s exist %s \n", indexName, exist);
            client.createIndex(indexName);
            client.searchData(indexName);
            client.insertData(indexName);
            client.searchData(indexName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.closeConnection();
        }
    }
}

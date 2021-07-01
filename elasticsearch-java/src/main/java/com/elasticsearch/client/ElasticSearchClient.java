package com.elasticsearch.client;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ElasticSearchClient {
    private RestHighLevelClient restHighLevelClient = null;

    ElasticSearchClient(){
        restHighLevelClient = buildLocalRestClient();
    }

    private RestHighLevelClient buildLocalRestClient(){
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")
                )
        );
    }

    public void closeConnection() throws IOException {
        restHighLevelClient.close();
        restHighLevelClient = null;
        System.out.println("Cliente desconectado.");
    }

    public boolean existIndex(String indexName) throws IOException {
        GetIndexRequest requestIndexExist = new GetIndexRequest(indexName);
        return restHighLevelClient.indices().exists(requestIndexExist, RequestOptions.DEFAULT);
    }

    private String loadResource(String fileName) throws IllegalArgumentException, IOException {
        String exceptionMessage = null;
        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName.concat(".json"));
        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        }

        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    public Boolean createIndex(String indexName) throws IOException {
        boolean isAcknowledged = false;
        if(existIndex(indexName)){
            System.out.printf( "%s index exist, please delete it if you want to re-create it.\n", indexName);
        } else {
            String indexMapping = loadResource(indexName);
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            request.source(indexMapping, XContentType.JSON);
            isAcknowledged = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT).isAcknowledged();
            System.out.printf( "%s index created.\n", indexName);
        }
        return isAcknowledged;
    }

    public void searchData(String indexName) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        for (SearchHit hit: searchResponse.getHits().getHits()){
            System.out.printf("Documento con id: [%s], value: [%s] \n", hit.getId(), hit.getSourceAsString());
        }
    }

    public void insertData(String indexName) throws IOException, ElasticsearchException {
        String record = loadResource("user_insert");
        IndexRequest indexRequest = new IndexRequest(indexName);
        indexRequest.id("4").source(XContentType.JSON, "_doc", record);
        IndexResponse response = restHighLevelClient.index(indexRequest,  RequestOptions.DEFAULT);
        System.out.printf("Insert message: %s.\n", response.toString());
    }
}

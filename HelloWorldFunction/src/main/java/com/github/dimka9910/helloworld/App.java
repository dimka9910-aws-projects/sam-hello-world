package com.github.dimka9910.helloworld;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import java.util.HashMap;
import java.util.Map;

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String REGION_NAME = System.getenv("REGION_NAME");
    private static final String TABLE_NAME = System.getenv("TABLE_NAME");

    private final AmazonDynamoDB dynamoDBClient;

    {
        // Initialize the DynamoDB client
        dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION_NAME)
                .build();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        System.out.println("Received event: " + input.getBody());

        // Create a DynamoDB instance
        DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);

        // Get the DynamoDB table
        Table table = dynamoDB.getTable(TABLE_NAME);

        // Create a ScanSpec to scan the entire table
        ScanSpec scanSpec = new ScanSpec();

        // Perform the scan operation
        ItemCollection<ScanOutcome> scanOutcome = table.scan(scanSpec);

        // Convert scan result to a JSON string
        StringBuilder jsonArrayBuilder = new StringBuilder("{[");
        boolean isFirstItem = true;

        for (Item item : scanOutcome) {
            if (!isFirstItem) {
                jsonArrayBuilder.append(",");
            }
            jsonArrayBuilder.append(item.toJSON());
            isFirstItem = false;
        }

        jsonArrayBuilder.append("]}");
        String scanResultJson = jsonArrayBuilder.toString();

        // Create a response
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(scanResultJson);

        // Set headers
        response.setHeaders(Map.of("Content-Type", "application/json"));

        return response;
    }
}
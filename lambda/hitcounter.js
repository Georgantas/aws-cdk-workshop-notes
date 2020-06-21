
const { DynamoDB, Lambda } = require('aws-sdk');

// relies on two environment variables:
// HITS_TABLE_NAME -> name of DynamoDB table to use for storage
// DOWNSTREAM_FUNCTION_NAME -> name of the downstream AWS lambda function
exports.handler = async function (event) {
    console.log("request:", JSON.stringify(event, undefined, 2));

    // create AWS SDK clients
    const dynamo = new DynamoDB();
    const lambda = new Lambda();

    // update dynamo entry for "path" with hits++
    await dynamo.updateItem({
        TableName: process.env.HITS_TABLE_NAME,
        Key: { path: { S: event.path } }, // "path" is the partition key (see HitCounter.java)
        UpdateExpression: 'ADD hits :incr',
        ExpressionAttributeValues: { ':incr': { N: '1' } }
    }).promise();

    // call downstream function and capture response
    const resp = await lambda.invoke({
        FunctionName: process.env.DOWNSTREAM_FUNCTION_NAME,
        Payload: JSON.stringify(event)
    }).promise();

    console.log('downstream response:', JSON.stringify(resp, undefined, 2));

    // return response back to upstream caller
    return JSON.parse(resp.Payload);
};


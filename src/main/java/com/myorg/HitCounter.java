package com.myorg;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awscdk.core.Construct;

import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

public class HitCounter extends Construct {
    private final Function handler;
    private final Table table;

    public HitCounter(final Construct scope, final String id, final HitCounterProps props) {
        // Propagate the scope and id to awscdk.core.Construct
        super(scope, id);

        this.table = Table.Builder.create(this, "Hits")
                // For more on partition keys:
                // https://aws.amazon.com/blogs/database/choosing-the-right-dynamodb-partition-key/
                .partitionKey(Attribute.builder().name("path").type(AttributeType.STRING).build()).build();

        final Map<String, String> environment = new HashMap<>();
        // The FunctionName and TableName properties are values that only resolve when
        // we deploy our stack (notice that we haven’t configured these physical names
        // when we defined the table/function, only logical IDs). This means that if you
        // print their values during synthesis, you will get a “TOKEN”, which is how the
        // CDK represents these late-bound values. You should treat tokens as opaque
        // strings. This means you can concatenate them together for example, but don’t
        // be tempted to parse them in your code.
        environment.put("DOWNSTREAM_FUNCTION_NAME", props.getDownstream().getFunctionName());
        environment.put("HITS_TABLE_NAME", this.table.getTableName());

        this.handler = Function.Builder.create(this, "HitCounterHandler").runtime(Runtime.NODEJS_10_X)
                .handler("hitcounter.handler").code(Code.fromAsset("lambda")).environment(environment).build();

        // Grant the lambda function read/write permissions to the table
        this.table.grantReadWriteData(this.handler);

        // Grant the lambda function invoke permissions to the downstream function
        props.getDownstream().grantInvoke(this.handler);
    }

    public Function getHandler() {
        return this.handler;
    }

    public Table getTable() {
        return this.table;
    }
}

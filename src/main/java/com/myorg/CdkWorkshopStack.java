package com.myorg;

import com.github.eladb.dynamotableviewer.TableViewer;

import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;

import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;

import software.amazon.awscdk.services.apigateway.LambdaRestApi;

public class CdkWorkshopStack extends Stack {
    public CdkWorkshopStack(final Construct parent, final String id) {
        this(parent, id, null);
    }

    public CdkWorkshopStack(final Construct parent, final String id, final StackProps props) {
        // For more on logical ids (second argument):
        // https://docs.aws.amazon.com/cdk/latest/guide/identifiers.html#identifiers_logical_ids

        // The last argument is always a set of initialization properties.
        super(parent, id, props);

        /*
         * The class constructors of both CdkWorkshopStack and Function (and many other
         * classes in the CDK) have the signature (scope, id, props). This is because
         * all of these classes are constructs. Constructs are the basic building block
         * of CDK apps. They represent abstract “cloud components” which can be composed
         * together into higher level abstractions via scopes. Scopes can include
         * constructs, which in turn can include other constructs, etc.
         */

        // Defines a new lambda resource
        final Function hello = Function.Builder.create(this, "HelloHandler").runtime(Runtime.NODEJS_10_X) // Execution
                                                                                                          // environment
                .code(Code.fromAsset("lambda")) // Code loaded from the "lambda" directory
                .handler("hello.handler") // File is hello.js, function is handler
                .build();

        // Defines our HitCounter resource
        final HitCounter helloWithCounter = new HitCounter(this, "HelloHitCounter",
                HitCounterProps.builder().downstream(hello).build());

        // Defines at API Gateway REST API resource backed by our "hello" function
        // When deployment is complete, you’ll notice this line:
        // CdkWorkshopStack.Endpoint8024A810 =
        // https://xxxxxxxxxx.execute-api.us-east-1.amazonaws.com/prod/
        // This is a stack output that’s automatically added by the API Gateway
        // construct and includes the URL of the API Gateway endpoint.
        LambdaRestApi.Builder.create(this, "Endpoint").handler(helloWithCounter.getHandler()).build();

        // Define a viewer for the HitCount table
        TableViewer.Builder.create(this, "ViewerHitCount").title("Hello Hits").table(helloWithCounter.getTable())
                .build();

        // On `cdk deploy` you’ll notice that cdk deploy not only deployed your
        // CloudFormation stack, but also archived and uploaded the lambda directory
        // from your disk to the bootstrap bucket.
        /*
         * final Queue queue = Queue.Builder.create(this,
         * "CdkWorkshopQueue").visibilityTimeout(Duration.seconds(300)) .build(); // SQS
         * Queue
         * 
         * final Topic topic = Topic.Builder.create(this,
         * "CdkWorkshopTopic").displayName("My First Topic Yeah").build(); // SNS //
         * Topic
         * 
         * topic.addSubscription(new SqsSubscription(queue)); // Subscribing to the
         * Queue to receive // any messages published to the queue
         */
    }
}

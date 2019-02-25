package io.anemos.protobeam.transform;

import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.model.QueryRequest;
import com.google.api.services.bigquery.model.QueryResponse;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.Int32Value;
import com.google.protobuf.StringValue;
import com.google.protobuf.util.Timestamps;
import io.anemos.protobeam.convert.ProtoTableRowExecutionPlan;
import io.anemos.protobeam.convert.SchemaProtoToBigQueryModel;
import io.anemos.protobeam.examples.ProtoBeamBasicPrimitive;
import io.anemos.protobeam.examples.ProtoBeamWktMessage;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryOptions;
import org.apache.beam.sdk.io.gcp.testing.BigqueryClient;
import org.apache.beam.sdk.options.ExperimentalOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestPipelineOptions;
import org.apache.beam.sdk.transforms.Create;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class BigQueryIT {

    private Bigquery bqClient;
    private String datasetName = "BigQueryIT";
    private BigQueryITOptions options;

    @Before
    public void setUp() {
        PipelineOptionsFactory.register(BigQueryITOptions.class);
        options = TestPipeline.testingPipelineOptions().as(BigQueryITOptions.class);
        options.setProject(System.getenv("GCP_PROJECT"));
        options.setTempLocation(System.getenv("GCS_TEMP_ROOT") +"/temp-it/");
        bqClient = BigqueryClient.getNewBigquerryClient(options.getAppName());
    }

    /** Customized PipelineOptions for BigQueryClustering Integration Test. */
    public interface BigQueryITOptions
            extends TestPipelineOptions, ExperimentalOptions, BigQueryOptions {
    }

    @Test
    public void testBasicPrimitiveE2EBigQuery() throws Exception {
        String tableName = "basic_primitive_" + System.currentTimeMillis();
        ProtoBeamBasicPrimitive protoIn = ProtoBeamBasicPrimitive.newBuilder()
                .setTestName("testName")
                .setTestIndex(1)
                .setPrimitiveDouble(1.0)
                .setPrimitiveFloat(1.0F)
                .setPrimitiveInt32(1)
                .setPrimitiveInt64(1)
                .setPrimitiveUint32(1)
                .setPrimitiveUint64(1)
                .setPrimitiveSint32(1)
                .setPrimitiveSint64(1)
                .setPrimitiveFixed32(1)
                .setPrimitiveFixed64(1)
                .setPrimitiveSfixed32(1)
                .setPrimitiveSfixed64(1)
                .setPrimitiveBool(true)
                .setPrimitiveString("test")
                .setPrimitiveBytes(ByteString.copyFromUtf8("test"))
                .build();

        Pipeline pipeline = Pipeline.create(options);

        ProtoTableRowExecutionPlan plan = new ProtoTableRowExecutionPlan(protoIn);
        SchemaProtoToBigQueryModel model = new SchemaProtoToBigQueryModel();
        TableSchema schema = model.getSchema(protoIn.getDescriptorForType());
        TableRow in = plan.convert(protoIn);
        ArrayList<TableRow> rows = new ArrayList<>();
        rows.add(in);

        pipeline.apply(Create.of(rows))
                .apply(BigQueryIO.writeTableRows()
                        .to(String.format("%s.%s", datasetName, tableName))
                        .withSchema(schema)
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE));

        pipeline.run().waitUntilFinish();

        bqClient = BigqueryClient.getNewBigquerryClient(options.getAppName());

        String query = String.format("SELECT * FROM `%s.%s`", datasetName, tableName);
        QueryRequest queryRequest = new QueryRequest().setQuery(query).setUseLegacySql(false);
        QueryResponse queryResponse = bqClient.jobs().query(options.getProject(), queryRequest).execute();

        //TODO https://stackoverflow.com/questions/41203261/how-to-merge-schema-with-rows-when-querying-a-full-row-from-bigquery-java
        // might be interesting to enrich the response tablerow with the schema
        TableRow out = queryResponse.getRows().get(0);
        String expected = "{\n" +
                "  \"f\" : [ {\n" +
                "    \"v\" : \"testName\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1.0\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1.0\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"true\"\n" +
                "  }, {\n" +
                "    \"v\" : \"test\"\n" +
                "  }, {\n" +
                "    \"v\" : \"dGVzdA==\"\n" + //base64 for "test"
                "  } ]\n" +
                "}";
        Assert.assertEquals(expected, out.toPrettyString());
    }

    @Test
    public void testWktE2EBigQuery() throws Exception {
        String tableName = "wkt_" + System.currentTimeMillis();
        ProtoBeamWktMessage protoIn = ProtoBeamWktMessage.newBuilder()
                .setTestName("testName")
                .setTestIndex(1)
                .setTimestamp(Timestamps.fromMillis(1550672770788L))
                .setNullableString(StringValue.newBuilder()
                        .setValue("test")
                        .build())
                .setNullableBool(BoolValue.newBuilder()
                        .setValue(true)
                        .build())
                .setNullableInt32(Int32Value.newBuilder()
                        .setValue(42)
                        .build())
                .build();

        Pipeline pipeline = Pipeline.create(options);

        ProtoTableRowExecutionPlan plan = new ProtoTableRowExecutionPlan(protoIn);
        SchemaProtoToBigQueryModel model = new SchemaProtoToBigQueryModel();
        TableSchema schema = model.getSchema(protoIn.getDescriptorForType());

        TableRow in = plan.convert(protoIn);
        ArrayList<TableRow> rows = new ArrayList<>();
        rows.add(in);

        pipeline.apply(Create.of(rows))
                .apply(BigQueryIO.writeTableRows()
                        .to(String.format("%s.%s", datasetName, tableName))
                        .withSchema(schema)
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE));

        pipeline.run().waitUntilFinish();

        bqClient = BigqueryClient.getNewBigquerryClient(options.getAppName());

        String query = String.format("SELECT * FROM `%s.%s`", datasetName, tableName);
        QueryRequest queryRequest = new QueryRequest().setQuery(query).setUseLegacySql(false);
        QueryResponse queryResponse = bqClient.jobs().query(options.getProject(), queryRequest).execute();


        TableRow out = queryResponse.getRows().get(0);
        String expected = "{\n" +
                "  \"f\" : [ {\n" +
                "    \"v\" : \"testName\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1\"\n" +
                "  }, {\n" +
                "    \"v\" : \"1.550672770788E9\"\n" +
                "  }, {\n" +
                "    \"v\" : \"test\"\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : \"42\"\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : \"true\"\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  } ]\n" +
                "}";
        Assert.assertEquals(expected, out.toPrettyString());
    }

    @Test
    public void testWktNulledTimeStampE2EBigQuery() throws Exception {
        String tableName = "wkt_nulled_ts_" + System.currentTimeMillis();
        ProtoBeamWktMessage protoIn = ProtoBeamWktMessage.newBuilder()
                .setTestName("nulled_timestamp")
                .build();

        Pipeline pipeline = Pipeline.create(options);

        ProtoTableRowExecutionPlan plan = new ProtoTableRowExecutionPlan(protoIn);
        SchemaProtoToBigQueryModel model = new SchemaProtoToBigQueryModel();
        TableSchema schema = model.getSchema(protoIn.getDescriptorForType());

        //Temp
        List<TableFieldSchema> fields = schema.getFields();
        fields.set(2, fields.get(2).set("mode", "NULLABLE"));
        schema.set("fields", fields);

        TableRow in = plan.convert(protoIn);
        ArrayList<TableRow> rows = new ArrayList<>();
        rows.add(in);


        pipeline.apply(Create.of(rows))
                .apply(BigQueryIO.writeTableRows()
                        .to(String.format("%s.%s", datasetName, tableName))
                        .withSchema(schema)
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE));

        pipeline.run().waitUntilFinish();

        bqClient = BigqueryClient.getNewBigquerryClient(options.getAppName());

        String query = String.format("SELECT * FROM `%s.%s`", datasetName, tableName);
        QueryRequest queryRequest = new QueryRequest().setQuery(query).setUseLegacySql(false);
        QueryResponse queryResponse = bqClient.jobs().query(options.getProject(), queryRequest).execute();

        TableRow out = queryResponse.getRows().get(0);
        String expected = "{\n" +
                "  \"f\" : [ {\n" +
                "    \"v\" : \"nulled_timestamp\"\n" +
                "  }, {\n" +
                "    \"v\" : \"0\"\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  }, {\n" +
                "    \"v\" : null\n" +
                "  } ]\n" +
                "}";
        Assert.assertEquals(expected, out.toPrettyString());
    }

}

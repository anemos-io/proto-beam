package io.anemos.protobeam.convert;


import io.anemos.protobeam.examples.ProtoBeamBasicRepeatPrimitive;
import org.junit.Before;
import org.junit.Test;

public class RepeatedTest extends AbstractProtoBigQueryTest {

    private ProtoTableRowExecutionPlan plan;


    @Before
    public void setup() {
        ProtoBeamBasicRepeatPrimitive x = ProtoBeamBasicRepeatPrimitive.newBuilder()
                .build();
        plan = new ProtoTableRowExecutionPlan(x);

        byte[] so = SerializeTest.serializeToByteArray(plan);
        plan = (ProtoTableRowExecutionPlan) SerializeTest.deserializeFromByteArray(so, "");
    }

    @Test
    public void booleanFieldTest() {
        ProtoBeamBasicRepeatPrimitive protoIn = ProtoBeamBasicRepeatPrimitive.newBuilder()
                .addRepeatedBool(false)
                .addRepeatedBool(true)
                .build();
        testPingPong(plan, protoIn);
    }


    @Test
    public void stringFieldTest() {
        ProtoBeamBasicRepeatPrimitive protoIn = ProtoBeamBasicRepeatPrimitive.newBuilder()
                .addRepeatedString("fooBar1")
                .addRepeatedString("fooBar2")
                .build();
        testPingPong(plan, protoIn);
    }
}

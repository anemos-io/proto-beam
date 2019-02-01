package io.anemos.protobeam.convert;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import io.anemos.protobeam.convert.nodes.beamsql.BeamSqlConvertNodeFactory;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

import java.io.Serializable;

public class ProtoBeamSqlExecutionPlan<T extends Message> extends AbstractExecutionPlan<T> implements Serializable {


    private final Schema schema;

    public ProtoBeamSqlExecutionPlan(Message message) {
        this((Class<T>) message.getClass());
    }

    public ProtoBeamSqlExecutionPlan(Class<T> messageClass) {
        super(messageClass, new BeamSqlConvertNodeFactory());
        this.schema = new SchemaProtoToBeamSQL().getSchema(this.descriptor);
    }

    public Row convert(T message) {
        Row.Builder row = Row.withSchema(schema);
        convert.convert(message, row);
        return row.build();
    }

    public T convertToProto(Row row) {
        DynamicMessage.Builder builder = DynamicMessage.newBuilder(descriptor);
        convert.convertToProto(builder, row);
        return convertToConcrete(builder);
    }
}

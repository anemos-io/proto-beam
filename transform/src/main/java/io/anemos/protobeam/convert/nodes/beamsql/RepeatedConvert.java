package io.anemos.protobeam.convert.nodes.beamsql;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.anemos.protobeam.convert.nodes.AbstractConvert;
import org.apache.beam.sdk.values.Row;

import java.util.List;

class RepeatedConvert extends AbstractBeamSqlConvert<Object> {

    private AbstractConvert field;

    public RepeatedConvert(Descriptors.FieldDescriptor descriptor, AbstractConvert field) {
        super(descriptor);
        this.field = field;
    }

    //TODO convert

    @Override
    public void convertToProto(Message.Builder builder, Row row) {
        List list = row.getArray(fieldDescriptor.getName());
        list.forEach(
                obj -> builder.addRepeatedField(fieldDescriptor, field.convertFrom(obj))
        );
    }
}

package io.anemos.protobeam.convert.nodes.beamsql;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import io.anemos.protobeam.convert.nodes.AbstractConvert;
import org.apache.beam.sdk.values.Row;

class FlattenFieldConvert extends AbstractBeamSqlConvert<Object> {

    private AbstractConvert field;

    public FlattenFieldConvert(Descriptors.FieldDescriptor descriptor, AbstractConvert field) {
        super(descriptor);
        this.field = field;
    }

    @Override
    public void fromProto(Message message, Row.Builder row) {
        Message messageField = (Message) message.getField(fieldDescriptor);
        fieldDescriptor.getMessageType().getFields().forEach(subfieldDescriptor -> {
            Object value = field.fromProtoValue(messageField.getField(subfieldDescriptor));
            row.addValue(value);
        });
    }

    @Override
    public void toProto(Row row, Message.Builder builder) {
        DynamicMessage.Builder dynamicMessageBuilder = DynamicMessage.newBuilder(fieldDescriptor.getMessageType());
        fieldDescriptor.getMessageType().getFields().forEach(subFieldDescriptor -> {
            Object value = row.getValue(subFieldDescriptor.getName());
            if (null != value) {
                dynamicMessageBuilder.setField(subFieldDescriptor, value);
            }
        });
        builder.setField(fieldDescriptor, dynamicMessageBuilder.build());
    }
}

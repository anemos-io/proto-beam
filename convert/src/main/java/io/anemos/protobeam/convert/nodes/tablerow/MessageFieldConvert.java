package io.anemos.protobeam.convert.nodes.tablerow;

import com.google.api.services.bigquery.model.TableRow;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import io.anemos.protobeam.convert.nodes.AbstractConvert;
import io.anemos.protobeam.convert.nodes.AbstractMessageConvert;

import java.util.Map;

class MessageFieldConvert extends AbstractConvert<Object, TableRow, Map<String, Object>> {
    AbstractMessageConvert convert;

    public MessageFieldConvert(Descriptors.FieldDescriptor descriptor, AbstractMessageConvert convert) {
        super(descriptor);
        this.convert = convert;
    }

    @Override
    public Object convert(Object in) {
        return in;
    }

    @Override
    public void convert(Message message, TableRow row) {
        if (message.hasField(descriptor)) {
            TableRow nested = new TableRow();
            convert.convert((AbstractMessage) message.getField(descriptor), nested);
            row.set(descriptor.getName(), nested);
        }
    }

    @Override
    public void convertToProto(Message.Builder builder, Map row) {
        Map nested = (Map) row.get(descriptor.getName());
        if (nested != null) {
            DynamicMessage.Builder fieldBuilder = DynamicMessage.newBuilder(descriptor.getMessageType());
            convert.convertToProto(fieldBuilder, nested);
            builder.setField(descriptor, fieldBuilder.build());
        }
    }
}

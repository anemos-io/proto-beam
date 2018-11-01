package io.anemos.protobeam.convert.nodes;

import com.google.api.services.bigquery.model.TableRow;
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

public class ObjectFieldConvert extends AbstractConvert {
    public ObjectFieldConvert(Descriptors.FieldDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public void convert(Message message, TableRow row) {
        row.set(descriptor.getName(), message.getField(descriptor));
    }

    @Override
    public void convertToProto(Message.Builder builder, TableRow row) {
        builder.setField(descriptor, row.get(descriptor.getName()));
    }
}

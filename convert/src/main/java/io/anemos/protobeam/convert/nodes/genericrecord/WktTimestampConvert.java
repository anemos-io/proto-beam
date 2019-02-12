package io.anemos.protobeam.convert.nodes.genericrecord;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.util.Timestamps;
import org.apache.avro.generic.GenericRecord;

class WktTimestampConvert extends AbstractGenericRecordConvert<Object> {
    public WktTimestampConvert(Descriptors.FieldDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public Object toProtoValue(Object in) {
        return Timestamps.fromMicros((Long) in);
    }

    @Override
    public void toProto(GenericRecord row, Message.Builder builder) {
        builder.setField(fieldDescriptor, toProtoValue(row.get(fieldDescriptor.getName())));
    }

}

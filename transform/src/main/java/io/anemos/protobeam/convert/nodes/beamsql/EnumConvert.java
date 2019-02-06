package io.anemos.protobeam.convert.nodes.beamsql;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.apache.avro.util.Utf8;
import org.apache.beam.sdk.values.Row;

class EnumConvert extends AbstractBeamSqlConvert<Object> {

    public EnumConvert(Descriptors.FieldDescriptor descriptor) {
        super(descriptor);
    }

    @Override
    public Object convertFrom(Object in) {
        Descriptors.EnumDescriptor enumType = fieldDescriptor.getEnumType();
        return enumType.findValueByName(((Utf8) in).toString());
    }

    @Override
    public void convertToProto(Message.Builder builder, Row row) {
        builder.setField(fieldDescriptor, convertFrom(row.getString(fieldDescriptor.getName())));
    }
}

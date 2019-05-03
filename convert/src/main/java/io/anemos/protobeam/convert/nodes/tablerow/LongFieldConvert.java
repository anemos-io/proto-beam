package io.anemos.protobeam.convert.nodes.tablerow;

import com.google.api.services.bigquery.model.TableRow;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.anemos.protobeam.convert.nodes.AbstractConvert;
import java.util.Map;

class LongFieldConvert extends AbstractConvert<Object, TableRow, Map<String, Object>> {
  public LongFieldConvert(Descriptors.FieldDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public Object fromProtoValue(Object in) {
    return in.toString();
  }

  @Override
  public void fromProto(Message message, TableRow row) {
    row.set(fieldDescriptor.getName(), fromProtoValue(message.getField(fieldDescriptor)));
  }

  @Override
  public void toProto(Map row, Message.Builder builder) {
    builder.setField(fieldDescriptor, toProtoValue(row.get(fieldDescriptor.getName())));
  }

  @Override
  public Object toProtoValue(Object in) {
    return Long.parseLong((String) in);
  }
}

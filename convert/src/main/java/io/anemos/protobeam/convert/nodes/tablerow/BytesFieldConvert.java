package io.anemos.protobeam.convert.nodes.tablerow;

import com.google.api.services.bigquery.model.TableRow;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import io.anemos.protobeam.convert.nodes.AbstractConvert;
import java.util.Base64;
import java.util.Map;

class BytesFieldConvert extends AbstractConvert<Object, TableRow, Map<String, Object>> {
  private Base64.Decoder decoder = Base64.getDecoder();
  private Base64.Encoder encoder = Base64.getEncoder();

  public BytesFieldConvert(Descriptors.FieldDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public Object fromProtoValue(Object in) {
    return encoder.encodeToString(((ByteString) in).toByteArray());
  }

  @Override
  public void fromProto(Message message, TableRow row) {
    row.set(fieldDescriptor.getName(), fromProtoValue(message.getField(fieldDescriptor)));
  }

  @Override
  public Object toProtoValue(Object in) {
    return decoder.decode((String) in);
  }

  @Override
  public void toProto(Map row, Message.Builder builder) {

    byte[] bytes = (byte[]) toProtoValue(row.get(fieldDescriptor.getName()));
    if (bytes != null && bytes.length > 0) {
      builder.setField(fieldDescriptor, bytes);
    }
  }
}

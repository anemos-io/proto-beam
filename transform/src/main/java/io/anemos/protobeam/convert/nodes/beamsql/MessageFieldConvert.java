package io.anemos.protobeam.convert.nodes.beamsql;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import io.anemos.protobeam.convert.SchemaProtoToBeamSQL;
import io.anemos.protobeam.convert.nodes.AbstractMessageConvert;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.values.Row;

public class MessageFieldConvert extends AbstractBeamSqlConvert<Object> {
  private final Schema schema;
  AbstractMessageConvert convert;

  public MessageFieldConvert(
      Descriptors.FieldDescriptor descriptor, AbstractMessageConvert convert) {
    super(descriptor);
    this.convert = convert;
    this.schema = new SchemaProtoToBeamSQL().getSchema(fieldDescriptor.getMessageType());
  }

  @Override
  public void fromProto(Message message, Row.Builder row) {
    if (message.hasField(fieldDescriptor)) {
      Row.Builder nested = Row.withSchema(schema);
      convert.fromProto((AbstractMessage) message.getField(fieldDescriptor), nested);
      row.addValue(nested.build());
    } else {
      row.addValue(null);
    }
  }

  @Override
  public void toProto(Row row, Message.Builder builder) {
    Row nested = row.getRow(fieldDescriptor.getName());
    if (nested != null) {
      DynamicMessage.Builder fieldBuilder =
          DynamicMessage.newBuilder(fieldDescriptor.getMessageType());
      convert.toProto(nested, fieldBuilder);
      builder.setField(fieldDescriptor, fieldBuilder.build());
    }
  }
}

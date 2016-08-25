import java.util.Objects;

/**
 * Created by wso2123 on 8/25/16.
 */
public class DataField {
  public String name;
  public Object value;
  public ColumnType type;


  public static enum ColumnType {
    TEXT,
    INT_POINT,
    FLOAT_POINT,
    DOUBLE_POINT,
    LatLon_POINT,
    GEO3D_POINT,
    DOUBLE_POINT_1D
  }

  public DataField(String name, Object value, ColumnType type)
  {
    this.name = name;
    this.value = value;
    this.type = type;
  }
}

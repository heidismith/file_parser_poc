/**
 * A description of particular column for a database table specification
 * User: Heidi
 * Date: 10/3/17
 */
public class ColumnSpecification {

    private final String name;
    private final int width;
    private final String dataType;

    public ColumnSpecification(String name, int width, String dataType) {
        this.name = name;
        this.width = width;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }

    public String getDataType() {
        return dataType;
    }

    public String toString() {
       return this.name + ": " + this.width + " - " + this.dataType;
    }
}

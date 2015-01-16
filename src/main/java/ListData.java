public class ListData {

    int marker_offset;
    String type = null;
    boolean tight = true;
    char bullet_char;
    int start; // null
    String delimiter;
    int padding = 0; // null

    public ListData(int indent) {
        this.marker_offset = indent;
    }

}

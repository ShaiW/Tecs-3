package translators.jackCompiler;

/**
 * An IdentifierProperties object. Contains the properties of an identifer:
 * type - The primitive Jack type or a class name
 * index - The index of the identifier
 */
public class IdentifierProperties {

    // The type of the identifier
    private String type;

    // The index of the identifier
    private short index;

    /**
     * Constructs a new IdentifierProperties
     * @param type The type of the identifier
     * @param index The index of the identifier
     */
    public IdentifierProperties(String type, short index) {
        this.type = type;
        this.index = index;
    }

    /**
     * Returns the type of the identifier
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the index of the identifier
     */
    public short getIndex() {
        return index;
    }


}

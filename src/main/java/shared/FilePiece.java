package shared;

import java.io.Serializable;

public class FilePiece implements Serializable {

    private final String name;

    private final byte[] content;


    public FilePiece(String name, byte[] content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public byte[] getContent() {
        return content;
    }
}

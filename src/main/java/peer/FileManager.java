package peer;

import shared.FilePiece;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static List<String> loadPieceNames(String folderName) {
        List<String> pieceList = new ArrayList<>();
        try {
            File folder = new File(folderName);
            if (folder.isDirectory()) {
                for (File file : folder.listFiles()) {
                    if (file.isFile()) {
                        pieceList.add(file.getName());
                    }
                }
            } else {
                System.err.println("O caminho especificado não é uma pasta.");
            }
            return pieceList;
        } catch (Exception e) {
            System.err.println("Erro ao carregar arquivos: " + e.getMessage());
            return null;
        }
    }

    public static FilePiece readFileFromDisk(String folderName, String fileName) throws IOException {
        File file = new File(folderName + "/" + fileName);
        byte[] content = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(content);
        }
        return new FilePiece(fileName, content);
    }

    public static void saveFile(String folderName, FilePiece filePiece) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(folderName + "/" + filePiece.getName())) {
            fos.write(filePiece.getContent());
        }
    }
}

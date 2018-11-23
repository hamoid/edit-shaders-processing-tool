package template.tool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileAction {
    Operation operation;
    Path src;
    Path dst;

    void execute() {
        try {
            if (operation == Operation.COPY) {
                Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            } else if (operation == Operation.MOVE) {
                Files.move(src, dst, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    enum Operation {COPY, MOVE}
}

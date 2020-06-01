import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FileManager {

    private Path pathToSave;
    private ThreadMonitor monitor;


    public FileManager(Path path, ThreadMonitor monitor){
        pathToSave = path;
        this.monitor = monitor;
    }

    public void save() throws IOException {
        if(!Files.exists(pathToSave)){
            Files.createFile(pathToSave);
        }

        PrintWriter writer = new PrintWriter(new File(String.valueOf(pathToSave)));
        writer.print(getSaveData());
        writer.close();
    }

    public String getSaveData(){
        HashMap<String, ArrayList<String>> keyMap = monitor.getKeyMap();
        ArrayList<Integer> hashes = monitor.getHashes();

        StringBuilder builder = new StringBuilder();
        SimpleDateFormat sdt = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
        Date now = new Date();
        builder.append("---URL_Update_Checker save file " + sdt.format(now) + "---" + "\n");
        builder.append("<Hashed values>" + "\n");
        for (Integer hash: hashes) {
            builder.append(hash + "\n");
        }
        builder.append("</Hashed values>" + "\n");
        builder.append("<URLs + keywords>" + "\n");
        for (String url: keyMap.keySet()) {
            builder.append(url + "|");
            if(!keyMap.get(url).isEmpty()){
                for (String keyword: keyMap.get(url)) {
                    builder.append(keyword + ",");
                }
                builder.deleteCharAt(builder.lastIndexOf(","));
            }
            builder.append("\n");
        }
        builder.append("</URLs + keywords>" + "\n");

        return builder.toString();
    }
}

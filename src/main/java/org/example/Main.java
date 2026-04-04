package org.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static volatile String lastWritten = null;
    private static volatile boolean ignoreNextWrite = false;

    static void main() throws IOException, InterruptedException {

        Path serviceFilePath = Path.of("C:\\Users\\larla\\OneDrive\\Desktop\\Spring26\\CS361\\Week 1\\image-service.txt");
        Path fileDirectory = serviceFilePath.getParent();

        if(fileDirectory == null){
            throw new IllegalArgumentException("Service file path does not have a valid parent directory");
        }

        System.out.println("Watching...");

        try(WatchService watcher = FileSystems.getDefault().newWatchService()){
            fileDirectory.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);


            //always run until the program is terminated, or an error occurs
            while(true){

                WatchKey key = watcher.take();

                for(WatchEvent<?> event : key.pollEvents()){
                    WatchEvent.Kind<?> eventType = event.kind();

                    if(eventType == StandardWatchEventKinds.OVERFLOW){
                        continue;
                    }

                    Path change = (Path) event.context();
                    if (change != null && change.equals(serviceFilePath.getFileName())){
                        if(ignoreNextWrite){
                            String currentContent = Files.readString(serviceFilePath).trim() ;
                            if(currentContent.equals(lastWritten)){
                                continue;
                            }else{
                                ignoreNextWrite = false;
                            }
                        }
                        System.out.println("File changed detected");

                        if(eventType == StandardWatchEventKinds.ENTRY_DELETE){
                            System.out.println("Service file deleted, exiting program");
                            return;
                        }else{
                            addImagePath(serviceFilePath);
                        }
                    }
                }

                boolean valid = key.reset();
                if(!valid){
                    System.out.println("Key is no longer valid");
                    break;
                }

            }
        }

    }

    private static void addImagePath(Path serviceFilePath) throws IOException{

        int imageIndex;
        try {
            imageIndex = Integer.parseInt(Files.readString(serviceFilePath).trim());

        } catch (NumberFormatException e) {
            System.out.println("Invalid image index format");
            return;
        }

        Path imageFolder = Path.of("C:\\Users\\larla\\OneDrive\\Desktop\\Spring26\\CS361\\Week 1\\week1Images");

        if (imageIndex < 0) {
            throw new IllegalArgumentException("Image index cannot be negative");
        }

        String[] imageFiles = imageFolder.toFile().list();

        if (imageFiles == null || imageFiles.length == 0){
            System.out.println("Image folder is empty");
            return;
        }

        //if the random number given is greater than the amount of images, make image index the random num % the amount of images
        if (imageIndex >= imageFiles.length) {
            imageIndex = imageIndex % imageFiles.length;
        }

        //full file path to the image based on the number given
        String imagePath = "C:\\Users\\larla\\OneDrive\\Desktop\\Spring26\\CS361\\Week 1\\week1Images\\" + imageFiles[imageIndex];

        lastWritten = imagePath;
        ignoreNextWrite = true;
        Files.writeString(serviceFilePath, imagePath);

    }
}

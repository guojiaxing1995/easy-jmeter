package io.github.guojiaxing1995.easyJmeter.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ZipUtil {
    public static void zipFolderWithArchiveOutputStream(String sourceFolderPath, String zipFilePath) throws IOException {
        Path sourcePath = Paths.get(sourceFolderPath);
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             BufferedOutputStream bos = new BufferedOutputStream(fos);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos)) {

            zipFolder(sourcePath, sourcePath.getParent(), zos);
            zos.finish();
        }
    }

    private static void zipFolder(Path sourcePath, Path parentPath, ZipArchiveOutputStream zos) throws IOException {
        Files.walk(sourcePath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    String entryName = parentPath.relativize(path).toString().replace("\\", "/");
                    ZipArchiveEntry entry = new ZipArchiveEntry(entryName);

                    try (InputStream is = new FileInputStream(path.toFile())) {
                        zos.putArchiveEntry(entry);
                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeArchiveEntry();
                    } catch (IOException e) {
                        log.error("zip file error", e);
                    }
                });
    }

    public static void unzipFile(InputStream inputStream, String targetDirectory) throws IOException, ArchiveException {
        ArchiveInputStream archiveInputStream = new ArchiveStreamFactory()
                .createArchiveInputStream(ArchiveStreamFactory.ZIP, inputStream);

        ArchiveEntry entry;
        while ((entry = archiveInputStream.getNextEntry()) != null) {
            String entryName = entry.getName();
            File outputFile = new File(targetDirectory, entryName);
            if (entry.isDirectory()) {
                outputFile.mkdirs();
            } else {
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = archiveInputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
            }
        }

        archiveInputStream.close();
    }
}

package hu.bme.szgbizt.levendula.caffplacc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@Slf4j
@Service
public class StorageService {

    public Resource getResource(String fileName, Path resourceLocation) throws FileNotFoundException {
        try {
            Path filePath = resourceLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException | FileNotFoundException ex) {
            throw new FileNotFoundException("File not found " + fileName);
        }
    }
}

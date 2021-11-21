package hu.bme.szgbizt.levendula.caffplacc.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

@Service
public class StorageService {
    void store(MultipartFile file){
        ;
    }

    Stream<Path> loadAll(){
        return null;
    }

    Path load(String filename){
        return null;
    }

    Resource loadAsResource(String filename){
        return null;
    }
}

package org.edgegallery.appstore.domain.model.app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Setter
@Getter
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 当前文件块，从1开始.
     */
    private Integer chunkNumber;

    /**
     * 分块大小.
     */
    private Long chunkSize;

    /**
     * 当前分块大小.
     */
    private Long currentChunkSize;

    /**
     * 总大小.
     */
    private Long totalSize;

    /**
     * 文件标识.
     */
    private String identifier;

    /**
     * 文件名.
     */
    private String filename;

    /**
     * 相对路径.
     */
    private String relativePath;

    /**
     * 总块数.
     */
    private Integer totalChunks;

    /**
     * 文件类型.
     */
    private String type;

    private MultipartFile file;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
}

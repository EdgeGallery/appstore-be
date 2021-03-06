/*
 *    Copyright 2021 Huawei Technologies Co., Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
     * Current file block，From1begin.
     */
    private Integer chunkNumber;

    /**
     * Block size.
     */
    private Long chunkSize;

    /**
     * Current block size.
     */
    private Long currentChunkSize;

    /**
     * Total size.
     */
    private Long totalSize;

    /**
     * File identification.
     */
    private String identifier;

    /**
     * file name.
     */
    private String filename;

    /**
     * relative path.
     */
    private String relativePath;

    /**
     * Total number of blocks.
     */
    private Integer totalChunks;

    /**
     * file type.
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

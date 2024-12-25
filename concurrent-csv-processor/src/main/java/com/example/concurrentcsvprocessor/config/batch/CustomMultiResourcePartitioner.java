package com.example.concurrentcsvprocessor.config.batch;

import com.example.concurrentcsvprocessor.service.FileSystemService;
import lombok.Setter;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.concurrentcsvprocessor.constants.BatchConstants.SLAVE_STEP_INPUT_FILE_NAME_JOB_PARAM_KEY;
import static com.example.concurrentcsvprocessor.constants.BatchConstants.SLAVE_STEP_OUTPUT_FILE_NAME_JOB_PARAM_KEY;

public class CustomMultiResourcePartitioner implements Partitioner {


    private static final String PARTITION_KEY = "partition";

    @Setter
    private List<? extends Resource> resources = Collections.emptyList();

    /**
     * Assign the filename of each of the injected resources to an
     * {@link ExecutionContext}.
     *
     * @see Partitioner#partition(int)
     */
    @Override
    @NonNull
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> map = new HashMap<>(gridSize);
        int i = 0;
        for (Resource resource : resources) {
            ExecutionContext context = new ExecutionContext();
            Assert.state(resource.exists(), "Resource does not exist: " + resource);
            context.putString(SLAVE_STEP_INPUT_FILE_NAME_JOB_PARAM_KEY, resource.getFilename());
            context.putString(SLAVE_STEP_OUTPUT_FILE_NAME_JOB_PARAM_KEY, FileSystemService.getSplitOutputFileName(i));
            context.putInt("read.count", 0);

            map.put(PARTITION_KEY + i, context);
            i++;
        }
        return map;
    }

}
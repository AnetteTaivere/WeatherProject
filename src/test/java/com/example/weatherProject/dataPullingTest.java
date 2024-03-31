package com.example.weatherProject;

import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class dataPullingTest {

    @Test
    public void testExecuteWithMockedHttpConnection() throws Exception {
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        // Mock the execution of the PullAndWriteData job
        PullAndWriteData pullAndWriteData = new PullAndWriteData();
        JobExecutionContext context = mock(JobExecutionContext.class);
        pullAndWriteData.execute(context);

        assertTrue(outputStreamCaptor.toString().contains("Data inserted successfully!"));
    }
}

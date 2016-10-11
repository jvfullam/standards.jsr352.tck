/*
 * Copyright 2016 International Business Machines Corp.
 * 
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.jbatch.tck.tests.jslxml;

import static com.ibm.jbatch.tck.utils.AssertionUtils.assertWithMessage;

import java.util.Properties;

import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;

import com.ibm.jbatch.tck.ann.*;
import com.ibm.jbatch.tck.artifacts.specialized.StepExitStatusBatchlet;
import com.ibm.jbatch.tck.artifacts.specialized.JobContextAfterJobListener;
import com.ibm.jbatch.tck.artifacts.specialized.JobExitStatusBatchlet;
import com.ibm.jbatch.tck.artifacts.specialized.StepContextAfterStepListener;
import com.ibm.jbatch.tck.utils.JobOperatorBridge;

import org.junit.BeforeClass;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ExitStatusTests {

	private static JobOperatorBridge jobOp = null;
    public static void setup(String[] args, Properties props) throws Exception {
        String METHOD = "setup";
        try {
            jobOp = new JobOperatorBridge();
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }

    @BeforeMethod
    @BeforeClass
    public static void setUp() throws Exception {
        jobOp = new JobOperatorBridge();
    }
    
    /* 
     * For the following four Step ExitStatus tests, we expect the following behavior:
     * 0] Step ExitStatus is initially set to null
     * 1] Any batch artifact that makes a call to StepContext.setExitStatus() immediately overwrites the current ExitStatus
     *    --> This INCLUDES a call to StepContext.setExitStatus(null), which resets the exit status to null
     * 2] If a batchlet's process() method returns a value that is NOT null, then the Step ExitStatus is overwritten
     *    --> Note: this acts differently than a call to StepContext.setExitStatus()
     * 3] If all batch artifacts (including StepListeners) for a step have completed execution and the Step ExitStatus is still null, 
     *    only then will the ExitStatus default to the String version of the BatchStatus
     */
    
    @TCKTest(
        versions = {"1.0"},
        assertions = {"If the return value of a batchlet's process() method is not null, then the step exit status is overwritten."},
        specRefs = {
           	@SpecRef(version = "1.0", section = "8.7.1"),
           	@SpecRef(version = "1.0", section = "9.1.2", notes = "API for javax.batch.api.Batchlet")
        },
        apiRefs = {
        	@APIRef(className = "javax.batch.api.Batchlet", methodNames = "process")
        },
        strategy = "Have the batchlet's process() method return a non-null value and verify that the final ES of the step is the process() return value."
    )
    @Test
    @org.junit.Test
    public void testNonNullBatchletReturnValueOverridesNullStepExitStatus() throws Exception {
        String METHOD = "testNonNullBatchletReturnValueOverridesNullStepExitStatus";
            
        try {
            Reporter.log("Create job parameters for execution #1:<p>");
            Reporter.log("set.step.exit.status=DO_NOT_SET_STEP_EXIT_STATUS<p>");
            Reporter.log("batchlet.return.value=BATCHLET_RETURN_VALUE<p>");
            Reporter.log("batchlet.return.value=BATCHLET_RETURN_VALUE<p>");
            
            Properties jobParams = new Properties();
            jobParams.put("set.step.exit.status", StepExitStatusBatchlet.DO_NOT_SET_STEP_EXIT_STATUS);
            jobParams.put("batchlet.return.value", StepExitStatusBatchlet.BATCHLET_RETURN_VALUE);
            jobParams.put("expected.step.exit.status", StepExitStatusBatchlet.BATCHLET_RETURN_VALUE);

            Reporter.log("Locate job XML file: stepContextAfterStepTest.xml<p>");
            Reporter.log("Invoke startJobAndWaitForResult for execution #1<p>");
            
            JobExecution execution1 = jobOp.startJobAndWaitForResult("stepContextAfterStepTest", jobParams);
                
            //Assertion 1: We expect that the Step ExitStatus will be the non-null batchlet return value
            //  (a non-null batchlet return value should always override any existing ExitStatus)
            String stepExitStatus = jobOp.getStepExecutions(execution1.getExecutionId()).get(0).getExitStatus();
            assertWithMessage("The Step Execution's ExitStatus is incorrect", StepExitStatusBatchlet.BATCHLET_RETURN_VALUE, stepExitStatus);
                
            //Assertion 2: We expect the Job to COMPLETE
            //  (The job is set up to throw an exception and fail if the wrong Step ExitStatus value is seen by StepListener.AfterStep())
            //  (This will also expose any other unexpected exceptions that might have been thrown during job execution)
            assertWithMessage("The Job Execution's BatchStatus is incorrect", BatchStatus.COMPLETED, execution1.getBatchStatus());                
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    @TCKTest(
        versions = {"1.0"},
        assertions = {"If the return value of a batchlet's process() method is not null, then the step exit status is overwritten."},
        specRefs = {
           	@SpecRef(version = "1.0", section = "8.7.1"),
           	@SpecRef(version = "1.0", section = "9.1.2", notes = "API for javax.batch.api.Batchlet")
        },
        apiRefs = {
        	@APIRef(className = "javax.batch.api.Batchlet", methodNames = "process")
        },
        strategy = "Have the batchlet set the step ES via stepCtx.setExitStatus(). Also have the batchlet's process() method return a non-null value. "
        		 + "Verify that the final ES of the step is the one set by the process() return value.",
        notes = "The assertion is not specified by the spec."
    )
    @Test
    @org.junit.Test
    public void testNonNullBatchletReturnValueOverridesNonNullStepExitStatus() throws Exception {
        String METHOD = "testNonNullBatchletReturnValueOverridesNonNullStepExitStatus";
        
        try {
            Reporter.log("Create job parameters for execution #1:<p>");
            Reporter.log("set.step.exit.status=SET_STEP_EXIT_STATUS<p>");
            Reporter.log("batchlet.return.value=BATCHLET_RETURN_VALUE<p>");
            Reporter.log("expected.step.exit.status=BATCHLET_RETURN_VALUE<p>");
            
            Properties jobParams = new Properties();            
            jobParams.put("set.step.exit.status", StepExitStatusBatchlet.SET_STEP_EXIT_STATUS);
            jobParams.put("batchlet.return.value", StepExitStatusBatchlet.BATCHLET_RETURN_VALUE);
            jobParams.put("expected.step.exit.status", StepExitStatusBatchlet.BATCHLET_RETURN_VALUE);

            Reporter.log("Locate job XML file: stepContextAfterStepTest.xml<p>");
            Reporter.log("Invoke startJobAndWaitForResult for execution #1<p>");
            
            JobExecution execution1 = jobOp.startJobAndWaitForResult("stepContextAfterStepTest", jobParams);
            
            //Assertion 1: We expect that the Step ExitStatus will be the non-null batchlet return value
            //  (a non-null batchlet return value should always override any existing ExitStatus)
            String stepExitStatus = jobOp.getStepExecutions(execution1.getExecutionId()).get(0).getExitStatus();
            assertWithMessage("The Step Execution's ExitStatus is incorrect", StepExitStatusBatchlet.BATCHLET_RETURN_VALUE, stepExitStatus);
                
            //Assertion 2: We expect the Job to COMPLETE
            //  (The job is set up to throw an exception and fail if the wrong Step ExitStatus value is seen by StepListener.AfterStep())
            //  (This will also expose any other unexpected exceptions that might have been thrown during job execution)
            assertWithMessage("The Job Execution's BatchStatus is incorrect", BatchStatus.COMPLETED, execution1.getBatchStatus());
            
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }

    @TCKTest(
        versions = {"1.0"},
        assertions = {"If the return value of a batchlet's process() method is null, then the step exit status is not changed."},
        specRefs = {
           	@SpecRef(version = "1.0", section = "8.7.1"),
           	@SpecRef(version = "1.0", section = "9.1.2", notes = "API for javax.batch.api.Batchlet")
        },
        apiRefs = {
        	@APIRef(className = "javax.batch.api.Batchlet", methodNames = "process")
        },
        strategy = "Have the batchlet set the step ES via stepCtx.setExitStatus(). Subsequently have the batchlet's process() method return null. "
        		 + "Verify that the final ES of the step is the one set by the call to setExitStatus().",
        notes = "The assertion is not specified by the spec."
    )
    @Test
    @org.junit.Test
    public void testNullBatchletReturnValueDoesNotChangeNonNullStepExitStatus() throws Exception {
        String METHOD = "testNullBatchletReturnValueDoesNotChangeNonNullStepExitStatus";
            
        try {
            Reporter.log("Create job parameters for execution #1:<p>");
            Reporter.log("set.step.exit.status=SET_STEP_EXIT_STATUS<p>");
            Reporter.log("batchlet.return.value=BATCHLET_RETURN_VALUE_NULL<p>");
            Reporter.log("expected.step.exit.status=SET_STEP_EXIT_STATUS<p>");
                
            Properties jobParams = new Properties();            
            jobParams.put("set.step.exit.status", StepExitStatusBatchlet.SET_STEP_EXIT_STATUS);
            jobParams.put("batchlet.return.value", StepExitStatusBatchlet.BATCHLET_RETURN_VALUE_NULL);
            jobParams.put("expected.step.exit.status", StepExitStatusBatchlet.SET_STEP_EXIT_STATUS);

            Reporter.log("Locate job XML file: stepContextAfterStepTest.xml<p>");
            Reporter.log("Invoke startJobAndWaitForResult for execution #1<p>");
               
            JobExecution execution1 = jobOp.startJobAndWaitForResult("stepContextAfterStepTest", jobParams);
              
            //Assertion 1: We expect that the Step ExitStatus will be the non-null value set by StepContext.setExitStatus()
            //  (a null batchlet return value should not change any existing ExitStatus)
            String stepExitStatus = jobOp.getStepExecutions(execution1.getExecutionId()).get(0).getExitStatus();
            assertWithMessage("The Step Execution's ExitStatus is incorrect", StepExitStatusBatchlet.SET_STEP_EXIT_STATUS, stepExitStatus);
                    
            //Assertion 2: We expect the Job to COMPLETE
            //  (The job is set up to throw an exception and fail if the wrong Step ExitStatus value is seen by StepListener.AfterStep())
            //  (This will also expose any other unexpected exceptions that might have been thrown during job execution)
            assertWithMessage("The Job Execution's BatchStatus is incorrect", BatchStatus.COMPLETED, execution1.getBatchStatus());
                
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    @TCKTest(
        versions = {"1.0"},
        assertions = {
        	"If the return value of a batchlet's process() method is null, then the step exit status is not changed.",
        	"A null Step ExitStatus does not default to the BatchStatus until all batch artifacts (including StepListeners) have completed execution"
        },
        specRefs = {
           	@SpecRef(version = "1.0", section = "8.7.1"),
           	@SpecRef(version = "1.0", section = "9.1.2", notes = "API for javax.batch.api.Batchlet")
        },
        apiRefs = {
        	@APIRef(className = "javax.batch.api.Batchlet", methodNames = "process")
        },
        strategy = "Do not set the step ES via stepCtx.setExitStatus(). Also have the batchlet's process() method return null. "
        		 + "Verify that the Step ExitStatus at the time of StepListener.AfterStep() is still null, and that the final "
        		 + "Step ExitStatus defaults to the String version of the BatchStatus."
    )
    @Test
    @org.junit.Test
    public void testNullBatchletReturnValueDoesNotChangeNullStepExitStatus() throws Exception {
        String METHOD = "testNullBatchletReturnValueDoesNotChangeNullStepExitStatus";
                
        try {
            Reporter.log("Create job parameters for execution #1:<p>");
            Reporter.log("set.step.exit.status=DO_NOT_SET_STEP_EXIT_STATUS<p>");
            Reporter.log("batchlet.return.value=BATCHLET_RETURN_VALUE_NULL<p>");
            Reporter.log("expected.step.exit.status=NULL_EXIT_STATUS<p>");
                    
            Properties jobParams = new Properties();            
            jobParams.put("set.step.exit.status", StepExitStatusBatchlet.DO_NOT_SET_STEP_EXIT_STATUS);
            jobParams.put("batchlet.return.value", StepExitStatusBatchlet.BATCHLET_RETURN_VALUE_NULL);
            jobParams.put("expected.step.exit.status", StepContextAfterStepListener.NULL_STEP_EXIT_STATUS);

            Reporter.log("Locate job XML file: stepContextAfterStepTest.xml<p>");
            Reporter.log("Invoke startJobAndWaitForResult for execution #1<p>");
                   
            JobExecution execution1 = jobOp.startJobAndWaitForResult("stepContextAfterStepTest", jobParams);
                  
            //Assertion 1: We expect that the Step ExitStatus will be the String version of the BatchStatus
            //  (a null Step ExitStatus should default to the BatchStatus if all batch artifacts have completed execution)
            String stepExitStatus = jobOp.getStepExecutions(execution1.getExecutionId()).get(0).getExitStatus();
            String stepBatchStatus = jobOp.getStepExecutions(execution1.getExecutionId()).get(0).getBatchStatus().toString();
            assertWithMessage("The Step Execution's ExitStatus is incorrect", stepBatchStatus, stepExitStatus);
                        
            //Assertion 2: We expect the Job to COMPLETE
            //  (The job is set up to throw an exception and fail if the wrong Step ExitStatus value is seen by StepListener.AfterStep())
            //  (This will also expose any other unexpected exceptions that might have been thrown during job execution)
            assertWithMessage("The Job Execution's BatchStatus is incorrect", BatchStatus.COMPLETED, execution1.getBatchStatus());
                    
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }    
    
    @TCKTest(
        versions = {"1.0"},
        assertions = {"A JobListener artifact can obtain the user-defined exit status for the job via a call to JobContext.getExitStatus()"},
        specRefs = {
           	@SpecRef(version = "1.0", section = "9.2.1", citations = "JobListener intercepts job execution", notes = "API for javax.batch.api.listener.JobListener"),
           	@SpecRef(version = "1.0", section = "10.9.1", notes = "API for javax.batch.runtime.context.JobContext"),
        },
        apiRefs = {
            @APIRef(className = "javax.batch.api.listener.JobListener", methodNames = "afterJob()"),
            @APIRef(className = "javax.batch.runtime.context.JobContext", methodNames = "getExitStatus()"),
        },
        strategy = "Have the batchlet set the job ES via jobCtx.setExitStatus(). Verify that a call to JobContext.getExitStatus() from within "
        		+ "the scope of JobListener.afterJob() returns the correct value."
    )
    @Test
    @org.junit.Test
    public void testNonNullJobExitStatus() throws Exception {
        String METHOD = "testNonNullJobExitStatus";

        try {
            Reporter.log("Create job parameters for execution #1:<p>");
            Reporter.log("set.job.exit.status=SET_JOB_EXIT_STATUS<p>");
            Reporter.log("expected.job.exit.status=SET_JOB_EXIT_STATUS<p>");
            
            Properties jobParams = new Properties();
            jobParams.put("set.job.exit.status", JobExitStatusBatchlet.SET_JOB_EXIT_STATUS);
            jobParams.put("expected.job.exit.status", JobExitStatusBatchlet.SET_JOB_EXIT_STATUS);

            Reporter.log("Locate job XML file: jobContextAfterJobTest.xml<p>");
            Reporter.log("Invoke startJobAndWaitForResult for execution #1<p>");
            
            JobExecution execution1 = jobOp.startJobAndWaitForResult("jobContextAfterJobTest", jobParams);

            //Assertion 1: We expect that the Job ExitStatus will be the non-null value set by JobContext.setExitStatus()
            String jobExitStatus = execution1.getExitStatus();
            assertWithMessage("The Job Execution's ExitStatus is incorrect", JobExitStatusBatchlet.SET_JOB_EXIT_STATUS, jobExitStatus);
                    
            //Assertion 2: We expect the Job to COMPLETE
            //  (The job is set up to throw an exception and fail if the wrong Job ExitStatus value is seen by JobListener.AfterJob())
            //  (This will also expose any other unexpected exceptions that might have been thrown during job execution)
            assertWithMessage("The Job Execution's BatchStatus is incorrect", BatchStatus.COMPLETED, execution1.getBatchStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    @TCKTest(
        versions = {"1.0"},
        assertions = {"A JobListener artifact can obtain the user-defined exit status for the job via a call to JobContext.getExitStatus(). In the case where no exit "
        		+ "status has been set, a job's exit status does not default to its batch status until after JobListener.afterJob() has been invoked."},
        specRefs = {
        	@SpecRef(version = "1.0", section = "8.7", 
        		citations = "Exit status is set to the final batch status if it was not overridden by any of the override means described earlier in this list.", 
        		notes = "Since JobListener is a batch artifact configured for the job, then it is able to set the job exit status via a call to setExitStatus(), even "
        				+ "from the scope of afterJob(). Therefore, the batch runtime won't default a job's exit status until after the JobListener completes execution."),
           	@SpecRef(version = "1.0", section = "9.2.1", notes = "API for javax.batch.api.listener.JobListener"),
           	@SpecRef(version = "1.0", section = "10.9.1", notes = "API for javax.batch.runtime.context.JobContext"),
        },
        apiRefs = {
            @APIRef(className = "javax.batch.api.listener.JobListener", methodNames = "afterJob()"),
            @APIRef(className = "javax.batch.runtime.context.JobContext", methodNames = "getExitStatus()"),
        },
        strategy = "Do not set an exit status for a job. Verify that a call to JobContext.getExitStatus() from within the scope of JobListener.afterJob() returns "
        		+ "null (and not a batch status)."
    )
    @Test
    @org.junit.Test
    public void testNullJobExitStatus() throws Exception {
        String METHOD = "testNullJobExitStatus";

        try {
            Reporter.log("Create job parameters for execution #1:<p>");
            Reporter.log("set.job.exit.status=DO_NOT_SET_JOB_EXIT_STATUS<p>");
            Reporter.log("expected.job.exit.status=NULL_JOB_EXIT_STATUS<p>");
                
            Properties jobParams = new Properties();
            jobParams.put("set.job.exit.status", JobExitStatusBatchlet.DO_NOT_SET_JOB_EXIT_STATUS);
            jobParams.put("expected.job.exit.status", JobContextAfterJobListener.NULL_JOB_EXIT_STATUS);

            Reporter.log("Locate job XML file: jobContextAfterJobTest.xml<p>");
            Reporter.log("Invoke startJobAndWaitForResult for execution #1<p>");
               
            JobExecution execution1 = jobOp.startJobAndWaitForResult("jobContextAfterJobTest", jobParams);

            //Assertion 1: We expect that the Job ExitStatus will be the String version of the BatchStatus
            //  (a null Job ExitStatus should default to the BatchStatus if all batch artifacts have completed execution)
            String jobExitStatus = execution1.getExitStatus();
            String jobBatchStatus = execution1.getBatchStatus().toString();
            assertWithMessage("The Job Execution's ExitStatus is incorrect", jobBatchStatus, jobExitStatus);
            
            //Assertion 2: We expect the Job to COMPLETE
            //  (The job is set up to throw an exception and fail if the wrong Job ExitStatus value is seen by JobListener.AfterJob())
            //  (This will also expose any other unexpected exceptions that might have been thrown during job execution)
            assertWithMessage("The Job Execution's BatchStatus is incorrect", BatchStatus.COMPLETED, execution1.getBatchStatus());
        } catch (Exception e) {
            handleException(METHOD, e);
        }
    }
    
    private static void handleException(String methodName, Exception e) throws Exception {
        Reporter.log("Caught exception: " + e.getMessage() + "<p>");
        Reporter.log(methodName + " failed<p>");
        throw e;
    }
}

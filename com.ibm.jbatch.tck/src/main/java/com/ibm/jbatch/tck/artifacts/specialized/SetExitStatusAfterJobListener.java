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
package com.ibm.jbatch.tck.artifacts.specialized;

import javax.batch.api.BatchProperty;
import javax.batch.api.listener.AbstractJobListener;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

@javax.inject.Named("setExitStatusAfterJobListener")
public class SetExitStatusAfterJobListener extends AbstractJobListener {
    
	@Inject 
    private JobContext jobCtx;
    
    @Inject    
    @BatchProperty(name="expected.job.exit.status")
    String expectedJobExitStatus;
    
    @Inject    
    @BatchProperty(name="set.job.exit.status.after.job")
    String setJobExitStatusAfterJob;
    
    public static final String NULL_JOB_EXIT_STATUS = "null job exit status";
    public static final String SET_JOB_EXIT_STATUS_AFTER_JOB = "Make a call to JobContext.setExitStatus(SET_JOB_EXIT_STATUS_AFTER_JOB) from the scope of afterJob()";
    public static final String SET_JOB_EXIT_STATUS_AFTER_JOB_NULL = "Explicitly make a call to JobContext.setExitStatus(null)";
    
    public static final String BAD_EXIT_STATUS_SEEN_BY_AFTER_JOB = "The wrong exit status was seen by JobListener.AfterJob()";
    
    @Override
    public void afterJob() throws Exception {
    	String jobExitStatus = jobCtx.getExitStatus();
    	if (jobExitStatus==null){
    		jobExitStatus = NULL_JOB_EXIT_STATUS;
    	}
    	
    	//First check that the expected Exit Status is present
    	if(expectedJobExitStatus!=null && !expectedJobExitStatus.equals(jobExitStatus)){
    		jobCtx.setExitStatus(BAD_EXIT_STATUS_SEEN_BY_AFTER_JOB);
    	}
    	//If the right Exit Status was present AND the listener is configured to set the Exit Status to something else, then do so
    	else if(expectedJobExitStatus!=null && setJobExitStatusAfterJob!=null){
    		if(setJobExitStatusAfterJob.equals(SET_JOB_EXIT_STATUS_AFTER_JOB_NULL)){
    			jobCtx.setExitStatus(null);
    		}
    		else if(setJobExitStatusAfterJob.equals(SET_JOB_EXIT_STATUS_AFTER_JOB)){
    			jobCtx.setExitStatus(SET_JOB_EXIT_STATUS_AFTER_JOB);
    		}
    		else{
        		throw new Exception("Jobs configured with SetExitStatusAfterStepListener must inject one of the pre-defined constants for set.step.exit.status.after.step");
        	}
    	}
    }
}

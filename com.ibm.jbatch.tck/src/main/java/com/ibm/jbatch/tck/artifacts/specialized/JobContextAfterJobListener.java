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

@javax.inject.Named("jobContextAfterJobListener")
public class JobContextAfterJobListener extends AbstractJobListener {
    
	@Inject 
    private JobContext jobCtx; 
    
    @Inject    
    @BatchProperty(name="expected.job.exit.status")
    String expectedJobExitStatus;
    
    public static final String NULL_JOB_EXIT_STATUS = "null job exit status";
    
    @Override
    public void afterJob() throws Exception {
    	String jobExitStatus = jobCtx.getExitStatus();
    	if (jobExitStatus==null){
    		jobExitStatus = NULL_JOB_EXIT_STATUS;
    	}
    	
    	if(expectedJobExitStatus!=null && !expectedJobExitStatus.equals(jobExitStatus)){
    		String errorMessage = "JobContext.getExitStatus() has the wrong exit status value when it reaches JobListener.afterJob(). "
    				            + "The expected exit status is: " + expectedJobExitStatus + ", but found: " + jobExitStatus;
    		throw new Exception(errorMessage);
    	}    	
    }
}

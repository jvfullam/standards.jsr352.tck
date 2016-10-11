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

import javax.batch.api.AbstractBatchlet;
import javax.batch.api.BatchProperty;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;

@javax.inject.Named("jobExitStatusBatchlet")
public class JobExitStatusBatchlet extends AbstractBatchlet {

    @Inject
    JobContext jobCtx;
    
    @Inject    
    @BatchProperty(name="set.job.exit.status")
    String setJobExitStatus;
    
    public static final String DO_NOT_SET_JOB_EXIT_STATUS = "Do not make a call to JobContext.setExitStatus()";
    public static final String SET_JOB_EXIT_STATUS = "Make a call to JobContext.setExitStatus(SET_JOB_EXIT_STATUS)";
    public static final String SET_JOB_EXIT_STATUS_NULL = "Explicitly make a call to JobContext.setExitStatus(null)";
        
    @Override
    public String process() throws Exception {
    	
    	//JobContext.setExitStatus()
    	if(setJobExitStatus==null || setJobExitStatus.equals(DO_NOT_SET_JOB_EXIT_STATUS)){
    		//Do nothing
    	}
    	else if(setJobExitStatus.equals(SET_JOB_EXIT_STATUS)){
    		jobCtx.setExitStatus(SET_JOB_EXIT_STATUS);
    	}
    	else if(setJobExitStatus.equals(SET_JOB_EXIT_STATUS_NULL)){
    		jobCtx.setExitStatus(null);
    	}
    	else{
    		throw new Exception("Jobs configured with JobExitStatusBatchlet must inject one of the pre-defined constants for set.job.exit.status");
    	}
    	
    	return null;  
    }
}

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
import javax.batch.api.listener.AbstractStepListener;
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;

@javax.inject.Named("setExitStatusAfterStepListener")
public class SetExitStatusAfterStepListener extends AbstractStepListener {
    
	@Inject 
    private StepContext stepCtx; 
    
    @Inject    
    @BatchProperty(name="expected.step.exit.status")
    String expectedStepExitStatus;
    
    @Inject    
    @BatchProperty(name="set.step.exit.status.after.step")
    String setStepExitStatusAfterStep;
    
    public static final String NULL_STEP_EXIT_STATUS = "null step exit status";
    public static final String SET_STEP_EXIT_STATUS_AFTER_STEP = "Make a call to StepContext.setExitStatus(SET_STEP_EXIT_STATUS_AFTER_STEP) from the scope of afterStep()";
    public static final String SET_STEP_EXIT_STATUS_AFTER_STEP_NULL = "Explicitly make a call to StepContext.setExitStatus(null)";
    
    public static final String BAD_EXIT_STATUS_SEEN_BY_AFTER_STEP = "The wrong exit status was seen by StepListener.AfterStep()";
    
    @Override
    public void afterStep() throws Exception {
    	String stepExitStatus = stepCtx.getExitStatus();
    	if (stepExitStatus==null){
    		stepExitStatus = NULL_STEP_EXIT_STATUS;
    	}
    	
    	//First check that the expected Exit Status is present
    	if(expectedStepExitStatus!=null && !expectedStepExitStatus.equals(stepExitStatus)){
    		stepCtx.setExitStatus(BAD_EXIT_STATUS_SEEN_BY_AFTER_STEP);
    	}
    	//If the right Exit Status was present AND the listener is configured to set the Exit Status to something else, then do so
    	else if(expectedStepExitStatus!=null && setStepExitStatusAfterStep!=null){
    		if(setStepExitStatusAfterStep.equals(SET_STEP_EXIT_STATUS_AFTER_STEP_NULL)){
    			stepCtx.setExitStatus(null);
    		}
    		else if(setStepExitStatusAfterStep.equals(SET_STEP_EXIT_STATUS_AFTER_STEP)){
    			stepCtx.setExitStatus(SET_STEP_EXIT_STATUS_AFTER_STEP);
    		}
    		else{
        		throw new Exception("Jobs configured with SetExitStatusAfterStepListener must inject one of the pre-defined constants for set.step.exit.status.after.step");
        	}
    	}
    }
}

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
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;

@javax.inject.Named("stepExitStatusBatchlet")
public class StepExitStatusBatchlet extends AbstractBatchlet {

    @Inject
    StepContext stepCtx;

    @Inject    
    @BatchProperty(name="set.step.exit.status")
    String setStepExitStatus;
    
    @Inject    
    @BatchProperty(name="batchlet.return.value")
    String batchletReturnValue;
    
    public static final String DO_NOT_SET_STEP_EXIT_STATUS = "Do not make a call to StepContext.setExitStatus()";
    public static final String SET_STEP_EXIT_STATUS = "Make a call to StepContext.setExitStatus(SET_STEP_EXIT_STATUS)";
    public static final String SET_STEP_EXIT_STATUS_NULL = "Explicitly make a call to StepContext.setExitStatus(null)";
    
    public static final String BATCHLET_RETURN_VALUE = "Batchlet process() method returns BATCHLET_RETURN_VALUE";
    public static final String BATCHLET_RETURN_VALUE_NULL = "Batchlet process() method returns null";
        
    @Override
    public String process() throws Exception {
    	
    	//StepContext.setExitStatus()
    	if(setStepExitStatus==null || setStepExitStatus.equals(DO_NOT_SET_STEP_EXIT_STATUS)){
    		//Do nothing
    	}
    	else if(setStepExitStatus.equals(SET_STEP_EXIT_STATUS)){
    		stepCtx.setExitStatus(SET_STEP_EXIT_STATUS);
    	}
    	else if(setStepExitStatus.equals(SET_STEP_EXIT_STATUS_NULL)){
    		stepCtx.setExitStatus(null);
    	}
    	else{
    		throw new Exception("Jobs configured with StepExitStatusBatchlet must inject one of the pre-defined constants for set.step.exit.status");
    	}
    	
    	//Batchet.process()
    	if(batchletReturnValue==null || batchletReturnValue.equals(BATCHLET_RETURN_VALUE_NULL)){
    		return null;
    	}
    	else if(batchletReturnValue.equals(BATCHLET_RETURN_VALUE)){
    		return BATCHLET_RETURN_VALUE;
    	}
    	else{
    		throw new Exception("Jobs configured with StepExitStatusBatchlet must inject one of the pre-defined constants for batchlet.return.value");
    	}   
    }
}

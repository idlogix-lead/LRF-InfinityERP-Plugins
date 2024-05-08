package com.idlogix.factories;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import com.idlogix.processes.AddProducts;
import com.idlogix.processes.UpdateAnimalCost;

public class ProcessFactory implements IProcessFactory{

	@Override
	public ProcessCall newProcessInstance(String className) {
		// TODO Auto-generated method stub
		

		if(className.equals("com.idlogix.processes.AddProducts"))
			return new AddProducts();
//		if(className.equals("com.idlogix.processes.TestProcess"))
//			return new TestProcess();
		if(className.equals("com.idlogix.processes.UpdateAnimalCost"))
			return new UpdateAnimalCost();
		

		return null;
	}
	
	

}

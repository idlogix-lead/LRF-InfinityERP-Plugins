package com.idlogix.factories;


import org.adempiere.base.IColumnCalloutFactory;
import org.adempiere.base.IColumnCallout;
import org.compiere.model.MProduct;

import com.idlogix.callouts.ActivityKeyNameGenerator;
import java.util.ArrayList;
import java.util.List;
import org.compiere.model.MOrder;


public class CalloutFactory implements IColumnCalloutFactory
{

	public IColumnCallout[] getColumnCallouts(String tableName,String columnName) 
	{
		List<IColumnCallout> list = new ArrayList<IColumnCallout>();
		
		
		if(tableName.equalsIgnoreCase("C_Activity") )
				 
		{
			if(columnName.equalsIgnoreCase("DatePurchased"))
			
			{
				list.add(new ActivityKeyNameGenerator());
			}
			
				
		}

		return list!= null ? list.toArray(new IColumnCallout[0]): new IColumnCallout[0];
	}
}
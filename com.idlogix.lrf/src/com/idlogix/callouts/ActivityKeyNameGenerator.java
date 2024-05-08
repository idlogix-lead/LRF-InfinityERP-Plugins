package com.idlogix.callouts;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.adempiere.base.IColumnCallout;
import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.DB;




public class ActivityKeyNameGenerator  implements IColumnCallout {

	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
		// TODO Auto-generated method stub
		

		String DatePurchased = mTab.get_ValueAsString("DatePurchased");
		if(DatePurchased.length()!=0) {
		String search_key = DatePurchased.substring(0,10);
		int batch_no = 0;
		
		String strSQL = "select count(*)::integer id \n"
				+ "	   from adempiere.c_activity\n";;

		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try
		{
			pstmt = DB.prepareStatement (strSQL.toString(), null);
			rs = pstmt.executeQuery ();
			
			while (rs.next ())		//	Order
			{
				batch_no = rs.getInt("id");
			}
		}
		catch (Exception e)
		{
			throw new AdempiereException(e);
		}
		batch_no++;
		search_key +=" B"+Integer.toString(batch_no);
		mTab.setValue("value", search_key);
		mTab.setValue("Name", search_key);

		}
		
		
		
		
		return null;
	}

}

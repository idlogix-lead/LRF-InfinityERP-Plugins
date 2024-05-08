package com.idlogix.processes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.Callback;
import org.compiere.model.MActivity;

public class AddProducts extends SvrProcess{

	BigDecimal quantity = Env.ZERO;
	int c_activity_id =0;
	int m_locator_id =0;
	int count =0;
	MActivity activity=null;
	String purchase_date = "";
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		ProcessInfoParameter[] paras = getParameter();
		for (ProcessInfoParameter p : paras) {
			String name = p.getParameterName();	
			if (name.equalsIgnoreCase("Qty")) {
				quantity = p.getParameterAsBigDecimal();
			}			
			else if (name.equalsIgnoreCase("C_Activity_ID")) {
				c_activity_id = p.getParameterAsInt();
			}
			else if (name.equalsIgnoreCase("M_Locator_ID")) {
				m_locator_id = p.getParameterAsInt();
			}
			else {
				log.severe("Unknown Parameter: " + name);
			}
		}
		if(quantity.compareTo(Env.ZERO)< 0 )
			quantity = Env.ZERO;
		quantity.setScale(0, RoundingMode.HALF_UP);
		count = quantity.intValue();
	}
	

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		
		if( count == 0 || c_activity_id ==0 )
			return null;
		activity = new MActivity(getCtx(), c_activity_id, null);
		purchase_date = activity.get_ValueAsString("DatePurchased");
		if(purchase_date.length()==0) {
			processUI.ask("Add PurchaseDate First then create Products ", new Callback<Boolean>(){

				@Override
				public void onCallback(Boolean result) {
					// TODO Auto-generated method stub
						
							
				}
				
			});
		}
		else {
			CreateProducts();
		}

		
		
		return null;
	}
	public void CreateProducts() {
		int prod_count = existingProducts();	
		for(int i=0;i<count;i++) {
			MProduct prod =new MProduct(Env.getCtx(),0,null);
			String name = activity.getValue()+"-"+String.format("%03d", prod_count);
			prod.setAD_Org_ID(activity.get_ValueAsInt("AD_Org_ID"));
			prod.setName(name);
			prod.setValue(name);
			prod.set_ValueNoCheck("M_Product_Category_ID", 1000002);
			prod.set_ValueNoCheck("C_Activity_ID", c_activity_id);
			prod.set_ValueNoCheck("C_TaxCategory_ID", 1000000);
			prod.set_ValueNoCheck("C_UOM_ID", 100);
			prod.set_ValueNoCheck("ProductType", "I");
			prod.set_ValueNoCheck("M_AttributeSet_ID", 1000001);
			prod.set_ValueNoCheck("M_Locator_ID", m_locator_id);
			prod.set_ValueNoCheck("SalesRep_ID", 1000000);
			prod.saveEx();
			
			AddProductPrice(prod.get_ID(),1000000);
//			AddProductPrice(prod.get_ID(),0);
			
			prod_count++;
			
		}
	}
	public void AddProductPrice(int prodID,int versionID) {
		MProductPrice price = new MProductPrice(getCtx(), 0, null);
		price.setAD_Org_ID(activity.get_ValueAsInt("AD_Org_ID"));
		price.set_ValueOfColumn("AD_Client_ID", Env.getAD_Client_ID(getCtx()));
		price.setM_Product_ID(prodID);
		price.setM_PriceList_Version_ID(versionID);
		price.setPriceList(Env.ZERO);
		price.setPriceStd(Env.ZERO);
		price.setPriceLimit(Env.ZERO);		
		price.save();
	}
	public int existingProducts() {
		int prod_count = 0;
		String strSQL = "select  count(*) id \n"
				      + "from adempiere.m_product p \n";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try
		{
			pstmt = DB.prepareStatement (strSQL.toString(), null);
			rs = pstmt.executeQuery ();
			
			while (rs.next ())	
			{
				prod_count = rs.getInt("id");
				
			}
		}
		catch (Exception e)
		{
			throw new AdempiereException(e);
		}
		
		return prod_count+1;
	}

}

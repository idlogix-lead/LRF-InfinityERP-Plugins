package com.idlogix.processes;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;

import java.math.RoundingMode;
import java.sql.SQLException;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;

public class UpdateAnimalCost extends SvrProcess{
	int ad_client_id;
	int ad_org_id;
	int c_activity_id;
	Timestamp asOn;
	
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		ProcessInfoParameter[] paras = getParameter();
		for (ProcessInfoParameter p : paras) {
			String name = p.getParameterName();	
			if (name.equalsIgnoreCase("AD_Client_ID")) {
				ad_client_id = p.getParameterAsInt();
			}			
			else if (name.equalsIgnoreCase("AD_Org_ID")) {
				ad_org_id = p.getParameterAsInt();
			}
			else if (name.equalsIgnoreCase("C_Activity_ID")) {
				c_activity_id = p.getParameterAsInt();
			}
			else if (name.equalsIgnoreCase("AsOn")) {
				asOn = p.getParameterAsTimestamp();
			}
			else {
				log.severe("Unknown Parameter: " + name);
			}
		}		
	}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		MInventory inv=null;
		String sql = "SELECT an.c_activity_id, an.m_product_id, an.value, an.name, an.m_attributesetinstance_id\n"
				+ "		, an.qtyonhand, ct.currentcostprice, am.cost_amount, tot.tot_animal\n"
				+ "		\n"
				+ "		,(SELECT org.orgname FROM (SELECT org.name orgname FROM adempiere.ad_org org WHERE org.ad_org_id = "+ ad_org_id +" ) org)\n"
				+ "		,(SELECT act.activity FROM (SELECT act.name activity FROM adempiere.c_activity act WHERE act.c_activity_id =  "+c_activity_id+" ) act)\n"
				+ "		\n"
				+ "FROM\n"
				+ "\n"
				+ "(SELECT pd.c_activity_id, st.m_product_id, pd.value, pd.name, st.m_attributesetinstance_id, SUM(st.qtyonhand) qtyonhand\n"
				+ "\n"
				+ "FROM adempiere.m_storageonhand st\n"
				+ "LEFT JOIN adempiere.m_product pd ON st.m_product_id = pd.m_product_id\n"
				+ "\n"
				+ " WHERE st.ad_client_id = "+ad_client_id+"  AND st.ad_org_id = "+ad_org_id+"  AND pd.c_activity_id = "+c_activity_id+"   AND st.m_attributesetinstance_id != 0 AND st.qtyonhand > 0\n"
				+ "\n"
				+ "GROUP BY pd.c_activity_id, pd.value, pd.name, st.m_attributesetinstance_id, st.m_product_id ) an\n"
				+ "\n"
				+ "LEFT JOIN adempiere.m_cost ct ON an.m_product_id = ct.m_product_id AND an.m_attributesetinstance_id = ct.m_attributesetinstance_id\n"
				+ "\n"
				+ "LEFT JOIN\n"
				+ "\n"
				+ "(SELECT fa.c_activity_id, SUM(fa.amtacctdr-amtacctcr) cost_amount\n"
				+ "\n"
				+ "FROM adempiere.fact_acct fa\n"
				+ "\n"
				+ "WHERE fa.ad_client_id = "+ad_client_id+"   AND fa.ad_org_id =   "+ad_org_id+" AND fa.account_id = 1000346  AND fa.c_activity_id =  "+c_activity_id+" AND fa.dateacct <=  '"+asOn.toString().split(" ")[0]+"' \n"
				+ "\n"
				+ "GROUP BY fa.c_activity_id) am\n"
				+ "\n"
				+ "ON an.c_activity_id = am.c_activity_id\n"
				+ "\n"
				+ "LEFT JOIN\n"
				+ "\n"
				+ "(SELECT pd.c_activity_id, SUM(st.qtyonhand) tot_animal\n"
				+ "\n"
				+ "FROM adempiere.m_storageonhand st\n"
				+ "LEFT JOIN adempiere.m_product pd ON st.m_product_id = pd.m_product_id\n"
				+ "\n"
				+ " WHERE st.ad_client_id = "+ad_client_id+"  AND st.ad_org_id = "+ad_org_id+"  AND pd.c_activity_id = "+c_activity_id+"   AND st.m_attributesetinstance_id != 0 AND st.qtyonhand > 0\n"
				+ "\n"
				+ "GROUP BY pd.c_activity_id ) tot\n"
				+ "\n"
				+ "ON an.c_activity_id = tot.c_activity_id";
				
		//
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), null);
			rs = pstmt.executeQuery();
			int count = 0;
			
			while (rs.next())
			{
				
				if(count==0) {
					inv = new MInventory(getCtx(), 0, (String) null);
					inv.setClientOrg(ad_client_id, ad_org_id);
					inv.setDescription("Update Cost of  : "+rs.getString("name"));
					inv.setC_DocType_ID(1000027);
					inv.setC_Currency_ID(306);
					inv.setCostingMethod("S");
					inv.setMovementDate(asOn);
					inv.setC_Activity_ID(c_activity_id);
					inv.save();
					count+=1;
				}
				MInventoryLine ivl  = new MInventoryLine(Env.getCtx(), 0, (String)null);
				ivl.setM_Inventory_ID(inv.getM_Inventory_ID());
				ivl.setAD_Org_ID(ad_org_id);
				ivl.setM_Product_ID(rs.getInt("m_product_id"));
				ivl.setM_AttributeSetInstance_ID(rs.getInt("m_attributesetinstance_id"));
				ivl.setC_Charge_ID(1000046);
				ivl.setCurrentCostPrice(rs.getBigDecimal("currentcostprice"));
				BigDecimal newCost =rs.getBigDecimal("currentcostprice").add((rs.getBigDecimal("cost_amount")).divide(rs.getBigDecimal("tot_animal"),10,RoundingMode.HALF_EVEN));
				ivl.setNewCostPrice(newCost);
				ivl.save();
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}	
		if(inv!=null) {
			addLog(inv.get_ID(), inv.getMovementDate(), null, "Cost Adjustment" + inv.getDocumentNo(), inv.get_Table_ID(), inv.get_ID());
		return "Done!";
		}
		
		
		return null;
		
	}

}

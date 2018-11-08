package com.zakaria.beans;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.vim25.AlarmDescription;
import com.vmware.vim25.AlarmExpression;
import com.vmware.vim25.ElementDescription;
import com.vmware.vim25.MetricAlarmExpression;
import com.vmware.vim25.ScheduledTaskDetail;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.TypeDescription;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ServiceInstance;

/**
 * http://vijava.sf.net
 * 
 * @author Steve Jin
 */
@Component
public class PrintAlarmManager {
   
	@Autowired
	  LoginTest loginUser;
	
 
	ServiceInstance si ;

    

      public LoginTest getLoginUser() {
		return loginUser;
	}

	public void setLoginUser(LoginTest loginUser) {
		this.loginUser = loginUser;
	}

	public ServiceInstance getSi() {
		return si;
	}

	public void setSi(ServiceInstance si) {
		this.si = si;
	}

	public AlarmDescription getAlarmInfo() throws RemoteException, MalformedURLException {
    	  
    	  
      AlarmManager alarmMgr = si.getAlarmManager();	  
      
      System.out.println("Alarm expressions:");
     AlarmExpression[] defaultExps = alarmMgr.getDefaultExpression();
     printAlarmExpressions(defaultExps);

      System.out.println("\n\nAlarm descriptions:");
      AlarmDescription ad = alarmMgr.getDescription();
      printAlarmDescription(ad);

      si.getServerConnection().logout();
	return ad;
	}

  public   void printAlarmDescription(AlarmDescription ad) {
	   //////////
	      
	      ///////////
      System.out.println("Entity statuses:");
      printElementDescriptions(ad.getEntityStatus());

      System.out.println("\nHostSystem connection states:");
      printElementDescriptions(ad.getHostSystemConnectionState());

      System.out.println("\nMetric operators:");
      printElementDescriptions(ad.getMetricOperator());

      System.out.println("\nState operators:");
      printElementDescriptions(ad.getStateOperator());

      System.out.println("\nVirtual machine power states:");
      printElementDescriptions(ad.getVirtualMachinePowerState());

      System.out.println("\nAction class descriptions:");
      printTypeDescriptions(ad.getAction());

      System.out.println("\nDescriptions of expressioin type for triggers:");
      printTypeDescriptions(ad.getExpr());
   }

  public  static void printAlarmExpressions(AlarmExpression[] exps) {
      for (int i = 0; exps != null && i < exps.length; i++) {
         System.out.println("\nAlarm expression #" + i);
         if (exps[i] instanceof MetricAlarmExpression) {
            MetricAlarmExpression mae = (MetricAlarmExpression) exps[i];
            System.out.println("metric:" + mae.getMetric().getCounterId());
            System.out.println("red:" + mae.getRed());
            System.out.println("type:" + mae.getType());
            System.out.println("yellow:" + mae.getYellow());
         } else if (exps[i] instanceof StateAlarmExpression) {
            StateAlarmExpression sae = (StateAlarmExpression) exps[i];
            System.out.println("operator:" + sae.getOperator());
            System.out.println("red:" + sae.getRed());
            System.out.println("statePath:" + sae.getStatePath());
            System.out.println("type:" + sae.getType());
            System.out.println("yellow:" + sae.getYellow());
         }
      }
   }

   static void printTypeDescriptions(TypeDescription[] tds) {
      for (int i = 0; tds != null && i < tds.length; i++) {
         printTypeDescription(tds[i]);
      }
   }

   static void printTypeDescription(TypeDescription td) {
      System.out.println("\nKey:" + td.getKey());
      System.out.println("Label:" + td.getLabel());
      System.out.println("Summary:" + td.getSummary());
      if (td instanceof ScheduledTaskDetail) {
         System.out.println("Frequency:" + ((ScheduledTaskDetail) td).getFrequency());
      }
   }

   static void printElementDescriptions(ElementDescription[] eds) {
      for (int i = 0; eds != null && i < eds.length; i++) {
         printElementDescription(eds[i]);
      }
   }

   static void printElementDescription(ElementDescription ed) {
      System.out.println("\nKey:" + ed.getKey());
      System.out.println("Label:" + ed.getLabel());
      System.out.println("Summary:" + ed.getSummary());
   }

}
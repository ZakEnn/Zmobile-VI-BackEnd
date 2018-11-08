package com.zakaria;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.zakaria.beans.CreateDS;
import com.zakaria.beans.CreateVM;
import com.zakaria.beans.LoginTest;
import com.zakaria.beans.PrintAlarmManager;
import com.zakaria.beans.RemoveDS;
import com.zakaria.beans.RemoveVM;
import com.zakaria.beans.VmCdOp;

@Controller
public class ZviServlet{

	@Autowired
	LoginTest userLog;
	
	@Autowired
	CreateVM createvm;            
	                                                            
	@Autowired
	RemoveVM removevm;
	
	@Autowired
	RemoveDS removeds;
	
	@Autowired
	VmCdOp vmcd;


	@Autowired
	CreateDS createds;
	
	
	
	@ResponseBody
	@RequestMapping(value="/")
	public String connection(HttpServletRequest request) throws RemoteException, MalformedURLException {
		
		 Enumeration enumeration = request.getParameterNames();
		 Map<String, String> modelMap = new HashMap<>();
		 
		    while(enumeration.hasMoreElements()){
		        String parameterName = (String) enumeration.nextElement();
		        modelMap.put(parameterName, request.getParameter(parameterName));
		    }
		 userLog = new LoginTest(modelMap.get("ip_value"),modelMap.get("user_name"),modelMap.get("pass"));
		 System.out.println(userLog);
		 return modelMap.toString();
	}
	
	@ResponseBody
	@RequestMapping(value="/getHostInfo" )
	public Map<String,String> hostData() throws InvalidProperty, RuntimeFault, RemoteException {

		return userLog.hostInfo();
	}
	

	@ResponseBody
	@RequestMapping(value="/getDataStoreInfo")
	public  Map<String,List> dataStore() throws RemoteException, MalformedURLException {
		
		return userLog.dataStoreInfo();
	}
	
	
	
	@ResponseBody
	@RequestMapping(value="/getGuestInfo")
	public Map<String,List> GuestInfo() throws RemoteException, MalformedURLException {

		return userLog.GuestInfo();
		
	}
	
	@ResponseBody
	@RequestMapping(value="/getNetworkInfo")
	public Map<String,String> NetworkInfo() throws InvalidProperty, RuntimeFault, RemoteException{
		
		return userLog.networkInfo();
	}

	
	@ResponseBody
	@RequestMapping(value="/createVm")
	public void createVM(HttpServletRequest request) throws Exception  {
		
		 Enumeration enumeration = request.getParameterNames();
		 Map<String, String> modelMap = new HashMap<>();
		 
		    while(enumeration.hasMoreElements()){
		        String parameterName = (String) enumeration.nextElement();
		        modelMap.put(parameterName, request.getParameter(parameterName));
		    }
		    
		
		    
		createvm = new CreateVM(userLog,modelMap.get("vm_name"),Long.valueOf(modelMap.get("memory_size")).longValue()
				,modelMap.get("guest_id"),Long.valueOf(modelMap.get("disk_size")).longValue() *1024,
				modelMap.get("datastore_name"),Integer.valueOf(modelMap.get("nbr_Cpu")).intValue(),
				modelMap.get("net_name"));
		
			if(modelMap.get("vm_cd").equals("Fichier ISO")) {
				vmcd =  new VmCdOp(userLog,modelMap.get("vm_name"),modelMap.get("datastore_name"),modelMap.get("guest_id"),"add");
					System.out.println("check again--->"+vmcd);

			}
	
	}
	
	
	@ResponseBody
	@RequestMapping(value="/removeVm")
	public void removeVM(HttpServletRequest request) throws Exception  {
		
		 Enumeration enumeration = request.getParameterNames();
		 Map<String, String> modelMap = new HashMap<>();
		 ArrayList<String> vmNames = new ArrayList<>();
		 
		    while(enumeration.hasMoreElements()){
		    	
		        String parameterName = (String) enumeration.nextElement();
		        modelMap.put(parameterName, request.getParameter(parameterName));
		        vmNames.add(request.getParameter(parameterName));
		        System.out.println(request.getParameter(parameterName));
		    }
		
		 removevm = new RemoveVM(userLog,vmNames);
	
	}
	
	
	@ResponseBody
	@RequestMapping(value="/removeDs")
	public void removeDS(HttpServletRequest request) throws Exception  {
		
		 Enumeration enumeration = request.getParameterNames();
		 Map<String, String> modelMap = new HashMap<>();
		 ArrayList<String> dsNames = new ArrayList<>();
		 
		    while(enumeration.hasMoreElements()){
		    	
		        String parameterName = (String) enumeration.nextElement();
		        modelMap.put(parameterName, request.getParameter(parameterName));
		        dsNames.add(request.getParameter(parameterName));
		        System.out.println(request.getParameter(parameterName));
		    }
		
		 removeds = new RemoveDS(userLog,dsNames);
	
	}
	
	@ResponseBody
	@RequestMapping(value="/addDs")
	public void AddDS(HttpServletRequest request) throws Exception  {
		
		 Enumeration enumeration = request.getParameterNames();
		 Map<String, String> modelMap = new HashMap<>();
		 
		    while(enumeration.hasMoreElements()){
		        String parameterName = (String) enumeration.nextElement();
		        modelMap.put(parameterName, request.getParameter(parameterName));
		    }
		    
		createds = new CreateDS(userLog,modelMap.get("dst_name"));
		
	}
	
}



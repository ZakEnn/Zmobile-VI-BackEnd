package com.zakaria.beans;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.joda.DateTimeParser;
import org.springframework.stereotype.Component;

import com.vmware.vim25.FloatOption;
import com.vmware.vim25.HostConfigInfo;
import com.vmware.vim25.HostFileSystemMountInfo;
import com.vmware.vim25.HostFileSystemVolumeInfo;
import com.vmware.vim25.HostNetworkInfo;
import com.vmware.vim25.HostVirtualSwitch;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualCdrom;
import com.vmware.vim25.VirtualCdromAtapiBackingInfo;
import com.vmware.vim25.VirtualCdromIsoBackingInfo;
import com.vmware.vim25.VirtualCdromRemoteAtapiBackingInfo;
import com.vmware.vim25.VirtualCdromRemotePassthroughBackingInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualE1000;
import com.vmware.vim25.VirtualE1000e;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualMachineCapability;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineVMCIDevice;
import com.vmware.vim25.VirtualMachineVideoCard;
import com.vmware.vim25.VirtualPCIController;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualPS2Controller;
import com.vmware.vim25.VirtualSCSIController;
import com.vmware.vim25.VirtualSCSIPassthrough;
import com.vmware.vim25.VirtualSIOController;
import com.vmware.vim25.VirtualVmxnet;
import com.vmware.vim25.VirtualVmxnet2;
import com.vmware.vim25.VirtualVmxnet3;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.DistributedVirtualSwitch;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostDatastoreBrowser;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

@Component
public class LoginTest {
	
	private String ip,username,password;
	
	private ServiceInstance si;
	
	public LoginTest()  {
		super();
		// TODO Auto-generated constructor stub
		this.ip="";
		this.username="";
		this.password="";
	
		
	}
	   
	public ServiceInstance getSi() {
		return si;
	}

	public LoginTest(String ip, String username, String password) throws RemoteException, MalformedURLException {
		super();
		this.ip = ip;
		this.username = username;
		this.password = password;
		this.si = new ServiceInstance(new URL(
                "https://"+this.ip+"/sdk"), this.username, this.password, true);
		
	}



	public String getIp() {
		return ip;
	}



	public void setIp(String ip) {
		this.ip = ip;
	}



	public String getUsername() {
		return username;
	}



	public void setUsername(String username) {
		this.username = username;
	}



	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public void closeConnection() {
		 si.getServerConnection().logout();
	}
   
	public Map<String,String> hostInfo() throws InvalidProperty, RuntimeFault, RemoteException  {
		
		
		Folder rootFolder = (this.si).getRootFolder();
		ManagedEntity[] ht =   new InventoryNavigator(rootFolder)
		.searchManagedEntities("HostSystem");
		
		HostSystem host = (HostSystem) ht[0];
		
		 Map<String, String> lines = new LinkedHashMap<String, String>();
	        //get all about host properties here ....  
	     java.lang.reflect.Field[] fields = si.getServiceContent().about.getClass().getFields();
	       // List<String> lines = new ArrayList<>(fields.length);

	        Arrays.stream(fields)
	                .forEach(
	                        field -> {
	                            field.setAccessible(true);
	                            try {
	                                lines.put(field.getName() , 
	                                		(String) si.getServiceContent().about.getClass().getField(field.getName()).get(si.getServiceContent().about));
	                           
	                            } catch (final Exception e) {
	                                lines.put(field.getName() , e.getClass().getSimpleName());
	                                        
	                            }
	                        });

	      lines.put("HostName", host.getSummary().getConfig().name);
	      lines.put("cpuModel", host.getSummary().getHardware().cpuModel);
	      lines.put("memorySize", String.format(Locale.US, "%.2f",(float)host.getSummary().getHardware().memorySize /(1024*1024*1024)));
	      lines.put("model", host.getSummary().getHardware().model);
	      lines.put("UUID", host.getSummary().getHardware().uuid);
	      lines.put("nbrCpuCores", Integer.toString(host.getSummary().getHardware().numCpuCores));
	      lines.put("cpuUsage", Integer.toString(host.getSummary().quickStats.overallCpuUsage));
	      lines.put("memoryUsage",String.format(Locale.US, "%.2f",(float)(host.getSummary().quickStats.overallMemoryUsage) / 1024));
	      lines.put("bootTime",(host.getSummary().runtime.bootTime).getTime().toGMTString());
	      lines.put("serverClock",(this.si).getServerClock().getTime().toGMTString());
	      lines.put("ConnectionState", host.getSummary().runtime.connectionState.toString());
	      lines.put("nbrDatastores", Integer.toString(host.getDatastores().length));
	      
	      String hostds = "";
	      Float hostdsCapacity =(float) 0 ;
	      Float hostdsFreeSpace = (float) 0;
	      
	      for(int i =0 ;i<host.getDatastores().length;i++) {
	    	  
		    	  hostds += host.getDatastores()[i].getName();
		    	  hostds += "\n     ";
		    	  
		    	  hostdsFreeSpace += (float)host.getDatastores()[i].getInfo().freeSpace/(1024*1024*1024);
		    	  hostdsCapacity += (float)host.getDatastores()[i].getSummary().capacity/(1024*1024*1024);
		    	  
		      }
	      lines.put("hostdsCapacity", String.format(Locale.US, "%.2f",(float)(hostdsCapacity)));
	      lines.put("hostdsFreeSpace", String.format(Locale.US, "%.2f",(float)(hostdsFreeSpace)));
	      lines.put("hostds", hostds);
	      lines.put("nbrVMs", Integer.toString(host.getVms().length));
	      
	      String hostvms = "";
	      for(int i =0 ;i<host.getVms().length;i++) {
		    	  hostvms += host.getVms()[i].getName();
		    	  hostvms += "\n     ";
		      }
	      lines.put("hostVms", hostvms);
	      lines.put("cpuReservation", Long.toString(host.getSystemResources().config.cpuAllocation.reservation));
	      lines.put("memoryReservation", String.format(Locale.US, "%.2f",(float)host.getSystemResources().config.memoryAllocation.reservation /1024));
	      lines.put("dnsAdress", Arrays.toString(host.getConfig().network.dnsConfig.address));
	      lines.put("defaultGateway", host.getConfig().network.ipRouteConfig.defaultGateway );
	      lines.put("VnicDevice", host.getConfig().virtualNicManagerInfo.netConfig[0].candidateVnic[0].device );
	      lines.put("macAdress", host.getConfig().virtualNicManagerInfo.netConfig[0].candidateVnic[0].spec.mac );
	      lines.put("ipv4Adress", host.getConfig().virtualNicManagerInfo.netConfig[0].candidateVnic[0].spec.ip.ipAddress );
	      lines.put("ipv6Adress", host.getConfig().virtualNicManagerInfo.netConfig[0].candidateVnic[0].spec.ip.ipV6Config.ipV6Address[0].ipAddress );
          lines.put("nbrVswitches", Integer.toString(host.getConfig().network.vswitch.length ));
	      lines.put("nbrPortGroups", Integer.toString(host.getConfig().network.portgroup.length ));
	      
	      String portGroups = "";
	      for(int i =0 ;i<host.getConfig().network.portgroup.length;i++) {
		    	  portGroups += host.getConfig().network.portgroup[i].spec.name;
		    	  portGroups += "\n     ";
		      }
	      String vswitches = "";
	      for(int i =0 ;i<host.getConfig().network.vswitch.length;i++) {
	    	  vswitches += host.getConfig().network.vswitch[i].name ;
	    	  vswitches += "\n     ";
		      }
	      
	      lines.put("PortGroups", portGroups);
	      lines.put("vswitches", vswitches);
	      
	      
	       System.out.println(lines);	         
		
		return lines;
	}
	

	
	public  Map<String,List> dataStoreInfo() throws RemoteException, MalformedURLException {
	
		Map<String,List> datastores = new LinkedHashMap<String, List>();
		
		List<LinkedHashMap<?,?>> listOfMaps = new ArrayList<LinkedHashMap<?, ?>>(); 		

		String hostname = "localhost.localdomain"; //Pass the FQDN i.e. DNS name of the ESXi host, ESXi host IP will not work
	
		HostSystem host = null;
		Folder rootFolder = (this.si).getRootFolder();
		host = (HostSystem) new InventoryNavigator(rootFolder)
		.searchManagedEntity("HostSystem", hostname);
		 
		if (host == null) {
			System.out.println("Host not found");
			//lines.put("Error", "Nothing to show");
		//	listOfMaps.add(lines);
			si.getServerConnection().logout();
			return datastores;
		}
			 
		HostDatastoreBrowser hdb = host.getDatastoreBrowser();
		 
		System.out.println("Datastore Summary connected to ESXi host");
		System.out.println();
		Datastore[] ds = hdb.getDatastores();
		 
	    HostConfigInfo hostConfigInfo = host.getConfig();
	 
		for (int i = 0; ds != null && i < ds.length; i++) {
			
			LinkedHashMap<String, Object> lines = new LinkedHashMap<String, Object>();
			LinkedHashMap<String, String> lines2 = new LinkedHashMap<String, String>();
			
			lines.put("DatastoreName", ds[i].getName());
			lines.put("DSType", ds[i].getSummary().getType());
			lines.put("TotalCapacity", String.format(Locale.US, "%.2f",((float)(ds[i].getSummary().getCapacity()) / (1024 * 1024 * 1024))));
			lines.put("FreeSpace",String.format(Locale.US, "%.2f",((float)(ds[i].getSummary().getFreeSpace())/ (1024 * 1024 * 1024) )));
			lines.put("UsedSpace",String.format(Locale.US, "%.2f",((float)((ds[i].getSummary().getCapacity())-(ds[i].getSummary().getFreeSpace()))/ (1024 * 1024 * 1024) )));
			lines.put("Location", ds[i].getSummary().url);
			lines.put("maintenacemode", ds[i].getSummary().maintenanceMode);
			lines.put("UUID", ds[i].getSummary().getDatastore().getVal());
			lines.put("DSTYP", ds[i].getSummary().getDatastore().type);
			lines.put("DSBrowser", ds[i].getBrowser().toString());
			lines.put("ConfigStatus", ds[i].getConfigStatus().toString());
	
			try{
				lines.put("DatastoreTag", ds[i].getTag()[0].key);
			}catch(Exception e) {
				lines.put("DatastoreTag", "null");
			}
			
			lines.put("Local supported",Boolean.toString(hostConfigInfo.getDatastoreCapabilities().localDatastoreSupported));
			lines.put("NbrOfVm",Integer.toString(ds[i].getVms().length));
		
			
			System.out.println("DatastoreName:" + ds[i].getName() + " "
			+ "DSType:" + ds[i].getSummary().getType() + " "
			+ "TotalCapacity(in GB):"
			+ (float)(ds[i].getSummary().getCapacity()) / (1024 * 1024 * 1024)
			+ " " + "FreeSpace (in GB): "
			+ (float)(ds[i].getSummary().getFreeSpace())
			/ (1024 * 1024 * 1024) + " ");
			System.out.println();
			
				for (int j = 0; (ds[i].getVms() != null) && (j < ds[i].getVms().length); j++) {				
					lines2.put(ds[i].getVms()[j].getName(),ds[i].getVms()[j].getConfig().guestFullName);
				} 
	
			lines.put("VMs", lines2);	  
			listOfMaps.add(lines);
			}
			
	    datastores.put("Datastores", listOfMaps);
	
	    return datastores;
	    
	}
	
	
	
	public Map<String,List> GuestInfo() throws RemoteException, MalformedURLException {
		
		//try to get an array inside json
        Map<String,List> guests = new LinkedHashMap<String, List>();
		
		List<LinkedHashMap<String, String>> listOfMaps = new ArrayList<LinkedHashMap<String, String>>(); 	

		
		Folder rootFolder = si.getRootFolder();
	
	    String name = rootFolder.getName();
	    System.out.println("root:" + name);
	    ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
	    
	    if(mes==null || mes.length ==0)
	    {
	        return guests;
	    }

	    
	    for (int i = 0; mes != null && i < mes.length; i++) {
	    
		    LinkedHashMap<String, String> lines = new LinkedHashMap<String, String>();
		    	
		    VirtualMachine vm = (VirtualMachine) mes[i]; 
		    
		    
		    VirtualMachineConfigInfo vminfo = vm.getConfig();
		    VirtualMachineCapability vmc = vm.getCapability();
	
		    vm.getResourcePool();
		    
		  
		    System.out.println("--------> " + vminfo.getGuestFullName());
		    lines.put("VM name",vm.getName());
		    lines.put("GuestOS", vminfo.getGuestFullName());
		    lines.put("Multiple snapshot supported", Boolean.toString(vmc.isMultipleSnapshotsSupported()));
			lines.put("MemoryGB", String.format(Locale.US, "%.2f", ((float)vminfo.getHardware().getMemoryMB() / 1024)));
			lines.put("dsName", vminfo.datastoreUrl[0].name);
			lines.put("memoryAllocGB",String.format(Locale.US, "%.2f",((float)vminfo.memoryAllocation.reservation /1024)));
			
			lines.put("cpuAllocation",String.format(Locale.US, "%.2f",((float)vm.getSummary().config.getCpuReservation() / 1024)));
			lines.put("cpuLimit",String.format(Locale.US, "%.2f",((float)vm.getSummary().runtime.maxCpuUsage /1024)));
			lines.put("numCPU",Integer.toString(vminfo.hardware.numCPU));
			lines.put("guestId",vminfo.guestId);
			//--------------------------------------------------------------------------------//
			
			lines.put("version",vminfo.getVersion());
		    lines.put("VM ip",vm.getGuest().getIpAddress());
		    lines.put("guestState",vm.getGuest().getGuestState());
		    lines.put("guestFamily",vm.getGuest().getGuestFamily());
		    lines.put("toolVersion",vm.getGuest().getToolsVersion());
		    lines.put("toolVersionStatus",vm.getGuest().getToolsVersionStatus());
		    lines.put("firmware",vminfo.getFirmware());
		    lines.put("instanceUuid",vminfo.instanceUuid);
		    lines.put("vmPathName",vminfo.files.vmPathName);
		    lines.put("logDirectory",vminfo.files.logDirectory);
		    lines.put("snapShotDirectory",vminfo.files.snapshotDirectory);
		   
		    lines.put("VmUsageOnDatastoreUsed",String.format(Locale.US, "%.2f",((float) vm.getStorage().getPerDatastoreUsage()[0].committed / (1024*1024*1024))));
		    lines.put("VmUsageOnDatastoreNotShared",Long.toString(vm.getStorage().getPerDatastoreUsage()[0].unshared /(1024*1024*1024)));
		    lines.put("VmUsageOnDatastoreUncommited",Long.toString(vm.getStorage().getPerDatastoreUsage()[0].uncommitted / (1024*1024*1024)));

		    for (VirtualDevice vd : vm.getConfig().getHardware().getDevice()) { 
		        try { 
		            if (vd instanceof VirtualMachineVideoCard) { 
		                VirtualMachineVideoCard vVideoCard = (VirtualMachineVideoCard) vd; 
		                lines.put("VmVideoCardLabel",vVideoCard.deviceInfo.label);
		                lines.put("VmVideoCardUsed3dRender",vVideoCard.use3dRenderer);
		                lines.put("VmVideoCardNumDisplays",Integer.toString(vVideoCard.numDisplays));
		                lines.put("VmVideoCardRamSize",Long.toString(vVideoCard.getVideoRamSizeInKB()));		                
		                
		            } else if ((vd instanceof VirtualIDEController)|(vd instanceof VirtualPCIController)|
		            		(vd instanceof VirtualPS2Controller)|(vd instanceof VirtualSIOController)|(vd instanceof VirtualSCSIController)){ 
		            		 
		            	lines.put(vd.deviceInfo.label,vd.deviceInfo.summary);
         
		            } else if (vd instanceof VirtualSCSIPassthrough) { 
		                VirtualSCSIPassthrough vSCSI = (VirtualSCSIPassthrough) vd; 
		                lines.put("SCSI Adapters",vSCSI.deviceInfo.label+"("+vSCSI.deviceInfo.summary+")");

		                
		            } else if (vd instanceof VirtualDisk) { 
		                VirtualDisk vDisk = (VirtualDisk) vd; 
		                VirtualDiskFlatVer2BackingInfo vbi = (VirtualDiskFlatVer2BackingInfo) vd.getBacking(); 
		                
		                lines.put("DiskLabel",vDisk.deviceInfo.label);
		    		    lines.put("DiskCapacityGb",String.format(Locale.US, "%.2f",((float)vDisk.capacityInKB /(1024*1024))));
		    		    lines.put("DiskMode",vbi.diskMode);
		    		    lines.put("DiskThinProvisioned",vbi.thinProvisioned.toString());
		    		    lines.put("DiskFileName",vbi.fileName);

		    		    
		               System.out.println("capacity : "+vDisk.capacityInKB); 
		               System.out.println("label : "+vDisk.deviceInfo.label); 
		               System.out.println("diskMode : "+vbi.diskMode); 
		               System.out.println("thinProvisioned : "+vbi.thinProvisioned); 
		                
		            } else if (vd instanceof VirtualCdrom) { 
		                VirtualCdrom vCDrom = (VirtualCdrom) vd; 
		                
		                if (vCDrom.getBacking() instanceof VirtualCdromRemoteAtapiBackingInfo) { 
		                	
		                
		                } else if (vCDrom.getBacking() instanceof VirtualCdromAtapiBackingInfo) { 
		                	
		                     VirtualCdromAtapiBackingInfo vabi = (VirtualCdromAtapiBackingInfo) vCDrom.getBacking(); 
		                     lines.put("VmCDBacking",vabi.deviceName);
		                     lines.put("VmCDConnected",Boolean.toString(vCDrom.connectable.connected));
		                     lines.put("VmCDController",vm.getConfig().getHardware().getDevice()[vCDrom.controllerKey].deviceInfo.label);
		                     
		                } else if (vCDrom.getBacking() instanceof VirtualCdromIsoBackingInfo) { 
		                	
		                	
		                     VirtualCdromIsoBackingInfo vibi = (VirtualCdromIsoBackingInfo) vCDrom.getBacking(); 
		                     lines.put("VmCDBacking",vibi.fileName);
		                     lines.put("VmCDConnected",Boolean.toString(vCDrom.connectable.connected));
		                     lines.put("VmCDController",vm.getConfig().getHardware().getDevice()[vCDrom.controllerKey].deviceInfo.label);
		                     
		                } else if (vCDrom.getBacking() instanceof VirtualCdromRemotePassthroughBackingInfo) { 
		                	
		                	
		                     VirtualCdromRemotePassthroughBackingInfo vpbi = (VirtualCdromRemotePassthroughBackingInfo) vCDrom.getBacking();
		                     lines.put("VmCDBacking",vpbi.deviceName);
		                     lines.put("VmCDConnected",Boolean.toString(vCDrom.connectable.connected));
		                     lines.put("VmCDController",vm.getConfig().getHardware().getDevice()[vCDrom.controllerKey].deviceInfo.label);
		                     
		                } 
		            } else if (vd instanceof VirtualEthernetCard) { 
		            	
		                VirtualEthernetCard vEth = (VirtualEthernetCard) vd; 
		                if (vEth instanceof VirtualE1000) {  
		                	 lines.put("vEthernetAdapterType","E1000");
		                    System.out.println("Adapter type: E1000");  
		                    
		                } else if (vEth instanceof VirtualE1000e) {  
		                	lines.put("vEthernetAdapterType","E1000E");
		                    System.out.println("Adapter type: E1000E");  
		                    
		                } else if (vEth instanceof VirtualPCNet32) {  
		                	lines.put("vEthernetAdapterType","PCnet32");
		                    System.out.println("Adapter type: PCnet32");  
		                    
		                } else if (vEth instanceof VirtualVmxnet) {  
		                	lines.put("vEthernetAdapterType","Vmxnet");
		                    System.out.println("Adapter type: Vmxnet");
		                    
		                } else if (vEth instanceof VirtualVmxnet2) {  
		                	lines.put("vEthernetAdapterType","Vmxnet2");
		                    System.out.println("Adapter type: Vmxnet2");  
		                    
		                } else if (vEth instanceof VirtualVmxnet3) { 
		                	lines.put("vEthernetAdapterType","Vmxnet3");
		                    System.out.println("Adapter type: Vmxnet3");  		                    
		                }  
		                
		                lines.put("vEthernetConnected",Boolean.toString(vEth.getConnectable().connected));
		                System.out.println("connected "+vEth.getConnectable().connected);  
		                
		                lines.put("vEthernetConnectedAtPowerOn",Boolean.toString(vEth.getConnectable().startConnected));
		                System.out.println("Connect at power on "+vEth.getConnectable().startConnected);  
		                
		                lines.put("vEthernetAdressType",vEth.addressType);
		                System.out.println("Manual"+vEth.addressType);  
		                
		                lines.put("vEthernetAdressMac",vEth.macAddress);
		                System.out.println("MAC address:"+vEth.macAddress);
		                
		            } 
		        } catch (Exception e) { 
		        }
		        
		     }
		       
			listOfMaps.add(lines);
			
	    }
	    
	    guests.put("VMs", listOfMaps);
	    
	    return guests;

	}
	
	public Map<String,String> networkInfo() throws InvalidProperty, RuntimeFault, RemoteException {
		
	     Map<String, String> lines = new LinkedHashMap<String, String>();
		 
			String hostname = "localhost.localdomain"; //Pass the FQDN i.e. DNS name of the ESXi host, ESXi host IP will not work
			
			HostSystem host = null;
			Folder rootFolder = (this.si).getRootFolder();
			host = (HostSystem) new InventoryNavigator(rootFolder)
			.searchManagedEntity("HostSystem", hostname);
		//..................................DO SOMETHING LOL..........................
		 return lines;
		 
	}
}

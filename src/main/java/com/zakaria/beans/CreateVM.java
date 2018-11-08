package com.zakaria.beans;

import java.net.URL;
import java.rmi.RemoteException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.vmware.vim25.Description;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;

@Component
public class CreateVM {
	
	@Autowired
      LoginTest login ;

    
	    String dcName = "ha-datacenter";
	    String vmName ;
	    long memorySizeMB ;
	    int cpuCount ;
	    String guestOsId ;
	    long diskSizeKB ;
	    // mode: persistent|independent_persistent,
	    // independent_nonpersistent
	    String diskMode ="persistent";
	    String datastoreName ;
	    String netName ;
	    String nicName = "Network Adapter 1";
	    
        public CreateVM(LoginTest login ,String vmName, long memorySizeMB, String guestOsId, long diskSizeKB,
				String datastoreName,int cpuCount,String netName) throws Exception {
			super();
			this.login = login ;
			this.vmName = vmName;
			this.memorySizeMB = memorySizeMB;
			this.guestOsId = guestOsId;
			this.diskSizeKB = diskSizeKB;
			this.datastoreName = datastoreName;		
			this.cpuCount = cpuCount;
			this.netName = netName;
			
			createVm();
	
		}
		public CreateVM() {
			super();
			// TODO Auto-generated constructor stub
		}

		
        
	    public void createVm() throws Exception {
	    
		   	Folder rootFolder =login.getSi().getRootFolder();
		    
		    Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", dcName);
		    ResourcePool rp = (ResourcePool) new InventoryNavigator(
		        dc).searchManagedEntities("ResourcePool")[0];
		    
		    Folder vmFolder = dc.getVmFolder();
	
		    // create vm config spec
		    VirtualMachineConfigSpec vmSpec =  new VirtualMachineConfigSpec();
		    vmSpec.setName(vmName);
		    vmSpec.setAnnotation("VirtualMachine Annotation");
		    vmSpec.setMemoryMB(memorySizeMB);
		    vmSpec.setNumCPUs(cpuCount);
		    vmSpec.setGuestId(guestOsId);
		   
		    // create virtual devices
		    int cKey = 1000;
		    VirtualDeviceConfigSpec scsiSpec = createScsiSpec(cKey);
		    VirtualDeviceConfigSpec diskSpec = createDiskSpec(
		        datastoreName, cKey, diskSizeKB, diskMode);
		    VirtualDeviceConfigSpec nicSpec = createNicSpec(netName, nicName);
	
		    vmSpec.setDeviceChange(new VirtualDeviceConfigSpec[] 
		        {scsiSpec, diskSpec, nicSpec});
		    
		    // create vm file info for the vmx file
		    VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
		    vmfi.setVmPathName("["+ datastoreName +"]");
		    vmSpec.setFiles(vmfi);
	
		    // call the createVM_Task method on the vm folder
		    Task task = vmFolder.createVM_Task(vmSpec, rp, null);
		    try {
		    task.waitForTask();     
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
		    String result = task.waitForTask();  
		    if(result == Task.SUCCESS) 
		    {
		      System.out.println("VM Created Sucessfully");
		    }
		    else 
		    {
		      System.out.println("VM could not be created. ");
		    }
	    }
	static VirtualDeviceConfigSpec createScsiSpec(int cKey)
	  {
	    VirtualDeviceConfigSpec scsiSpec = 
	      new VirtualDeviceConfigSpec();
	    scsiSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
	    VirtualLsiLogicController scsiCtrl = 
	        new VirtualLsiLogicController();
	    scsiCtrl.setKey(cKey);
	    scsiCtrl.setBusNumber(0);
	    scsiCtrl.setSharedBus(VirtualSCSISharing.noSharing);
	    scsiSpec.setDevice(scsiCtrl);
	    return scsiSpec;
	  }
	  
	  static VirtualDeviceConfigSpec createDiskSpec(String dsName, 
	      int cKey, long diskSizeKB, String diskMode)
	  {
	    VirtualDeviceConfigSpec diskSpec = 
	        new VirtualDeviceConfigSpec();
	    diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
	    diskSpec.setFileOperation(
	        VirtualDeviceConfigSpecFileOperation.create);
	    
	    VirtualDisk vd = new VirtualDisk();
	    vd.setCapacityInKB(diskSizeKB);
	    diskSpec.setDevice(vd);
	    vd.setKey(0);
	    vd.setUnitNumber(0);
	    vd.setControllerKey(cKey);

	    VirtualDiskFlatVer2BackingInfo diskfileBacking = 
	        new VirtualDiskFlatVer2BackingInfo();
	    String fileName = "["+ dsName +"]";
	    diskfileBacking.setFileName(fileName);
	    diskfileBacking.setDiskMode(diskMode);
	    diskfileBacking.setThinProvisioned(true);
	    vd.setBacking(diskfileBacking);
	    return diskSpec;
	  }
	  
	  static VirtualDeviceConfigSpec createNicSpec(String netName, 
	      String nicName) throws Exception
	  {
	    VirtualDeviceConfigSpec nicSpec = 
	        new VirtualDeviceConfigSpec();
	    nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);

	    VirtualEthernetCard nic =  new VirtualPCNet32();
	    VirtualEthernetCardNetworkBackingInfo nicBacking = 
	        new VirtualEthernetCardNetworkBackingInfo();
	    nicBacking.setDeviceName(netName);

	    Description info = new Description();
	    info.setLabel(nicName);
	    info.setSummary(netName);
	    nic.setDeviceInfo(info);
	    
	    // type: "generated", "manual", "assigned" by VC
	    nic.setAddressType("generated");
	    nic.setBacking(nicBacking);
	    nic.setKey(0);
	   
	    nicSpec.setDevice(nic);
	    return nicSpec;
	  }
	
}

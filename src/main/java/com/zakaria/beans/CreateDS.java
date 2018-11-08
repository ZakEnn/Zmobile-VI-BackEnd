package com.zakaria.beans;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.vmware.vim25.HostConfigFault;
import com.vmware.vim25.HostScsiDisk;
import com.vmware.vim25.NotFound;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VmfsDatastoreCreateSpec;
import com.vmware.vim25.VmfsDatastoreOption;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.HostDatastoreSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;

@Component
public class CreateDS {

	LoginTest login;
	String datastoreName;
	
	public CreateDS() {
		// TODO Auto-generated constructor stub
	}

	public CreateDS(LoginTest login, String datastoreName ) throws HostConfigFault, NotFound, RuntimeFault, RemoteException {
		  
		  super();
	   	  this.login = login;
		  this.datastoreName = datastoreName;
		
	      String HostIp = "192.168.64.132";
	    
	      ManagedEntity datacenter = login.getSi().getRootFolder().getParent();

	      // Find the host with specified Ip in this datacenter.
	      HostSystem host = (HostSystem) login.getSi().getSearchIndex().findByIp((Datacenter)datacenter, HostIp, false);

	      //Datastore Manager
	      HostDatastoreSystem hostDatastoreSystem= host.getHostDatastoreSystem();

	      //Query to list disks on Datastore Manager
	      HostScsiDisk[] hostScsiDiskList = hostDatastoreSystem.queryAvailableDisksForVmfs(null);
	     
	      if(hostScsiDiskList != null) {
	
	        HostScsiDisk hScsiDisk= hostScsiDiskList[0];
	        VmfsDatastoreOption[] vmfsDatastoreOption =hostDatastoreSystem.queryVmfsDatastoreCreateOptions(hScsiDisk.getDevicePath());

	        VmfsDatastoreCreateSpec vmfsDatastoreCreateSpec = (VmfsDatastoreCreateSpec)vmfsDatastoreOption[0].getSpec();

	        vmfsDatastoreCreateSpec.getVmfs().setVolumeName(datastoreName);

	        Datastore datastore = hostDatastoreSystem.createVmfsDatastore(vmfsDatastoreCreateSpec);

	        if(datastore == null) {
	          System.out.println("datastore '" + datastoreName
	              + "' not formed");
	        } else {
	          System.out.println("datastore '" + datastoreName
	              + "'  created successfully"); 
	        }
	      }
	      else {
	        System.out.println("No extent vailable");
	      }
	      
	    }
	
	}



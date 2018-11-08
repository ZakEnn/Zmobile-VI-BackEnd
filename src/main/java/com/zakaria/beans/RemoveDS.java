package com.zakaria.beans;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostDatastoreSystem;
import com.vmware.vim25.mo.HostStorageSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.VirtualMachine;

@Component
public class RemoveDS {

	LoginTest login;
	
	public RemoveDS() {
		// TODO Auto-generated constructor stub
	}

	public RemoveDS(LoginTest login,ArrayList<String> dsNames) throws InvalidProperty, 
    RuntimeFault,RemoteException, InterruptedException {

		this.login = login ;
		System.out.println("Removed begining");
		Folder rootFolder = login.getSi().getRootFolder();
		
	//	InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
		ManagedEntity[] ht =   new InventoryNavigator(rootFolder)
				.searchManagedEntities("HostSystem");
				
		HostSystem host = (HostSystem) ht[0];
		
		HostDatastoreSystem hds = host.getHostDatastoreSystem();
		
		
		System.out.println(hds);
		Datastore[] datastores = hds.getDatastores();
		System.out.println(datastores[0].getName());
		for(int i=0;i<dsNames.size();i++) {
			System.out.println(datastores[dsNames.indexOf(dsNames.get(i))]);
        //	datastores[dsNames.indexOf(dsNames.get(i))].destroyDatastore();
			Datastore d = datastores[dsNames.indexOf(dsNames.get(i))+1];
			
			for (int j = 0; (d.getVms() != null) && (j < d.getVms().length); j++) {				
				d.getVms()[j].destroy_Task();
			} 
			
			d.destroyDatastore();
			
		}
		System.out.println("Removed succefully");


}
	
}

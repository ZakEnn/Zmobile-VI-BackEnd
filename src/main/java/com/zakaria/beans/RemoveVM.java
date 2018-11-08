package com.zakaria.beans;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.springframework.stereotype.Component;

import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

@Component
public class RemoveVM {

	LoginTest login;
	
	public RemoveVM() {
		// TODO Auto-generated constructor stub
	}

	public RemoveVM(LoginTest login,ArrayList<String> vmNames) throws InvalidProperty, 
	               RuntimeFault,RemoteException, InterruptedException {
		
		this.login = login ;
		System.out.println("Removed begining");

			VirtualMachine vm;
			Folder rootFolder = login.getSi().getRootFolder();
			
			InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);

			for(int i=0;i<vmNames.size();i++) {
				vm = (VirtualMachine) inventoryNavigator.searchManagedEntity(VirtualMachine.class.getSimpleName(), vmNames.get(i));
				vm.destroy_Task();
			}
			System.out.println("Removed succefully");

		
	}
}

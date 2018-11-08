package com.zakaria.beans;

import java.rmi.RemoteException;

import org.springframework.stereotype.Component;

import com.vmware.vim25.ConfigTarget;
import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualCdrom;
import com.vmware.vim25.VirtualCdromIsoBackingInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualIDEController;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigOption;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineDatastoreInfo;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.EnvironmentBrowser;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

@Component
public class VmCdOp {

	LoginTest login;
	String vmname;
//	String op = "add";
	String op ;
	String datastorename;

	public VmCdOp() {
		// TODO Auto-generated constructor stub
	}
	
	public VmCdOp(LoginTest login, String vmname, String datastorename ,String guest, String op) throws Exception {
		super();
		
		this.login = login;
		this.vmname = vmname;
		this.op = op;
		this.datastorename = datastorename;
		
		Folder rootFolder = login.getSi().getRootFolder();
		InventoryNavigator inventoryNavigator = new InventoryNavigator(rootFolder);
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine",
				vmname);
		if (vm == null) {
			System.out.println("No VM " + vmname + " found");
			login.getSi().getServerConnection().logout();
			return;
		}

		VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();

		if ("add".equalsIgnoreCase(op)) {
			String dsName = datastorename;
			String iso;
			if(guest.equals("ubuntu64Guest")) {
				 iso = "ubuntu-16-04-64-bit";
			}
			else {
				iso = guest;
			}
			VirtualDeviceConfigSpec cdSpec = createAddCdConfigSpec(vm, dsName, iso);
			vmConfigSpec.setDeviceChange(new VirtualDeviceConfigSpec[] { cdSpec });
		} else if ("remove".equalsIgnoreCase(op)) {
			String cdName = "CD/DVD Drive 2";
			VirtualDeviceConfigSpec cdSpec = createRemoveCdConfigSpec(vm, cdName);
			vmConfigSpec.setDeviceChange(new VirtualDeviceConfigSpec[] { cdSpec });
		} else {
			System.out.println("Invlaid device type [disk|cd]");
			return;
		}

		Task task = vm.reconfigVM_Task(vmConfigSpec);
		task.waitForTask();
		System.out.println("VmCdOp Job done !");
	}

	static VirtualDeviceConfigSpec createAddCdConfigSpec(VirtualMachine vm, String dsName, String isoName)
			throws Exception {
		VirtualDeviceConfigSpec cdSpec = new VirtualDeviceConfigSpec();
		cdSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		VirtualCdrom cdrom = new VirtualCdrom();
		VirtualCdromIsoBackingInfo cdDeviceBacking = new VirtualCdromIsoBackingInfo();
		DatastoreSummary ds = findDatastoreSummary(vm, dsName);
		cdDeviceBacking.setDatastore(ds.getDatastore());
		cdDeviceBacking.setFileName("[" + dsName + "] " + vm.getName() + "/" + isoName + ".iso");
		VirtualDevice vd = getIDEController(vm);
		cdrom.setBacking(cdDeviceBacking);
		cdrom.setControllerKey(vd.getKey());
		cdrom.setUnitNumber(vd.getUnitNumber());
		cdrom.setKey(-1);
		cdSpec.setDevice(cdrom);
		return cdSpec;
	}

	static VirtualDeviceConfigSpec createRemoveCdConfigSpec(VirtualMachine vm, String cdName) throws Exception {
		VirtualDeviceConfigSpec cdSpec = new VirtualDeviceConfigSpec();
		cdSpec.setOperation(VirtualDeviceConfigSpecOperation.remove);
		VirtualCdrom cdRemove = (VirtualCdrom) findVirtualDevice(vm.getConfig(), cdName);
		if (cdRemove != null) {
			cdSpec.setDevice(cdRemove);
			return cdSpec;
		} else {
			System.out.println("No device available " + cdName);
			return null;
		}
	}

	private static VirtualDevice findVirtualDevice(VirtualMachineConfigInfo vmConfig, String name) {
		VirtualDevice[] devices = vmConfig.getHardware().getDevice();
		for (int i = 0; i < devices.length; i++) {
			if (devices[i].getDeviceInfo().getLabel().equals(name)) {
				return devices[i];
			}
		}
		return null;
	}

	static DatastoreSummary findDatastoreSummary(VirtualMachine vm, String dsName) throws Exception {
		DatastoreSummary dsSum = null;
		VirtualMachineRuntimeInfo vmRuntimeInfo = vm.getRuntime();
		EnvironmentBrowser envBrowser = vm.getEnvironmentBrowser();
		ManagedObjectReference hmor = vmRuntimeInfo.getHost();
		if (hmor == null) {
			System.out.println("No Datastore found");
			return null;
		}

		ConfigTarget configTarget = envBrowser.queryConfigTarget(new HostSystem(vm.getServerConnection(), hmor));
		VirtualMachineDatastoreInfo[] dis = configTarget.getDatastore();
		for (int i = 0; dis != null && i < dis.length; i++) {
			dsSum = dis[i].getDatastore();
			if (dsSum.isAccessible() && dsName.equals(dsSum.getName())) {
				break;
			}
		}
		return dsSum;
	}

	static VirtualDevice getIDEController(VirtualMachine vm) throws Exception {
		VirtualDevice ideController = null;
		VirtualDevice[] defaultDevices = getDefaultDevices(vm);
		for (int i = 0; i < defaultDevices.length; i++) {
			if (defaultDevices[i] instanceof VirtualIDEController) {
				ideController = defaultDevices[i];
				break;
			}
		}
		return ideController;
	}

	static VirtualDevice[] getDefaultDevices(VirtualMachine vm) throws Exception {
		VirtualMachineRuntimeInfo vmRuntimeInfo = vm.getRuntime();
		EnvironmentBrowser envBrowser = vm.getEnvironmentBrowser();
		ManagedObjectReference hmor = vmRuntimeInfo.getHost();
		VirtualMachineConfigOption cfgOpt = envBrowser.queryConfigOption(null,
				new HostSystem(vm.getServerConnection(), hmor));
		VirtualDevice[] defaultDevs = null;
		if (cfgOpt != null) {
			defaultDevs = cfgOpt.getDefaultDevice();
			if (defaultDevs == null) {
				throw new Exception("No Datastore found in ComputeResource");
			}
		} else {
			throw new Exception("No VirtualHardwareInfo found in ComputeResource");
		}
		return defaultDevs;
	}		
	



}

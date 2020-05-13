package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Simulation {

	private static List<Cloudlet> cloudletList,cloudletListSJF;

	private static List<Vm> vmlist;

	private static List<Vm> createVM(int userId, int vms, int idShift) {
		LinkedList<Vm> list = new LinkedList<Vm>();

		long size = 60;
		int ram = 256;
		int mips = 10;
		long bw = 80;
		int pesNumber = 1; 
		String vmm = "Xen"; 
                int jk=1;

		Vm[] vm = new Vm[vms];
		for(int i=0;i<vms;i++){
			
                        if (i%2==0 )
				mips += jk;
			else 
				mips -= jk;
				vm[i] = new Vm(idShift + i, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared());
                                jk+=2;
			list.add(vm[i]);
		}

		return list;
	}

	private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift){
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		long length=100;
		long fileSize =300;
		long outputSize = 200;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];
		Random rand = new Random();
		for(int i=0;i<cloudlets;i++){
			length+=rand.nextInt(300);
			cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
                        Log.printLine("Cloudlet size is " +cloudlet[i].getCloudletTotalLength());
		}

		return list;
	}	
	
	private static void getCloudletListSJF(List<Cloudlet> clist)
	{
		int min=0;
		for (int i=0; i<clist.size();i++)
			if (clist.get(i).getCloudletLength() < clist.get(min).getCloudletLength())
				min=i;
		cloudletListSJF.add(clist.get(min));
		clist.remove(min);
		if (clist.size()!=0)
			getCloudletListSJF(clist);
	}

	private static double VmArt(List<Cloudlet> list, int VmId)
	{
		int c = 0;
		double trt = 0; 
		double art = 0;
		for(int i=0;i<list.size();i++) 
			if (list.get(i).getVmId() == VmId)
			{
				trt = trt + list.get(i).getExecStartTime();    c++;
			}
		art =  trt / c;
		return art;
	}
	

	public static void main(String[] args) {
		Log.printLine("Starting CloudSimSJF ...");

		try {
			int num_user = 1;   
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; 

			CloudSim.init(num_user, calendar, trace_flag);

			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			
               int vms = 5;
               int cloudlets=25;
                        
                        
			 DatacenterBroker broker = createBroker("Broker_0");
						
			int brokerId = broker.getId();

			vmlist  = createVM(brokerId, vms, 1);

			broker.submitVmList(vmlist);

			cloudletList = createCloudlet(brokerId, cloudlets, 1); 
		
			Log.printLine("Total Number of Cloudlets is " + cloudletList.size());
			cloudletListSJF = new LinkedList<Cloudlet>();
			getCloudletListSJF(cloudletList);    		
			
			broker.submitCloudletList(cloudletListSJF);
					

			CloudSim.startSimulation();


			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();
			DecimalFormat dft = new DecimalFormat("###.##");
        	printCloudletList(newList);
        	for (int a=0; a<vmlist.size();a++)
        		Log.printLine("Average Response Time of Virtual Machine " + vmlist.get(a).getId() + "  is  " + dft.format(VmArt( newList, vmlist.get(a).getId())));
			Log.printLine("CloudSimSJF finished");
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	private static Datacenter createDatacenter(String name){


		List<Host> hostList = new ArrayList<Host>();

		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 114;

		peList1.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

		int hostId=0;
		int ram = 5482;
		long storage = 14200;
		int bw = 340000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); 

		hostId++;
		String arch = "x86"; 
		String os = "Linux";    
		String vmm = "Xen";
		double time_zone = 5.5;        
		double cost = 3.0;            
		double costPerMem = 0.05;		
		double costPerStorage = 0.1;	
		double costPerBw = 0.1;		
		LinkedList<Storage> storageList = new LinkedList<Storage>();

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	private static DatacenterBroker createBroker(String name){

		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
                
                double avrt=0;

		String indent = "    ";
		Log.printLine();
		Log.printLine("				  ========================== OUTPUT ===========================");
		Log.printLine("Cloudlet ID" + indent + "Status" + indent +
				"DataCenter ID" + indent + "VM ID" + indent + indent + "Burst Time" + indent + "Submission Time" + indent + "Start Time" + indent + "Finish Time"+ indent + "Waiting Time");

		DecimalFormat dft = new DecimalFormat("000.00");
		DecimalFormat dft1 = new DecimalFormat("00");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + dft1.format(cloudlet.getCloudletId()) + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");
                                avrt += cloudlet.getActualCPUTime();   
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
						indent + indent + indent + dft.format(cloudlet.getSubmissionTime()) + indent+indent  + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + dft.format(cloudlet.getFinishTime())
						+ indent + indent + dft.format(cloudlet.getWaitingTime()));
                                
                        }
                        else
                    {
                        Log.print("Failure");
                    }
                        
                        
		}
		Log.printLine("Totol execution Time = " + dft.format(avrt));
		Log.printLine("Average execution Time = " + dft.format(avrt/size));
	}
}

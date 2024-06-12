package org.example;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.vms.Vm;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        // Regular approach simulation
        CloudSimPlus simulation1 = new CloudSimPlus();
        DatacenterSimple datacenter1 = CloudSimConfig.createDatacenter(simulation1);
        DatacenterBrokerSimple broker1 = new DatacenterBrokerSimple(simulation1);
        List<Vm> vmList1 = CloudSimConfig.createSinglePowerfulVM();
        List<Cloudlet> cloudletList1 = ComplexTaskGenerator.createComplexCloudlets(10);

        broker1.submitVmList(vmList1);
        double delay = 0;
        for (Cloudlet cloudlet : cloudletList1) {
            cloudlet.setVm(vmList1.get(0)); // Bind each cloudlet to the single VM
            broker1.submitCloudletList(List.of(cloudlet), delay);
            // Delay it the time it takes to execute the previous cloudlet
            delay += (int) ((cloudlet.getLength() / vmList1.get(0).getMips()) + 1);
        }
        simulation1.start();

        List<Cloudlet> finishedCloudlets1 = broker1.getCloudletFinishedList();
        new CloudletsTableBuilder(finishedCloudlets1).build();
        double totalCost1 = calculateTotalCost(finishedCloudlets1);
        System.out.println("Total cost for regular approach: " + totalCost1);

        // Optimized approach simulation
        CloudSimPlus simulation2 = new CloudSimPlus();
        DatacenterSimple datacenter2 = CloudSimConfig.createDatacenter(simulation2);
        DatacenterBrokerSimple broker2 = new DatacenterBrokerSimple(simulation2);
        List<Vm> vmList2 = CloudSimConfig.createVmsWithDifferentCharacteristics();
        List<Cloudlet> cloudletList2 = ComplexTaskGenerator.createComplexCloudlets(10);

        broker2.submitVmList(vmList2);
        List<Cloudlet> assignedCloudlets = DynamicProgramming.optimizeResourceAllocation(cloudletList2, vmList2, broker2);
        delay = 0;
        for (Cloudlet cloudlet : assignedCloudlets) {
            broker2.submitCloudletList(List.of(cloudlet), delay);
            // Delay it the time it takes to execute the previous cloudlet
            delay += (int) ((cloudlet.getLength() / cloudlet.getVm().getMips()) + 1);
        }
        simulation2.start();

        List<Cloudlet> finishedCloudlets2 = broker2.getCloudletFinishedList();
        new CloudletsTableBuilder(finishedCloudlets2).build();
        double totalCost2 = calculateTotalCost(finishedCloudlets2);
        System.out.println("Total cost for optimized approach: " + totalCost2);

        // Plot the results
        PlotResults.plotTimes(finishedCloudlets1, finishedCloudlets2);
        PlotResults.plotCosts(finishedCloudlets1, finishedCloudlets2);
    }

    private double calculateTotalCost(List<Cloudlet> cloudlets) {
        double totalCost = 0;
        for (Cloudlet cloudlet : cloudlets) {
            double cost = cloudlet.getDcArrivalTime();
            totalCost += cost;
        }
        return totalCost;
    }
}
